package com.chainores.gear;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class DragonBreathBowItem extends BowItem {

    public DragonBreathBowItem(Settings settings) {
        super(settings.maxDamage(4500).fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier 115").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Balística Dracónica: ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal("Flechas de Aliento de Dragón hiper-veloces (+6 daño) y rugido de dragón al disparar").formatted(Formatting.GOLD)));
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float roll, LivingEntity target) {
        super.shoot(shooter, projectile, index, speed * 1.6f, divergence, roll, target);
        ItemStack bowStack = shooter.getActiveItem();
        if (bowStack.isEmpty() || !(bowStack.getItem() instanceof DragonBreathBowItem)) {
            bowStack = shooter.getMainHandStack().getItem() instanceof DragonBreathBowItem ? shooter.getMainHandStack() : shooter.getOffHandStack();
        }
        float mult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(bowStack);

        projectile.addCommandTag("dragon_arrow");
        if (projectile instanceof PersistentProjectileEntity arrow) {
            arrow.applyDamageModifier(1.8f * mult);
            arrow.setCritical(true);
        }
        if (shooter.getEntityWorld() instanceof ServerWorld sw) {
            sw.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 0.9f, 1.6f);
            sw.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.7f, 1.8f);

            net.minecraft.util.math.Vec3d rot = shooter.getRotationVector();
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.WITCH,
                    shooter.getX() + rot.x * 1.2, shooter.getEyeY() - 0.1, shooter.getZ() + rot.z * 1.2,
                    18, 0.25, 0.25, 0.25, 0.08);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL,
                    shooter.getX() + rot.x * 0.8, shooter.getEyeY() - 0.1, shooter.getZ() + rot.z * 0.8,
                    12, 0.15, 0.15, 0.15, 0.05);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            com.chainores.modifier.ChainModifiers.applyReforgeHeldBuffs(entity, stack);
        }
    }
}
