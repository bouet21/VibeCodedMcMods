package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.gear.ChainArmorItem;
import com.chainores.gear.ChainPickaxeItem;
import com.chainores.gear.ChainSwordItem;
import com.chainores.gear.GearTier;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import java.util.ArrayList;
import java.util.List;

public class ModGear {

    public static class GearSet {
        public final GearTier tierData;
        public final Item sword;
        public final Item pickaxe;
        public final Item helmet;
        public final Item chestplate;
        public final Item leggings;
        public final Item boots;

        public GearSet(GearTier tierData, Item sword, Item pickaxe, Item helmet, Item chestplate, Item leggings, Item boots) {
            this.tierData = tierData;
            this.sword = sword;
            this.pickaxe = pickaxe;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
        }
    }

    public static final List<GearTier> TIERS = new ArrayList<>();
    public static final List<GearSet> GEAR_SETS = new ArrayList<>();

    static {
        // 16 Progressive Gear Milestone Sets
        addTier(1, "copperite", "Copperite", "Cobreprita", 0xD97843, 350, 6.5f, 3.0f, 2, 5, 4, 1, 0.0f, 0.0f, "Armadura: Agilidad y -50% caída | Espada: Descarga Estática | Pico: Imán de ítems");
        addTier(10, "invar", "Invar", "Invar", 0x7E8582, 600, 7.5f, 4.0f, 2, 6, 5, 2, 0.5f, 0.0f, "Armadura: Resistencia I y Anti-retroceso | Espada: Impacto Contundente | Pico: Impacto acelerado");
        addTier(20, "absolute_zero", "Absolute Zero", "Cero Absoluto", 0x00E1D9, 1000, 8.5f, 5.0f, 3, 7, 5, 3, 1.0f, 0.0f, "Armadura: Anti-congelación y Prisa | Espada: Corte Glacial | Pico: Rompe-Hielo");
        addTier(30, "solarite", "Solarite", "Solarita", 0xFFCC00, 1500, 9.5f, 6.0f, 3, 8, 6, 3, 2.0f, 0.0f, "Armadura: Inmunidad Ígnea y Visión Nocturna | Espada: Llamarada Solar | Pico: Super-Fundición");
        addTier(40, "chlorophyte", "Chlorophyte", "Clorofita", 0x22E84D, 2000, 10.5f, 7.5f, 3, 8, 6, 3, 2.5f, 0.05f, "Armadura: Fotosíntesis viva y Fertilizante | Espada: Espora Vampírica | Pico: Tala en cadena");
        addTier(50, "runite", "Runite", "Runita", 0x24C7BA, 2500, 11.5f, 9.0f, 4, 9, 7, 4, 3.0f, 0.1f, "Armadura: Prisa II, Suerte y Orbes XP | Espada: Cercenador Arcano | Pico: Fortuna Rúnica");
        addTier(60, "coreium", "Coreium", "Mineral del Núcleo", 0xFF5522, 3200, 12.5f, 11.0f, 4, 9, 7, 4, 3.5f, 0.15f, "Armadura: Salto II y Anti-caída | Espada: Onda Tectónica AoE | Pico: Túnel 1x2");
        addTier(70, "primordial_sculk", "Primordial Sculk", "Sculk Primordial", 0x00FFD5, 4000, 14.0f, 13.0f, 5, 10, 8, 4, 4.0f, 0.2f, "Armadura: Absorción II continua y Sigilo Sculk | Espada: Aullido Sónico | Pico: Eco Sónico 3x3");
        addTier(80, "infernal_core", "Infernal Core", "Núcleo Infernal", 0xFF6B1A, 5000, 15.5f, 15.0f, 5, 10, 8, 5, 4.5f, 0.25f, "Armadura: Espinas Ígneas y Resistencia II | Espada: Fuego del Averno | Pico: Extinción de Lava");
        addTier(90, "nether_soul_gem", "Soul Gem", "Gema del Purgatorio", 0x16A694, 6000, 17.0f, 18.0f, 5, 11, 8, 5, 5.0f, 0.3f, "Armadura: Velocidad II y Purga de Maldiciones | Espada: Cosecha de Almas | Pico: Doble XP");
        addTier(100, "nether_heart", "Nether Heart", "Corazón del Nether", 0xD61142, 7500, 19.0f, 21.0f, 6, 12, 9, 5, 5.5f, 0.35f, "Armadura: +6 Corazones y Furia Berserker | Espada: Desangrado Carmesí | Pico: Martillo Coloso 3x3");
        addTier(115, "dragon_breath_gem", "Dragon Breath", "Aliento de Dragón", 0xDE3CE3, 9000, 22.0f, 25.0f, 6, 12, 9, 6, 6.0f, 0.4f, "Armadura: Salto III y Planeo Dracónico | Espada: Vuelo Dracónico | Pico: Vínculo de Ender");
        addTier(125, "galacticite", "Galacticite", "Galactita", 0x483296, 11000, 25.0f, 30.0f, 7, 13, 10, 6, 7.0f, 0.45f, "Armadura: Escudo Gravitacional y Desvío de Flechas | Espada: Rayo Orbital | Pico: Agujero Negro 3x3");
        addTier(135, "infinity_shard_ore", "Infinity Shard", "Fragmento Infinito", 0xEB5CE8, 14000, 30.0f, 40.0f, 7, 14, 10, 7, 8.0f, 0.5f, "Armadura: Bucle Temporal y Rescate del Vacío | Espada: Falla Temporal | Pico: Minería 5x5");
        addTier(140, "genesis_ore", "Genesis", "Génesis", 0xE8F768, 18000, 35.0f, 55.0f, 8, 15, 11, 7, 9.0f, 0.55f, "Armadura: Árbol de la Vida (+10 Corazones, Sin Hambre) | Espada: Resonancia Génesis | Pico: Transmutación");
        addTier(150, "godmetal", "Godmetal", "Metal de los Dioses", 0xFFE885, 30000, 50.0f, 100.0f, 10, 18, 14, 8, 12.0f, 0.6f, "★ ASCENSIÓN DIVINA ★: +14 Corazones, Modo Dios, Rayos, Juicio Letal, Rescate del Vacío y Minería 5x5");
    }

