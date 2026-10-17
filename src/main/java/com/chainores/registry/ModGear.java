package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.gear.ChainArmorItem;
import com.chainores.gear.ChainAxeItem;
import com.chainores.gear.ChainHoeItem;
import com.chainores.gear.ChainPickaxeItem;
import com.chainores.gear.ChainShieldItem;
import com.chainores.gear.SentinelShieldItem;
import com.chainores.gear.FrostguardShieldItem;
import com.chainores.gear.AbyssalAegisItem;
import com.chainores.gear.InfernalBulwarkItem;
import com.chainores.gear.CosmicShieldItem;
import com.chainores.gear.ChainShovelItem;
import com.chainores.gear.ChainSwordItem;
import com.chainores.gear.DragonBreathBowItem;
import com.chainores.gear.GalacticCrossbowItem;
import com.chainores.gear.GearTier;
import com.chainores.gear.GodOmnitoolItem;
import com.chainores.gear.MiningExoskeletonItem;
import com.chainores.gear.LumberjackHauberkItem;
import com.chainores.gear.CosmicElytraChestplateItem;
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
        public final Item axe;
        public final Item shovel;
        public final Item hoe;
        public final Item helmet;
        public final Item chestplate;
        public final Item leggings;
        public final Item boots;

        public GearSet(GearTier tierData, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe, Item helmet, Item chestplate, Item leggings, Item boots) {
            this.tierData = tierData;
            this.sword = sword;
            this.pickaxe = pickaxe;
            this.axe = axe;
            this.shovel = shovel;
            this.hoe = hoe;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
        }
    }

    public static final List<GearTier> TIERS = new ArrayList<>();
    public static final List<GearSet> GEAR_SETS = new ArrayList<>();

    // Legendary Thematic Ranged & Special Weapons
    public static Item DRAGON_BOW;
    public static Item GALACTIC_CROSSBOW;
    public static Item AEGIS_SHIELD;
    public static Item SENTINEL_SHIELD;
    public static Item FROSTGUARD_SHIELD;
    public static Item ABYSSAL_AEGIS;
    public static Item INFERNAL_BULWARK;
    public static Item COSMIC_SHIELD;
    public static Item GOD_OMNITOOL;

    // Specialized Utility Armors
    public static Item MINING_EXOSKELETON;
    public static Item LUMBERJACK_HAUBERK;
    public static Item COSMIC_ELYTRA_CHESTPLATE;

    static {
        // 16 Progressive Gear Milestone Sets
        addTier(1, "copperite", "Copperite", "Cobreprita", 0xD97843, 350, 6.5f, 3.0f, 2, 5, 4, 1, 0.0f, 0.0f, "Armadura: Agilidad y -50% caída | Espada: Descarga Estática | Pico: Imán de ítems");
        addTier(10, "invar", "Invar", "Invar", 0x7E8582, 600, 7.5f, 4.0f, 2, 6, 5, 2, 0.5f, 0.0f, "Armadura: Resistencia I y Anti-retroceso | Espada: Impacto Contundente | Pico: Impacto acelerado");
        addTier(20, "absolute_zero", "Absolute Zero", "Cero Absoluto", 0x00E1D9, 1000, 8.5f, 5.0f, 3, 7, 5, 3, 1.0f, 0.0f, "Armadura: Anti-congelación y Prisa | Espada: Corte Glacial | Pico: Rompe-Hielo");
        addTier(30, "solarite", "Solarite", "Solarita", 0xFFCC00, 1500, 9.5f, 6.0f, 3, 8, 6, 3, 2.0f, 0.0f, "Armadura: Inmunidad Ígnea y Visión Nocturna | Espada: Llamarada Solar | Pico: Super-Fundición");
        addTier(40, "chlorophyte", "Chlorophyte", "Clorofita", 0x22E84D, 2000, 10.5f, 7.5f, 3, 8, 6, 3, 2.5f, 0.05f, "Armadura: Fotosíntesis viva y Fertilizante | Espada: Espora Vampírica | Hacha: TreeCapitator");
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

            // 3. Axe
            RegistryKey<Item> axeKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("axe_tier_" + tier.tier()));
            Item axe = Registry.register(
                    Registries.ITEM,
                    axeKey,
                    new ChainAxeItem(toolMat, tier.tier(), 6.0f, new Item.Settings().registryKey(axeKey).fireproof())
            );

            // 4. Shovel
            RegistryKey<Item> shovelKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("shovel_tier_" + tier.tier()));
            Item shovel = Registry.register(
                    Registries.ITEM,
                    shovelKey,
                    new ChainShovelItem(toolMat, tier.tier(), 1.5f, new Item.Settings().registryKey(shovelKey).fireproof())
            );

            // 5. Hoe
            RegistryKey<Item> hoeKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("hoe_tier_" + tier.tier()));
            Item hoe = Registry.register(
                    Registries.ITEM,
                    hoeKey,
                    new ChainHoeItem(toolMat, tier.tier(), 0.0f, new Item.Settings().registryKey(hoeKey).fireproof())
            );

            // 6. Helmet
            RegistryKey<Item> helmetKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("helmet_tier_" + tier.tier()));
            Item helmet = Registry.register(
                    Registries.ITEM,
                    helmetKey,
                    new ChainArmorItem(armorMat, EquipmentType.HELMET, tier.tier(), new Item.Settings().registryKey(helmetKey).fireproof())
            );

            // 7. Chestplate
            RegistryKey<Item> chestKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chestplate_tier_" + tier.tier()));
            Item chestplate = Registry.register(
                    Registries.ITEM,
                    chestKey,
                    new ChainArmorItem(armorMat, EquipmentType.CHESTPLATE, tier.tier(), new Item.Settings().registryKey(chestKey).fireproof())
            );

            // 8. Leggings
            RegistryKey<Item> legsKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("leggings_tier_" + tier.tier()));
            Item leggings = Registry.register(
                    Registries.ITEM,
                    legsKey,
                    new ChainArmorItem(armorMat, EquipmentType.LEGGINGS, tier.tier(), new Item.Settings().registryKey(legsKey).fireproof())
            );

            // 9. Boots
            RegistryKey<Item> bootsKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("boots_tier_" + tier.tier()));
            Item boots = Registry.register(
                    Registries.ITEM,
                    bootsKey,
                    new ChainArmorItem(armorMat, EquipmentType.BOOTS, tier.tier(), new Item.Settings().registryKey(bootsKey).fireproof())
            );

            GEAR_SETS.add(new GearSet(tier, sword, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings, boots));
        }

        // Register Special Weapons
        RegistryKey<Item> bowKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("dragon_breath_bow"));
        DRAGON_BOW = Registry.register(
                Registries.ITEM,
                bowKey,
                new DragonBreathBowItem(new Item.Settings().registryKey(bowKey).fireproof())
        );

        RegistryKey<Item> xbowKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("galactic_crossbow"));
        GALACTIC_CROSSBOW = Registry.register(
                Registries.ITEM,
                xbowKey,
                new GalacticCrossbowItem(new Item.Settings().registryKey(xbowKey).fireproof())
        );

        RegistryKey<Item> shieldKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("aegis_shield"));
        AEGIS_SHIELD = Registry.register(
                Registries.ITEM,
                shieldKey,
                new ChainShieldItem(new Item.Settings().registryKey(shieldKey).fireproof())
        );

        RegistryKey<Item> sentinelKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("sentinel_shield"));
        SENTINEL_SHIELD = Registry.register(
                Registries.ITEM,
                sentinelKey,
                new SentinelShieldItem(new Item.Settings().registryKey(sentinelKey).fireproof())
        );

        RegistryKey<Item> frostguardKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("frostguard_shield"));
        FROSTGUARD_SHIELD = Registry.register(
                Registries.ITEM,
                frostguardKey,
                new FrostguardShieldItem(new Item.Settings().registryKey(frostguardKey).fireproof())
        );

        RegistryKey<Item> abyssalKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("abyssal_aegis"));
        ABYSSAL_AEGIS = Registry.register(
                Registries.ITEM,
                abyssalKey,
                new AbyssalAegisItem(new Item.Settings().registryKey(abyssalKey).fireproof())
        );

        RegistryKey<Item> infernalKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("infernal_bulwark"));
        INFERNAL_BULWARK = Registry.register(
                Registries.ITEM,
                infernalKey,
                new InfernalBulwarkItem(new Item.Settings().registryKey(infernalKey).fireproof())
        );

        RegistryKey<Item> cosmicKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("cosmic_shield"));
        COSMIC_SHIELD = Registry.register(
                Registries.ITEM,
                cosmicKey,
                new CosmicShieldItem(new Item.Settings().registryKey(cosmicKey).fireproof())
        );

        RegistryKey<Item> omniKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("god_omnitool"));
        GOD_OMNITOOL = Registry.register(
                Registries.ITEM,
                omniKey,
                new GodOmnitoolItem(new Item.Settings().registryKey(omniKey).fireproof())
        );

        // Register Specialized Utility Armors
        RegistryKey<Item> exoKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("mining_exoskeleton"));
        MINING_EXOSKELETON = Registry.register(
                Registries.ITEM,
                exoKey,
                new MiningExoskeletonItem(new Item.Settings().registryKey(exoKey).fireproof().rarity(net.minecraft.util.Rarity.RARE))
        );

        RegistryKey<Item> lumberKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("lumberjack_hauberk"));
        LUMBERJACK_HAUBERK = Registry.register(
                Registries.ITEM,
                lumberKey,
                new LumberjackHauberkItem(new Item.Settings().registryKey(lumberKey).rarity(net.minecraft.util.Rarity.RARE))
        );

        RegistryKey<Item> elytraKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("cosmic_elytra_chestplate"));
        COSMIC_ELYTRA_CHESTPLATE = Registry.register(
                Registries.ITEM,
                elytraKey,
                new CosmicElytraChestplateItem(new Item.Settings().registryKey(elytraKey).fireproof().rarity(net.minecraft.util.Rarity.EPIC))
        );

        ChainOresMod.LOGGER.info("Registered complete progressive arsenal: swords, pickaxes, axes, shovels, hoes, armors, and legendary weapons!");
    }
}
