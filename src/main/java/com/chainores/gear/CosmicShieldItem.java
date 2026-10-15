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

public class CosmicShieldItem extends ChainShieldItem {

    public CosmicShieldItem(Settings settings) {
        super(settings, 35000);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("★ ÉGIDA CELESTIAL DEL GÉNESIS (T150) ★").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva Divina / Cósmica:").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal(" • Espejo Cuántico: Refleja proyectiles con 2.5x de aceleración e impacto devastador").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Vórtice Gravitacional: Agáchate bloqueando para succionar agresores y detonar una Supernova (26 daño divino)").formatted(Formatting.LIGHT_PURPLE));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Deflect incoming projectiles with quantum speed
            Box box = user.getBoundingBox().expand(4.0);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-2.5));
                p.setOwner(user);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.2f, 1.8f);
                serverWorld.spawnParticles(ParticleTypes.FIREWORK, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
                serverWorld.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
            });

            // Regeneration & Resistance aura while guarding
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, true, false));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, true, false));

            // Ambient stellar particles
            if (remainingUseTicks % 6 == 0) {
                serverWorld.spawnParticles(ParticleTypes.END_ROD, user.getX(), user.getY() + 0.8, user.getZ(), 2, 0.4, 0.6, 0.4, 0.05);
                serverWorld.spawnParticles(ParticleTypes.PORTAL, user.getX(), user.getY() + 0.5, user.getZ(), 4, 0.5, 0.5, 0.5, 0.1);
            }

            // Gravitational Singularity + Supernova when sneak blocking
            if (user.isSneaking() && remainingUseTicks % 20 == 0) {
                Box singularityBox = user.getBoundingBox().expand(9.0);
                serverWorld.getEntitiesByClass(HostileEntity.class, singularityBox, LivingEntity::isAlive).forEach(mob -> {
                    // Pull vortex towards user then blast
                    Vec3d pull = user.getEntityPos().subtract(mob.getEntityPos()).normalize().multiply(1.0);
                    mob.setVelocity(pull);
                    mob.velocityDirty = true;
                    mob.damage(serverWorld, serverWorld.getDamageSources().magic(), 26.0f);
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + 1.0, mob.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
                });

                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.5f, 1.5f);
                serverWorld.spawnParticles(ParticleTypes.FIREWORK, user.getX(), user.getY() + 1.0, user.getZ(), 1, 0, 0, 0, 0);
                serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, user.getX(), user.getY() + 1.0, user.getZ(), 30, 2.0, 1.0, 2.0, 0.2);
            }
        }
    }
}
