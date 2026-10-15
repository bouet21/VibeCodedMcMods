package com.chainores.client.gui;

import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModGems;
import com.chainores.registry.ModItems;
import com.chainores.registry.ModWorkstations;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChainOresGuideScreen extends Screen {

    public enum Tab {
        MINERALS("Minerales"),
        ARSENAL("Arsenal"),
        WORKSTATIONS("Estaciones"),
        BOSSES("Jefes y Mundo"),
        MODIFIERS("Gemas y Reforje");

        public final String title;
        Tab(String title) {
            this.title = title;
        }
    }

    private Tab currentTab = Tab.MINERALS;
    private int pageIndex = 0;
    private final int totalPages = OreRegistryData.TIERS.size();
    private int subPage = 0;
    private TextFieldWidget searchField;

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
        int panelWidth = 360;
        int panelHeight = 230;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        this.clearChildren();

        // 1. Navigation Tab Buttons at the very top (5 buttons with width 54, 3px spacing)
        int tabW = 54;
        int tabSpacing = 3;
        int tabStartX = x + 8;
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            boolean active = (t == currentTab);
            Text label = Text.literal((active ? "▶ " : "") + t.title)
                    .formatted(active ? Formatting.GOLD : Formatting.GRAY, active ? Formatting.BOLD : Formatting.RESET);

            this.addDrawableChild(ButtonWidget.builder(label, button -> {
                this.currentTab = t;
                this.subPage = 0;
                playPageTurnSound();
                init();
            }).dimensions(tabStartX + i * (tabW + tabSpacing), y + 6, tabW, 16).build());
        }

        // Close Button (safely separated from the 5th tab)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), button -> this.close())
                .dimensions(x + panelWidth - 22, y + 6, 16, 16).build());

        // Tab-specific controls
        if (currentTab == Tab.MINERALS) {
            // Interactive Search Field
            int searchX = x + 258;
            int searchY = y + 28;
            this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, 92, 13, Text.literal("Buscar"));
            this.searchField.setPlaceholder(Text.literal("🔍 Buscar ore...").formatted(Formatting.DARK_GRAY));
            this.searchField.setMaxLength(24);
            this.searchField.setChangedListener(query -> {
                if (query.trim().isEmpty()) return;
                String q = query.trim().toLowerCase();
                for (int i = 0; i < OreRegistryData.TIERS.size(); i++) {
                    OreTier o = OreRegistryData.TIERS.get(i);
                    if (String.valueOf(o.tier).equals(q) ||
                            o.esName.toLowerCase().contains(q) ||
                            o.enName.toLowerCase().contains(q)) {
                        this.pageIndex = i;
                        break;
                    }
                }
            });
            this.addSelectableChild(this.searchField);

            // Prev & Next Buttons
            this.addDrawableChild(ButtonWidget.builder(Text.literal("< Anterior"), button -> {
                if (pageIndex > 0) {
                    pageIndex--;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + 10, y + panelHeight - 22, 75, 16).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Siguiente >"), button -> {
                if (pageIndex < totalPages - 1) {
                    pageIndex++;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + panelWidth - 85, y + panelHeight - 22, 75, 16).build());

            // Quick Stage Jump Buttons
            int jumpY = y + panelHeight - 40;
            addStageJumpButton(x + 10, jumpY, 30, "T1", 0);
            addStageJumpButton(x + 44, jumpY, 30, "T11", 10);
            addStageJumpButton(x + 78, jumpY, 30, "T21", 20);
            addStageJumpButton(x + 112, jumpY, 30, "T31", 30);
            addStageJumpButton(x + 146, jumpY, 30, "T41", 40);
            addStageJumpButton(x + 180, jumpY, 30, "T61", 60);
            addStageJumpButton(x + 214, jumpY, 30, "T71", 70);
            addStageJumpButton(x + 248, jumpY, 30, "T91", 90);
            addStageJumpButton(x + 282, jumpY, 34, "T106", 105);
            addStageJumpButton(x + 320, jumpY, 34, "T136", 135);
        } else {
            // Sub-page controls for all content-rich tabs (ARSENAL, WORKSTATIONS, BOSSES, MODIFIERS)
            int max = getMaxSubPages(currentTab);
            this.addDrawableChild(ButtonWidget.builder(Text.literal("< Pág Ant."), button -> {
                if (subPage > 0) {
                    subPage--;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + 10, y + panelHeight - 22, 75, 16).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Pág Sig. >"), button -> {
                if (subPage < max - 1) {
                    subPage++;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + panelWidth - 85, y + panelHeight - 22, 75, 16).build());
        }
    }

    private int getMaxSubPages(Tab tab) {
        return switch (tab) {
            case ARSENAL -> 3;
            case WORKSTATIONS, BOSSES, MODIFIERS -> 2;
            default -> 1;
        };
    }

    private void addStageJumpButton(int bx, int by, int bw, String label, int targetIndex) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
            this.pageIndex = targetIndex;
            playPageTurnSound();
            init();
        }).dimensions(bx, by, bw, 15).build());
    }

    private void playPageTurnSound() {
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(net.minecraft.sound.SoundEvents.ITEM_BOOK_PAGE_TURN, 0.7f, 1.0f);
        }
    }

    private void drawCustomBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.searchField != null && this.searchField.isFocused()) {
            if (this.searchField.keyPressed(input)) {
                return true;
            }
            if (input.key() != 256) {
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = 360;
        int panelHeight = 230;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        // Outer Box & Background
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xF4181410);
        drawCustomBorder(context, x, y, panelWidth, panelHeight, 0xFFD4AF37);
        drawCustomBorder(context, x + 1, y + 1, panelWidth - 2, panelHeight - 2, 0xFF5C4718);

        // Content Area Background
        int contentY = y + 26;
        int contentH = panelHeight - 54;
        context.fill(x + 6, contentY, x + panelWidth - 6, contentY + contentH, 0x88000000);
        drawCustomBorder(context, x + 6, contentY, panelWidth - 12, contentH, 0xFF4A3E31);

        // Render buttons and widgets on top of background panel
        super.render(context, mouseX, mouseY, delta);

        if (currentTab == Tab.MINERALS && searchField != null) {
            searchField.render(context, mouseX, mouseY, delta);
        } else if (currentTab != Tab.MINERALS) {
            String pageStr = "Página " + (subPage + 1) + " / " + getMaxSubPages(currentTab);
            int strW = this.textRenderer.getWidth(pageStr);
            context.drawText(this.textRenderer, pageStr, x + (panelWidth - strW) / 2, y + panelHeight - 18, 0xFFE0E0E0, false);
        }

        switch (currentTab) {
            case MINERALS -> renderMineralsTab(context, x, y, mouseX, mouseY);
            case ARSENAL -> renderArsenalTab(context, x, y, mouseX, mouseY);
            case WORKSTATIONS -> renderWorkstationsTab(context, x, y, mouseX, mouseY);
            case BOSSES -> renderBossesTab(context, x, y, mouseX, mouseY);
            case MODIFIERS -> renderModifiersTab(context, x, y, mouseX, mouseY);
        }
    }

    private void renderMineralsTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        OreTier ore = OreRegistryData.TIERS.get(pageIndex);

        String pageNumText = "Tier " + (pageIndex + 1) + " / " + totalPages;
        context.drawText(this.textRenderer, Text.literal(pageNumText).formatted(Formatting.YELLOW),
                x + 115, y + 30, 0xFFFFF1A8, false);

        int leftX = x + 10;
        int topY = y + 28;

        context.drawText(this.textRenderer, Text.literal("MUESTRAS:").formatted(Formatting.AQUA, Formatting.BOLD),
                leftX + 4, topY + 4, 0xFF55FFFF, false);

        ItemStack oreStack = new ItemStack(ore.oreBlockItem);
        context.drawItem(oreStack, leftX + 4, topY + 16);
        context.drawText(this.textRenderer, "Mineral", leftX + 24, topY + 20, 0xFFFFFFFF, false);

        ItemStack rawStack = new ItemStack(ore.rawItem);
        context.drawItem(rawStack, leftX + 4, topY + 42);
        context.drawText(this.textRenderer, "Crudo", leftX + 24, topY + 46, 0xFFE0E0E0, false);

        ItemStack ingotStack = new ItemStack(ore.ingotItem);
        context.drawItem(ingotStack, leftX + 4, topY + 68);
        context.drawText(this.textRenderer, "Lingote", leftX + 24, topY + 72, 0xFFFFD700, false);

        ItemStack storageStack = new ItemStack(ore.storageBlockItem);
        context.drawItem(storageStack, leftX + 4, topY + 94);
        context.drawText(this.textRenderer, "Bloque", leftX + 24, topY + 98, 0xFFAAAAAA, false);

        int rightX = x + 114;
        int rightW = 236;

        String oreTitle = "#" + ore.tier + " " + ore.esName;
        context.drawText(this.textRenderer, Text.literal(oreTitle).formatted(Formatting.GOLD, Formatting.BOLD),
                rightX, topY + 14, 0xFFFFCC00, false);

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
        context.drawText(this.textRenderer, "Dimensión: " + dimName, rightX, topY + 26, dimColor, false);
        context.drawText(this.textRenderer, "Biomas: " + getBiomeDescription(ore.biomeGroup), rightX, topY + 38, 0xFFFFFF88, false);
        context.drawText(this.textRenderer, "Y: " + ore.minY + " a " + ore.maxY + " (Veta: " + ore.veinSize + ")", rightX, topY + 50, 0xFFE0E0E0, false);
        context.drawText(this.textRenderer, "Pico mín.: " + getToolDescription(ore.harvestLevel), rightX, topY + 62, 0xFF55FFFF, false);

        int recBoxY = topY + 74;
        drawCustomBorder(context, rightX, recBoxY, rightW, 40, 0xFF5A4938);
        context.drawText(this.textRenderer, Text.literal("Receta en Mesa de Herrería / Forja:").formatted(Formatting.LIGHT_PURPLE),
                rightX + 4, recBoxY + 3, 0xFFFF77FF, false);

        ItemStack templateStack = new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE);
        ItemStack prevStack = (ore.tier == 1)
                ? new ItemStack(Items.COPPER_INGOT)
                : new ItemStack(OreRegistryData.TIERS.get(ore.tier - 2).ingotItem);

        int recItemY = recBoxY + 16;
        context.drawItem(templateStack, rightX + 6, recItemY);
        context.drawText(this.textRenderer, "+", rightX + 26, recItemY + 4, 0xFFFFFFFF, false);
        context.drawItem(prevStack, rightX + 34, recItemY);
        context.drawText(this.textRenderer, "+", rightX + 54, recItemY + 4, 0xFFFFFFFF, false);
        context.drawItem(rawStack, rightX + 62, recItemY);
        context.drawText(this.textRenderer, "➔", rightX + 82, recItemY + 4, 0xFF55FF55, false);
        context.drawItem(ingotStack, rightX + 96, recItemY);
        context.drawText(this.textRenderer, "Lingote T" + ore.tier, rightX + 116, recItemY + 4, 0xFFFFD700, false);

        checkTooltip(context, oreStack, leftX + 4, topY + 16, mouseX, mouseY);
        checkTooltip(context, rawStack, leftX + 4, topY + 42, mouseX, mouseY);
        checkTooltip(context, ingotStack, leftX + 4, topY + 68, mouseX, mouseY);
        checkTooltip(context, storageStack, leftX + 4, topY + 94, mouseX, mouseY);
        checkTooltip(context, templateStack, rightX + 6, recItemY, mouseX, mouseY);
        checkTooltip(context, prevStack, rightX + 34, recItemY, mouseX, mouseY);
        checkTooltip(context, ingotStack, rightX + 96, recItemY, mouseX, mouseY);
    }

    private void renderArsenalTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;
        int maxW = 332;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ ARSENAL DE COMBATE Y HERRAMIENTAS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "• Hachas de Tala (TreeCapitator):", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Shift al talar derriba árboles enteros al instante.", cx, cy + 24, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Palas 3x3 y Azadas de Cultivo 9x9:", cx, cy + 38, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Excavan y aplanan áreas o hidratan campos enteros.", cx, cy + 48, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Armas a Distancia Épicas:", cx, cy + 62, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "  Arco Dragón (T115): Proyectiles guiados con fuego.", cx, cy + 72, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  Ballesta Galáctica (T135): 3 virotes rebotantes.", cx, cy + 82, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ Herramienta Universal de los Dioses (T150):", cx, cy + 96, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  Mina 5x5, tala, pala y azada. 120 daño y rayos.", cx, cy + 106, 0xFFFFF1A8, false);

            // Dedicated Showcase Dock at the bottom
            int dockY = cy + 120;
            drawCustomBorder(context, cx, dockY, maxW, 24, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Muestras:", cx + 6, dockY + 8, 0xFFAAAAAA, false);

            ItemStack bow = new ItemStack(ModGear.DRAGON_BOW);
            ItemStack xbow = new ItemStack(ModGear.GALACTIC_CROSSBOW);
            ItemStack omni = new ItemStack(ModGear.GOD_OMNITOOL);

            context.drawItem(bow, cx + 62, dockY + 4);
            context.drawItem(xbow, cx + 86, dockY + 4);
            context.drawItem(omni, cx + 110, dockY + 4);

            checkTooltip(context, bow, cx + 62, dockY + 4, mouseX, mouseY);
            checkTooltip(context, xbow, cx + 86, dockY + 4, mouseX, mouseY);
            checkTooltip(context, omni, cx + 110, dockY + 4, mouseX, mouseY);

        } else if (subPage == 1) {
            context.drawText(this.textRenderer, Text.literal("✦ ARSENAL DEFENSIVO: 6 ESCUDOS TEMÁTICOS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack s1 = new ItemStack(ModGear.SENTINEL_SHIELD);
            ItemStack s2 = new ItemStack(ModGear.FROSTGUARD_SHIELD);
            ItemStack s3 = new ItemStack(ModGear.AEGIS_SHIELD);
            ItemStack s4 = new ItemStack(ModGear.ABYSSAL_AEGIS);
            ItemStack s5 = new ItemStack(ModGear.INFERNAL_BULWARK);
            ItemStack s6 = new ItemStack(ModGear.COSMIC_SHIELD);

            int sy = cy + 14;
            int rowH = 17;

            context.drawItem(s1, cx, sy);
            context.drawText(this.textRenderer, "Centinela (T10): ", cx + 20, sy + 4, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Bloqueo firme y absorción temporal.", cx + 96, sy + 4, 0xFFE0E0E0, false);

            context.drawItem(s2, cx, sy + rowH);
            context.drawText(this.textRenderer, "Escarcha (T30): ", cx + 20, sy + rowH + 4, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "Onda glacial; congela y Lentitud IV.", cx + 94, sy + rowH + 4, 0xFFE0E0E0, false);

            context.drawItem(s3, cx, sy + rowH * 2);
            context.drawText(this.textRenderer, "Égida (T70): ", cx + 20, sy + rowH * 2 + 4, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "Refleja proyectiles (200% de daño devuelto).", cx + 76, sy + rowH * 2 + 4, 0xFFE0E0E0, false);

            context.drawItem(s4, cx, sy + rowH * 3);
            context.drawText(this.textRenderer, "Abisal (T105): ", cx + 20, sy + rowH * 3 + 4, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "Onda sónica destructiva de Warden.", cx + 86, sy + rowH * 3 + 4, 0xFFE0E0E0, false);

            context.drawItem(s5, cx, sy + rowH * 4);
            context.drawText(this.textRenderer, "Infernal (T135): ", cx + 20, sy + rowH * 4 + 4, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "Inmola al agresor y quema en 5 bloques.", cx + 96, sy + rowH * 4 + 4, 0xFFE0E0E0, false);

            context.drawItem(s6, cx, sy + rowH * 5);
            context.drawText(this.textRenderer, "Cósmico (T150): ", cx + 20, sy + rowH * 5 + 4, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "Distorsión gravitatoria; repele y empuja.", cx + 96, sy + rowH * 5 + 4, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ Mantén Clic Derecho para bloquear y activar el contraataque.", cx, sy + rowH * 6 + 4, 0xFFFFF1A8, false);

            checkTooltip(context, s1, cx, sy, mouseX, mouseY);
            checkTooltip(context, s2, cx, sy + rowH, mouseX, mouseY);
            checkTooltip(context, s3, cx, sy + rowH * 2, mouseX, mouseY);
            checkTooltip(context, s4, cx, sy + rowH * 3, mouseX, mouseY);
            checkTooltip(context, s5, cx, sy + rowH * 4, mouseX, mouseY);
            checkTooltip(context, s6, cx, sy + rowH * 5, mouseX, mouseY);

        } else {
            context.drawText(this.textRenderer, Text.literal("✦ SETS DE ARMADURA Y HABILIDADES ✦").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (2/4 Piezas):", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Prisa minera, visión nocturna y resistencia basal.", cx, cy + 24, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (3/4 Piezas):", cx, cy + 38, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  Inmunidad a daño de caída, fuego y purga de venenos.", cx, cy + 48, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ Set Completo (4/4 Piezas):", cx, cy + 62, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  T70 Manto Warden | T135 Rescate Vacío | T150 Dios (+14♥).", cx, cy + 72, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "⚡ Dash Impulso (Habilidad Activa Universal):", cx, cy + 88, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Esprintar + Shift ejecuta un impulso en el aire (CD: 4s).", cx, cy + 98, 0xFFFFF1A8, false);

            // Dedicated Showcase Dock for Armor
            int dockY = cy + 116;
            drawCustomBorder(context, cx, dockY, maxW, 26, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Muestra Set T150:", cx + 6, dockY + 9, 0xFFAAAAAA, false);

            ItemStack h = new ItemStack(ModGear.GEAR_SETS.get(0).helmet);
            ItemStack c = new ItemStack(ModGear.GEAR_SETS.get(6).chestplate);
            ItemStack l = new ItemStack(ModGear.GEAR_SETS.get(9).leggings);
            ItemStack b = new ItemStack(ModGear.GEAR_SETS.get(ModGear.GEAR_SETS.size() - 1).boots);

            context.drawItem(h, cx + 115, dockY + 5);
            context.drawItem(c, cx + 138, dockY + 5);
            context.drawItem(l, cx + 161, dockY + 5);
            context.drawItem(b, cx + 184, dockY + 5);

            checkTooltip(context, h, cx + 115, dockY + 5, mouseX, mouseY);
            checkTooltip(context, c, cx + 138, dockY + 5, mouseX, mouseY);
            checkTooltip(context, l, cx + 161, dockY + 5, mouseX, mouseY);
            checkTooltip(context, b, cx + 184, dockY + 5, mouseX, mouseY);
        }
    }

    private void renderWorkstationsTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ ESTACIONES DE TRABAJO ESPECIALIZADAS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack forge = new ItemStack(ModWorkstations.CHAIN_FORGE_ITEM);
            context.drawItem(forge, cx, cy + 14);
            context.drawText(this.textRenderer, "Forja en Cadena (Chain Forge):", cx + 22, cy + 14, 0xFFFFCC00, false);
            context.drawText(this.textRenderer, "Mesa con interfaz para ascender equipo entre tiers.", cx + 22, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "¡Conserva encantamientos, gemas y prefijos intactos!", cx + 22, cy + 34, 0xFF88FF88, false);

            ItemStack anvil = new ItemStack(ModWorkstations.CHAIN_ANVIL_ITEM);
            context.drawItem(anvil, cx, cy + 50);
            context.drawText(this.textRenderer, "Yunque en Cadena (Indestructible):", cx + 22, cy + 50, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "¡Nunca se rompe ni aumenta el coste de experiencia!", cx + 22, cy + 60, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "Permite engarzar gemas, reforjar prefijos y reparar.", cx + 22, cy + 70, 0xFFAAAAAA, false);

            ItemStack smelter = new ItemStack(ModWorkstations.COSMIC_SMELTER_ITEM);
            context.drawItem(smelter, cx, cy + 86);
            context.drawText(this.textRenderer, "Fundidor Cósmico (Cosmic Smelter):", cx + 22, cy + 86, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "Duplica (x2) los lingotes producidos de minerales crudos.", cx + 22, cy + 96, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "35% de probabilidad de transmutar al tier superior.", cx + 22, cy + 106, 0xFFFFFF55, false);

            checkTooltip(context, forge, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, anvil, cx, cy + 50, mouseX, mouseY);
            checkTooltip(context, smelter, cx, cy + 86, mouseX, mouseY);

        } else {
            context.drawText(this.textRenderer, Text.literal("⚒ GUÍA DE USO PASO A PASO DE LAS MESAS ⚒").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "1. FORJA EN CADENA (Ascenso de Equipo):", cx, cy + 14, 0xFFFFCC00, false);
            context.drawText(this.textRenderer, "  • Clic derecho para abrir la interfaz interactiva.", cx, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Ranura Base: Equipo | Ranura Cat.: Lingote/Catalizador.", cx, cy + 34, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Recoge el objeto conservando todas las mejoras.", cx, cy + 44, 0xFF88FF88, false);

            context.drawText(this.textRenderer, "2. YUNQUE EN CADENA (Engarce, Reforje y Reparación):", cx, cy + 58, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  • Engarzar: Arma en mano princ. + Gema en sec. + Clic.", cx, cy + 68, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Reforjar: Shift + Clic con Amatista o Lapislázuli.", cx, cy + 78, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Reparar: Clic derecho con 1 lingote del tier.", cx, cy + 88, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "3. FUNDIDOR CÓSMICO (Duplicación & Transmutación):", cx, cy + 102, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "  • Mineral crudo arriba y combustible abajo (Lava ideal).", cx, cy + 112, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Recibe el doble de lingotes y transmutaciones gratis.", cx, cy + 122, 0xFFFFF1A8, false);
        }
    }

    private void renderBossesTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ JEFES SUPREMOS DE DIMENSIÓN ✦").formatted(Formatting.RED, Formatting.BOLD),
                    cx, cy, 0xFFFF5555, false);

            ItemStack eye = new ItemStack(ModBossItems.ABYSSAL_EYE);
            context.drawItem(eye, cx, cy + 14);
            context.drawText(this.textRenderer, "1. Guardián del Abismo (T70 - Overworld, Y < 0):", cx + 22, cy + 14, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Forja Abandonada u Ojo Abisal. Drop: Catalizador.", cx + 22, cy + 24, 0xFFE0E0E0, false);

            ItemStack heart = new ItemStack(ModBossItems.FLAME_HEART);
            context.drawItem(heart, cx, cy + 40);
            context.drawText(this.textRenderer, "2. Behemoth del Nether (T105 - Nether, Y 25-100):", cx + 22, cy + 40, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "  Santuario Nether o Corazón Ígneo. Drop: Cat. Ígneo.", cx + 22, cy + 50, 0xFFE0E0E0, false);

            ItemStack beacon = new ItemStack(ModBossItems.VOID_BEACON);
            context.drawItem(beacon, cx, cy + 66);
            context.drawText(this.textRenderer, "3. Coloso del Vacío (T135 - The End, Islas):", cx + 22, cy + 66, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "  Altar Cósmico o Baliza Vacío. Drop: Cat. del Vacío.", cx + 22, cy + 76, 0xFFE0E0E0, false);

            ItemStack core = new ItemStack(ModBossItems.COSMIC_ALTAR_CORE);
            context.drawItem(core, cx, cy + 92);
            context.drawText(this.textRenderer, "4. Soberano Cósmico (T150 - Boss Final):", cx + 22, cy + 92, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  Invocado con Núcleo del Altar. Drop: Singularidad.", cx + 22, cy + 102, 0xFFFFF1A8, false);

            context.drawText(this.textRenderer, "★ Pasa el ratón por los iconos para ver sus recetas e info.", cx, cy + 118, 0xFFAAAAAA, false);

            checkTooltip(context, eye, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, heart, cx, cy + 40, mouseX, mouseY);
            checkTooltip(context, beacon, cx, cy + 66, mouseX, mouseY);
            checkTooltip(context, core, cx, cy + 92, mouseX, mouseY);

        } else {
            context.drawText(this.textRenderer, Text.literal("✦ ESTRUCTURAS ÉPICAS Y BRÚJULA RÚNICA ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "• Generación de Estructuras Legendarias:", cx, cy + 14, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  1 en 8.000 a 10.000 chunks con 2.000-2.500 bloques", cx, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  de distancia mínima. ¡Tienen arena y cofre del tesoro!", cx, cy + 34, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "🧭 CÓMO USAR LA BRÚJULA DE ESTRUCTURAS:", cx, cy + 50, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "• Shift + Clic der.: Alterna la estructura objetivo:", cx, cy + 60, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  1. Forja Abandonada | 2. Santuario | 3. Altar Cósmico", cx, cy + 70, 0xFF88FF88, false);

            context.drawText(this.textRenderer, "• Clic der. normal: Pulso sonar de resonancia rúnica.", cx, cy + 84, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  Calcula distancia, rumbo y envía coordenadas al chat.", cx, cy + 94, 0xFFE0E0E0, false);

            // Dedicated Compass Slot at bottom
            int compassBoxY = cy + 108;
            drawCustomBorder(context, cx, compassBoxY, 332, 24, 0xFF4A3E31);

            ItemStack compass = new ItemStack(ModItems.STRUCTURE_COMPASS);
            context.drawItem(compass, cx + 4, compassBoxY + 4);
            context.drawText(this.textRenderer, "Brújula de Estructuras (Crafteable con cadenas)", cx + 24, compassBoxY + 8, 0xFFFFD700, false);

            checkTooltip(context, compass, cx + 4, compassBoxY + 4, mouseX, mouseY);
        }
    }

    private void renderModifiersTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ GEMAS MÁGICAS Y RANURAS DE EQUIPO ✦").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                    cx, cy, 0xFFFF77FF, false);

            ItemStack blood = new ItemStack(ModGems.BLOOD_GEM);
            context.drawItem(blood, cx, cy + 14);
            context.drawText(this.textRenderer, "Gema Carmesí: ", cx + 20, cy + 14, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "+15% Robo de Vida (Armas)", cx + 96, cy + 14, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "Armadura: Regeneración pasiva II y salvavidas.", cx + 20, cy + 24, 0xFFAAAAAA, false);

            ItemStack voidGem = new ItemStack(ModGems.VOID_GEM);
            context.drawItem(voidGem, cx, cy + 38);
            context.drawText(this.textRenderer, "Gema del Vacío: ", cx + 20, cy + 38, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "+30% Daño cósmico y levitación", cx + 104, cy + 38, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "Armadura: Inmunidad a daño de caída y planeo.", cx + 20, cy + 48, 0xFFAAAAAA, false);

            ItemStack core = new ItemStack(ModGems.CORE_GEM);
            context.drawItem(core, cx, cy + 62);
            context.drawText(this.textRenderer, "Gema del Núcleo: ", cx + 20, cy + 62, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "+35% Vel. ataque e ignición", cx + 110, cy + 62, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "Armadura: Inmune a fuego/lava y Velocidad II.", cx + 20, cy + 72, 0xFFAAAAAA, false);

            ItemStack genesis = new ItemStack(ModGems.GENESIS_GEM);
            context.drawItem(genesis, cx, cy + 86);
            context.drawText(this.textRenderer, "Gema del Génesis: ", cx + 20, cy + 86, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "Rayos sagrados en críticos", cx + 116, cy + 86, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "Armadura: +4 Absorción y retribución sagrada.", cx + 20, cy + 96, 0xFFAAAAAA, false);

            // Tier slot breakdown box
            int slotBoxY = cy + 110;
            drawCustomBorder(context, cx, slotBoxY, 332, 24, 0xFF4A3E31);
            context.drawText(this.textRenderer, "• T1-9: 0 ranuras | T10-39: 1 [◆] | T40-79: 2 [◆] | T80+: 3-4 [◆]", cx + 6, slotBoxY + 8, 0xFFFFF1A8, false);

            checkTooltip(context, blood, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, voidGem, cx, cy + 38, mouseX, mouseY);
            checkTooltip(context, core, cx, cy + 62, mouseX, mouseY);
            checkTooltip(context, genesis, cx, cy + 86, mouseX, mouseY);

        } else {
            context.drawText(this.textRenderer, Text.literal("✦ REFORJADO, PREFIJOS Y ENCANTAMIENTOS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack amethyst = new ItemStack(Items.AMETHYST_SHARD);
            context.drawItem(amethyst, cx, cy + 12);
            context.drawText(this.textRenderer, "⚡ PREFIJOS EN YUNQUE (Shift + Clic):", cx + 20, cy + 16, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "• [DIVINO] (8%): +50% Daño / +50% Vel. Minera / Resistencia.", cx, cy + 28, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "• [LEGENDARIO] (14%): +25% Daño / +30% Minería / Suerte.", cx, cy + 40, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "• [FEROZ] (25%): +35% Daño / Fuerza I pasiva.", cx, cy + 52, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "• [VELOZ] (26%): +40% Vel. Minera / Velocidad I pasiva.", cx, cy + 64, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "• [TITÁNICO] (27%): +10% Daño / Resistencia pasiva.", cx, cy + 76, 0xFFAAAAAA, false);

            context.drawText(this.textRenderer, "✦ ENCANTAMIENTOS ARCANOS EXCLUSIVOS:", cx, cy + 90, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "• Desintegración en Cadena: Mina la veta completa de golpe.", cx, cy + 100, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Drenaje del Vacío: Absorbe XP y vida de criaturas cercanas.", cx, cy + 110, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Radiancia Cósmica: Ciega agresores y emite luz brillante.", cx, cy + 120, 0xFFE0E0E0, false);

            checkTooltip(context, amethyst, cx, cy + 12, mouseX, mouseY);
        }
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
