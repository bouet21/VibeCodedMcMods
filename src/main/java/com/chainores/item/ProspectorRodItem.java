package com.chainores.item;

import com.chainores.block.ChainOreBlock;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class ProspectorRodItem extends Item {

    public ProspectorRodItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ Radar de Prospección Subterránea ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Clic derecho: Escanea un radio de 24 bloques buscando vetas").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("Indica distancia, dirección cardinal y altura").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos playerPos = player.getBlockPos();
            int radius = 24;

            BlockPos closestPos = null;
            double closestDistSq = Double.MAX_VALUE;
            int foundTier = -1;

            // Search radius around player
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq > radius * radius) continue;

                        BlockPos checkPos = playerPos.add(dx, dy, dz);
                        BlockState state = serverWorld.getBlockState(checkPos);

                        if (state.getBlock() instanceof ChainOreBlock oreBlock) {
                            if (distSq < closestDistSq) {
                                closestDistSq = distSq;
                                closestPos = checkPos;
                                foundTier = oreBlock.getTier();
                            }
                        }
                    }
                }
            }

            if (closestPos != null && foundTier > 0) {
                int dist = (int) Math.sqrt(closestDistSq);
                OreTier oreData = (foundTier <= OreRegistryData.TIERS.size()) ? OreRegistryData.TIERS.get(foundTier - 1) : null;
                String oreName = oreData != null ? ("#" + foundTier + " " + oreData.esName) : ("Tier " + foundTier);

                String directionStr = getCardinalDirection(playerPos, closestPos);
                int yDiff = closestPos.getY() - playerPos.getY();
                String heightStr = yDiff < 0 ? (Math.abs(yDiff) + " bloques abajo") : yDiff > 0 ? (yDiff + " bloques arriba") : "a tu misma altura";

                // Action bar ping
                player.sendMessage(
                        Text.literal("✦ Veta detectada: ").formatted(Formatting.GOLD, Formatting.BOLD)
                                .append(Text.literal(oreName).formatted(Formatting.YELLOW, Formatting.BOLD))
                                .append(Text.literal(" a " + dist + "m al " + directionStr + " (" + heightStr + ")").formatted(Formatting.AQUA)),
                        true
                );

                // Closer = higher pitch sound
                float pitch = Math.clamp(2.0f - ((float) dist / radius) * 1.2f, 0.8f, 2.0f);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.9f, pitch);

                // Particle trail pointing to the vein
                Vec3d start = player.getEyePos();
                Vec3d target = closestPos.toCenterPos();
                Vec3d dir = target.subtract(start).normalize();
                for (int i = 1; i <= 6; i++) {
                    Vec3d p = start.add(dir.multiply(i * 0.7));
                    serverWorld.spawnParticles(ParticleTypes.WAX_ON, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
                }

                player.getItemCooldownManager().set(stack, 25);
            } else {
                player.sendMessage(
                        Text.literal("✖ No se detectaron vetas de Chain Ores en un radio de 24 bloques.").formatted(Formatting.RED),
                        true
                );
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.7f, 0.6f);
                player.getItemCooldownManager().set(stack, 20);
            }
        }

        return ActionResult.SUCCESS;
    }

    private String getCardinalDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360;

        if (angle >= 337.5 || angle < 22.5) return "Sur";
        if (angle >= 22.5 && angle < 67.5) return "Suroeste";
        if (angle >= 67.5 && angle < 112.5) return "Oeste";
        if (angle >= 112.5 && angle < 157.5) return "Noroeste";
        if (angle >= 157.5 && angle < 202.5) return "Norte";
        if (angle >= 202.5 && angle < 247.5) return "Noreste";
        if (angle >= 247.5 && angle < 292.5) return "Este";
        return "Sureste";
    }
}
