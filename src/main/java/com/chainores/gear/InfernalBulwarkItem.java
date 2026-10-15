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

public class InfernalBulwarkItem extends ChainShieldItem {

    public InfernalBulwarkItem(Settings settings) {
        super(settings, 16000);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("★ BALUARTE INFERNAL DEL MAGMA (T80) ★").formatted(Formatting.DARK_RED, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva del Inframundo:").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal(" • Coraza Ígnea: Inmunidad total a fuego y lava al bloquear; quema a los agresores").formatted(Formatting.RED));
        textConsumer.accept(Text.literal(" • Estallido Magmático: Agáchate bloqueando para detonar una erupción en 6 bloques (14 daño de fuego)").formatted(Formatting.YELLOW));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Extinguish user and grant fire immunity while actively guarding
            user.extinguish();
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));

            // Deflect incoming projectiles and ignite them
            Box box = user.getBoundingBox().expand(3.0);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-1.5));
                p.setOwner(user);
                p.setOnFireFor(8.0f);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.8f, 1.2f);
                serverWorld.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY(), p.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            });

            // Ambient embers
            if (remainingUseTicks % 8 == 0) {
                serverWorld.spawnParticles(ParticleTypes.FLAME, user.getX(), user.getY() + 0.8, user.getZ(), 3, 0.5, 0.5, 0.5, 0.03);
                serverWorld.spawnParticles(ParticleTypes.LAVA, user.getX(), user.getY() + 0.6, user.getZ(), 1, 0.3, 0.3, 0.3, 0.0);
            }

            // Magma burst when sneak blocking
            if (user.isSneaking() && remainingUseTicks % 16 == 0) {
                Box blastBox = user.getBoundingBox().expand(6.0);
                serverWorld.getEntitiesByClass(HostileEntity.class, blastBox, LivingEntity::isAlive).forEach(mob -> {
                    Vec3d push = mob.getEntityPos().subtract(user.getEntityPos()).normalize().multiply(1.4).add(0, 0.4, 0);
                    mob.setVelocity(push);
                    mob.velocityDirty = true;
                    mob.setOnFireFor(10.0f);
                    mob.damage(serverWorld, serverWorld.getDamageSources().inFire(), 14.0f);
                    serverWorld.spawnParticles(ParticleTypes.FLAME, mob.getX(), mob.getY() + 0.8, mob.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
                });

                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.9f, 1.3f);
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, user.getX(), user.getY() + 0.5, user.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }
}
