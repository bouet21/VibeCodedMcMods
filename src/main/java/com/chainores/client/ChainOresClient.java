package com.chainores.client;

import com.chainores.client.gui.ChainForgeScreen;
import com.chainores.client.gui.ChainOresGuideScreen;
import com.chainores.client.gui.CosmicSmelterScreen;
import com.chainores.config.ChainOresConfig;
import com.chainores.gear.ChainArmorItem;
import com.chainores.item.GuideBookItem;
import com.chainores.network.DashC2SPayload;
import com.chainores.registry.ModWorkstations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainOresClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chainores-client");

    public static KeyBinding DASH_KEY;
    private static long clientLastDashTime = 0L;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Chain Ores Client initializing guide screen opener, workstations & dash HUD...");

        GuideBookItem.CLIENT_SCREEN_OPENER = player -> {
            MinecraftClient.getInstance().setScreen(new ChainOresGuideScreen());
        };

        // Register custom handled workstation screens
        HandledScreens.register(ModWorkstations.CHAIN_FORGE_SCREEN_HANDLER, ChainForgeScreen::new);
        HandledScreens.register(ModWorkstations.COSMIC_SMELTER_SCREEN_HANDLER, CosmicSmelterScreen::new);

        // Register boss entity renderers
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.chainores.registry.ModEntities.ABYSSAL_GUARDIAN,
                com.chainores.client.render.AbyssalGuardianRenderer::new
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.chainores.registry.ModEntities.NETHER_BEHEMOTH,
                com.chainores.client.render.NetherBehemothRenderer::new
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.chainores.registry.ModEntities.VOID_COLOSSUS,
                com.chainores.client.render.VoidColossusRenderer::new
        );
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                com.chainores.registry.ModEntities.COSMIC_SOVEREIGN,
                com.chainores.client.render.CosmicSovereignRenderer::new
        );

        // Register dynamic modifier and gem socket tooltips
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            com.chainores.modifier.ChainModifiers.appendModifierTooltips(stack, lines);
        });

        // Register dedicated Dash KeyBinding (Default: 'V' for Velocity, freeing 'R' for EMI/JEI recipes)
        DASH_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chainores.dash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.MOVEMENT
        ));

        // Client Tick: Check Dash Key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (DASH_KEY.wasPressed()) {
                if (client.player != null && !client.player.isSpectator()) {
                    if (ChainArmorItem.hasAnyChainArmor(client.player)) {
                        long now = System.currentTimeMillis();
                        long cdMillis = ChainOresConfig.get().dashCooldownSeconds * 1000L;
                        if (now - clientLastDashTime >= cdMillis) {
                            clientLastDashTime = now;

                            // Immediate client-side movement prediction for zero-latency feel
                            net.minecraft.util.math.Vec3d look = client.player.getRotationVector();
                            net.minecraft.util.math.Vec3d horizontal = new net.minecraft.util.math.Vec3d(look.x, 0, look.z);
                            if (horizontal.lengthSquared() < 0.001) {
                                horizontal = net.minecraft.util.math.Vec3d.fromPolar(0, client.player.getYaw());
                            }
                            horizontal = horizontal.normalize();
                            net.minecraft.util.math.Vec3d dashVelocity = new net.minecraft.util.math.Vec3d(horizontal.x * 1.85, 0.32, horizontal.z * 1.85);
                            client.player.setVelocity(dashVelocity);
                            client.player.fallDistance = 0;

                            ClientPlayNetworking.send(new DashC2SPayload());
                        } else {
                            long remainingSec = (cdMillis - (now - clientLastDashTime) + 999) / 1000;
                            client.player.sendMessage(
                                    Text.literal("⏳ Dash en recarga (" + remainingSec + "s)").formatted(Formatting.RED),
                                    true
                            );
                        }
                    }
                }
            }
        });

        // In-game HUD: Dash Cooldown Indicator
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            if (!ChainArmorItem.hasAnyChainArmor(client.player)) return;

            long now = System.currentTimeMillis();
            long cdMillis = ChainOresConfig.get().dashCooldownSeconds * 1000L;
            long elapsed = now - clientLastDashTime;

            int screenWidth = drawContext.getScaledWindowWidth();
            int screenHeight = drawContext.getScaledWindowHeight();

            // Position at bottom-right above hotbar
            int x = screenWidth - 110;
            int y = screenHeight - 22;

            if (elapsed < cdMillis) {
                float remainingSeconds = (cdMillis - elapsed) / 1000.0f;
                String text = String.format("⚡ Dash: %.1fs", remainingSeconds);
                drawContext.drawText(client.textRenderer, text, x, y, 0xFFFFAA00, true);
            } else {
                String keyName = DASH_KEY.getBoundKeyLocalizedText().getString();
                String text = "⚡ Dash: LISTO [" + keyName + "]";
                drawContext.drawText(client.textRenderer, text, x, y, 0xFF55FFFF, true);
            }
        });
    }
}
