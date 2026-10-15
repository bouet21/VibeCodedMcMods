package com.chainores.gear;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class AbyssalAegisItem extends ChainShieldItem {

    public AbyssalAegisItem(Settings settings) {
        super(settings, 12000);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("★ ÉGIDA ABISAL DE LAS SOMBRAS (T70) ★").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva del Vacío:").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Purificación de Tinieblas: Inmune a Ceguera y Oscuridad mientras bloqueas").formatted(Formatting.DARK_PURPLE));
        textConsumer.accept(Text.literal(" • Grito Sónico del Abismo: Agáchate bloqueando para disparar un Sonic Boom perforante de 18 de daño").formatted(Formatting.BLUE));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Cleanse Darkness and Blindness
            if (user.hasStatusEffect(StatusEffects.DARKNESS)) {
                user.removeStatusEffect(StatusEffects.DARKNESS);
            }
            if (user.hasStatusEffect(StatusEffects.BLINDNESS)) {
                user.removeStatusEffect(StatusEffects.BLINDNESS);
            }

            // Deflect incoming projectiles with sculk resonance
            Box box = user.getBoundingBox().expand(3.2);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-1.6));
                p.setOwner(user);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.PLAYERS, 0.8f, 1.4f);
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY(), p.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
            });

            // Ambient sculk aura
            if (remainingUseTicks % 12 == 0) {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, user.getX(), user.getY() + 0.9, user.getZ(), 2, 0.4, 0.5, 0.4, 0.02);
            }

            // Sonic Boom attack when sneak blocking
            if (user.isSneaking() && remainingUseTicks % 20 == 0) {
                Vec3d look = user.getRotationVector();
                Vec3d start = user.getEyePos();

                for (int i = 1; i <= 10; i++) {
                    Vec3d beamPoint = start.add(look.multiply(i));
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, beamPoint.x, beamPoint.y, beamPoint.z, 1, 0, 0, 0, 0);
                }

                Box hitBox = user.getBoundingBox().stretch(look.multiply(10.0)).expand(2.0);
                serverWorld.getEntitiesByClass(HostileEntity.class, hitBox, LivingEntity::isAlive).forEach(mob -> {
                    Vec3d push = look.multiply(1.8).add(0, 0.4, 0);
                    mob.setVelocity(push);
                    mob.velocityDirty = true;
                    mob.damage(serverWorld, serverWorld.getDamageSources().sonicBoom(user), 18.0f);
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));
                });

                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 140, 1));
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.2f, 1.1f);
            }
        }
    }
}
