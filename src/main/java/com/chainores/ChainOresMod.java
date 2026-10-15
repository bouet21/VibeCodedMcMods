package com.chainores;

import com.chainores.registry.ModBlocks;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModItemGroups;
import com.chainores.registry.ModItems;
import com.chainores.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainOresMod implements ModInitializer {
    public static final String MOD_ID = "chainores";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Chain Ores: 150 Tier Progression Mod with In-Game Guide...");

        ModBlocks.initialize();
        ModItems.initialize();
        ModGear.initialize();
        ModItemGroups.initialize();
        ModWorldGen.initialize();

        // Give Guide Book automatically on first player spawn / join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!player.getCommandTags().contains("chainores_given_book")) {
                player.addCommandTag("chainores_given_book");
                ItemStack book = new ItemStack(ModItems.GUIDE_BOOK);
                if (!player.getInventory().insertStack(book)) {
                    player.dropItem(book, false);
                }
                player.sendMessage(
                        Text.translatable("message.chainores.welcome_book").formatted(Formatting.GOLD, Formatting.BOLD),
                        false
                );
            }
        });

        LOGGER.info("Chain Ores has been successfully loaded!");
    }
}
