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
        int panelWidth = 340;
        int panelHeight = 230;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        this.clearChildren();

        // 1. Navigation Tab Buttons at the very top
        int tabW = 60;
        int tabSpacing = 4;
        int tabStartX = x + 10;
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

        // Close Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), button -> this.close())
                .dimensions(x + panelWidth - 22, y + 6, 16, 16).build());

        // Tab-specific controls
        if (currentTab == Tab.MINERALS) {
            // Interactive Search Field
            int searchX = x + 242;
            int searchY = y + 28;
            this.searchField = new TextFieldWidget(this.textRenderer, searchX, searchY, 86, 13, Text.literal("Buscar"));
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
            addStageJumpButton(x + 10, jumpY, 28, "T1", 0);
            addStageJumpButton(x + 40, jumpY, 28, "T11", 10);
            addStageJumpButton(x + 70, jumpY, 28, "T21", 20);
            addStageJumpButton(x + 100, jumpY, 28, "T31", 30);
            addStageJumpButton(x + 130, jumpY, 28, "T41", 40);
            addStageJumpButton(x + 160, jumpY, 28, "T61", 60);
            addStageJumpButton(x + 190, jumpY, 28, "T71", 70);
            addStageJumpButton(x + 220, jumpY, 28, "T91", 90);
            addStageJumpButton(x + 250, jumpY, 34, "T106", 105);
            addStageJumpButton(x + 286, jumpY, 34, "T136", 135);
        } else if (currentTab == Tab.ARSENAL || currentTab == Tab.BOSSES) {
            // Sub-page controls for content-rich tabs
            this.addDrawableChild(ButtonWidget.builder(Text.literal("< Pág Ant."), button -> {
                if (subPage > 0) {
                    subPage--;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + 10, y + panelHeight - 22, 75, 16).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Pág Sig. >"), button -> {
                if (subPage < 1) {
                    subPage++;
                    playPageTurnSound();
                    init();
                }
            }).dimensions(x + panelWidth - 85, y + panelHeight - 22, 75, 16).build());
        }
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
            if (input.key() != 256) { // 256 = GLFW_KEY_ESCAPE
                return true; // Prevent inventory key ('E') from prematurely closing guide codex
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = 340;
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

        // Subheader Info
        String pageNumText = "Tier " + (pageIndex + 1) + " / " + totalPages;
        context.drawText(this.textRenderer, Text.literal(pageNumText).formatted(Formatting.YELLOW),
                x + 115, y + 30, 0xFFFFF1A8, false);

        // 1. Left Showcase: Ore, Raw, Ingot, Block
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

        // 2. Right Column: Detailed Location and Crafting Info
        int rightX = x + 115;
        int rightW = 215;

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

        // Smithing Upgrade Recipe Box
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

        // Check Tooltips
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

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ ARSENAL COMPLETO Y ARMAS ESPECIALES ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            context.drawText(this.textRenderer, "• Hachas de Tala (TreeCapitator):", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Talan bosques completos al agacharse (hasta 256 bloques en T150).", cx, cy + 24, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Palas de Excavación 3x3 y Azadas de Cultivo 9x9:", cx, cy + 38, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Nivelan áreas instantáneamente y aran/hidratan campos masivos.", cx, cy + 48, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Arco del Aliento de Dragón (T115):", cx, cy + 62, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "  Flechas explosivas teledirigidas con fuego cósmico persistente.", cx, cy + 72, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Ballesta Galáctica (T125) y Escudo Égida (T100):", cx, cy + 86, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  Multidisparo estelar quíntuple y bloqueo con aturdimiento.", cx, cy + 96, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ HERRAMIENTA UNIVERSAL DE LOS DIOSES (T150):", cx, cy + 112, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  Combina pico 5x5, hacha, pala y azada. 120 daño sagrado y rayo.", cx, cy + 122, 0xFFFFFF55, false);

            // Icon Showcase with Tooltips
            ItemStack bow = new ItemStack(ModGear.DRAGON_BOW);
            ItemStack xbow = new ItemStack(ModGear.GALACTIC_CROSSBOW);
            ItemStack shield = new ItemStack(ModGear.AEGIS_SHIELD);
            ItemStack omni = new ItemStack(ModGear.GOD_OMNITOOL);

            context.drawItem(bow, cx + 230, cy + 58);
            context.drawItem(xbow, cx + 252, cy + 58);
            context.drawItem(shield, cx + 274, cy + 58);
            context.drawItem(omni, cx + 296, cy + 58);

            checkTooltip(context, bow, cx + 230, cy + 58, mouseX, mouseY);
            checkTooltip(context, xbow, cx + 252, cy + 58, mouseX, mouseY);
            checkTooltip(context, shield, cx + 274, cy + 58, mouseX, mouseY);
            checkTooltip(context, omni, cx + 296, cy + 58, mouseX, mouseY);
        } else {
            context.drawText(this.textRenderer, Text.literal("✦ SISTEMA DE SETS DE ARMADURA Y DASH ✦").formatted(Formatting.AQUA, Formatting.BOLD),
                    cx, cy, 0xFF55FFFF, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (2/4 Piezas):", cx, cy + 14, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "  Otorga movilidad, prisa, visión nocturna y resistencia basal.", cx, cy + 24, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "• Bonus Parcial (3/4 Piezas):", cx, cy + 40, 0xFFFFAA00, false);
            context.drawText(this.textRenderer, "  Inmunidad a daño de caída, fuego, purga de venenos y suerte.", cx, cy + 50, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "★ Set Completo (4/4 Piezas):", cx, cy + 68, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "  Habilidad suprema: Manto del Warden (T70), Rescate del Vacío (T135),", cx, cy + 78, 0xFFE0E0E0, false);
            context.drawText(this.textRenderer, "  y Ascensión Divina (T150) con corazones masivos y omnipotencia.", cx, cy + 88, 0xFFE0E0E0, false);

            context.drawText(this.textRenderer, "⚡ Habilidad Activa Universal (Dash Impulso):", cx, cy + 104, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "  Esprintar + Agacharse ejecuta un impulso sónico en el aire (CD: 4s).", cx, cy + 114, 0xFFFFF1A8, false);

            // Armor Showcase with Tooltips
            ItemStack h = new ItemStack(ModGear.GEAR_SETS.get(0).helmet);
            ItemStack c = new ItemStack(ModGear.GEAR_SETS.get(6).chestplate);
            ItemStack l = new ItemStack(ModGear.GEAR_SETS.get(9).leggings);
            ItemStack b = new ItemStack(ModGear.GEAR_SETS.get(ModGear.GEAR_SETS.size() - 1).boots);

            context.drawItem(h, cx + 230, cy + 58);
            context.drawItem(c, cx + 252, cy + 58);
            context.drawItem(l, cx + 274, cy + 58);
            context.drawItem(b, cx + 296, cy + 58);

            checkTooltip(context, h, cx + 230, cy + 58, mouseX, mouseY);
            checkTooltip(context, c, cx + 252, cy + 58, mouseX, mouseY);
            checkTooltip(context, l, cx + 274, cy + 58, mouseX, mouseY);
            checkTooltip(context, b, cx + 296, cy + 58, mouseX, mouseY);
        }
    }

    private void renderWorkstationsTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        context.drawText(this.textRenderer, Text.literal("✦ ESTACIONES DE TRABAJO ESPECIALIZADAS ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                cx, cy, 0xFFFFD700, false);

        ItemStack forge = new ItemStack(ModWorkstations.CHAIN_FORGE_ITEM);
        context.drawItem(forge, cx, cy + 14);
        context.drawText(this.textRenderer, "Forja en Cadena (Chain Forge):", cx + 22, cy + 14, 0xFFFFCC00, false);
        context.drawText(this.textRenderer, "Mesa de forja con interfaz GUI y combustible de calor.", cx + 22, cy + 24, 0xFFE0E0E0, false);
        context.drawText(this.textRenderer, "Mejora equipamiento nivel a nivel y requiere Catalizadores de Jefes.", cx + 22, cy + 34, 0xFFAAAAAA, false);

        ItemStack anvil = new ItemStack(ModWorkstations.CHAIN_ANVIL_ITEM);
        context.drawItem(anvil, cx, cy + 50);
        context.drawText(this.textRenderer, "Yunque en Cadena (Indestructible):", cx + 22, cy + 50, 0xFF55FF55, false);
        context.drawText(this.textRenderer, "¡Nunca se rompe! Repara ítems al 100% sin penalización de XP.", cx + 22, cy + 60, 0xFFE0E0E0, false);
        context.drawText(this.textRenderer, "Templado (+50 durabilidad), engarce de gemas y reforjado.", cx + 22, cy + 70, 0xFFAAAAAA, false);

        ItemStack smelter = new ItemStack(ModWorkstations.COSMIC_SMELTER_ITEM);
        context.drawItem(smelter, cx, cy + 86);
        context.drawText(this.textRenderer, "Fundidor Cósmico (Cosmic Smelter):", cx + 22, cy + 86, 0xFFFF55FF, false);
        context.drawText(this.textRenderer, "Cuadruplica (x4) los lingotes producidos por cada mineral crudo.", cx + 22, cy + 96, 0xFFE0E0E0, false);
        context.drawText(this.textRenderer, "35% de probabilidad de transmutar al tier inmediatamente superior.", cx + 22, cy + 106, 0xFFFFFF55, false);

        checkTooltip(context, forge, cx, cy + 14, mouseX, mouseY);
        checkTooltip(context, anvil, cx, cy + 50, mouseX, mouseY);
        checkTooltip(context, smelter, cx, cy + 86, mouseX, mouseY);
    }

    private void renderBossesTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        if (subPage == 0) {
            context.drawText(this.textRenderer, Text.literal("✦ JEFES SUPREMOS DE DIMENSIÓN ✦").formatted(Formatting.RED, Formatting.BOLD),
                    cx, cy, 0xFFFF5555, false);

            ItemStack eye = new ItemStack(ModBossItems.ABYSSAL_EYE);
            context.drawItem(eye, cx, cy + 14);
            context.drawText(this.textRenderer, "1. Guardián del Abismo (Tier 70 - Deep Dark):", cx + 22, cy + 14, 0xFF55FFFF, false);
            context.drawText(this.textRenderer, "Invócalo con el Ojo del Abismo. Dropea el Catalizador del Abismo.", cx + 22, cy + 24, 0xFFE0E0E0, false);

            ItemStack heart = new ItemStack(ModBossItems.FLAME_HEART);
            context.drawItem(heart, cx, cy + 42);
            context.drawText(this.textRenderer, "2. Behemoth del Nether (Tier 105 - Nether):", cx + 22, cy + 42, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "Invócalo con el Corazón Ígneo. Dropea el Núcleo del Nether.", cx + 22, cy + 52, 0xFFE0E0E0, false);

            ItemStack beacon = new ItemStack(ModBossItems.VOID_BEACON);
            context.drawItem(beacon, cx, cy + 70);
            context.drawText(this.textRenderer, "3. Coloso del Vacío (Tier 135 - The End):", cx + 22, cy + 70, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "Invócalo con la Baliza del Vacío. Dropea el Fragmento del Vacío.", cx + 22, cy + 80, 0xFFE0E0E0, false);

            ItemStack core = new ItemStack(ModBossItems.COSMIC_ALTAR_CORE);
            context.drawItem(core, cx, cy + 98);
            context.drawText(this.textRenderer, "4. Soberano Cósmico (Tier 150 - Dimensión Cósmica):", cx + 22, cy + 98, 0xFFFFD700, false);
            context.drawText(this.textRenderer, "Jefe final invocado con el Altar Cósmico. Dropea la Singularidad.", cx + 22, cy + 108, 0xFFFFF1A8, false);

            checkTooltip(context, eye, cx, cy + 14, mouseX, mouseY);
            checkTooltip(context, heart, cx, cy + 42, mouseX, mouseY);
            checkTooltip(context, beacon, cx, cy + 70, mouseX, mouseY);
            checkTooltip(context, core, cx, cy + 98, mouseX, mouseY);
        } else {
            context.drawText(this.textRenderer, Text.literal("✦ ESTRUCTURAS EN EL MUNDO ✦").formatted(Formatting.GOLD, Formatting.BOLD),
                    cx, cy, 0xFFFFD700, false);

            ItemStack cat = new ItemStack(ModBossItems.ABYSSAL_CATALYST);
            context.drawItem(cat, cx, cy + 16);
            context.drawText(this.textRenderer, "Forjas Abandonadas (Overworld):", cx + 22, cy + 16, 0xFF55FF55, false);
            context.drawText(this.textRenderer, "Ruinas subterráneas con Yunque en Cadena y plantillas de herrería.", cx + 22, cy + 26, 0xFFE0E0E0, false);

            ItemStack ncore = new ItemStack(ModBossItems.NETHER_CORE);
            context.drawItem(ncore, cx, cy + 46);
            context.drawText(this.textRenderer, "Cámaras Selladas de Netherita (Nether):", cx + 22, cy + 46, 0xFFFF5555, false);
            context.drawText(this.textRenderer, "Fortalezas protegidas por lava con minerales de tier Nether.", cx + 22, cy + 56, 0xFFE0E0E0, false);

            ItemStack shard = new ItemStack(ModBossItems.VOID_SHARD);
            context.drawItem(shard, cx, cy + 76);
            context.drawText(this.textRenderer, "Templos Cósmicos (The End):", cx + 22, cy + 76, 0xFFFF55FF, false);
            context.drawText(this.textRenderer, "Torres flotantes con cofres de lingotes cósmicos y altares arcanos.", cx + 22, cy + 86, 0xFFE0E0E0, false);

            ItemStack trophy = new ItemStack(ModBossItems.COSMIC_TROPHY);
            context.drawItem(trophy, cx + 295, cy + 104);
            context.drawText(this.textRenderer, "ℹ Estructuras con loot exclusivo de progresión y trofeos.", cx, cy + 110, 0xFFFFAA00, false);

            checkTooltip(context, cat, cx, cy + 16, mouseX, mouseY);
            checkTooltip(context, ncore, cx, cy + 46, mouseX, mouseY);
            checkTooltip(context, shard, cx, cy + 76, mouseX, mouseY);
            checkTooltip(context, trophy, cx + 295, cy + 104, mouseX, mouseY);
        }
    }

    private void renderModifiersTab(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int cx = x + 14;
        int cy = y + 32;

        context.drawText(this.textRenderer, Text.literal("✦ GEMAS, REFORJADO Y ENCANTAMIENTOS ✦").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD),
                cx, cy, 0xFFFF77FF, false);

        ItemStack blood = new ItemStack(ModGems.BLOOD_GEM);
        context.drawItem(blood, cx, cy + 14);
        context.drawText(this.textRenderer, "Gema de Sangre: Robo de Vida (+2.5♥) y Regeneración pasiva.", cx + 20, cy + 18, 0xFFFF5555, false);

        ItemStack voidGem = new ItemStack(ModGems.VOID_GEM);
        context.drawItem(voidGem, cx, cy + 34);
        context.drawText(this.textRenderer, "Gema del Vacío: Levitación enemiga y Antigravedad al caer.", cx + 20, cy + 38, 0xFFFF55FF, false);

        ItemStack core = new ItemStack(ModGems.CORE_GEM);
        context.drawItem(core, cx, cy + 54);
        context.drawText(this.textRenderer, "Gema del Núcleo: +35% Prisa minera extrema y Velocidad I.", cx + 20, cy + 58, 0xFFFFAA00, false);

        ItemStack genesis = new ItemStack(ModGems.GENESIS_GEM);
        context.drawItem(genesis, cx, cy + 74);
        context.drawText(this.textRenderer, "Gema del Génesis: Rayo celestial, escudo de Absorción y +50% poder.", cx + 20, cy + 78, 0xFF55FFFF, false);

        // Reforge Prefixes info with Amethyst Shard icon
        ItemStack amethyst = new ItemStack(Items.AMETHYST_SHARD);
        context.drawItem(amethyst, cx + 285, cy + 96);
        context.drawText(this.textRenderer, "⚡ Prefijos en Yunque (con Amatista):", cx, cy + 98, 0xFFFFD700, false);
        context.drawText(this.textRenderer, "[DIVINO] (+50%), [LEGENDARIO] (+25%), [FEROZ], [VELOZ], [TITÁNICO].", cx, cy + 108, 0xFFFFF1A8, false);

        // Enchantments
        context.drawText(this.textRenderer, "✦ Encantamientos: Desintegración en Cadena, Drenaje del Vacío, Radiancia.", cx, cy + 122, 0xFF88FF88, false);

        checkTooltip(context, blood, cx, cy + 14, mouseX, mouseY);
        checkTooltip(context, voidGem, cx, cy + 34, mouseX, mouseY);
        checkTooltip(context, core, cx, cy + 54, mouseX, mouseY);
        checkTooltip(context, genesis, cx, cy + 74, mouseX, mouseY);
        checkTooltip(context, amethyst, cx + 285, cy + 96, mouseX, mouseY);
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
