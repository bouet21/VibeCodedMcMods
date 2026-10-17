package com.chainores.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.Consumer;

public class GemItem extends Item {
    public enum GemType {
        BLOOD("Gema Carmesí", Formatting.DARK_RED, "Robo de Vida Carmesí",
                new String[]{"+15% del daño curado como vida", "+30% del daño curado como vida", "+50% del daño curado como vida"},
                new String[]{"Regeneración vital continua (+0.5 HP/s)", "Regeneración vital reforzada (+1.2 HP/s)", "Regeneración vital suprema (+2.5 HP/s)"},
                "Extrae la fuerza vital de los caídos."),
        VOID("Gema del Vacío", Formatting.DARK_PURPLE, "Distorsión Gravitacional",
                new String[]{"+30% daño y suspensión de levitación", "+60% daño y vórtice de vacío", "+100% daño y colapso gravitatorio"},
                new String[]{"Antigravedad total (inmune a caída) y planeo", "Antigravedad + impulso aéreo al agacharse", "Vuelo celestial pasivo y planeo infinito"},
                "Encierra la nada del abismo cósmico."),
        CORE("Gema del Núcleo", Formatting.GOLD, "Fervor Ígneo Tectónico",
                new String[]{"+35% velocidad de ataque e ignición", "+70% velocidad de ataque e ignición solar", "+120% velocidad de ataque y quemadura cósmica"},
                new String[]{"Inmunidad a Fuego/Lava y Velocidad I", "Inmunidad Fuego/Lava, Velocidad II y Prisa I", "Inmunidad Fuego/Lava, Velocidad III y Prisa II"},
                "Forjada en las entrañas ardientes del planeta."),
        GENESIS("Gema del Génesis", Formatting.LIGHT_PURPLE, "Creación Primordial",
                new String[]{"+50% daño sagrado y castigo de rayos", "+100% daño sagrado y tormenta de rayos", "+160% daño cósmico y aniquilación estelar"},
                new String[]{"Absorción celestial continua (+4♥) y retribución", "Absorción celestial continua (+8♥) y escudo divino", "Absorción celestial suprema (+14♥) e invulnerabilidad parcial"},
                "Fragmento condensado del inicio del universo."),
        EARTH("Gema de la Tierra", Formatting.DARK_GREEN, "Prospección y Fortuna Geológica",
                new String[]{"+40% velocidad minera, Prisa I y 25% duplicar drops", "+80% velocidad minera, Prisa II y 50% duplicar drops", "+150% velocidad minera, Prisa III y 100% duplicar drops"},
                new String[]{"Suerte I y Visión Nocturna bajo Y=60", "Suerte II, Visión Nocturna y Héroe de la Aldea", "Suerte III, Visión Nocturna, Imán 8m y Detección de Vetas"},
                "Extraída de los estratos minerales más puros del mundo.");

        public final String displayName;
        public final Formatting color;
        public final String powerName;
        public final String[] weaponEffects;
        public final String[] armorEffects;
        public final String lore;

        GemType(String displayName, Formatting color, String powerName, String[] weaponEffects, String[] armorEffects, String lore) {
            this.displayName = displayName;
            this.color = color;
            this.powerName = powerName;
            this.weaponEffects = weaponEffects;
            this.armorEffects = armorEffects;
            this.lore = lore;
        }
    }

    private final GemType gemType;
    private final int tier;

    public GemItem(GemType gemType, Settings settings) {
        this(gemType, 1, settings);
    }

    public GemItem(GemType gemType, int tier, Settings settings) {
        super(settings);
        this.gemType = gemType;
        this.tier = Math.clamp(tier, 1, 3);
    }

    public GemType getGemType() {
        return gemType;
    }

    public int getTier() {
        return tier;
    }

    public String getRomanNumeral() {
        return switch (tier) {
            case 2 -> "II";
            case 3 -> "III";
            default -> "I";
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);

        int idx = Math.clamp(tier - 1, 0, 2);
        Formatting tierColor = switch (tier) {
            case 2 -> Formatting.AQUA;
            case 3 -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };

        textConsumer.accept(Text.literal("✦ " + gemType.displayName + " ")
                .formatted(gemType.color, Formatting.BOLD)
                .append(Text.literal(getRomanNumeral()).formatted(tierColor, Formatting.BOLD))
                .append(Text.literal(" ✦").formatted(gemType.color, Formatting.BOLD)));

        textConsumer.accept(Text.literal("Poder: " + gemType.powerName).formatted(Formatting.YELLOW, Formatting.ITALIC));
        textConsumer.accept(Text.empty());

        textConsumer.accept(Text.literal("⚔ Efecto en Armas/Herramientas:").formatted(Formatting.RED, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • " + gemType.weaponEffects[idx]).formatted(Formatting.GRAY));

        textConsumer.accept(Text.literal("🛡 Efecto en Armaduras/Accesorios:").formatted(Formatting.AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • " + gemType.armorEffects[idx]).formatted(Formatting.GRAY));

        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("⚒ Cómo Engarzar:").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal("  Sostén tu ítem en mano principal y esta gema en offhand,").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal("  luego haz clic derecho en un Yunque en Cadena.").formatted(Formatting.DARK_AQUA));

        if (tier < 3) {
            textConsumer.accept(Text.literal("★ Fusión: Combina 2 de estas gemas en el Yunque para crear Rango " + (tier == 1 ? "II" : "III") + "!")
                    .formatted(Formatting.GREEN, Formatting.BOLD));
        } else {
            textConsumer.accept(Text.literal("★ Rango Máximo Trascendente").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("\"" + gemType.lore + "\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
