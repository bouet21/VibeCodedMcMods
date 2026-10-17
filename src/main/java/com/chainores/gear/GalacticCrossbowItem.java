package com.chainores.gear;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.Consumer;

public class GalacticCrossbowItem extends CrossbowItem {

    public GalacticCrossbowItem(Settings settings) {
        super(settings.maxDamage(8000).fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier 125").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Balística Cósmica: ").formatted(Formatting.BLUE)
                .append(Text.literal("Proyectiles hiper-densos sin gravedad (+12 daño), penetración 5x y estruendo sónico").formatted(Formatting.AQUA)));
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float roll, LivingEntity target) {
        super.shoot(shooter, projectile, index, speed * 2.2f, divergence, roll, target);
        projectile.setNoGravity(true);
        ItemStack xbowStack = shooter.getActiveItem();
        if (xbowStack.isEmpty() || !(xbowStack.getItem() instanceof GalacticCrossbowItem)) {
            xbowStack = shooter.getMainHandStack().getItem() instanceof GalacticCrossbowItem ? shooter.getMainHandStack() : shooter.getOffHandStack();
        }
        float mult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(xbowStack);

        projectile.addCommandTag("galactic_arrow");
        if (projectile instanceof PersistentProjectileEntity arrow) {
            arrow.applyDamageModifier(2.8f * mult);
            arrow.setCritical(true);
        }
        if (shooter.getEntityWorld() instanceof ServerWorld sw) {
            sw.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.8f, 1.8f);
            sw.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 1.0f, 2.0f);

            net.minecraft.util.math.Vec3d rot = shooter.getRotationVector();
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.SONIC_BOOM,
                    shooter.getX() + rot.x * 1.5, shooter.getEyeY() - 0.1, shooter.getZ() + rot.z * 1.5,
                    1, 0, 0, 0, 0);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK,
                    shooter.getX() + rot.x * 1.0, shooter.getEyeY() - 0.1, shooter.getZ() + rot.z * 1.0,
                    15, 0.3, 0.3, 0.3, 0.2);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                    shooter.getX() + rot.x * 0.8, shooter.getEyeY(), shooter.getZ() + rot.z * 0.8,
                    8, 0.1, 0.1, 0.1, 0.05);
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
