package com.chainores.registry;

import com.chainores.ChainOresMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

public class ModGems {
    public static final Item BLOOD_GEM = register("blood_gem", new Item(
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("blood_gem")))
    ));

    public static final Item VOID_GEM = register("void_gem", new Item(
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_gem")))
    ));

    public static final Item CORE_GEM = register("core_gem", new Item(
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("core_gem")))
    ));

    public static final Item GENESIS_GEM = register("genesis_gem", new Item(
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("genesis_gem")))
    ));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ChainOresMod.id(name), item);
    }

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered Chain Ores Sockets & Gems.");
    }
}
