package com.chainores.client.gui;

import com.chainores.workstation.CosmicSmelterScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

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
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        // Dark cosmic panel
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF141121);
        // Bevel borders
        context.fill(x, y, x + this.backgroundWidth, y + 2, 0xFF43346B);
        context.fill(x, y, x + 2, y + this.backgroundHeight, 0xFF43346B);
        context.fill(x + this.backgroundWidth - 2, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF0A0712);
        context.fill(x, y + this.backgroundHeight - 2, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF0A0712);

        // Header plate
        context.fill(x + 4, y + 4, x + this.backgroundWidth - 4, y + 16, 0xFF211B36);

        // Slot Recesses: Input (56, 17), Fuel (56, 53), Output (116, 35)
        drawSlotRecess(context, x + 55, y + 16);
        drawSlotRecess(context, x + 55, y + 52);
        drawSlotRecess(context, x + 115, y + 34);

        // Fuel Flame indicator (vertical between input and fuel)
        if (this.handler.isBurning()) {
            int fuelHeight = this.handler.getFuelProgress();
            context.fill(x + 60, y + 36 + (14 - fuelHeight), x + 68, y + 50, 0xFF00FFD5); // Cyan soul flame
            context.fill(x + 62, y + 38 + (14 - fuelHeight), x + 66, y + 48, 0xFFFFFFFF);
        } else {
            context.fill(x + 60, y + 36, x + 68, y + 50, 0xFF1B152B);
        }

        // Cook Progress Arrow (from x + 79 to x + 103, y + 35)
        context.fill(x + 79, y + 39, x + 103, y + 45, 0xFF241C3B);
        int cookWidth = this.handler.getCookProgress();
        if (cookWidth > 0) {
            context.fill(x + 79, y + 39, x + 79 + cookWidth, y + 45, 0xFFDE3CE3); // Dragon magenta glow
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
        context.fill(x, y, x + 18, y + 18, 0xFF0D0A17);
        context.fill(x, y, x + 18, y + 1, 0xFF06040A);
        context.fill(x, y, x + 1, y + 18, 0xFF06040A);
        context.fill(x + 17, y, x + 18, y + 18, 0xFF2D234A);
        context.fill(x, y + 17, x + 18, y + 18, 0xFF2D234A);
    }
}
