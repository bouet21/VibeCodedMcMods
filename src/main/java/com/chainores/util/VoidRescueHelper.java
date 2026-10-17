package com.chainores.util;

import com.chainores.config.ChainOresConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles balanced void rescue mechanics with cooldowns and safe landing detection
 * for Tier 135/150 armors and the Cosmic Elytra.
 */
public class VoidRescueHelper {
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    public static boolean tryVoidRescue(ServerPlayerEntity player, double defaultY) {
        long now = System.currentTimeMillis();
        long cooldownMs = ChainOresConfig.get().voidRescueCooldownSeconds * 1000L;
        Long lastUsed = COOLDOWNS.get(player.getUuid());

        if (lastUsed != null && (now - lastUsed) < cooldownMs) {
            long remainingSec = (cooldownMs - (now - lastUsed)) / 1000L;
            if (player.age % 20 == 0) {
                player.sendMessage(Text.literal("⏳ Rescate del Vacío en Enfriamiento (" + remainingSec + "s restantes)")
                        .formatted(Formatting.RED), true);
            }
            return false;
        }

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        BlockPos safePos = findSafeLandingPos(world, player.getBlockPos());

        double targetX;
        double targetY;
        double targetZ;

        if (safePos != null) {
            targetX = safePos.getX() + 0.5;
            targetY = safePos.getY() + 1.0;
            targetZ = safePos.getZ() + 0.5;
        } else {
            // No island beneath: teleport up with strong flight / slow-falling buffer
            targetX = player.getX();
            targetY = Math.max(player.getY() + 90.0, defaultY);
            targetZ = player.getZ();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 1, true, false));
        }

        // Teleport & reset momentum
        player.requestTeleport(targetX, targetY, targetZ);
        player.setVelocity(0, 0, 0);
        player.velocityDirty = true;
        player.fallDistance = 0.0f;

        // Buffs for survival
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 240, 0, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 2, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 160, 1, true, false));

        // Audio & visual feedback
        world.playSound(null, targetX, targetY, targetZ, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 1.2f, 1.2f);
        world.playSound(null, targetX, targetY, targetZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.PORTAL, targetX, targetY + 1.0, targetZ, 40, 0.5, 0.8, 0.5, 0.2);
        world.spawnParticles(ParticleTypes.END_ROD, targetX, targetY + 1.0, targetZ, 25, 0.4, 0.6, 0.4, 0.05);

        COOLDOWNS.put(player.getUuid(), now);
        player.sendMessage(Text.literal("✦ ¡Rescate Celestial de Vacío Activado! ✦ (CD: " + ChainOresConfig.get().voidRescueCooldownSeconds + "s)")
                .formatted(Formatting.AQUA, Formatting.BOLD), true);
        return true;
    }

    private static BlockPos findSafeLandingPos(ServerWorld world, BlockPos startPos) {
        int radius = 32;
        int px = startPos.getX();
        int pz = startPos.getZ();

        for (int r = 0; r <= radius; r += 4) {
            for (int dx = -r; dx <= r; dx += 4) {
                for (int dz = -r; dz <= r; dz += 4) {
                    for (int y = 50; y <= 130; y += 2) {
                        BlockPos check = new BlockPos(px + dx, y, pz + dz);
                        BlockState floor = world.getBlockState(check);
                        if (!floor.isAir() && floor.isOpaque() && !floor.isLiquid()) {
                            BlockPos feet = check.up();
                            BlockPos head = check.up(2);
                            if (world.getBlockState(feet).isAir() && world.getBlockState(head).isAir()) {
                                return check;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
