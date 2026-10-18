package com.chainores.entity;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossArenaHelper {
    public static final double DEFAULT_RADIUS = 38.0;

    public record ActiveBossDomain(UUID bossId, RegistryKey<World> dimension, BlockPos center, double radius, long lastTick) {}

    private static final Map<UUID, ActiveBossDomain> ACTIVE_DOMAINS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_WARN_TIME = new ConcurrentHashMap<>();

    public static void registerBlockProtection() {
        // 1. Prevent placing blocks or pouring buckets inside an active boss arena
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isCreative() || player.isSpectator()) return ActionResult.PASS;

            BlockPos targetPos = hitResult.getBlockPos();
            BlockPos placePos = targetPos.offset(hitResult.getSide());

            if (isBlockModificationBlocked(world, player, targetPos) || isBlockModificationBlocked(world, player, placePos)) {
                ItemStack held = player.getStackInHand(hand);
                if (held.getItem() instanceof BlockItem || held.getItem() instanceof BucketItem) {
                    notifyBlocked(player, true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // 2. Prevent initiating block break/attack in survival
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.isCreative() || player.isSpectator()) return ActionResult.PASS;

            if (isBlockModificationBlocked(world, player, pos)) {
                notifyBlocked(player, false);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // 3. Absolute block break cancellation right before destruction
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player.isCreative() || player.isSpectator()) return true;

            if (isBlockModificationBlocked(world, player, pos)) {
                notifyBlocked(player, false);
                return false;
            }
            return true;
        });
    }

    public static boolean isBoss(Entity entity) {
        return entity instanceof FrostwardenEntity ||
               entity instanceof SylvaAncientEntity ||
               entity instanceof AbyssalGuardianEntity ||
               entity instanceof NetherBehemothEntity ||
               entity instanceof VoidColossusEntity ||
               entity instanceof CosmicSovereignEntity;
    }

    public static boolean isBlockModificationBlocked(World world, PlayerEntity player, BlockPos pos) {
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }

        long now = System.currentTimeMillis();
        // Prune stale domains older than 4 seconds
        ACTIVE_DOMAINS.values().removeIf(d -> (now - d.lastTick()) > 4000L);

        RegistryKey<World> dim = world.getRegistryKey();

        // Check tracked active boss domains
        for (ActiveBossDomain domain : ACTIVE_DOMAINS.values()) {
            if (domain.dimension().equals(dim)) {
                double rSq = domain.radius() * domain.radius();

                // Block position check
                double bdx = domain.center().getX() + 0.5 - (pos.getX() + 0.5);
                double bdz = domain.center().getZ() + 0.5 - (pos.getZ() + 0.5);
                if ((bdx * bdx + bdz * bdz) <= rSq) {
                    return true;
                }

                // Player position check (player inside arena trying to place/break anything)
                double pdx = domain.center().getX() + 0.5 - player.getX();
                double pdz = domain.center().getZ() + 0.5 - player.getZ();
                if ((pdx * pdx + pdz * pdz) <= rSq) {
                    return true;
                }
            }
        }

        // Fallback spatial check for any living boss in chunk vicinity
        if (world instanceof ServerWorld sw) {
            Box searchBox = new Box(pos).expand(DEFAULT_RADIUS + 4.0);
            List<HostileEntity> bosses = sw.getEntitiesByClass(HostileEntity.class, searchBox, e -> isBoss(e) && e.isAlive());
            if (!bosses.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static void notifyBlocked(PlayerEntity player, boolean isPlacing) {
        long now = System.currentTimeMillis();
        long last = LAST_WARN_TIME.getOrDefault(player.getUuid(), 0L);
        if (now - last > 1200L) { // 1.2s debounce to prevent sound/chat spam
            LAST_WARN_TIME.put(player.getUuid(), now);
            Text msg = isPlacing
                    ? Text.literal("⚔ ¡EL PODER DEL JEFE BLOQUEA LA COLOCACIÓN DE BLOQUES EN LA ARENA! ⚔").formatted(Formatting.RED, Formatting.BOLD)
                    : Text.literal("⚔ ¡LA BARRERA DEL JEFE HACE LOS BLOQUES INDESTRUCTIBLES DURANTE EL COMBATE! ⚔").formatted(Formatting.GOLD, Formatting.BOLD);
            player.sendMessage(msg, true);

            if (player.getEntityWorld() instanceof ServerWorld sw) {
                sw.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 0.85f, 1.7f);
                sw.spawnParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    public static void cleanseArenaFoliage(ServerWorld world, BlockPos center, int radius) {
        int radSq = radius * radius;
        BlockPos.Mutable mut = new BlockPos.Mutable();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radSq) {
                    for (int y = 1; y <= 16; y++) {
                        mut.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        BlockState state = world.getBlockState(mut);
                        if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.SAPLINGS)
                                || state.isOf(Blocks.VINE) || state.isOf(Blocks.HANGING_ROOTS)
                                || state.isOf(Blocks.SPORE_BLOSSOM) || state.isOf(Blocks.BIG_DRIPLEAF)
                                || state.isOf(Blocks.SMALL_DRIPLEAF) || state.isOf(Blocks.CHORUS_PLANT)
                                || state.isOf(Blocks.CHORUS_FLOWER)) {
                            world.setBlockState(mut, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

    public static void enforceDomain(HostileEntity boss, ServerWorld world, BlockPos center, double radius, ParticleEffect domainParticle) {
        // Register / refresh active domain in global tracker
        ACTIVE_DOMAINS.put(boss.getUuid(), new ActiveBossDomain(boss.getUuid(), world.getRegistryKey(), center, radius, System.currentTimeMillis()));

        // Cleanse any rogue tree blocks or foliage on boss spawn / start of combat
        if (boss.age == 1 || boss.age == 20) {
            cleanseArenaFoliage(world, center, (int) Math.min(radius, 16.0));
        }

        if (boss.age % 6 != 0) return;

        double rSq = radius * radius;
        double outerSq = (radius + 8.0) * (radius + 8.0);
        double cx = center.getX() + 0.5;
        double cz = center.getZ() + 0.5;

        // 1. Boundary particles to show the barrier ring
        if (boss.age % 18 == 0 && domainParticle != null) {
            for (int i = 0; i < 24; i++) {
                double ang = (world.random.nextDouble()) * Math.PI * 2.0;
                double px = cx + Math.cos(ang) * radius;
                double pz = cz + Math.sin(ang) * radius;
                world.spawnParticles(domainParticle, px, boss.getY() + 1.0 + (world.random.nextDouble() * 3.0), pz,
                        1, 0, 0.05, 0, 0.02);
            }
        }

        // 2. Prevent survival players from fleeing or cheesing from outside
        for (ServerPlayerEntity player : world.getPlayers(p -> !p.isCreative() && !p.isSpectator())) {
            double distSq = (player.getX() - cx) * (player.getX() - cx) + (player.getZ() - cz) * (player.getZ() - cz);
            if (distSq >= (radius - 1.5) * (radius - 1.5) && distSq <= outerSq) {
                // Push player back inwards toward boss / center
                Vec3d inward = new Vec3d(cx - player.getX(), 0, cz - player.getZ()).normalize().multiply(0.9).add(0, 0.25, 0);
                player.setVelocity(inward);
                player.velocityDirty = true;

                player.sendMessage(
                        Text.literal("⚔ ¡LA BARRERA DEL JEFE TE IMPIDE ESCAPAR! ⚔").formatted(Formatting.RED, Formatting.BOLD),
                        true
                );
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 1.0f, 1.6f);

                if (domainParticle != null) {
                    world.spawnParticles(domainParticle, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.4, 0.5, 0.4, 0.05);
                }
            }
        }

        // 3. Vaporize incoming projectiles from outside into domain to prevent distant sniping
        Box domainBox = new Box(center).expand(radius);
        List<ProjectileEntity> projectiles = world.getEntitiesByClass(ProjectileEntity.class, domainBox,
                proj -> {
                    if (proj.getOwner() instanceof ServerPlayerEntity sp) {
                        double ownerDistSq = (sp.getX() - cx) * (sp.getX() - cx) + (sp.getZ() - cz) * (sp.getZ() - cz);
                        return ownerDistSq > (radius + 2.0) * (radius + 2.0);
                    }
                    return false;
                });

        for (ProjectileEntity proj : projectiles) {
            world.spawnParticles(domainParticle, proj.getX(), proj.getY(), proj.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            proj.discard();
        }
    }

    public static void breakDomain(ServerWorld world, BlockPos center, double radius, ParticleEffect particle) {
        // Remove from active domains
        ACTIVE_DOMAINS.values().removeIf(d -> d.dimension().equals(world.getRegistryKey())
                && d.center().getSquaredDistance(center) < 16.0);

        double cx = center.getX() + 0.5;
        double cy = center.getY() + 1.5;
        double cz = center.getZ() + 0.5;

        // Expanding ring of shattered domain particles
        for (int i = 0; i < 90; i++) {
            double ang = Math.toRadians(i * 4.0);
            double px = cx + Math.cos(ang) * radius;
            double pz = cz + Math.sin(ang) * radius;
            if (particle != null) {
                world.spawnParticles(particle, px, cy, pz, 1, 0, 0.2, 0, 0.1);
            }
        }

        world.playSound(null, center, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 2.5f, 0.8f);
        world.playSound(null, center, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.HOSTILE, 2.0f, 0.6f);
    }
}
