package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.item.GuideBookItem;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModItems {

    public static Item GUIDE_BOOK;
    public static Item CHAIN_SMITHING_TEMPLATE;
    public static Item PROSPECTOR_ROD;

    public static void initialize() {
        // Register Guide Book
        RegistryKey<Item> guideKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("guide_book"));
        GUIDE_BOOK = Registry.register(
                Registries.ITEM,
                guideKey,
                new GuideBookItem(new Item.Settings().registryKey(guideKey).maxCount(1))
        );

        // Register Smithing Template
        RegistryKey<Item> templateKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chain_smithing_template"));
        CHAIN_SMITHING_TEMPLATE = Registry.register(
                Registries.ITEM,
                templateKey,
                new Item(new Item.Settings().registryKey(templateKey).maxCount(64))
        );

        // Register Prospector Rod
        RegistryKey<Item> rodKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("prospector_rod"));
        PROSPECTOR_ROD = Registry.register(
                Registries.ITEM,
                rodKey,
                new com.chainores.item.ProspectorRodItem(new Item.Settings().registryKey(rodKey).maxCount(1))
        );

        // Register items for each tier
        for (OreTier ore : OreRegistryData.TIERS) {
            RegistryKey<Item> oreBlockItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("ore_tier_" + ore.tier));
            Item oreBlockItem = Registry.register(
                    Registries.ITEM,
                    oreBlockItemKey,
                    new BlockItem(ore.oreBlock, new Item.Settings().registryKey(oreBlockItemKey).useBlockPrefixedTranslationKey())
            );
            ore.oreBlockItem = oreBlockItem;

            RegistryKey<Item> storageBlockItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("block_tier_" + ore.tier));
            Item storageBlockItem = Registry.register(
                    Registries.ITEM,
                    storageBlockItemKey,
                    new BlockItem(ore.storageBlock, new Item.Settings().registryKey(storageBlockItemKey).useBlockPrefixedTranslationKey())
            );
            ore.storageBlockItem = storageBlockItem;

            RegistryKey<Item> rawItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("raw_tier_" + ore.tier));
            Item rawItem = Registry.register(
                    Registries.ITEM,
                    rawItemKey,
                    new Item(new Item.Settings().registryKey(rawItemKey))
            );
            ore.rawItem = rawItem;

            RegistryKey<Item> ingotItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("ingot_tier_" + ore.tier));
            Item ingotItem = Registry.register(
                    Registries.ITEM,
                    ingotItemKey,
                    new Item(new Item.Settings().registryKey(ingotItemKey).fireproof())
            );
            ore.ingotItem = ingotItem;
        }

        ChainOresMod.LOGGER.info("Registered all items with registry keys for 1.21.11.");
    }
}
