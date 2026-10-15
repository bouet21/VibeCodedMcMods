package com.chainores.item;

import com.chainores.block.ChainOreBlock;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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

    public static int getRadarMode(ItemStack stack) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (comp == null) return 0;
        NbtCompound nbt = comp.copyNbt();
        return nbt.contains("radar_mode") ? nbt.getInt("radar_mode").orElse(0) : 0;
    }

    public static void cycleRadarMode(ItemStack stack) {
        int current = getRadarMode(stack);
        int next = (current + 1) % 3;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt("radar_mode", next);
        });
    }

    public static String getModeName(int mode) {
        return switch (mode) {
            case 1 -> "VETA DE MAYOR TIER";
            case 2 -> "SONAR GEOLÓGICO PROFUNDO";
            default -> "PROXIMIDAD (MÁS CERCANA)";
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ Radar de Prospección Subterránea Avanzado ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("• Clic derecho: Escanea vetas en el área").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal("• Shift + Clic derecho: Cambiar modo de escáner").formatted(Formatting.AQUA));

        int mode = getRadarMode(stack);
        Formatting col = switch (mode) {
            case 1 -> Formatting.LIGHT_PURPLE;
            case 2 -> Formatting.GREEN;
            default -> Formatting.AQUA;
        };
        textConsumer.accept(Text.literal("Modo actual: ").formatted(Formatting.GRAY)
                .append(Text.literal("[" + getModeName(mode) + "]").formatted(col, Formatting.BOLD)));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            cycleRadarMode(stack);
            int newMode = getRadarMode(stack);
            if (!world.isClient()) {
                user.sendMessage(Text.literal("⚡ Radar sintonizado a: [" + getModeName(newMode) + "]").formatted(Formatting.GOLD, Formatting.BOLD), true);
            }
            user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8f, 1.4f);
            return ActionResult.SUCCESS;
        }

        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos playerPos = player.getBlockPos();

            int mode = getRadarMode(stack);
            int radius = (mode == 2) ? 48 : (mode == 1 ? 36 : 28);

            BlockPos targetPos = null;
            double closestDistSq = Double.MAX_VALUE;
            int bestTier = -1;

            // Search radius around player
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq > radius * radius) continue;

                        BlockPos checkPos = playerPos.add(dx, dy, dz);
                        BlockState state = serverWorld.getBlockState(checkPos);

                        if (state.getBlock() instanceof ChainOreBlock oreBlock) {
                            int t = oreBlock.getTier();
                            if (mode == 1) {
                                // Prioritize highest tier
                                if (t > bestTier || (t == bestTier && distSq < closestDistSq)) {
                                    bestTier = t;
                                    closestDistSq = distSq;
                                    targetPos = checkPos;
                                }
                            } else {
                                // Prioritize closest distance
                                if (distSq < closestDistSq) {
                                    closestDistSq = distSq;
                                    targetPos = checkPos;
                                    bestTier = t;
                                }
                            }
                        }
                    }
                }
            }

            if (targetPos != null && bestTier > 0) {
                int dist = (int) Math.sqrt(closestDistSq);
                OreTier oreData = (bestTier <= OreRegistryData.TIERS.size()) ? OreRegistryData.TIERS.get(bestTier - 1) : null;
                String oreName = oreData != null ? ("#" + bestTier + " " + oreData.esName) : ("Tier " + bestTier);

                String directionStr = getCardinalDirection(playerPos, targetPos);
                int yDiff = targetPos.getY() - playerPos.getY();
                String heightStr = yDiff < 0 ? (Math.abs(yDiff) + "m abajo") : yDiff > 0 ? (yDiff + "m arriba") : "misma altura";

                // Action bar ping
                player.sendMessage(
                        Text.literal("✦ Eco detectado: ").formatted(Formatting.GOLD, Formatting.BOLD)
                                .append(Text.literal(oreName).formatted(Formatting.YELLOW, Formatting.BOLD))
                                .append(Text.literal(" a " + dist + "m al " + directionStr + " (" + heightStr + ")").formatted(Formatting.AQUA)),
                        true
                );

                // Closer = higher pitch sound
                float pitch = Math.clamp(2.0f - ((float) dist / radius) * 1.2f, 0.8f, 2.0f);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0f, pitch);

                // Particle trail pointing to the vein
                Vec3d start = player.getEyePos();
                Vec3d target = targetPos.toCenterPos();
                Vec3d dir = target.subtract(start).normalize();
                for (int i = 1; i <= 8; i++) {
                    Vec3d p = start.add(dir.multiply(i * 0.8));
                    serverWorld.spawnParticles(ParticleTypes.WAX_ON, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
                }

                player.getItemCooldownManager().set(stack, 20);
            } else {
                player.sendMessage(
                        Text.literal("✖ No se detectaron vetas en un radio de " + radius + " bloques.").formatted(Formatting.RED),
                        true
                );
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.7f, 0.6f);
                player.getItemCooldownManager().set(stack, 15);
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
