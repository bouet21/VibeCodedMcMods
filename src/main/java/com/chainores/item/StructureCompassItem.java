package com.chainores.item;

import com.chainores.ChainOresMod;
import com.chainores.world.StructureLocator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

public class StructureCompassItem extends Item {

    public StructureCompassItem() {
        super(new Item.Settings()
                .maxCount(1)
                .rarity(Rarity.RARE)
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("structure_compass")))
        );
    }

    public static int getTargetMode(ItemStack stack) {
        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt != null) {
            return nbt.copyNbt().getInt("structure_mode").orElse(0);
        }
        return 0; // 0 = Auto/Dimension, 1 = Forge, 2 = Shrine, 3 = Altar
    }

    public static void cycleTargetMode(ItemStack stack) {
        int current = getTargetMode(stack);
        int next = (current + 1) % 4;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt("structure_mode", next);
        });
    }

    public static String getModeName(int mode) {
        return switch (mode) {
            case 1 -> "FORJA ABANDONADA";
            case 2 -> "SANTUARIO DEL NETHER";
            case 3 -> "ALTAR CÓSMICO";
            default -> "AUTO (SEGÚN DIMENSIÓN)";
        };
    }

    private static String getTargetTypeFromMode(int mode, World world) {
        return switch (mode) {
            case 1 -> StructureLocator.TYPE_FORGE;
            case 2 -> StructureLocator.TYPE_SHRINE;
            case 3 -> StructureLocator.TYPE_ALTAR;
            default -> {
                if (world.getRegistryKey().equals(World.OVERWORLD)) {
                    yield StructureLocator.TYPE_FORGE;
                } else if (world.getRegistryKey().equals(World.NETHER)) {
                    yield StructureLocator.TYPE_SHRINE;
                } else {
                    yield StructureLocator.TYPE_ALTAR;
                }
            }
        };
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            BlockPos playerPos = player.getBlockPos();

            // Shift-click cycles mode or displays full chat coordinates
            if (player.isSneaking()) {
                cycleTargetMode(stack);
                int newMode = getTargetMode(stack);
                player.sendMessage(Text.literal("🧭 Brújula sintonizada a: [" + getModeName(newMode) + "]").formatted(Formatting.GOLD, Formatting.BOLD), true);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8f, 1.4f);
                return ActionResult.SUCCESS;
            }

            int mode = getTargetMode(stack);
            String targetType = getTargetTypeFromMode(mode, world);

            Optional<StructureLocator.StructureEntry> result = StructureLocator.findNearest(serverWorld, playerPos, targetType);

            if (result.isPresent()) {
                StructureLocator.StructureEntry entry = result.get();
                int dx = entry.x() - playerPos.getX();
                int dz = entry.z() - playerPos.getZ();
                int dist = (int) Math.sqrt(dx * dx + dz * dz);
                String cardinal = getCardinalDirection(dx, dz);
                String name = StructureLocator.getLocalizedName(entry.type());

                // Action bar notification
                player.sendMessage(
                        Text.literal("🧭 ").formatted(Formatting.GOLD)
                                .append(Text.literal(name).formatted(Formatting.AQUA, Formatting.BOLD))
                                .append(Text.literal(" a ").formatted(Formatting.GRAY))
                                .append(Text.literal(dist + " bloques").formatted(Formatting.YELLOW, Formatting.BOLD))
                                .append(Text.literal(" al ").formatted(Formatting.GRAY))
                                .append(Text.literal(cardinal).formatted(Formatting.GREEN, Formatting.BOLD))
                                .append(Text.literal(" [X: " + entry.x() + ", Y: " + entry.y() + ", Z: " + entry.z() + "]").formatted(Formatting.DARK_AQUA)),
                        true
                );

                // Detailed clickable chat info
                player.sendMessage(Text.literal("═══════════════════════════════════════════════").formatted(Formatting.DARK_PURPLE), false);
                player.sendMessage(Text.literal("✦ RESONANCIA ESTRUCTURAL DETECTADA ✦").formatted(Formatting.GOLD, Formatting.BOLD), false);
                player.sendMessage(Text.literal("• Estructura: ").formatted(Formatting.GRAY).append(Text.literal(name).formatted(Formatting.AQUA, Formatting.BOLD)), false);
                player.sendMessage(Text.literal("• Coordenadas: ").formatted(Formatting.GRAY)
                        .append(Text.literal("X: " + entry.x() + "  Y: " + entry.y() + "  Z: " + entry.z()).formatted(Formatting.YELLOW)), false);
                player.sendMessage(Text.literal("• Distancia: ").formatted(Formatting.GRAY)
                        .append(Text.literal(dist + " bloques al " + cardinal).formatted(Formatting.GREEN)), false);

                Text tpBtn = Text.literal("✦ [TELETRANSPORTARSE AQUÍ] ✦")
                        .styled(s -> s.withColor(Formatting.LIGHT_PURPLE)
                                .withBold(true)
                                .withUnderline(true)
                                .withClickEvent(new ClickEvent.RunCommand("/tp @s " + entry.x() + " " + entry.y() + " " + entry.z()))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal("Haz clic para teletransportarte a la estructura"))));
                player.sendMessage(tpBtn, false);
                player.sendMessage(Text.literal("═══════════════════════════════════════════════").formatted(Formatting.DARK_PURPLE), false);

                // Play sound
                player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.9f, 1.2f);
                player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.4f);

                // Spawn directional sonar particle beam pointing to structure
                double dirX = dx;
                double dirY = (entry.y() + 1) - player.getEyeY();
                double dirZ = dz;
                double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
                if (length > 0) {
                    dirX /= length;
                    dirY /= length;
                    dirZ /= length;
                    for (double step = 1.0; step <= 10.0; step += 0.5) {
                        serverWorld.spawnParticles(
                                ParticleTypes.PORTAL,
                                player.getX() + dirX * step,
                                player.getEyeY() + dirY * step,
                                player.getZ() + dirZ * step,
                                2, 0.04, 0.04, 0.04, 0.01
                        );
                        serverWorld.spawnParticles(
                                ParticleTypes.GLOW,
                                player.getX() + dirX * step,
                                player.getEyeY() + dirY * step,
                                player.getZ() + dirZ * step,
                                1, 0.02, 0.02, 0.02, 0.01
                        );
                    }
                }

                player.getItemCooldownManager().set(stack, 20);
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.literal("⚠ No se detectaron frecuencias de estructuras en esta dimensión.").formatted(Formatting.RED), true);
                player.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.0f);
            }
        }

        return ActionResult.SUCCESS;
    }

    private static String getCardinalDirection(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) angle += 360.0;

        if (angle >= 337.5 || angle < 22.5) return "Sur [S]";
        if (angle >= 22.5 && angle < 67.5) return "Suroeste [SW]";
        if (angle >= 67.5 && angle < 112.5) return "Oeste [W]";
        if (angle >= 112.5 && angle < 157.5) return "Noroeste [NW]";
        if (angle >= 157.5 && angle < 202.5) return "Norte [N]";
        if (angle >= 202.5 && angle < 247.5) return "Noreste [NE]";
        if (angle >= 247.5 && angle < 292.5) return "Este [E]";
        return "Sureste [SE]";
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ Brújula Rúnica de Estructuras ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Detecta la resonancia mágica de estructuras ancestrales.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Clic derecho: ").formatted(Formatting.YELLOW)
                .append(Text.literal("Rastrear estructura y emitir pulso sonar").formatted(Formatting.WHITE)));
        textConsumer.accept(Text.literal("• Shift + Clic derecho: ").formatted(Formatting.AQUA)
                .append(Text.literal("Cambiar objetivo de rastreo").formatted(Formatting.WHITE)));

        int mode = getTargetMode(stack);
        textConsumer.accept(Text.literal("Objetivo: ").formatted(Formatting.GRAY)
                .append(Text.literal("[" + getModeName(mode) + "]").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)));
    }
}
