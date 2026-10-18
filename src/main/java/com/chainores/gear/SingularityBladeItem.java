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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class SingularityBladeItem extends Item {
    private static final ToolMaterial MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 5200, 14.0f, 8.0f, 25, ItemTags.SWORDS
    );

    public SingularityBladeItem(Settings settings) {
        super(settings.sword(MATERIAL, 15.0f, -2.2f).maxDamage(5200).fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ ARMA MÍTICA DE JEFE: COLOSO DEL VACÍO ✦").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic Derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Vórtice de Singularidad Gravitacional (CD: 12s)").formatted(Formatting.LIGHT_PURPLE)));
        textConsumer.accept(Text.literal("• Genera un micro agujero negro que succiona a todos los enemigos.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Colapsa en una detonación de vacío infligiendo 35 de daño.").formatted(Formatting.DARK_PURPLE));
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
        Vec3d center = eyePos.add(look.multiply(10.0));

        serverWorld.playSound(null, center.x, center.y, center.z,
                ModSounds.SINGULARITY_BLADE, SoundCategory.PLAYERS, 2.0f, 0.8f);

        // Black hole vortex particles
        for (int i = 0; i < 60; i++) {
            double ang = Math.toRadians(i * 6.0);
            double r = 4.0;
            double px = center.x + Math.cos(ang) * r;
            double pz = center.z + Math.sin(ang) * r;
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, px, center.y, pz, 1, 0, 0, 0, 0.05);
            serverWorld.spawnParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 2, 0.3, 0.3, 0.3, 0.5);
        }

        Box suctionBox = new Box(center.x - 10.0, center.y - 6.0, center.z - 10.0,
                center.x + 10.0, center.y + 6.0, center.z + 10.0);
        List<LivingEntity> enemies = serverWorld.getEntitiesByClass(LivingEntity.class, suctionBox,
                e -> e != user && e.isAlive() && !e.isTeammate(user));

        for (LivingEntity enemy : enemies) {
            Vec3d pull = center.subtract(enemy.getEntityPos()).normalize().multiply(1.5).add(0, 0.3, 0);
            enemy.setVelocity(pull);
            enemy.velocityDirty = true;
            enemy.damage(serverWorld, serverWorld.getDamageSources().magic(), 35.0f);
            serverWorld.spawnParticles(ParticleTypes.WITCH, enemy.getX(), enemy.getY() + 1.0, enemy.getZ(), 10, 0.2, 0.4, 0.2, 0.05);
        }

        // Final implosion burst
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.5, 0.5, 0.5, 0.0);
        serverWorld.spawnParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 40, 1.0, 1.0, 1.0, 0.1);

        user.getItemCooldownManager().set(stack, 240);
        stack.damage(1, user);
        return ActionResult.SUCCESS;
    }
}
