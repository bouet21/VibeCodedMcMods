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
        BLOOD("Gema Carmesí", Formatting.DARK_RED, "Robo de Vida Carmesí", "+15% del daño infligido curado como vitalidad", "Regeneración vital continua (+0.5 HP/s)", "Extrae la fuerza vital de los caídos."),
        VOID("Gema del Vacío", Formatting.DARK_PURPLE, "Distorsión Gravitacional", "+30% daño y suspensión por levitación aérea", "Antigravedad total (inmune a caída) y planeo", "Encierra la nada del abismo cósmico."),
        CORE("Gema del Núcleo", Formatting.GOLD, "Fervor Ígneo Tectónico", "+35% velocidad de ataque y fuego abrasador", "Inmunidad a Fuego/Lava y Velocidad I (+20%)", "Forjada en las entrañas ardientes del planeta."),
        GENESIS("Gema del Génesis", Formatting.LIGHT_PURPLE, "Creación Primordial", "+50% daño sagrado y castigo de rayos", "Absorción celestial continua (+4♥) y retribución", "Fragmento condensado del inicio del universo.");

        public final String displayName;
        public final Formatting color;
        public final String powerName;
        public final String weaponEffect;
        public final String armorEffect;
        public final String lore;

        GemType(String displayName, Formatting color, String powerName, String weaponEffect, String armorEffect, String lore) {
            this.displayName = displayName;
            this.color = color;
            this.powerName = powerName;
            this.weaponEffect = weaponEffect;
            this.armorEffect = armorEffect;
            this.lore = lore;
        }
    }

    private final GemType gemType;

    public GemItem(GemType gemType, Settings settings) {
        super(settings);
        this.gemType = gemType;
    }

    public GemType getGemType() {
        return gemType;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);

        textConsumer.accept(Text.literal("✦ " + gemType.displayName + " ✦").formatted(gemType.color, Formatting.BOLD));
        textConsumer.accept(Text.literal("Poder: " + gemType.powerName).formatted(Formatting.YELLOW, Formatting.ITALIC));
        textConsumer.accept(Text.literal(""));

        textConsumer.accept(Text.literal("⚔ Efecto en Armas/Herramientas:").formatted(Formatting.RED, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • " + gemType.weaponEffect).formatted(Formatting.GRAY));

        textConsumer.accept(Text.literal("🛡 Efecto en Armaduras:").formatted(Formatting.AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • " + gemType.armorEffect).formatted(Formatting.GRAY));

        textConsumer.accept(Text.literal(""));
        textConsumer.accept(Text.literal("⚒ Cómo Engarzar:").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal("  Sostén tu arma/armadura en mano principal y esta gema").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal("  en mano secundaria, luego haz clic derecho en un Yunque en Cadena.").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal(""));
        textConsumer.accept(Text.literal("\"" + gemType.lore + "\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
