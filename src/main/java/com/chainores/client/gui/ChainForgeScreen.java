package com.chainores.client.gui;

import com.chainores.workstation.ChainForgeScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

        int gx = this.x;
        int gy = this.y;

        // Slot Guidance Tooltips when hovering over empty slots
        if (!this.handler.getSlot(0).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 43, gy + 34, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Forja").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("\nColoca tu lingote, mena cruda, equipamiento o gema.").formatted(Formatting.GRAY))
                            .append(Text.literal("\n(Ranuras de entrada 100% intercambiables)").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)),
                    mouseX, mouseY);
        } else if (!this.handler.getSlot(1).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 67, gy + 16, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Catalizador").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("\nColoca aquí el lingote/mena superior, catalizador o plantilla.").formatted(Formatting.GRAY))
                            .append(Text.literal("\n(Ranuras de entrada 100% intercambiables)").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)),
                    mouseX, mouseY);
        } else if (!this.handler.getSlot(2).hasStack() && isMouseOverArea(mouseX, mouseY, gx + 125, gy + 34, 18, 18)) {
            context.drawTooltip(this.textRenderer,
                    Text.literal("Ranura de Salida").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal("\nAquí aparecerá tu lingote o equipamiento mejorado al instante.").formatted(Formatting.GRAY)),
                    mouseX, mouseY);
        } else if (isMouseOverArea(mouseX, mouseY, gx + 89, gy + 38, 28, 8)) {
            int progress = this.handler.getForgeProgress();
            int total = this.handler.getTotalForgeTime();
            int pct = total > 0 ? (progress * 100 / total) : 0;
            if (progress > 0) {
                context.drawTooltip(this.textRenderer,
                        Text.literal("Forjando: " + pct + "%").formatted(Formatting.GOLD, Formatting.BOLD),
                        mouseX, mouseY);
            } else {
                context.drawTooltip(this.textRenderer,
                        Text.literal("Forja de Cadena").formatted(Formatting.YELLOW, Formatting.BOLD)
                                .append(Text.literal("\nCombina lingotes, menas crudas, gemas o equipamiento.").formatted(Formatting.GRAY)),
                        mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverArea(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFFFFCC00, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0xFFAAAAAA, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        // Dark metallic forge base panel
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF181B20);

        // Bevel borders with forge bronze accents
        context.fill(x, y, x + this.backgroundWidth, y + 2, 0xFF5C5248);
        context.fill(x, y, x + 2, y + this.backgroundHeight, 0xFF5C5248);
        context.fill(x + this.backgroundWidth - 2, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF101216);
        context.fill(x, y + this.backgroundHeight - 2, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF101216);

        // Header plate
        context.fill(x + 4, y + 4, x + this.backgroundWidth - 4, y + 16, 0xFF282522);
        context.fill(x + 5, y + 15, x + this.backgroundWidth - 5, y + 16, 0xFFD4AF37); // Gold accent stripe

        // Slot Recesses: Slot 0 (Base), Slot 1 (Catalyst), Slot 2 (Output)
        drawSlotRecess(context, x + 43, y + 34);
        drawSlotRecess(context, x + 67, y + 16);
        drawSlotRecess(context, x + 125, y + 34);

        // Output Slot Gold Glowing Halo
        context.fill(x + 124, y + 33, x + 144, y + 34, 0xFFFFCC00);
        context.fill(x + 124, y + 52, x + 144, y + 53, 0xFFFFCC00);
        context.fill(x + 124, y + 34, x + 125, y + 52, 0xFFFFCC00);
        context.fill(x + 143, y + 34, x + 144, y + 52, 0xFFFFCC00);

        // Forging Track Path
        context.fill(x + 89, y + 39, x + 117, y + 45, 0xFF2A2E38);

        // Forging Progress Bar (Glowing Lava Fire Fill)
        int progress = this.handler.getForgeProgress();
        int total = this.handler.getTotalForgeTime();
        if (total > 0 && progress > 0) {
            int arrowWidth = (progress * 28) / total;
            context.fill(x + 89, y + 39, x + 89 + arrowWidth, y + 45, 0xFFFF6A00);
            context.fill(x + 89, y + 41, x + 89 + arrowWidth, y + 43, 0xFFFFEE88);
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
        context.fill(x, y, x + 18, y + 18, 0xFF121418);
        context.fill(x, y, x + 18, y + 1, 0xFF0A0C0E);
        context.fill(x, y, x + 1, y + 18, 0xFF0A0C0E);
        context.fill(x + 17, y, x + 18, y + 18, 0xFF323640);
        context.fill(x, y + 17, x + 18, y + 18, 0xFF323640);
    }
}
