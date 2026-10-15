package com.chainores.client.gui;

import com.chainores.workstation.CosmicSmelterScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CosmicSmelterScreen extends HandledScreen<CosmicSmelterScreenHandler> {

    public CosmicSmelterScreen(CosmicSmelterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int gx = this.x;
        int gy = this.y;

        // Hover Guidance Tooltips
        if (!this.handler.getSlot(0).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 55, gy + 16, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Mineral Crudo").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("\nColoca aquí cualquier mineral crudo de Chain Ores.").formatted(Formatting.GRAY))
                            .append(Text.literal("\n★ Duplica lingotes producidos y tiene 35% de transmutar.").formatted(Formatting.LIGHT_PURPLE)),
                    mouseX, mouseY);
        } else if (!this.handler.getSlot(1).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 55, gy + 52, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Combustible").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("\nUsa Carbón, Varas de Blaze o Cubos de Lava.").formatted(Formatting.GRAY)),
                    mouseX, mouseY);
        } else if (!this.handler.getSlot(2).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 115, gy + 34, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Salida Cósmica").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                            .append(Text.literal("\nAquí se recolectan los lingotes refinados y multiplicados.").formatted(Formatting.GRAY)),
                    mouseX, mouseY);
        } else if (isMouseOverArea(mouseX, mouseY, gx + 79, gy + 38, 24, 8)) {
            int cook = this.handler.getCookProgress();
            int pct = (cook * 100 / 24);
            context.drawTooltip(this.textRenderer,
                    Text.literal("Transmutación Cósmica: " + pct + "%").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                    mouseX, mouseY);
        }
    }

    private boolean isMouseOverArea(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFF00FFD5, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0xFFAAAAAA, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        // Dark cosmic panel background
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF100D1C);

        // Celestial purple bevel borders
        context.fill(x, y, x + this.backgroundWidth, y + 2, 0xFF4A3A75);
        context.fill(x, y, x + 2, y + this.backgroundHeight, 0xFF4A3A75);
        context.fill(x + this.backgroundWidth - 2, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF08060E);
        context.fill(x, y + this.backgroundHeight - 2, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF08060E);

        // Header plate
        context.fill(x + 4, y + 4, x + this.backgroundWidth - 4, y + 16, 0xFF1E1733);
        context.fill(x + 5, y + 15, x + this.backgroundWidth - 5, y + 16, 0xFF00FFD5); // Cyan soul stripe

        // Slot Recesses: Input (55, 16), Fuel (55, 52), Output (115, 34)
        drawSlotRecess(context, x + 55, y + 16);
        drawSlotRecess(context, x + 55, y + 52);
        drawSlotRecess(context, x + 115, y + 34);

        // Output Halo in Celestial Magenta
        context.fill(x + 114, y + 33, x + 134, y + 34, 0xFFDE3CE3);
        context.fill(x + 114, y + 52, x + 134, y + 53, 0xFFDE3CE3);
        context.fill(x + 114, y + 34, x + 115, y + 52, 0xFFDE3CE3);
        context.fill(x + 133, y + 34, x + 134, y + 52, 0xFFDE3CE3);

        // Fuel Flame Indicator (Soul flame)
        if (this.handler.isBurning()) {
            int fuelHeight = this.handler.getFuelProgress();
            context.fill(x + 60, y + 36 + (14 - fuelHeight), x + 68, y + 50, 0xFF00FFD5);
            context.fill(x + 62, y + 38 + (14 - fuelHeight), x + 66, y + 48, 0xFFFFFFFF);
        } else {
            context.fill(x + 60, y + 36, x + 68, y + 50, 0xFF191326);
        }

        // Cook Progress Arrow
        context.fill(x + 79, y + 39, x + 103, y + 45, 0xFF221A38);
        int cookWidth = this.handler.getCookProgress();
        if (cookWidth > 0) {
            context.fill(x + 79, y + 39, x + 79 + cookWidth, y + 45, 0xFFDE3CE3);
            context.fill(x + 79, y + 41, x + 79 + cookWidth, y + 43, 0xFFFFAAFF);
        }

        // Draw Player Inventory Slot Recesses (3 rows x 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                drawSlotRecess(context, x + 7 + col * 18, y + 83 + row * 18);
            }
        }

        // Draw Player Hotbar Slot Recesses (9 cols)
        for (int col = 0; col < 9; ++col) {
            drawSlotRecess(context, x + 7 + col * 18, y + 141);
        }
    }

    private void drawSlotRecess(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0xFF0C0916);
        context.fill(x, y, x + 18, y + 1, 0xFF050308);
        context.fill(x, y, x + 1, y + 18, 0xFF050308);
        context.fill(x + 17, y, x + 18, y + 18, 0xFF281F40);
        context.fill(x, y + 17, x + 18, y + 18, 0xFF281F40);
    }
}
