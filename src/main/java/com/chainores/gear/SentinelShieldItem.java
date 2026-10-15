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

public class SentinelShieldItem extends ChainShieldItem {

    public SentinelShieldItem(Settings settings) {
        super(settings, 4000);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("★ ESCUDO DEL CENTINELA CINÉTICO (T10) ★").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva Cinética:").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal(" • Descarga de Impacto: Ralentiza atacantes y desvía proyectiles a 1.4x").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Pulso Electrostático: Agáchate bloqueando para emitir una descarga en cadena y ganar Velocidad II").formatted(Formatting.LIGHT_PURPLE));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Deflect incoming projectiles
            Box box = user.getBoundingBox().expand(2.8);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-1.4));
                p.setOwner(user);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.3f);
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY(), p.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
            });

            // Electrostatic aura when blocking
            if (remainingUseTicks % 10 == 0) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, user.getX(), user.getY() + 1.0, user.getZ(), 3, 0.4, 0.5, 0.4, 0.05);
            }

            // Shockwave pulse if sneaking while holding guard
            if (user.isSneaking() && remainingUseTicks % 15 == 0) {
                Box shockBox = user.getBoundingBox().expand(5.0);
                serverWorld.getEntitiesByClass(HostileEntity.class, shockBox, LivingEntity::isAlive).forEach(mob -> {
                    Vec3d push = mob.getEntityPos().subtract(user.getEntityPos()).normalize().multiply(1.3).add(0, 0.3, 0);
                    mob.setVelocity(push);
                    mob.velocityDirty = true;
                    mob.damage(serverWorld, serverWorld.getDamageSources().thorns(user), 7.0f);
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + 0.8, mob.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
                });
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 80, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 80, 0));
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.5f, 1.8f);
            }
        }
    }
}
