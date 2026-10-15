package com.chainores.client.gui;

import com.chainores.workstation.ChainForgeScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class ChainForgeScreen extends HandledScreen<ChainForgeScreenHandler> {

    public ChainForgeScreen(ChainForgeScreenHandler handler, PlayerInventory inventory, Text title) {
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

        // Dark metallic forge base panel
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF1F2229);
        // Bevel borders
        context.fill(x, y, x + this.backgroundWidth, y + 2, 0xFF4A4E5C);
        context.fill(x, y, x + 2, y + this.backgroundHeight, 0xFF4A4E5C);
        context.fill(x + this.backgroundWidth - 2, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF121419);
        context.fill(x, y + this.backgroundHeight - 2, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF121419);

        // Header plate
        context.fill(x + 4, y + 4, x + this.backgroundWidth - 4, y + 16, 0xFF2A2E38);

        // Slot Recesses: Slot 0 (Base), Slot 1 (Catalyst), Slot 2 (Output)
        drawSlotRecess(context, x + 43, y + 34);
        drawSlotRecess(context, x + 67, y + 16);
        drawSlotRecess(context, x + 125, y + 34);

        // Anvil / Forge indicator path
        context.fill(x + 89, y + 39, x + 117, y + 45, 0xFF2F3440);

        // Forging Progress Bar (Arrow fill)
        int progress = this.handler.getForgeProgress();
        int total = this.handler.getTotalForgeTime();
        if (total > 0 && progress > 0) {
            int arrowWidth = (progress * 28) / total;
            // Glowing fire progress
            context.fill(x + 89, y + 39, x + 89 + arrowWidth, y + 45, 0xFFFF7A00);
            context.fill(x + 89, y + 41, x + 89 + arrowWidth, y + 43, 0xFFFFEA7A);
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
        // Recessed slot with 18x18 dimension
        context.fill(x, y, x + 18, y + 18, 0xFF14161C);
        context.fill(x, y, x + 18, y + 1, 0xFF0D0F13);
        context.fill(x, y, x + 1, y + 18, 0xFF0D0F13);
        context.fill(x + 17, y, x + 18, y + 18, 0xFF353945);
        context.fill(x, y + 17, x + 18, y + 18, 0xFF353945);
    }
}
