package com.chainores.client;

import com.chainores.client.gui.ChainOresGuideScreen;
import com.chainores.item.GuideBookItem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainOresClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chainores-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Chain Ores Client initializing guide screen opener...");
        GuideBookItem.CLIENT_SCREEN_OPENER = player -> {
            MinecraftClient.getInstance().setScreen(new ChainOresGuideScreen());
        };
    }
}
