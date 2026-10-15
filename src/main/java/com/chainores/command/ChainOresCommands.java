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
        );
    }
}