    private static void addTier(int tier, String id, String en, String es, int color, int dur, float speed, float swordDmg, int h, int c, int l, int b, float tough, float kb, String perk) {
        TIERS.add(new GearTier(tier, id, en, es, color, dur, speed, swordDmg, h, c, l, b, tough, kb, perk));
    }

    public static void initialize() {
        for (GearTier tier : TIERS) {
            ToolMaterial toolMat = tier.createToolMaterial();
            ArmorMaterial armorMat = tier.createArmorMaterial();

            // 1. Sword
            RegistryKey<Item> swordKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("sword_tier_" + tier.tier()));
            Item sword = Registry.register(
                    Registries.ITEM,
                    swordKey,
                    new ChainSwordItem(toolMat, tier.tier(), tier.swordDamage(), new Item.Settings().registryKey(swordKey).fireproof())
            );

            // 2. Pickaxe
            RegistryKey<Item> pickaxeKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("pickaxe_tier_" + tier.tier()));
            Item pickaxe = Registry.register(
                    Registries.ITEM,
                    pickaxeKey,
                    new ChainPickaxeItem(toolMat, tier.tier(), 1.5f, new Item.Settings().registryKey(pickaxeKey).fireproof())
            );

            // 3. Helmet
            RegistryKey<Item> helmetKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("helmet_tier_" + tier.tier()));
            Item helmet = Registry.register(
                    Registries.ITEM,
                    helmetKey,
                    new ChainArmorItem(armorMat, EquipmentType.HELMET, tier.tier(), new Item.Settings().registryKey(helmetKey).fireproof())
            );

            // 4. Chestplate
            RegistryKey<Item> chestKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chestplate_tier_" + tier.tier()));
            Item chestplate = Registry.register(
                    Registries.ITEM,
                    chestKey,
                    new ChainArmorItem(armorMat, EquipmentType.CHESTPLATE, tier.tier(), new Item.Settings().registryKey(chestKey).fireproof())
            );

            // 5. Leggings
            RegistryKey<Item> legsKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("leggings_tier_" + tier.tier()));
            Item leggings = Registry.register(
                    Registries.ITEM,
                    legsKey,
                    new ChainArmorItem(armorMat, EquipmentType.LEGGINGS, tier.tier(), new Item.Settings().registryKey(legsKey).fireproof())
            );

            // 6. Boots
            RegistryKey<Item> bootsKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("boots_tier_" + tier.tier()));
            Item boots = Registry.register(
                    Registries.ITEM,
                    bootsKey,
                    new ChainArmorItem(armorMat, EquipmentType.BOOTS, tier.tier(), new Item.Settings().registryKey(bootsKey).fireproof())
            );

            GEAR_SETS.add(new GearSet(tier, sword, pickaxe, helmet, chestplate, leggings, boots));
        }

        ChainOresMod.LOGGER.info("Registered 96 progressive weapons, tools, and armor items for Chain Ores.");
    }
}
