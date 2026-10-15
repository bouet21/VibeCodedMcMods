package com.chainores.gear;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class ChainShieldItem extends Item {

    public ChainShieldItem(Settings settings) {
        super(settings.maxDamage(15000).fireproof());
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("★ ESCUDO DE LA ÉGIDA CÓSMICA ★").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Habilidad Defensiva Superior: ").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal(" • Reflejo Balístico: Desvía proyectiles y flechas de vuelta al atacante").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Pulso Repulsor: Agáchate mientras bloqueas para emitir ondas de choque").formatted(Formatting.LIGHT_PURPLE));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Deflect incoming projectiles
            Box box = user.getBoundingBox().expand(3.0);
            serverWorld.getEntitiesByClass(ProjectileEntity.class, box, p -> p.getOwner() != user).forEach(p -> {
                p.setVelocity(p.getVelocity().multiply(-1.5));
                p.setOwner(user);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.4f);
                serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY(), p.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
            });

            // Shockwave pulse if sneaking while holding guard
            if (user.isSneaking() && remainingUseTicks % 15 == 0) {
                Box shockBox = user.getBoundingBox().expand(4.5);
                serverWorld.getEntitiesByClass(HostileEntity.class, shockBox, LivingEntity::isAlive).forEach(mob -> {
                    Vec3d push = mob.getEntityPos().subtract(user.getEntityPos()).normalize().multiply(1.2).add(0, 0.35, 0);
                    mob.setVelocity(push);
                    mob.velocityDirty = true;
                    mob.damage(serverWorld, serverWorld.getDamageSources().thorns(user), 8.0f);
                });
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 0.7f, 1.6f);
                serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, user.getX(), user.getY() + 0.5, user.getZ(), 1, 0, 0, 0, 0);
            }
        }
        super.usageTick(world, user, stack, remainingUseTicks);
    }
}
