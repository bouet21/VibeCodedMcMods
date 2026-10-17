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
            case ARSENAL -> 6;
            case BOSSES -> 4;
            case WORKSTATIONS -> 4;
            case MODIFIERS -> 3;
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
            context.drawText(this.textRenderer,
                    Text.literal("Centinela (T10): ").formatted(Formatting.WHITE)
                            .append(Text.literal("Bloqueo firme y absorción temporal.").formatted(Formatting.GRAY)),
                    cx + 20, sy + 4, 0xFFFFFFFF, false);

            context.drawItem(s2, cx, sy + rowH);
            context.drawText(this.textRenderer,
                    Text.literal("Escarcha (T30): ").formatted(Formatting.AQUA)
                            .append(Text.literal("Onda glacial; congela y Lentitud IV.").formatted(Formatting.GRAY)),
                    cx + 20, sy + rowH + 4, 0xFFFFFFFF, false);

            context.drawItem(s3, cx, sy + rowH * 2);
            context.drawText(this.textRenderer,
                    Text.literal("Égida (T70): ").formatted(Formatting.GOLD)
                            .append(Text.literal("Refleja proyectiles (200% daño devuelto).").formatted(Formatting.GRAY)),
                    cx + 20, sy + rowH * 2 + 4, 0xFFFFFFFF, false);

            context.drawItem(s4, cx, sy + rowH * 3);
            context.drawText(this.textRenderer,
                    Text.literal("Abisal (T105): ").formatted(Formatting.DARK_AQUA)
                            .append(Text.literal("Onda sónica destructiva de Warden.").formatted(Formatting.GRAY)),
                    cx + 20, sy + rowH * 3 + 4, 0xFFFFFFFF, false);

            context.drawItem(s5, cx, sy + rowH * 4);
            context.drawText(this.textRenderer,
                    Text.literal("Infernal (T135): ").formatted(Formatting.RED)
                            .append(Text.literal("Inmola al agresor y quema en 5 bloques.").formatted(Formatting.GRAY)),
                    cx + 20, sy + rowH * 4 + 4, 0xFFFFFFFF, false);

            context.drawItem(s6, cx, sy + rowH * 5);
            context.drawText(this.textRenderer,
                    Text.literal("Cósmico (T150): ").formatted(Formatting.LIGHT_PURPLE)
                            .append(Text.literal("Distorsión gravitatoria; repele y empuja.").formatted(Formatting.GRAY)),
                    cx + 20, sy + rowH * 5 + 4, 0xFFFFFFFF, false);

            context.drawText(this.textRenderer, "★ Mantén Clic Derecho para bloquear y activar el contraataque.", cx, sy + rowH * 6 + 4, 0xFFFFF1A8, false);

            checkTooltip(context, s1, cx, sy, mouseX, mouseY);
            checkTooltip(context, s2, cx, sy + rowH, mouseX, mouseY);
            checkTooltip(context, s3, cx, sy + rowH * 2, mouseX, mouseY);
            checkTooltip(context, s4, cx, sy + rowH * 3, mouseX, mouseY);
            checkTooltip(context, s5, cx, sy + rowH * 4, mouseX, mouseY);
            checkTooltip(context, s6, cx, sy + rowH * 5, mouseX, mouseY);

        } else if (subPage == 2) {
            context.drawText(this.textRenderer, Text.literal("✦ SETS DE ARMADURA Y HABILIDADES ✦").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (2/4 Piezas):", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Prisa minera, visión nocturna y resistencia basal.", cx, cy + 24, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (3/4 Piezas):", cx, cy + 38, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  Inmunidad a daño de caída, fuego y purga de venenos.", cx, cy + 48, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ Set Completo (4/4 Piezas):", cx, cy + 62, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  T70 Manto Warden | T135 Rescate Vacío | T150 Dios (+14♥).", cx, cy + 72, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "⚡ Dash Impulso (Habilidad Activa Universal):", cx, cy + 88, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Presiona [V] (o Esprintar + Shift) para impulsarte.", cx, cy + 98, 0xFFFFF1A8, false);

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

        } else if (subPage == 3) {
            // subPage 3: Joyería Arcana & Trinkets
            context.drawText(this.textRenderer, Text.literal("✦ JOYERÍA ARCANA Y MOD TRINKETS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack ring = new ItemStack(ModItems.CHAIN_RING);
            ItemStack amulet = new ItemStack(ModItems.CHAIN_AMULET);

            context.drawItem(ring, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("Anillo en Cadena: ").formatted(Formatting.YELLOW, Formatting.BOLD)
                            .append(Text.literal("2 Ranuras de Gemas [◆][◆]").formatted(Formatting.WHITE)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Otorga pasivos continuos. Compatible con reforja.", cx + 22, cy + 24, 0xFFAAAAAA, false);

            context.drawItem(amulet, cx, cy + 38);
            context.drawText(this.textRenderer,
                    Text.literal("Amuleto en Cadena: ").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("4 Ranuras de Gemas [◆][◆][◆][◆]").formatted(Formatting.WHITE)),
                    cx + 22, cy + 38, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Permite combinar hasta 4 gemas simultáneas.", cx + 22, cy + 48, 0xFFAAAAAA, false);

            context.drawText(this.textRenderer, "⚡ COMPATIBILIDAD CON MOD TRINKETS:", cx, cy + 64, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "• Puedes equipar el Anillo en la ranura de mano/anillo.", cx, cy + 74, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Puedes equipar el Amuleto en la ranura de pecho/collar.", cx, cy + 84, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Si no usas Trinkets, ¡funcionan igual en tu inventario!", cx, cy + 96, 0xFFFFF1A8, false);

            int dockY = cy + 116;
            drawCustomBorder(context, cx, dockY, maxW, 26, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Joyería Muestra:", cx + 6, dockY + 9, 0xFFAAAAAA, false);
            context.drawItem(ring, cx + 115, dockY + 5);
            context.drawItem(amulet, cx + 140, dockY + 5);

            checkTooltip(context, ring, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, amulet, cx, cy + 38, mouseX, mouseY);
            checkTooltip(context, ring, cx + 115, dockY + 5, mouseX, mouseY);
            checkTooltip(context, amulet, cx + 140, dockY + 5, mouseX, mouseY);

        } else if (subPage == 4) {
            // subPage 4: Bolsa de Minería en Cadena & Auto-Compactador
            context.drawText(this.textRenderer, Text.literal("✦ BOLSA DE MINERÍA Y AUTO-COMPACTADOR ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack satchel = new ItemStack(ModItems.MINING_SATCHEL);
            context.drawItem(satchel, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("Bolsa de Minería en Cadena: ").formatted(Formatting.YELLOW, Formatting.BOLD)
                            .append(Text.literal("54 Ranuras Profundas").formatted(Formatting.AQUA)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Almacena minerales, lingotes y gemas sin saturar inventario.", cx + 22, cy + 24, 0xFFAAAAAA, false);

            context.drawText(this.textRenderer, "🧲 IMÁN Y AUTO-COMPACTADOR:", cx, cy + 40, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "• Clic Derecho: Abre el inventario de 54 casillas.", cx, cy + 50, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Shift + Clic Derecho: Alterna el auto-aspirador a 8m.", cx, cy + 60, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Auto-Compactador: Fusión automática de 9 lingotes en 1 bloque.", cx, cy + 70, 0xFF88FF88, false);

            context.drawText(this.textRenderer, "✨ COMPATIBILIDAD CON TRINKETS:", cx, cy + 86, 0xFFFF77FF, false);
            context.drawText(this.textRenderer, "• Equípala en cinturón o espalda para aspirar mientras caminas.", cx, cy + 96, 0xFFE0E0E0, false);

            int dockY = cy + 116;
            drawCustomBorder(context, cx, dockY, maxW, 26, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Muestra Satchel:", cx + 6, dockY + 9, 0xFFAAAAAA, false);
            context.drawItem(satchel, cx + 115, dockY + 5);

            checkTooltip(context, satchel, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, satchel, cx + 115, dockY + 5, mouseX, mouseY);

        } else {
            // subPage 5: Armaduras Especializadas (Minería, Tala, Élitros)
            context.drawText(this.textRenderer, Text.literal("✦ ARMADURAS ESPECIALIZADAS Y ÉLITROS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack exo = new ItemStack(ModGear.MINING_EXOSKELETON);
            ItemStack lumber = new ItemStack(ModGear.LUMBERJACK_HAUBERK);
            ItemStack elytraArmor = new ItemStack(ModGear.COSMIC_ELYTRA_CHESTPLATE);

            context.drawItem(exo, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("Peto de Minería Profunda: ").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("Prisa II, Fuego, Imán 6m y Sonar").formatted(Formatting.WHITE)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "La coraza definitiva para cavernas y pozos oscuros.", cx + 22, cy + 24, 0xFFAAAAAA, false);

            context.drawItem(lumber, cx, cy + 38);
            context.drawText(this.textRenderer,
                    Text.literal("Coraza del Guardabosques: ").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal("Velocidad II, Tala Masiva y Fertilidad").formatted(Formatting.WHITE)),
                    cx + 22, cy + 38, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Derriba árboles enteros en un toque y acelera cultivos.", cx + 22, cy + 48, 0xFFAAAAAA, false);

            context.drawItem(elytraArmor, cx, cy + 62);
            context.drawText(this.textRenderer,
                    Text.literal("Coraza Alada Cósmica: ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                            .append(Text.literal("Armadura Pesada + Vuelo Élitros").formatted(Formatting.AQUA)),
                    cx + 22, cy + 62, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Vuelo con propulsión a reacción [Shift en vuelo] e inmune a choques.", cx + 22, cy + 72, 0xFFAAAAAA, false);

            int dockY = cy + 116;
            drawCustomBorder(context, cx, dockY, maxW, 26, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Muestra Especialistas:", cx + 6, dockY + 9, 0xFFAAAAAA, false);
            context.drawItem(exo, cx + 130, dockY + 5);
            context.drawItem(lumber, cx + 155, dockY + 5);
            context.drawItem(elytraArmor, cx + 180, dockY + 5);

            checkTooltip(context, exo, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, lumber, cx, cy + 38, mouseX, mouseY);
            checkTooltip(context, elytraArmor, cx, cy + 62, mouseX, mouseY);
            checkTooltip(context, exo, cx + 130, dockY + 5, mouseX, mouseY);
            checkTooltip(context, lumber, cx + 155, dockY + 5, mouseX, mouseY);
            checkTooltip(context, elytraArmor, cx + 180, dockY + 5, mouseX, mouseY);
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

        } else if (subPage == 1) {
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

            context.drawText(this.textRenderer, "3. FUNDIDOR CÓSMICO (Cuadruplicación x4 & Transmutación):", cx, cy + 102, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "  • Mineral crudo arriba y combustible abajo (Lava ideal).", cx, cy + 112, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  • Recibe el cuádruple (x4) de lingotes y transmutación 35%.", cx, cy + 122, 0xFFFFF1A8, false);

        } else if (subPage == 2) {
            // subPage 2: Catalizadores & Ascensión Dimensional
            context.drawText(this.textRenderer, Text.literal("✦ CATALIZADORES Y ASCENSIÓN DIMENSIONAL ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "🔑 ¿PARA QUÉ SIRVEN LOS CATALIZADORES?:", cx, cy + 12, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "Son reliquias alquímicas soltadas por los Jefes. Se insertan en la", cx, cy + 22, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "ranura DERECHA (activador) de la Forja en Cadena.", cx, cy + 31, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "🚪 PUERTAS DE ASCENSIÓN OBLIGATORIA (DIMENSIONES):", cx, cy + 44, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "• T70 ➔ T71: Requiere Catalizador Abisal (Guardián del Abismo).", cx, cy + 54, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "• T105 ➔ T106: Requiere Núcleo Ígneo (Behemoth del Nether).", cx, cy + 64, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "• T135 ➔ T136: Requiere Fragmento del Vacío (Coloso del Vacío).", cx, cy + 74, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "• T150 (Final): Requiere Singularidad Cósmica (Soberano Cósmico).", cx, cy + 84, 0xFFFFF1A8, false);

            context.drawText(this.textRenderer, "⚡ RECETAS LEGENDARIAS & DUPLICACIÓN (x2):", cx, cy + 97, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "• Escudo Criogénico (Cat. Glacial) | Coraza Guardabosques (Cat. Silvestre).", cx, cy + 107, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Duplicación: Lingote en base + Cat. en derecha = ¡x2 lingotes gratis!", cx, cy + 117, 0xFF88FF88, false);

            int catBoxY = cy + 130;
            drawCustomBorder(context, cx, catBoxY, 332, 24, 0xFF4A3E31);

            ItemStack c1 = new ItemStack(ModBossItems.GLACIAL_CATALYST);
            ItemStack c2 = new ItemStack(ModBossItems.SYLVA_CATALYST);
            ItemStack c3 = new ItemStack(ModBossItems.ABYSSAL_CATALYST);
            ItemStack c4 = new ItemStack(ModBossItems.NETHER_CORE);
            ItemStack c5 = new ItemStack(ModBossItems.VOID_SHARD);
            ItemStack c6 = new ItemStack(ModBossItems.COSMIC_SINGULARITY);

            context.drawItem(c1, cx + 6, catBoxY + 4);
            context.drawItem(c2, cx + 42, catBoxY + 4);
            context.drawItem(c3, cx + 78, catBoxY + 4);
            context.drawItem(c4, cx + 114, catBoxY + 4);
            context.drawItem(c5, cx + 150, catBoxY + 4);
            context.drawItem(c6, cx + 186, catBoxY + 4);
            context.drawText(this.textRenderer, "◀ Reliquias Alquímicas", cx + 210, catBoxY + 8, 0xFFD4AF37, false);

            checkTooltip(context, c1, cx + 6, catBoxY + 4, mouseX, mouseY);
            checkTooltip(context, c2, cx + 42, catBoxY + 4, mouseX, mouseY);
            checkTooltip(context, c3, cx + 78, catBoxY + 4, mouseX, mouseY);
            checkTooltip(context, c4, cx + 114, catBoxY + 4, mouseX, mouseY);
            checkTooltip(context, c5, cx + 150, catBoxY + 4, mouseX, mouseY);
            checkTooltip(context, c6, cx + 186, catBoxY + 4, mouseX, mouseY);

        } else {
            // subPage 3: Altar Cósmico & Dificultades Escalables
            context.drawText(this.textRenderer, Text.literal("✦ ALTAR CÓSMICO: MODOS Y DIFICULTADES ✦").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                    cx, cy, 0xFFFF55FF, false);

            ItemStack altar = new ItemStack(ModWorkstations.COSMIC_ALTAR_ITEM);
            context.drawItem(altar, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("Altar Cósmico: ").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("Arena Escalable de Oleadas").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "Elige la dificultad según el objeto activador usado:", cx + 22, cy + 24, 0xFFAAAAAA, false);

            context.drawText(this.textRenderer, "⚔ NORMAL (3 Oleadas): Clic derecho con Trofeo Cósmico.", cx, cy + 38, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Invasores cósmicos básicos. Botín: Lingotes T140 y Gemas II.", cx, cy + 48, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "⚔ DIFÍCIL (5 Oleadas): Shift + Clic der. con Trofeo Cósmico.", cx, cy + 62, 0xFFFFCC00, false);
            context.drawText(this.textRenderer, "  Invoca al Guardián Criogénico y Titán Silvestre en oleada 4.", cx, cy + 72, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "☠ PESADILLA (7 Oleadas): Clic derecho con Singularidad.", cx, cy + 86, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "  Invoca a los 4 Jefes y al Soberano. Drop: Herramienta de Dios,", cx, cy + 96, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "  Gemas Trascendentes III, 8x Singularidades y 35,000 XP.", cx, cy + 106, 0xFFFFF1A8, false);

            checkTooltip(context, altar, cx, cy + 14, mouseX, mouseY);
        }
    }

    private void renderBossesTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ MINI-JEFES DEL OVERWORLD (T20 & T40) ✦").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            ItemStack horn = new ItemStack(ModBossItems.GLACIAL_HORN);
            context.drawItem(horn, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("1. Guardián Criogénico ").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("(T20 - Santuario Glacial / Nieve)").formatted(Formatting.DARK_AQUA)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Spawn: Santuario Glacial o Cuerno. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Catalizador Glacial.").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 24, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "  Habilidades: Golpe sísmico helado, Congelación y Nova.", cx + 22, cy + 34, 0xFFB3E5FC, false);

            ItemStack heartS = new ItemStack(ModBossItems.VERDANT_HEART);
            context.drawItem(heartS, cx, cy + 48);
            context.drawText(this.textRenderer,
                    Text.literal("2. Titán Silvestre ").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal("(T40 - Arboleda Ancestral / Bosque)").formatted(Formatting.DARK_GREEN)),
                    cx + 22, cy + 48, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Spawn: Arboleda Ancestral o Corazón. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Catalizador Silvestre.").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 58, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "  Habilidades: Raíces inmovilizadoras, Terremoto y Fotosíntesis.", cx + 22, cy + 68, 0xFFA5D6A7, false);

            // Recompensa de Forja box
            int boxY = cy + 82;
            drawCustomBorder(context, cx, boxY, 332, 42, 0xFF4A3E31);
            context.drawText(this.textRenderer, "⚒ RECETAS DE FORJA CON SUS CATALIZADORES:", cx + 6, boxY + 6, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "• Cat. Glacial: Escudo Criogénico (Rebote helado) y x2 lingote T20.", cx + 6, boxY + 17, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Cat. Silvestre: Coraza del Guardabosques (Tala masiva) y x2 lingote T40.", cx + 6, boxY + 28, 0xFFE0E0E0, false);

            checkTooltip(context, horn, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, heartS, cx, cy + 48, mouseX, mouseY);

        } else if (subPage == 1) {
            context.drawText(this.textRenderer, Text.literal("✦ JEFES SUPREMOS DE DIMENSIÓN ✦").formatted(Formatting.RED, Formatting.BOLD),
                    cx, cy, 0xFFFF5555, false);

            ItemStack eye = new ItemStack(ModBossItems.ABYSSAL_EYE);
            context.drawItem(eye, cx, cy + 14);
            context.drawText(this.textRenderer,
                    Text.literal("1. Guardián del Abismo ").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("(T70 - Overworld, Y < 0)").formatted(Formatting.DARK_AQUA)),
                    cx + 22, cy + 14, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Forja Abandonada u Ojo Abisal. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Catalizador.").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 24, 0xFFFFFFFF, false);

            ItemStack heart = new ItemStack(ModBossItems.FLAME_HEART);
            context.drawItem(heart, cx, cy + 40);
            context.drawText(this.textRenderer,
                    Text.literal("2. Behemoth del Nether ").formatted(Formatting.RED, Formatting.BOLD)
                            .append(Text.literal("(T105 - Nether, Y 25-100)").formatted(Formatting.DARK_RED)),
                    cx + 22, cy + 40, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Santuario Nether o Corazón Ígneo. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Cat. Ígneo.").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 50, 0xFFFFFFFF, false);

            ItemStack beacon = new ItemStack(ModBossItems.VOID_BEACON);
            context.drawItem(beacon, cx, cy + 66);
            context.drawText(this.textRenderer,
                    Text.literal("3. Coloso del Vacío ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                            .append(Text.literal("(T135 - The End, Islas)").formatted(Formatting.DARK_PURPLE)),
                    cx + 22, cy + 66, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Altar Cósmico o Baliza Vacío. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Cat. del Vacío.").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 76, 0xFFFFFFFF, false);

            ItemStack core = new ItemStack(ModBossItems.COSMIC_ALTAR_CORE);
            context.drawItem(core, cx, cy + 92);
            context.drawText(this.textRenderer,
                    Text.literal("4. Soberano Cósmico ").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("(T150 - Boss Final)").formatted(Formatting.YELLOW)),
                    cx + 22, cy + 92, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer,
                    Text.literal("  Invocado con Núcleo del Altar. ").formatted(Formatting.GRAY)
                            .append(Text.literal("Drop: Singularidad.").formatted(Formatting.GOLD)),
                    cx + 22, cy + 102, 0xFFFFFFFF, false);

            context.drawText(this.textRenderer, "⚠ ¡CUIDADO! Poseen regeneración, resistencia, furia <50% y devoran almas.", cx, cy + 118, 0xFFFF7777, false);

            checkTooltip(context, eye, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, heart, cx, cy + 40, mouseX, mouseY);
            checkTooltip(context, beacon, cx, cy + 66, mouseX, mouseY);
            checkTooltip(context, core, cx, cy + 92, mouseX, mouseY);

        } else if (subPage == 2) {
            context.drawText(this.textRenderer, Text.literal("✦ ESTRUCTURAS ÉPICAS Y BRÚJULA RÚNICA ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "• Generación de Estructuras Legendarias:", cx, cy + 14, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  1 en 8.000 a 10.000 chunks con 2.000-2.500 bloques", cx, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  de distancia mínima. ¡Tienen arena y cofre del tesoro!", cx, cy + 34, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "🧭 CÓMO USAR LA BRÚJULA DE ESTRUCTURAS:", cx, cy + 50, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "• Shift + Clic der.: Alterna entre los 6 objetivos:", cx, cy + 60, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  0: Auto | 1: Forja | 2: Glacial | 3: Arboleda | 4: Nether | 5: Altar", cx, cy + 70, 0xFF88FF88, false);

            context.drawText(this.textRenderer, "• Clic der. normal: Pulso sonar de resonancia rúnica.", cx, cy + 84, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  Calcula distancia, rumbo y envía coordenadas al chat.", cx, cy + 94, 0xFFE0E0E0, false);

            int compassBoxY = cy + 108;
            drawCustomBorder(context, cx, compassBoxY, 332, 24, 0xFF4A3E31);

            ItemStack compass = new ItemStack(ModItems.STRUCTURE_COMPASS);
            context.drawItem(compass, cx + 4, compassBoxY + 4);
            context.drawText(this.textRenderer, "Brújula de Estructuras (Crafteable con cadenas)", cx + 24, compassBoxY + 8, 0xFFFFD700, false);

            checkTooltip(context, compass, cx + 4, compassBoxY + 4, mouseX, mouseY);

        } else {
            // subPage 3: Escalado Multijugador, Mobs Infundidos y EMI
            context.drawText(this.textRenderer, Text.literal("✦ MULTIJUGADOR, MOBS INFUNDIDOS Y EMI ✦").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "⚔ ESCALADO DINÁMICO EN MULTIJUGADOR:", cx, cy + 14, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "• Los 4 Jefes escalan según jugadores cercanos (64b):", cx, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  +35% Vida y +15% Daño por cada jugador adicional.", cx, cy + 34, 0xFFFFF1A8, false);

            context.drawText(this.textRenderer, "👹 CRIATURAS INFUNDIDAS DIMENSIONALES:", cx, cy + 48, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "• En el Nether y en el End aparecen mobs con auras.", cx, cy + 58, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Tienen mayor dureza y dropean lingotes y gemas.", cx, cy + 68, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "📖 VISOR DE RECETAS EMI INTEGRADO:", cx, cy + 82, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "• Pulsa [R] sobre un lingote o ítem para ver su receta.", cx, cy + 92, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Pulsa [U] para ver todos sus usos en la Forja/Altar.", cx, cy + 102, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• La tecla de Dash universal se ubica en [V].", cx, cy + 114, 0xFF88FF88, false);
        }
    }

    private void renderModifiersTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ GEMAS MÁGICAS Y RANURAS DE EQUIPO ✦").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                    cx, cy, 0xFFFF77FF, false);

            ItemStack blood = new ItemStack(ModGems.BLOOD_GEM);
            context.drawItem(blood, cx, cy + 12);
            context.drawText(this.textRenderer,
                    Text.literal("Gema Carmesí: ").formatted(Formatting.RED, Formatting.BOLD)
                            .append(Text.literal("+15% Robo de Vida | Armadura: Regen II").formatted(Formatting.WHITE)),
                    cx + 20, cy + 12, 0xFFFFFFFF, false);

            ItemStack voidGem = new ItemStack(ModGems.VOID_GEM);
            context.drawItem(voidGem, cx, cy + 32);
            context.drawText(this.textRenderer,
                    Text.literal("Gema del Vacío: ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                            .append(Text.literal("+30% Daño cósmico | Armadura: Sin daño caída").formatted(Formatting.WHITE)),
                    cx + 20, cy + 32, 0xFFFFFFFF, false);

            ItemStack core = new ItemStack(ModGems.CORE_GEM);
            context.drawItem(core, cx, cy + 52);
            context.drawText(this.textRenderer,
                    Text.literal("Gema del Núcleo: ").formatted(Formatting.GOLD, Formatting.BOLD)
                            .append(Text.literal("+35% Vel. ataque | Armadura: Inmune fuego/lava").formatted(Formatting.WHITE)),
                    cx + 20, cy + 52, 0xFFFFFFFF, false);

            ItemStack genesis = new ItemStack(ModGems.GENESIS_GEM);
            context.drawItem(genesis, cx, cy + 72);
            context.drawText(this.textRenderer,
                    Text.literal("Gema del Génesis: ").formatted(Formatting.AQUA, Formatting.BOLD)
                            .append(Text.literal("Rayos sagrados críticos | Armadura: +4 Absorción").formatted(Formatting.WHITE)),
                    cx + 20, cy + 72, 0xFFFFFFFF, false);

            ItemStack earth = new ItemStack(ModGems.EARTH_GEM);
            context.drawItem(earth, cx, cy + 92);
            context.drawText(this.textRenderer,
                    Text.literal("Gema de la Tierra: ").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal("+25% Minería, Prisa, doble drop | Suerte").formatted(Formatting.WHITE)),
                    cx + 20, cy + 92, 0xFFFFFFFF, false);

            // Tier slot breakdown box
            int slotBoxY = cy + 112;
            drawCustomBorder(context, cx, slotBoxY, 332, 18, 0xFF4A3E31);
            context.drawText(this.textRenderer, "• T10-39: 1 [◆] | T40-79: 2 [◆] | T80+: 3-4 [◆] | Exos / Élitros: 3-4 [◆]", cx + 6, slotBoxY + 5, 0xFFFFF1A8, false);

            checkTooltip(context, blood, cx, cy + 12, mouseX, mouseY);
            checkTooltip(context, voidGem, cx, cy + 32, mouseX, mouseY);
            checkTooltip(context, core, cx, cy + 52, mouseX, mouseY);
            checkTooltip(context, genesis, cx, cy + 72, mouseX, mouseY);
            checkTooltip(context, earth, cx, cy + 92, mouseX, mouseY);

        } else if (subPage == 1) {
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

        } else {
            // subPage 2: Fusión de Gemas T1-T3
            context.drawText(this.textRenderer, Text.literal("✦ FUSIÓN Y NIVELES DE GEMAS (TIER I-III) ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "⚒ FUSIÓN EN EL YUNQUE EN CADENA:", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "• Combina 2 gemas iguales de Tier I para forjar Tier II.", cx, cy + 24, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "• Combina 2 gemas de Tier II para forjar Tier III (Máx).", cx, cy + 34, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "💎 ESCALADO DE PODER DE TIER III:", cx, cy + 46, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "• Sangre III: Regen III letal. | Vacío III: Inmune caída.", cx, cy + 56, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "• Núcleo III: Inmune fuego, Vel II y Prisa II.", cx, cy + 66, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, "• Génesis III: ¡Hasta +10 Corazones Amarillos y Fuerza II!", cx, cy + 76, 0xFFFFF1A8, false);
            context.drawText(this.textRenderer, "• Tierra III: +60% minería, Prisa II, 50% doble mena y +4 HP.", cx, cy + 86, 0xFFA5D6A7, false);

            int dockY = cy + 102;
            drawCustomBorder(context, cx, dockY, 332, 28, 0xFF4A3E31);
            context.drawText(this.textRenderer, "Gemas Tier III:", cx + 6, dockY + 10, 0xFFAAAAAA, false);

            ItemStack b3 = new ItemStack(ModGems.BLOOD_GEM_3);
            ItemStack v3 = new ItemStack(ModGems.VOID_GEM_3);
            ItemStack c3 = new ItemStack(ModGems.CORE_GEM_3);
            ItemStack g3 = new ItemStack(ModGems.GENESIS_GEM_3);
            ItemStack e3 = new ItemStack(ModGems.EARTH_GEM_3);

            context.drawItem(b3, cx + 110, dockY + 5);
            context.drawItem(v3, cx + 135, dockY + 5);
            context.drawItem(c3, cx + 160, dockY + 5);
            context.drawItem(g3, cx + 185, dockY + 5);
            context.drawItem(e3, cx + 210, dockY + 5);

            checkTooltip(context, b3, cx + 110, dockY + 5, mouseX, mouseY);
            checkTooltip(context, v3, cx + 135, dockY + 5, mouseX, mouseY);
            checkTooltip(context, c3, cx + 160, dockY + 5, mouseX, mouseY);
            checkTooltip(context, g3, cx + 185, dockY + 5, mouseX, mouseY);
            checkTooltip(context, e3, cx + 210, dockY + 5, mouseX, mouseY);
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
