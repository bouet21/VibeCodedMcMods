package com.chainores.command;

import com.chainores.modifier.ChainModifiers;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChainOresCommands {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(ChainOresCommands::register);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                 CommandRegistryAccess registryAccess,
                                 CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("chainores")
                // /chainores guide
                .then(CommandManager.literal("guide")
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity player = source.getPlayer();
                            if (player != null) {
                                ItemStack book = new ItemStack(ModItems.GUIDE_BOOK);
                                if (!player.getInventory().insertStack(book)) {
                                    player.dropItem(book, false);
                                }
                                player.sendMessage(Text.literal("📖 ¡Has recibido el Códice de Chain Ores! Clic derecho para abrir.").formatted(Formatting.GOLD), false);
                                player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                            }
                            return 1;
                        })
                )
                // /chainores info <tier>
                .then(CommandManager.literal("info")
                        .then(CommandManager.argument("tier", IntegerArgumentType.integer(1, 150))
                                .executes(ctx -> {
                                    int tier = IntegerArgumentType.getInteger(ctx, "tier");
                                    ServerCommandSource source = ctx.getSource();
                                    OreTier ore = OreRegistryData.TIERS.get(tier - 1);

                                    source.sendMessage(Text.literal("═════════════════════════════════").formatted(Formatting.DARK_PURPLE));
                                    source.sendMessage(Text.literal("✦ Tier " + ore.tier + ": " + ore.esName + " (" + ore.enName + ") ✦").formatted(Formatting.GOLD, Formatting.BOLD));
                                    source.sendMessage(Text.literal("• Dimensión: ").formatted(Formatting.YELLOW).append(Text.literal(ore.dimension.name()).formatted(Formatting.AQUA)));
                                    source.sendMessage(Text.literal("• Altura: ").formatted(Formatting.YELLOW).append(Text.literal("Y: " + ore.minY + " a " + ore.maxY).formatted(Formatting.GREEN)));
                                    source.sendMessage(Text.literal("• Veta máxima: ").formatted(Formatting.YELLOW).append(Text.literal(ore.veinSize + " bloques").formatted(Formatting.WHITE)));
                                    source.sendMessage(Text.literal("• Nivel de minería: ").formatted(Formatting.YELLOW).append(Text.literal("Nivel " + ore.harvestLevel).formatted(Formatting.LIGHT_PURPLE)));
                                    source.sendMessage(Text.literal("═════════════════════════════════").formatted(Formatting.DARK_PURPLE));
                                    return 1;
                                })
                        )
                )
                // /chainores reforge
                .then(CommandManager.literal("reforge")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity player = source.getPlayer();
                            if (player != null) {
                                ItemStack held = player.getMainHandStack();
                                if (!held.isEmpty()) {
                                    String rolled = ChainModifiers.rollRandomReforge(player.getEntityWorld().random);
                                    ChainModifiers.setReforge(held, rolled);
                                    player.sendMessage(Text.literal("⚡ ¡Ítem reforjado exitosamente con prefijo: [" + rolled.toUpperCase() + "]!").formatted(Formatting.GOLD, Formatting.BOLD), false);
                                    player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0f, 1.2f);
                                } else {
                                    player.sendMessage(Text.literal("Debes sostener un ítem en la mano principal para reforjar.").formatted(Formatting.RED), false);
                                }
                            }
                            return 1;
                        })
                )
                // /chainores locate
                .then(CommandManager.literal("locate")
                        .executes(ctx -> executeLocate(ctx.getSource(), null))
                        .then(CommandManager.literal("forge").executes(ctx -> executeLocate(ctx.getSource(), "forge")))
                        .then(CommandManager.literal("shrine").executes(ctx -> executeLocate(ctx.getSource(), "shrine")))
                        .then(CommandManager.literal("altar").executes(ctx -> executeLocate(ctx.getSource(), "altar")))
                )
                // /chainores reload
                .then(CommandManager.literal("reload")
                        .requires(source -> source.getEntity() == null || (source.getPlayer() != null && source.getServer().getPlayerManager().isOperator(new net.minecraft.server.PlayerConfigEntry(source.getPlayer().getGameProfile()))))
                        .executes(ctx -> {
                            com.chainores.config.ChainOresConfig.load();
                            ctx.getSource().sendMessage(Text.literal("⚙ ¡Configuración de Chain Ores (config/chainores.json) recargada exitosamente!").formatted(Formatting.GREEN, Formatting.BOLD));
                            return 1;
                        })
                )
        );

        // Standalone alias /structures as requested
        dispatcher.register(CommandManager.literal("structures")
                .executes(ctx -> executeLocate(ctx.getSource(), null))
                .then(CommandManager.literal("forge").executes(ctx -> executeLocate(ctx.getSource(), "forge")))
                .then(CommandManager.literal("shrine").executes(ctx -> executeLocate(ctx.getSource(), "shrine")))
                .then(CommandManager.literal("altar").executes(ctx -> executeLocate(ctx.getSource(), "altar")))
        );
    }

    private static int executeLocate(ServerCommandSource source, String typeArg) {
        ServerPlayerEntity player = source.getPlayer();
        net.minecraft.server.world.ServerWorld world = source.getWorld();
        net.minecraft.util.math.BlockPos origin = player != null ? player.getBlockPos() : net.minecraft.util.math.BlockPos.ORIGIN;

        String targetType = null;
        if (typeArg != null) {
            String lower = typeArg.toLowerCase();
            if (lower.contains("forge") || lower.contains("forja")) {
                targetType = com.chainores.world.StructureLocator.TYPE_FORGE;
            } else if (lower.contains("shrine") || lower.contains("santuario")) {
                targetType = com.chainores.world.StructureLocator.TYPE_SHRINE;
            } else if (lower.contains("altar")) {
                targetType = com.chainores.world.StructureLocator.TYPE_ALTAR;
            }
        }

        if (targetType != null && !com.chainores.world.StructureLocator.isValidDimensionForType(world, targetType)) {
            if (targetType.equals(com.chainores.world.StructureLocator.TYPE_FORGE)) {
                source.sendMessage(Text.literal("⚠ La Forja Abandonada solo se encuentra en el Overworld.").formatted(Formatting.RED));
            } else if (targetType.equals(com.chainores.world.StructureLocator.TYPE_SHRINE)) {
                source.sendMessage(Text.literal("⚠ El Santuario del Nether solo se encuentra en el Nether.").formatted(Formatting.RED));
            } else {
                source.sendMessage(Text.literal("⚠ El Altar Cósmico solo se encuentra en The End.").formatted(Formatting.LIGHT_PURPLE));
            }
            return 0;
        }

        var result = com.chainores.world.StructureLocator.findNearest(world, origin, targetType);
        if (result.isPresent()) {
            var entry = result.get();
            int dx = entry.x() - origin.getX();
            int dz = entry.z() - origin.getZ();
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            String name = com.chainores.world.StructureLocator.getLocalizedName(entry.type());

            source.sendMessage(Text.literal("═══════════════════════════════════════════════").formatted(Formatting.DARK_PURPLE));
            source.sendMessage(Text.literal("✦ ESTRUCTURA LOCALIZADA ✦").formatted(Formatting.GOLD, Formatting.BOLD));
            source.sendMessage(Text.literal("• Tipo: ").formatted(Formatting.GRAY).append(Text.literal(name).formatted(Formatting.AQUA, Formatting.BOLD)));
            source.sendMessage(Text.literal("• Coordenadas: ").formatted(Formatting.GRAY)
                    .append(Text.literal("X: " + entry.x() + "  Y: " + entry.y() + "  Z: " + entry.z()).formatted(Formatting.YELLOW)));
            source.sendMessage(Text.literal("• Distancia: ").formatted(Formatting.GRAY)
                    .append(Text.literal(dist + " bloques de distancia").formatted(Formatting.GREEN)));

            Text tpBtn = Text.literal("✦ [CLICK AQUÍ PARA TELETRANSPORTARTE] ✦")
                    .styled(s -> s.withColor(Formatting.LIGHT_PURPLE)
                            .withBold(true)
                            .withUnderline(true)
                            .withClickEvent(new net.minecraft.text.ClickEvent.RunCommand("/tp @s " + entry.x() + " " + entry.y() + " " + entry.z()))
                            .withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(Text.literal("Clic para teletransportarte a la estructura"))));
            source.sendMessage(tpBtn);
            source.sendMessage(Text.literal("═══════════════════════════════════════════════").formatted(Formatting.DARK_PURPLE));

            if (player != null) {
                player.playSound(SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
            }
            return 1;
        } else {
            source.sendMessage(Text.literal("⚠ No se encontró ninguna estructura en esta dimensión.").formatted(Formatting.RED));
            return 0;
        }
    }
}
