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

        return null;
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ChainOresMod.id(name), item);
    }

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered Chain Ores Sockets & Gems (Tiers I, II, III).");
    }
}
