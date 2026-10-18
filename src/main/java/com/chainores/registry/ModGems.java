package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.item.GemItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

public class ModGems {
    // Tier I
    public static final Item BLOOD_GEM = register("blood_gem", new GemItem(
            GemItem.GemType.BLOOD, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("blood_gem")))
    ));

    public static final Item VOID_GEM = register("void_gem", new GemItem(
            GemItem.GemType.VOID, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_gem")))
    ));

    public static final Item CORE_GEM = register("core_gem", new GemItem(
            GemItem.GemType.CORE, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("core_gem")))
    ));

    public static final Item GENESIS_GEM = register("genesis_gem", new GemItem(
            GemItem.GemType.GENESIS, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("genesis_gem")))
    ));

    public static final Item EARTH_GEM = register("earth_gem", new GemItem(
            GemItem.GemType.EARTH, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("earth_gem")))
    ));

    public static final Item ECHO_GEM = register("echo_gem", new GemItem(
            GemItem.GemType.ECHO, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("echo_gem")))
    ));

    public static final Item MAGNET_GEM = register("magnet_gem", new GemItem(
            GemItem.GemType.MAGNET, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("magnet_gem")))
    ));

    public static final Item CHRONO_GEM = register("chrono_gem", new GemItem(
            GemItem.GemType.CHRONO, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chrono_gem")))
    ));

    public static final Item FROST_GEM = register("frost_gem", new GemItem(
            GemItem.GemType.FROST, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("frost_gem")))
    ));

    public static final Item STORM_GEM = register("storm_gem", new GemItem(
            GemItem.GemType.STORM, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("storm_gem")))
    ));

    public static final Item VENOM_GEM = register("venom_gem", new GemItem(
            GemItem.GemType.VENOM, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("venom_gem")))
    ));

    public static final Item SOLAR_GEM = register("solar_gem", new GemItem(
            GemItem.GemType.SOLAR, 1,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("solar_gem")))
    ));

    // Tier II
    public static final Item BLOOD_GEM_2 = register("blood_gem_2", new GemItem(
            GemItem.GemType.BLOOD, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("blood_gem_2")))
    ));

    public static final Item VOID_GEM_2 = register("void_gem_2", new GemItem(
            GemItem.GemType.VOID, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_gem_2")))
    ));

    public static final Item CORE_GEM_2 = register("core_gem_2", new GemItem(
            GemItem.GemType.CORE, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("core_gem_2")))
    ));

    public static final Item GENESIS_GEM_2 = register("genesis_gem_2", new GemItem(
            GemItem.GemType.GENESIS, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("genesis_gem_2")))
    ));

    public static final Item EARTH_GEM_2 = register("earth_gem_2", new GemItem(
            GemItem.GemType.EARTH, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("earth_gem_2")))
    ));

    public static final Item ECHO_GEM_2 = register("echo_gem_2", new GemItem(
            GemItem.GemType.ECHO, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("echo_gem_2")))
    ));

    public static final Item MAGNET_GEM_2 = register("magnet_gem_2", new GemItem(
            GemItem.GemType.MAGNET, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("magnet_gem_2")))
    ));

    public static final Item CHRONO_GEM_2 = register("chrono_gem_2", new GemItem(
            GemItem.GemType.CHRONO, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chrono_gem_2")))
    ));

    public static final Item FROST_GEM_2 = register("frost_gem_2", new GemItem(
            GemItem.GemType.FROST, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("frost_gem_2")))
    ));

    public static final Item STORM_GEM_2 = register("storm_gem_2", new GemItem(
            GemItem.GemType.STORM, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("storm_gem_2")))
    ));

    public static final Item VENOM_GEM_2 = register("venom_gem_2", new GemItem(
            GemItem.GemType.VENOM, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("venom_gem_2")))
    ));

    public static final Item SOLAR_GEM_2 = register("solar_gem_2", new GemItem(
            GemItem.GemType.SOLAR, 2,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("solar_gem_2")))
    ));

    // Tier III (Transcendent)
    public static final Item BLOOD_GEM_3 = register("blood_gem_3", new GemItem(
            GemItem.GemType.BLOOD, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("blood_gem_3")))
    ));

    public static final Item VOID_GEM_3 = register("void_gem_3", new GemItem(
            GemItem.GemType.VOID, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_gem_3")))
    ));

    public static final Item CORE_GEM_3 = register("core_gem_3", new GemItem(
            GemItem.GemType.CORE, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("core_gem_3")))
    ));

    public static final Item GENESIS_GEM_3 = register("genesis_gem_3", new GemItem(
            GemItem.GemType.GENESIS, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("genesis_gem_3")))
    ));

    public static final Item EARTH_GEM_3 = register("earth_gem_3", new GemItem(
            GemItem.GemType.EARTH, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("earth_gem_3")))
    ));

    public static final Item ECHO_GEM_3 = register("echo_gem_3", new GemItem(
            GemItem.GemType.ECHO, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("echo_gem_3")))
    ));

    public static final Item MAGNET_GEM_3 = register("magnet_gem_3", new GemItem(
            GemItem.GemType.MAGNET, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("magnet_gem_3")))
    ));

    public static final Item CHRONO_GEM_3 = register("chrono_gem_3", new GemItem(
            GemItem.GemType.CHRONO, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chrono_gem_3")))
    ));

    public static final Item FROST_GEM_3 = register("frost_gem_3", new GemItem(
            GemItem.GemType.FROST, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("frost_gem_3")))
    ));

    public static final Item STORM_GEM_3 = register("storm_gem_3", new GemItem(
            GemItem.GemType.STORM, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("storm_gem_3")))
    ));

    public static final Item VENOM_GEM_3 = register("venom_gem_3", new GemItem(
            GemItem.GemType.VENOM, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("venom_gem_3")))
    ));

    public static final Item SOLAR_GEM_3 = register("solar_gem_3", new GemItem(
            GemItem.GemType.SOLAR, 3,
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("solar_gem_3")))
    ));

    public static boolean isGem(Item item) {
        return item instanceof GemItem;
    }

    public static Item getFusedGem(Item item) {
        if (item == BLOOD_GEM) return BLOOD_GEM_2;
        if (item == BLOOD_GEM_2) return BLOOD_GEM_3;

        if (item == VOID_GEM) return VOID_GEM_2;
        if (item == VOID_GEM_2) return VOID_GEM_3;

        if (item == CORE_GEM) return CORE_GEM_2;
        if (item == CORE_GEM_2) return CORE_GEM_3;

        if (item == GENESIS_GEM) return GENESIS_GEM_2;
        if (item == GENESIS_GEM_2) return GENESIS_GEM_3;

        if (item == EARTH_GEM) return EARTH_GEM_2;
        if (item == EARTH_GEM_2) return EARTH_GEM_3;

        if (item == ECHO_GEM) return ECHO_GEM_2;
        if (item == ECHO_GEM_2) return ECHO_GEM_3;

        if (item == MAGNET_GEM) return MAGNET_GEM_2;
        if (item == MAGNET_GEM_2) return MAGNET_GEM_3;

        if (item == CHRONO_GEM) return CHRONO_GEM_2;
        if (item == CHRONO_GEM_2) return CHRONO_GEM_3;

        if (item == FROST_GEM) return FROST_GEM_2;
        if (item == FROST_GEM_2) return FROST_GEM_3;

        if (item == STORM_GEM) return STORM_GEM_2;
        if (item == STORM_GEM_2) return STORM_GEM_3;

        if (item == VENOM_GEM) return VENOM_GEM_2;
        if (item == VENOM_GEM_2) return VENOM_GEM_3;

        if (item == SOLAR_GEM) return SOLAR_GEM_2;
        if (item == SOLAR_GEM_2) return SOLAR_GEM_3;

        return null;
    }

    public static String getGemId(Item item) {
        if (item == BLOOD_GEM) return "blood_gem";
        if (item == BLOOD_GEM_2) return "blood_gem_2";
        if (item == BLOOD_GEM_3) return "blood_gem_3";

        if (item == VOID_GEM) return "void_gem";
        if (item == VOID_GEM_2) return "void_gem_2";
        if (item == VOID_GEM_3) return "void_gem_3";

        if (item == CORE_GEM) return "core_gem";
        if (item == CORE_GEM_2) return "core_gem_2";
        if (item == CORE_GEM_3) return "core_gem_3";

        if (item == GENESIS_GEM) return "genesis_gem";
        if (item == GENESIS_GEM_2) return "genesis_gem_2";
        if (item == GENESIS_GEM_3) return "genesis_gem_3";

        if (item == EARTH_GEM) return "earth_gem";
        if (item == EARTH_GEM_2) return "earth_gem_2";
        if (item == EARTH_GEM_3) return "earth_gem_3";

        if (item == ECHO_GEM) return "echo_gem";
        if (item == ECHO_GEM_2) return "echo_gem_2";
        if (item == ECHO_GEM_3) return "echo_gem_3";

        if (item == MAGNET_GEM) return "magnet_gem";
        if (item == MAGNET_GEM_2) return "magnet_gem_2";
        if (item == MAGNET_GEM_3) return "magnet_gem_3";

        if (item == CHRONO_GEM) return "chrono_gem";
        if (item == CHRONO_GEM_2) return "chrono_gem_2";
        if (item == CHRONO_GEM_3) return "chrono_gem_3";

        if (item == FROST_GEM) return "frost_gem";
        if (item == FROST_GEM_2) return "frost_gem_2";
        if (item == FROST_GEM_3) return "frost_gem_3";

        if (item == STORM_GEM) return "storm_gem";
        if (item == STORM_GEM_2) return "storm_gem_2";
        if (item == STORM_GEM_3) return "storm_gem_3";

        if (item == VENOM_GEM) return "venom_gem";
        if (item == VENOM_GEM_2) return "venom_gem_2";
        if (item == VENOM_GEM_3) return "venom_gem_3";

        if (item == SOLAR_GEM) return "solar_gem";
        if (item == SOLAR_GEM_2) return "solar_gem_2";
        if (item == SOLAR_GEM_3) return "solar_gem_3";

        return null;
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ChainOresMod.id(name), item);
    }

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered Chain Ores Sockets & Gems (Tiers I, II, III).");
    }
}
