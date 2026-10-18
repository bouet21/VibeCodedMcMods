package com.chainores.gear;

import com.chainores.registry.ModSounds;
import net.minecraft.block.Block;
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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class GlacialBladeItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3500, 10.0f, 4.0f, 25, ItemTags.SWORDS
    );

    public GlacialBladeItem(Settings settings) {
        super(settings.sword(MATERIAL, 9.0f, -2.2f).maxDamage(3500));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA MÍTICA DE JEFE: FROSTWARDEN ✦").formatted(Formatting.AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Ráfaga de Estacas de Hielo (CD: 7s)").formatted(Formatting.AQUA)));
        textConsumer.accept(Text.literal("• Desata un abanico cónico de carámbanos glaciales.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Inflige 16 de daño, Congelación y Lentitud IV (5s).").formatted(Formatting.BLUE));
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

        // Play active sound
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSounds.GLACIAL_BLADE, SoundCategory.PLAYERS, 1.3f, 1.0f);

        // Cone projectile burst
        int rays = 7;
        double spreadAngle = Math.toRadians(45.0);
        for (int i = 0; i < rays; i++) {
            double angle = -spreadAngle / 2.0 + (spreadAngle / (rays - 1)) * i;
            // Rotate look vector around Y axis
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vec3d rayDir = new Vec3d(
                    look.x * cos - look.z * sin,
                    look.y,
                    look.x * sin + look.z * cos
            ).normalize();

            for (double dist = 1.5; dist <= 10.0; dist += 0.8) {
                Vec3d point = eyePos.add(rayDir.multiply(dist));
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 2, 0.1, 0.1, 0.1, 0.02);
                serverWorld.spawnParticles(ParticleTypes.ITEM_SNOWBALL, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // Damage enemies in cone
        Box area = user.getBoundingBox().expand(10.0);
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, area,
                e -> e != user && e.isAlive() && !e.isTeammate(user));

        for (LivingEntity target : targets) {
            Vec3d toTarget = target.getEyePos().subtract(eyePos).normalize();
            double dot = look.dotProduct(toTarget);
            if (dot > 0.65) { // ~45 degree field of view
                target.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), 16.0f);
                target.setFrozenTicks(240);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3), user);
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
            }
        }

        user.getItemCooldownManager().set(stack, 140);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
