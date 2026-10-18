package com.chainores.gear;

import com.chainores.registry.ModSounds;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class AbyssalTridentItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4200, 12.0f, 6.0f, 25, ItemTags.SWORDS
    );

    public AbyssalTridentItem(Settings settings) {
        super(settings.sword(MATERIAL, 12.0f, -2.4f).maxDamage(4200));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA MÍTICA DE JEFE: GUARDIÁN DEL ABISMO ✦").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Onda Sónica de Sculk (CD: 10s)").formatted(Formatting.AQUA)));
        textConsumer.accept(Text.literal("• Dispara un cañonazo sónico de 24 bloques que atraviesa muros.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Inflige 28 de daño penetrante, rechazo masivo y Oscuridad.").formatted(Formatting.DARK_AQUA));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.PASS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        Vec3d eyePos = user.getEyePos();
        Vec3d look = user.getRotationVector();

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSounds.ABYSSAL_TRIDENT, SoundCategory.PLAYERS, 2.0f, 0.9f);

        Set<LivingEntity> hitEntities = new HashSet<>();
        double range = 24.0;
        double step = 1.0;

        for (double d = 1.0; d <= range; d += step) {
            Vec3d point = eyePos.add(look.multiply(d));
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, point.x, point.y, point.z, 2, 0.15, 0.15, 0.15, 0.03);

            Box sliceBox = new Box(point.x - 1.5, point.y - 1.5, point.z - 1.5, point.x + 1.5, point.y + 1.5, point.z + 1.5);
            List<LivingEntity> hitInSlice = serverWorld.getEntitiesByClass(LivingEntity.class, sliceBox,
                    e -> e != user && e.isAlive() && !e.isTeammate(user) && !hitEntities.contains(e));

            for (LivingEntity enemy : hitInSlice) {
                hitEntities.add(enemy);
                enemy.damage(serverWorld, serverWorld.getDamageSources().magic(), 28.0f);
                Vec3d knockback = look.multiply(1.8).add(0, 0.45, 0);
                enemy.setVelocity(knockback);
                enemy.velocityDirty = true;
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0), user);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2), user);
            }
        }

        user.getItemCooldownManager().set(stack, 200);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
