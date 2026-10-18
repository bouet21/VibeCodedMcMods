package com.chainores.entity;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BossArenaHelper {
    public static final double DEFAULT_RADIUS = 38.0;

    public static void enforceDomain(HostileEntity boss, ServerWorld world, BlockPos center, double radius, ParticleEffect domainParticle) {
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
