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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class SupernovaScepterItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 6000, 16.0f, 10.0f, 30, ItemTags.SWORDS
    );

    public SupernovaScepterItem(Settings settings) {
        super(settings.sword(MATERIAL, 18.0f, -2.0f).maxDamage(6000).fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA SUPREMA: SOBERANO CÓSMICO ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Lluvia Orbital de Meteoros Cósmicos (CD: 14s)").formatted(Formatting.GOLD)));
        textConsumer.accept(Text.literal("• Bombardea la zona designada con 3 impactos estelares.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• 45 de daño en área, onda expansiva y otorga Regeneración y Prisa.").formatted(Formatting.YELLOW));
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
        Vec3d targetCenter = eyePos.add(look.multiply(16.0));

        // Find floor position
        BlockPos bpos = BlockPos.ofFloored(targetCenter.x, targetCenter.y, targetCenter.z);
        while (bpos.getY() > serverWorld.getBottomY() && serverWorld.getBlockState(bpos).isAir()) {
            bpos = bpos.down();
        }
        double groundY = bpos.getY() + 1.0;
        Vec3d groundCenter = new Vec3d(targetCenter.x, groundY, targetCenter.z);

        serverWorld.playSound(null, groundCenter.x, groundCenter.y, groundCenter.z,
                ModSounds.SUPERNOVA_SCEPTER, SoundCategory.PLAYERS, 2.5f, 1.0f);

        // Ground targeting ring
        for (int i = 0; i < 36; i++) {
            double ang = Math.toRadians(i * 10.0);
            double rx = groundCenter.x + Math.cos(ang) * 5.0;
            double rz = groundCenter.z + Math.sin(ang) * 5.0;
            serverWorld.spawnParticles(ParticleTypes.END_ROD, rx, groundY + 0.2, rz, 1, 0, 0.05, 0, 0.02);
            serverWorld.spawnParticles(ParticleTypes.FIREWORK, rx, groundY + 0.2, rz, 1, 0, 0.1, 0, 0.05);
        }

        // 3 cosmic explosions
        double[][] offsets = {{0, 0}, {-2.5, 2.0}, {2.5, -2.0}};
        for (double[] off : offsets) {
            double ex = groundCenter.x + off[0];
            double ez = groundCenter.z + off[1];
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, ex, groundY + 0.5, ez, 1, 0, 0, 0, 0);
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, ex, groundY + 1.0, ez, 15, 0.3, 0.5, 0.3, 0.1);
            serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, ex, groundY + 1.5, ez, 40, 1.5, 1.5, 1.5, 0.2);

            Box blastBox = new Box(ex - 6.0, groundY - 2.0, ez - 6.0, ex + 6.0, groundY + 5.0, ez + 6.0);
            List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, blastBox,
                    e -> e != user && e.isAlive() && !e.isTeammate(user));

            for (LivingEntity target : targets) {
                target.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), 45.0f);
                Vec3d knock = target.getEntityPos().subtract(new Vec3d(ex, groundY, ez)).normalize().multiply(1.8).add(0, 0.6, 0);
                target.setVelocity(knock);
                target.velocityDirty = true;
            }
        }

        // Buff user with cosmic aura
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 160, 1));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, 1));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 160, 1));

        user.getItemCooldownManager().set(stack, 280);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
