package com.chainores.gear;

import com.chainores.registry.ModSounds;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class InfernalWaraxeItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4600, 13.0f, 7.0f, 25, ItemTags.AXES
    );

    public InfernalWaraxeItem(Settings settings) {
        super(settings.axe(MATERIAL, 14.0f, -3.0f).maxDamage(4600).fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA MÍTICA DE JEFE: BEHEMOTH DEL NETHER ✦").formatted(Formatting.RED, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Fisura Magmática y Erupción (CD: 9s)").formatted(Formatting.GOLD)));
        textConsumer.accept(Text.literal("• Desata una grieta sísmica de lava que catapulta enemigos.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Inflige 26 de daño ígneo, quemadura de 8s y lanzamiento aéreo.").formatted(Formatting.DARK_RED));
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
        Vec3d origin = user.getEntityPos();
        Vec3d lookHorizontal = new Vec3d(user.getRotationVector().x, 0, user.getRotationVector().z).normalize();

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                ModSounds.INFERNAL_WARAXE, SoundCategory.PLAYERS, 1.8f, 0.9f);

        Set<LivingEntity> hitEnemies = new HashSet<>();
        double length = 12.0;

        for (double d = 1.5; d <= length; d += 1.0) {
            Vec3d targetGround = origin.add(lookHorizontal.multiply(d));
            BlockPos bpos = BlockPos.ofFloored(targetGround.x, targetGround.y, targetGround.z);

            // Find surface block
            while (bpos.getY() > serverWorld.getBottomY() && serverWorld.getBlockState(bpos).isAir()) {
                bpos = bpos.down();
            }

            double py = bpos.getY() + 1.0;
            serverWorld.spawnParticles(ParticleTypes.LAVA, targetGround.x, py, targetGround.z, 3, 0.2, 0.4, 0.2, 0.1);
            serverWorld.spawnParticles(ParticleTypes.FLAME, targetGround.x, py + 0.5, targetGround.z, 8, 0.3, 0.8, 0.3, 0.08);
            serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, targetGround.x, py + 0.2, targetGround.z, 2, 0.2, 0.5, 0.2, 0.05);

            Box columnBox = new Box(targetGround.x - 1.5, py - 0.5, targetGround.z - 1.5,
                    targetGround.x + 1.5, py + 3.0, targetGround.z + 1.5);
            List<LivingEntity> enemiesInColumn = serverWorld.getEntitiesByClass(LivingEntity.class, columnBox,
                    e -> e != user && e.isAlive() && !e.isTeammate(user) && !hitEnemies.contains(e));

            for (LivingEntity enemy : enemiesInColumn) {
                hitEnemies.add(enemy);
                enemy.damage(serverWorld, serverWorld.getDamageSources().onFire(), 26.0f);
                enemy.setOnFireFor(8.0f);
                Vec3d currentVel = enemy.getVelocity();
                enemy.setVelocity(currentVel.x * 0.5, 0.95, currentVel.z * 0.5);
                enemy.velocityDirty = true;
            }
        }

        user.getItemCooldownManager().set(stack, 180);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
