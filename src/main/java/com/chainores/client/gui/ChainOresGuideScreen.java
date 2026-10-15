package com.chainores.client.gui;

import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChainOresGuideScreen extends Screen {

    private int pageIndex = 0;
    private final int totalPages = OreRegistryData.TIERS.size();

    public ChainOresGuideScreen() {
        this(0);
    }

    public ChainOresGuideScreen(int initialPage) {
        super(Text.literal("Códice de Minerales en Cadena"));
        this.pageIndex = Math.clamp(initialPage, 0, totalPages - 1);
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = 330;
        int panelHeight = 224;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        this.clearChildren();

        // Prev & Next Buttons
        this.addDrawableChild(ButtonWidget.builder(Text.literal("< Anterior"), button -> {
            if (pageIndex > 0) {
                pageIndex--;
                init();
            }
        }).dimensions(x + 10, y + panelHeight - 24, 75, 18).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Siguiente >"), button -> {
            if (pageIndex < totalPages - 1) {
                pageIndex++;
                init();
            }
        }).dimensions(x + panelWidth - 85, y + panelHeight - 24, 75, 18).build());

        // Quick Stage Jump Buttons
        int jumpY = y + panelHeight - 44;
        addStageJumpButton(x + 10, jumpY, 30, "T1", 0);
        addStageJumpButton(x + 43, jumpY, 30, "T11", 10);
        addStageJumpButton(x + 76, jumpY, 30, "T21", 20);
        addStageJumpButton(x + 109, jumpY, 30, "T31", 30);
        addStageJumpButton(x + 142, jumpY, 30, "T41", 40);
        addStageJumpButton(x + 175, jumpY, 30, "T61", 60);
        addStageJumpButton(x + 208, jumpY, 30, "T71", 70);
        addStageJumpButton(x + 241, jumpY, 30, "T91", 90);
        addStageJumpButton(x + 274, jumpY, 30, "T106", 105);
        addStageJumpButton(x + 307, jumpY, 23, "T136", 135);

        // Close Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), button -> this.close())
                .dimensions(x + panelWidth - 22, y + 4, 18, 16).build());
    }

    private void addStageJumpButton(int bx, int by, int bw, String label, int targetIndex) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
            this.pageIndex = targetIndex;
            init();
        }).dimensions(bx, by, bw, 16).build());
    }

    private void drawCustomBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int panelWidth = 330;
        int panelHeight = 224;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        OreTier ore = OreRegistryData.TIERS.get(pageIndex);

        // 1. Outer Box & Background
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xF21C1815);
        // Outer Gold Border
        drawCustomBorder(context, x, y, panelWidth, panelHeight, 0xFFD4AF37);
        drawCustomBorder(context, x + 1, y + 1, panelWidth - 2, panelHeight - 2, 0xFF5C4718);

        // Header Background
        context.fill(x + 2, y + 2, x + panelWidth - 2, y + 22, 0xFF2A221A);
        context.drawText(this.textRenderer, Text.literal("✦ CÓDICE DE MINERALES EN CADENA ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                x + 10, y + 7, 0xFFE6C65A, false);

        String pageNumText = "Tier " + (pageIndex + 1) + " / " + totalPages;
        context.drawText(this.textRenderer, Text.literal(pageNumText).formatted(Formatting.YELLOW),
                x + panelWidth - 95, y + 7, 0xFFFFF1A8, false);

        // 2. Left Column: Visual Showcase (Icons / Pictures)
        int leftW = 105;
        context.fill(x + 8, y + 26, x + 8 + leftW, y + 176, 0x80000000);
        drawCustomBorder(context, x + 8, y + 26, leftW, 150, 0xFF544436);

        context.drawText(this.textRenderer, Text.literal("MUESTRAS:").formatted(Formatting.AQUA, Formatting.BOLD),
                x + 14, y + 32, 0xFF55FFFF, false);

        // Item 1: Ore Block
        ItemStack oreStack = new ItemStack(ore.oreBlockItem);
        context.drawItem(oreStack, x + 14, y + 46);
        context.drawText(this.textRenderer, "Mineral", x + 36, y + 50, 0xFFFFFFFF, false);

        // Item 2: Raw Material
        ItemStack rawStack = new ItemStack(ore.rawItem);
        context.drawItem(rawStack, x + 14, y + 78);
        context.drawText(this.textRenderer, "Crudo", x + 36, y + 82, 0xFFE0E0E0, false);

        // Item 3: Refined Ingot
        ItemStack ingotStack = new ItemStack(ore.ingotItem);
        context.drawItem(ingotStack, x + 14, y + 110);
        context.drawText(this.textRenderer, "Lingote", x + 36, y + 114, 0xFFFFD700, false);

        // Item 4: Storage Block
        ItemStack storageStack = new ItemStack(ore.storageBlockItem);
        context.drawItem(storageStack, x + 14, y + 142);
        context.drawText(this.textRenderer, "Bloque", x + 36, y + 146, 0xFFAAAAAA, false);

        // 3. Right Column: Location & Crafting Information
        int rightX = x + 118;
        int rightW = panelWidth - 126;
        context.fill(rightX, y + 26, rightX + rightW, y + 176, 0x80000000);
        drawCustomBorder(context, rightX, y + 26, rightW, 150, 0xFF544436);

        // Title of Ore
        String oreTitle = "#" + ore.tier + " " + ore.esName;
        context.drawText(this.textRenderer, Text.literal(oreTitle).formatted(Formatting.GOLD, Formatting.BOLD),
                rightX + 6, y + 32, 0xFFFFCC00, false);
        context.drawText(this.textRenderer, Text.literal("(" + ore.enName + ")").formatted(Formatting.DARK_GRAY),
                rightX + 6 + this.textRenderer.getWidth(oreTitle) + 6, y + 32, 0xFFAAAAAA, false);

        // Dimension
        String dimName = switch (ore.dimension) {
            case OVERWORLD -> "Overworld";
            case NETHER -> "El Nether";
            case END -> "The End";
        };
        int dimColor = switch (ore.dimension) {
            case OVERWORLD -> 0xFF55FF55;
            case NETHER -> 0xFFFF5555;
            case END -> 0xFFFF55FF;
        };
        context.drawText(this.textRenderer, "Dimensión: ", rightX + 6, y + 46, 0xFFCCCCCC, false);
        context.drawText(this.textRenderer, dimName, rightX + 66, y + 46, dimColor, false);

        // Biomes
        context.drawText(this.textRenderer, "Biomas:", rightX + 6, y + 58, 0xFFCCCCCC, false);
        String biomeDesc = getBiomeDescription(ore.biomeGroup);
        context.drawText(this.textRenderer, biomeDesc, rightX + 48, y + 58, 0xFFFFFF88, false);

        // Heights & Tool
        context.drawText(this.textRenderer, "Altura: ", rightX + 6, y + 70, 0xFFCCCCCC, false);
        context.drawText(this.textRenderer, "Y: " + ore.minY + " a " + ore.maxY + " (Veta: " + ore.veinSize + ")",
                rightX + 50, y + 70, 0xFFE0E0E0, false);

        context.drawText(this.textRenderer, "Pico mín.: ", rightX + 6, y + 82, 0xFFCCCCCC, false);
        context.drawText(this.textRenderer, getToolDescription(ore.harvestLevel), rightX + 60, y + 82, 0xFF55FFFF, false);

        // 4. Smithing Upgrade Section
        drawCustomBorder(context, rightX + 4, y + 96, rightW - 8, 48, 0xFF665544);
        context.drawText(this.textRenderer, Text.literal("Receta en Mesa de Herrería:").formatted(Formatting.LIGHT_PURPLE),
                rightX + 8, y + 100, 0xFFFF77FF, false);

        ItemStack templateStack = new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE);
        ItemStack prevStack = (ore.tier == 1)
                ? new ItemStack(Items.COPPER_INGOT)
                : new ItemStack(OreRegistryData.TIERS.get(ore.tier - 2).ingotItem);

        int recY = y + 116;
        context.drawItem(templateStack, rightX + 12, recY);
        context.drawText(this.textRenderer, "+", rightX + 34, recY + 4, 0xFFFFFFFF, false);

        context.drawItem(prevStack, rightX + 44, recY);
        context.drawText(this.textRenderer, "+", rightX + 66, recY + 4, 0xFFFFFFFF, false);

        context.drawItem(rawStack, rightX + 76, recY);
        context.drawText(this.textRenderer, "➔", rightX + 98, recY + 4, 0xFF55FF55, false);

        context.drawItem(ingotStack, rightX + 114, recY);

        context.drawText(this.textRenderer, "Lingote T" + ore.tier, rightX + 136, recY + 4, 0xFFFFD700, false);

        // 5. Gear Milestone Showcase (if available for this tier)
        com.chainores.registry.ModGear.GearSet gear = com.chainores.registry.ModGear.GEAR_SETS.stream()
                .filter(g -> g.tierData.tier() == ore.tier).findFirst().orElse(null);
        if (gear != null) {
            ItemStack swordStack = new ItemStack(gear.sword);
            ItemStack pickStack = new ItemStack(gear.pickaxe);
            ItemStack chestStack = new ItemStack(gear.chestplate);
            int gearY = y + 150;
            context.drawItem(swordStack, rightX + 6, gearY);
            context.drawItem(pickStack, rightX + 24, gearY);
            context.drawItem(chestStack, rightX + 42, gearY);
            context.drawText(this.textRenderer, Text.literal("⚔ Set Habilidades").formatted(Formatting.GOLD), rightX + 62, gearY + 4, 0xFFFFDD55, false);
            checkTooltip(context, swordStack, rightX + 6, gearY, mouseX, mouseY);
            checkTooltip(context, pickStack, rightX + 24, gearY, mouseX, mouseY);
            checkTooltip(context, chestStack, rightX + 42, gearY, mouseX, mouseY);
        }

        // Tooltip hover rendering
        checkTooltip(context, oreStack, x + 14, y + 46, mouseX, mouseY);
        checkTooltip(context, rawStack, x + 14, y + 78, mouseX, mouseY);
        checkTooltip(context, ingotStack, x + 14, y + 110, mouseX, mouseY);
        checkTooltip(context, storageStack, x + 14, y + 142, mouseX, mouseY);

        checkTooltip(context, templateStack, rightX + 12, recY, mouseX, mouseY);
        checkTooltip(context, prevStack, rightX + 44, recY, mouseX, mouseY);
        checkTooltip(context, rawStack, rightX + 76, recY, mouseX, mouseY);
        checkTooltip(context, ingotStack, rightX + 114, recY, mouseX, mouseY);
    }

    private void checkTooltip(DrawContext context, ItemStack stack, int itemX, int itemY, int mouseX, int mouseY) {
        if (mouseX >= itemX && mouseX <= itemX + 16 && mouseY >= itemY && mouseY <= itemY + 16) {
            context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
        }
    }

    private String getBiomeDescription(OreTier.BiomeGroup bg) {
        return switch (bg) {
            case PLAINS_FOREST -> "Llanuras, Bosques y Praderas";
            case MOUNTAINS -> "Cumbres Heladas y Picos Nevados";
            case DESERT_BADLANDS -> "Desiertos y Tierras Baldías";
            case SWAMP_JUNGLE -> "Pantanos, Junglas y Océanos";
            case DEEPSLATE_CAVES -> "Cavernas de Pizarra (Deepslate)";
            case DEEP_DARK -> "La Oscuridad Profunda (Deep Dark)";
            case NETHER_WASTES -> "Tierras Baldías del Nether";
            case SOUL_SAND_VALLEY -> "Valle de Arena de Almas";
            case CRIMSON_WARPED -> "Bosques Carmesí y Deformados";
            case BASALT_DELTAS -> "Deltas de Basalto";
            case END_MIDLANDS -> "Tierras Medias del End";
            case END_HIGHLANDS -> "Tierras Altas del End";
            case END_ISLANDS -> "Islas Flotantes del Vacío";
            case COSMIC -> "Picos Cósmicos y Trascendentes";
        };
    }

    private String getToolDescription(int harvestLevel) {
        return switch (harvestLevel) {
            case 1 -> "Pico de Piedra";
            case 2 -> "Pico de Hierro";
            case 3 -> "Pico de Diamante";
            case 4 -> "Pico de Netherita";
            default -> "Pico de Madera";
        };
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
