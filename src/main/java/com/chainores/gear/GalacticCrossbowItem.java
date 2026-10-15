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
        if (projectile instanceof PersistentProjectileEntity arrow) {
            arrow.applyDamageModifier(2.8f);
            arrow.setCritical(true);
        }
        if (shooter.getEntityWorld() instanceof ServerWorld) {
            shooter.getEntityWorld().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.7f, 1.8f);
        }
    }
}
