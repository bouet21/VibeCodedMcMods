package com.chainores.entity;

import com.chainores.registry.ModSounds;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BossCinematicsHelper {

    public static void sendBossSpawnTitle(ServerWorld world, BlockPos pos, Text title, Text subtitle, SoundEvent sound) {
        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(10, 65, 20);
        TitleS2CPacket titlePacket = new TitleS2CPacket(title);
        SubtitleS2CPacket subtitlePacket = new SubtitleS2CPacket(subtitle);

        for (ServerPlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 100.0 * 100.0)) {
            player.networkHandler.sendPacket(fadePacket);
            player.networkHandler.sendPacket(titlePacket);
            player.networkHandler.sendPacket(subtitlePacket);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), sound != null ? sound : SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5f, 0.8f);
        }
    }

    public static void triggerScreenShake(ServerWorld world, Vec3d pos, double radius, double intensity) {
        for (ServerPlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(pos.x, pos.y, pos.z) <= radius * radius)) {
            double dist = Math.sqrt(player.squaredDistanceTo(pos.x, pos.y, pos.z));
            double falloff = Math.max(0.2, 1.0 - (dist / radius));
            double jx = (world.random.nextDouble() - 0.5) * intensity * falloff;
            double jy = intensity * 0.25 * falloff;
            double jz = (world.random.nextDouble() - 0.5) * intensity * falloff;
            player.setVelocity(player.getVelocity().add(jx, jy, jz));
            player.velocityDirty = true;
        }
    }

    public static boolean tickBossDeath(HostileEntity boss, ServerWorld world, int deathTicks, ParticleEffect themeParticle, Runnable onFinished) {
        if (deathTicks < 60) {
            boss.setVelocity(0, 0.035, 0);
            boss.velocityDirty = true;

            // Spiraling ascending beams and particles
            if (deathTicks % 3 == 0) {
                world.spawnParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY() + 1.2, boss.getZ(), 4, 0.5, 0.8, 0.5, 0.05);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, boss.getX(), boss.getY() + 1.0, boss.getZ(), 6, 0.4, 0.6, 0.4, 0.1);
            }
            if (themeParticle != null) {
                world.spawnParticles(themeParticle, boss.getX(), boss.getY() + 0.8, boss.getZ(), 5, 0.6, 0.6, 0.6, 0.04);
            }

            if (deathTicks % 12 == 0) {
                triggerScreenShake(world, boss.getEntityPos(), 24.0, 0.18);
                world.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.HOSTILE, 1.2f, 0.6f + (deathTicks / 80.0f));
            }
            return false;
        }

        // Final catastrophic death detonation
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, boss.getX(), boss.getY() + 1.0, boss.getZ(), 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, boss.getX(), boss.getY() + 1.2, boss.getZ(), 80, 1.5, 1.5, 1.5, 0.3);

        world.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                ModSounds.BOSS_DEATH, SoundCategory.HOSTILE, 2.5f, 0.7f);

        triggerScreenShake(world, boss.getEntityPos(), 32.0, 0.35);

        // Victory celebration broadcast
        TitleFadeS2CPacket fadePacket = new TitleFadeS2CPacket(10, 60, 20);
        TitleS2CPacket victoryTitle = new TitleS2CPacket(Text.literal("⚔ ¡VICTORIA ÉPICA! ⚔").formatted(Formatting.GOLD, Formatting.BOLD));
        SubtitleS2CPacket victorySub = new SubtitleS2CPacket(
                Text.literal(boss.getName().getString() + " ha sido erradicado del reino.").formatted(Formatting.YELLOW)
        );

        for (ServerPlayerEntity p : world.getPlayers(pl -> pl.squaredDistanceTo(boss.getX(), boss.getY(), boss.getZ()) <= 80.0 * 80.0)) {
            p.networkHandler.sendPacket(fadePacket);
            p.networkHandler.sendPacket(victoryTitle);
            p.networkHandler.sendPacket(victorySub);
        }

        onFinished.run();
        return true;
    }
}
