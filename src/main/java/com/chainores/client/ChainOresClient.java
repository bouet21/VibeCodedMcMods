package com.chainores.client;

import com.chainores.client.gui.ChainForgeScreen;
import com.chainores.client.gui.ChainOresGuideScreen;
import com.chainores.client.gui.CosmicSmelterScreen;
import com.chainores.item.GuideBookItem;
import com.chainores.registry.ModWorkstations;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainOresClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chainores-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Chain Ores Client initializing guide screen opener & workstations...");

        GuideBookItem.CLIENT_SCREEN_OPENER = player -> {
            MinecraftClient.getInstance().setScreen(new ChainOresGuideScreen());
        };

        // Register custom handled workstation screens
        HandledScreens.register(ModWorkstations.CHAIN_FORGE_SCREEN_HANDLER, ChainForgeScreen::new);
        HandledScreens.register(ModWorkstations.COSMIC_SMELTER_SCREEN_HANDLER, CosmicSmelterScreen::new);
    }
}
