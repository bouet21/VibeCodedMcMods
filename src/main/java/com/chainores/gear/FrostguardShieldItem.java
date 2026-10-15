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

public class FrostguardShieldItem extends ChainShieldItem {

    public FrostguardShieldItem(Settings settings) {
        super(settings, 7500);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("★ BALUARTE DE ESCARCHA ETERNA (T20) ★").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva Glacial:").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Espinas Glaciares: Congela a los agresores y ralentiza proyectiles desviados").formatted(Formatting.BLUE));
        textConsumer.accept(Text.literal(" • Ventisca Cero Absoluto: Agáchate bloqueando para congelar el entorno en 6 bloques (Ralentización IV)").formatted(Formatting.WHITE));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Deflect incoming projectiles with icy kickback
            Box box = user.getBoundingBox().expand(3.0);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-1.5));
                p.setOwner(user);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.9f, 1.6f);
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY(), p.getZ(), 6, 0.2, 0.2, 0.2, 0.05);
            });

            // Passive freezing aura around shield
            if (remainingUseTicks % 10 == 0) {
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, user.getX(), user.getY() + 0.8, user.getZ(), 4, 0.5, 0.5, 0.5, 0.02);
            }

            // Blizzard burst if sneaking while holding guard
            if (user.isSneaking() && remainingUseTicks % 15 == 0) {
                Box frostBox = user.getBoundingBox().expand(6.0);
                serverWorld.getEntitiesByClass(HostileEntity.class, frostBox, LivingEntity::isAlive).forEach(mob -> {
                    Vec3d push = mob.getEntityPos().subtract(user.getEntityPos()).normalize().multiply(0.8);
                    mob.setVelocity(push);
                    mob.velocityDirty = true;
                    mob.setFrozenTicks(Math.min(mob.getFrozenTicks() + 100, 300));
                    mob.damage(serverWorld, serverWorld.getDamageSources().freeze(), 9.0f);
                    mob.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3));
                    serverWorld.spawnParticles(ParticleTypes.ITEM_SNOWBALL, mob.getX(), mob.getY() + 0.8, mob.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
                });
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 1));
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 1.0f, 0.8f);
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, user.getX(), user.getY() + 0.5, user.getZ(), 20, 2.0, 0.5, 2.0, 0.08);
            }
        }
    }
}
