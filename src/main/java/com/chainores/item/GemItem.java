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
                "Extrae la fuerza vital de los caídos.",
                "4x Bloque de Redstone + Lingote de Netherite",
                "Guardián Abisal (Deep Dark)"),
        VOID("Gema del Vacío", Formatting.DARK_PURPLE, "Distorsión Gravitacional",
                new String[]{"+30% daño y suspensión de levitación", "+60% daño y vórtice de vacío", "+100% daño y colapso gravitatorio"},
                new String[]{"Antigravedad total (inmune a caída) y planeo", "Antigravedad + impulso aéreo al agacharse", "Vuelo celestial pasivo y planeo infinito"},
                "Encierra la nada del abismo cósmico.",
                "4x Ojo de Ender + Lingote de Netherite",
                "Coloso del Vacío (El End)"),
        CORE("Gema del Núcleo", Formatting.GOLD, "Fervor Ígneo Tectónico",
                new String[]{"+35% velocidad de ataque e ignición", "+70% velocidad de ataque e ignición solar", "+120% velocidad de ataque y quemadura cósmica"},
                new String[]{"Inmunidad a Fuego/Lava y Velocidad I", "Inmunidad Fuego/Lava, Velocidad II y Prisa I", "Inmunidad Fuego/Lava, Velocidad III y Prisa II"},
                "Forjada en las entrañas ardientes del planeta.",
                "4x Crema de Magma + Lingote de Netherite",
                "Behemoth del Nether"),
        GENESIS("Gema del Génesis", Formatting.LIGHT_PURPLE, "Creación Primordial",
                new String[]{"+50% daño sagrado y castigo de rayos", "+100% daño sagrado y tormenta de rayos", "+160% daño cósmico y aniquilación estelar"},
                new String[]{"Absorción celestial continua (+4♥) y retribución", "Absorción celestial continua (+8♥) y escudo divino", "Absorción celestial suprema (+14♥) e invulnerabilidad parcial"},
                "Fragmento condensado del inicio del universo.",
                "2x Estrella del Nether + 2x Tótem + Netherite",
                "Soberano Cósmico (Altar Cósmico)"),
        EARTH("Gema de la Tierra", Formatting.DARK_GREEN, "Prospección y Fortuna Geológica",
                new String[]{"+40% velocidad minera, Prisa I y 25% duplicar drops", "+80% velocidad minera, Prisa II y 50% duplicar drops", "+150% velocidad minera, Prisa III y 100% duplicar drops"},
                new String[]{"Suerte I y Visión Nocturna bajo Y=60", "Suerte II, Visión Nocturna y Héroe de la Aldea", "Suerte III, Visión Nocturna, Imán 8m y Detección de Vetas"},
                "Extraída de los estratos minerales más puros del mundo.",
                "4x Bloque de Esmeralda + Lingote de Netherite",
                "Ancestro de Sylva (Arboleda)"),
        ECHO("Gema del Eco", Formatting.DARK_AQUA, "Resonancia Sónica y Percepción Espectral",
                new String[]{"+30% vel. minera, pulso sónico y silencio de vibraciones", "+60% vel. minera, inmunidad a sensores Sculk y radar de vetas 16m", "+100% vel. minera, sonar de minerales 32m y explosión sónica al golpear"},
                new String[]{"Inmunidad total a Oscuridad y Ceguera subterránea", "Inmunidad a Oscuridad/Ceguera y sigilo absoluto ante el Warden", "Inmunidad total, sigilo espectral y pulso de ecolocalización continuo (24m)"},
                "Cristal resonante pulsando con las frecuencias ocultas de las profundidades del mundo.",
                "4x Fragmento de Eco + Lingote de Netherite",
                "Guardián Abisal (Deep Dark)"),
        MAGNET("Gema Magnética", Formatting.BLUE, "Atracción Gravitatoria y Fundición Polar",
                new String[]{"+35% vel. minera y drops teletransportados directo a tu inventario", "+70% vel. minera, drops a inventario y Auto-Fundición de metales en bruto", "+120% vel. minera, drops a inventario, Auto-Fundición y doble experiencia al minar"},
                new String[]{"Imán de ítems y orbes de experiencia en radio de 8 bloques", "Imán de ítems y orbes en 16 bloques + Campo deflector de proyectiles lentos", "Vórtice magnético total (24 bloques) + Campo de fuerza que desvía flechas y proyectiles"},
                "Canaliza las líneas de fuerza electromagnética de los núcleos planetarios.",
                "4x Bloque de Hierro + Lingote de Netherite",
                "Soberano Cósmico"),
        CHRONO("Gema Crono", Formatting.GOLD, "Dilatación Temporal y Auto-Reparación",
                new String[]{"+25% vel. minera/ataque y auto-repara la durabilidad del ítem cada 3s", "+50% vel. minera/ataque, auto-repara cada 1.5s y probabilidad de estasis temporal", "+75% vel. minera/ataque, auto-reparación continua (0.5s) y congelación temporal de enemigos"},
                new String[]{"Aceleración de crecimiento vegetal (3x3) y acelera fundición en hornos cercanos (2x)", "Aceleración vegetal (5x5), hornos (3x) y auto-repara armaduras equipadas pasivamente", "Aceleración vegetal (7x7), hornos (5x), auto-reparación total y Prisa/Velocidad temporal constante"},
                "Encapsula un fragmento detenido del tejido mismo del flujo del tiempo.",
                "2x Reloj + 2x Bloque de Amatista + Netherite",
                "Soberano Cósmico"),
        FROST("Gema Glacial", Formatting.AQUA, "Cero Absoluto Criogénico",
                new String[]{"+25% daño criogénico, congelación (5s) y Lentitud II", "+50% daño criogénico, congelación (8s) y fragmentación de hielo en área", "+100% daño criogénico, congelación instantánea (12s) y explosión glacial masiva"},
                new String[]{"Inmunidad al frío/nieve en polvo y efecto Caminante de Hielo", "Inmunidad al frío, Caminante de Hielo y contraataque que congela al atacante", "Inmunidad total, Caminante de Hielo y Aura de Ventisca continua en 6 bloques"},
                "Cristal polar eterno extraído del corazón de los glaciares.",
                "4x Hielo Azul + Lingote de Netherite",
                "Frostwarden (Santuario Glacial)"),
        STORM("Gema de la Tempestad", Formatting.YELLOW, "Descarga Voltaica en Cadena",
                new String[]{"+20% daño y arco eléctrico que salta a 2 enemigos cercanos", "+45% daño, arco eléctrico a 4 enemigos y chispas de aturdimiento", "+80% daño eléctrico, arco a 7 enemigos y llamada de relámpago celestial"},
                new String[]{"+15% velocidad de movimiento e inmunidad a daño por rayos", "+25% velocidad, Salto I y espinas eléctricas que repelen atacantes", "+40% velocidad, Salto II, inmunidad a rayos y aura de estática continua (4m)"},
                "Condensa la furia pura de una tormenta eléctrica primordial.",
                "2x Pararrayos + 2x Bloque de Redstone + Netherite",
                "Coloso del Vacío (El End)"),
        VENOM("Gema de Veneno", Formatting.DARK_GREEN, "Corrosión Ácida y Neurotoxina",
                new String[]{"Infecta con Veneno II (5s) y reduce la armadura del objetivo -20%", "Veneno III + Wither I (6s), funde armadura -40% y charco ácido", "Veneno IV + Wither II (8s), funde armadura -60% y plaga contagiosa"},
                new String[]{"Inmunidad total a Veneno y Hambre putrefacta", "Inmunidad a Veneno, Wither y efectos negativos en cuevas", "Caparazón Tóxico: Inmunidad total y nube de esporas venenosas al recibir daño"},
                "Gema viscosa destilada del veneno de las bestias más letales.",
                "2x Ojo de Araña Fermentado + 2x Bloque de Slime + Netherite",
                "Ancestro de Sylva (Arboleda)"),
        SOLAR("Gema Solar", Formatting.GOLD, "Purificación Radiante del Amanecer",
                new String[]{"+50% daño a No-Muertos e ilumina objetivos con Brillo radiante", "+100% daño a No-Muertos, Fuego Solar continuo y ceguera al objetivo", "+180% daño a No-Muertos, desintegración letal (<30% vida) y pulso solar"},
                new String[]{"Fotosíntesis: regenera hambre y saturación bajo la luz del sol", "Fotosíntesis + Regeneración I bajo el sol y Visión Nocturna en cuevas", "Avatar Solar: regeneración vital acelerada, aura que quema no-muertos cercanos"},
                "Fragmento tallado a partir de una corona solar pura.",
                "2x Sensor de Luz Solar + 2x Bloque de Oro + Netherite",
                "Behemoth del Nether");

        public final String displayName;
        public final Formatting color;
        public final String powerName;
        public final String[] weaponEffects;
        public final String[] armorEffects;
        public final String lore;
        public final String craftMaterial;
        public final String bossSource;

        GemType(String displayName, Formatting color, String powerName, String[] weaponEffects, String[] armorEffects, String lore, String craftMaterial, String bossSource) {
            this.displayName = displayName;
            this.color = color;
            this.powerName = powerName;
            this.weaponEffects = weaponEffects;
            this.armorEffects = armorEffects;
            this.lore = lore;
            this.craftMaterial = craftMaterial;
            this.bossSource = bossSource;
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
        textConsumer.accept(Text.literal("✦ CÓMO OBTENER:").formatted(Formatting.GOLD, Formatting.BOLD));
        if (tier == 1) {
            textConsumer.accept(Text.literal(" • Crafteo: " + gemType.craftMaterial).formatted(Formatting.YELLOW));
            textConsumer.accept(Text.literal(" • Drop de Jefe: " + gemType.bossSource).formatted(Formatting.LIGHT_PURPLE));
            textConsumer.accept(Text.literal(" • Fusión: Combina 2 en Yunque en Cadena para Rango II.").formatted(Formatting.DARK_GREEN));
        } else if (tier == 2) {
            textConsumer.accept(Text.literal(" • Fusión: Combina 2 " + gemType.displayName + " I en el Yunque en Cadena.").formatted(Formatting.AQUA));
            textConsumer.accept(Text.literal(" • Mejora: Combina 2 en Yunque en Cadena para Rango III.").formatted(Formatting.DARK_GREEN));
        } else {
            textConsumer.accept(Text.literal(" • Fusión Máxima: Combina 2 " + gemType.displayName + " II en el Yunque en Cadena.").formatted(Formatting.LIGHT_PURPLE));
            textConsumer.accept(Text.literal(" ★ Rango Máximo Trascendente Alcanzado").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("⚒ Cómo Engarzar:").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal("  Sostén tu ítem en mano principal y esta gema en offhand,").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal("  luego haz clic derecho en un Yunque en Cadena.").formatted(Formatting.DARK_AQUA));

        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("\"" + gemType.lore + "\"").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
