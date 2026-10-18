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

import java.util.List;
import java.util.function.Consumer;

public class BrambleWhipItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3800, 11.0f, 5.0f, 25, ItemTags.SWORDS
    );

    public BrambleWhipItem(Settings settings) {
        super(settings.sword(MATERIAL, 10.0f, -2.0f).maxDamage(3800));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA MÍTICA DE JEFE: TITÁN SILVESTRE ✦").formatted(Formatting.GREEN, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Zarzas Enraizadoras y Drenaje (CD: 8s)").formatted(Formatting.DARK_GREEN)));
        textConsumer.accept(Text.literal("• Enraíza a los enemigos con zarzas vivientes durante 3.5s.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Inflige Veneno II y drena vida curando al portador.").formatted(Formatting.GREEN));
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
                ModSounds.BRAMBLE_WHIP, SoundCategory.PLAYERS, 1.4f, 1.1f);

        // Particle vine line
        for (double d = 1.0; d <= 12.0; d += 0.5) {
            Vec3d p = eyePos.add(look.multiply(d));
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.02);
            serverWorld.spawnParticles(ParticleTypes.COMPOSTER, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.01);
        }

        Box targetBox = user.getBoundingBox().expand(8.0, 4.0, 8.0);
        List<LivingEntity> enemies = serverWorld.getEntitiesByClass(LivingEntity.class, targetBox,
                e -> e != user && e.isAlive() && !e.isTeammate(user));

        float totalHealed = 0.0f;
        int hitCount = 0;
        for (LivingEntity enemy : enemies) {
            Vec3d toEnemy = enemy.getEyePos().subtract(eyePos);
            if (toEnemy.length() <= 12.0 && look.dotProduct(toEnemy.normalize()) > 0.60) {
                // Root enemy (Slowness VI prevents moving)
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 70, 5), user);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1), user);
                enemy.damage(serverWorld, serverWorld.getDamageSources().playerAttack(user), 18.0f);
                enemy.setVelocity(0, 0, 0);
                enemy.velocityDirty = true;

                // Burst of thorn particles at enemy feet
                serverWorld.spawnParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, enemy.getX(), enemy.getY() + 0.5, enemy.getZ(), 25, 0.4, 0.5, 0.4, 0.05);
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, enemy.getX(), enemy.getY() + 1.0, enemy.getZ(), 10, 0.3, 0.4, 0.3, 0.02);

                hitCount++;
                totalHealed += 3.0f;
            }
        }

        if (totalHealed > 0.0f) {
            user.heal(Math.min(10.0f, totalHealed));
            serverWorld.spawnParticles(ParticleTypes.HEART, user.getX(), user.getY() + 1.2, user.getZ(), 6, 0.3, 0.4, 0.3, 0.05);
        }

        user.getItemCooldownManager().set(stack, 160);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
