package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.workstation.ChainAnvilBlock;
import com.chainores.workstation.ChainForgeBlock;
import com.chainores.workstation.ChainForgeBlockEntity;
import com.chainores.workstation.ChainForgeScreenHandler;
import com.chainores.workstation.CosmicSmelterBlock;
import com.chainores.workstation.CosmicSmelterBlockEntity;
import com.chainores.workstation.CosmicSmelterScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ModWorkstations {

    // 1. Blocks
    public static Block CHAIN_FORGE_BLOCK;
    public static Block CHAIN_ANVIL_BLOCK;
    public static Block COSMIC_SMELTER_BLOCK;

    // Block Items
    public static Item CHAIN_FORGE_ITEM;
    public static Item CHAIN_ANVIL_ITEM;
    public static Item COSMIC_SMELTER_ITEM;

    // 2. Block Entity Types
    public static BlockEntityType<ChainForgeBlockEntity> CHAIN_FORGE_BLOCK_ENTITY;
    public static BlockEntityType<CosmicSmelterBlockEntity> COSMIC_SMELTER_BLOCK_ENTITY;

    // 3. Screen Handler Types
    public static ScreenHandlerType<ChainForgeScreenHandler> CHAIN_FORGE_SCREEN_HANDLER;
    public static ScreenHandlerType<CosmicSmelterScreenHandler> COSMIC_SMELTER_SCREEN_HANDLER;

    public static void initialize() {
        // A. Register Blocks
        RegistryKey<Block> forgeKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("chain_forge"));
        CHAIN_FORGE_BLOCK = Registry.register(
                Registries.BLOCK,
                forgeKey,
                new ChainForgeBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).registryKey(forgeKey).requiresTool().strength(5.0f, 6.0f))
        );

        RegistryKey<Block> anvilKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("chain_anvil"));
        CHAIN_ANVIL_BLOCK = Registry.register(
                Registries.BLOCK,
                anvilKey,
                new ChainAnvilBlock(AbstractBlock.Settings.copy(Blocks.ANVIL).registryKey(anvilKey).requiresTool().strength(6.0f, 1200.0f))
        );

        RegistryKey<Block> smelterKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("cosmic_smelter"));
        COSMIC_SMELTER_BLOCK = Registry.register(
                Registries.BLOCK,
                smelterKey,
                new CosmicSmelterBlock(AbstractBlock.Settings.copy(Blocks.OBSIDIAN).registryKey(smelterKey).requiresTool().strength(7.0f, 10.0f))
        );

        // B. Register Block Items
        RegistryKey<Item> forgeItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chain_forge"));
        CHAIN_FORGE_ITEM = Registry.register(
                Registries.ITEM,
                forgeItemKey,
                new BlockItem(CHAIN_FORGE_BLOCK, new Item.Settings().registryKey(forgeItemKey).useBlockPrefixedTranslationKey())
        );

        RegistryKey<Item> anvilItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("chain_anvil"));
        CHAIN_ANVIL_ITEM = Registry.register(
                Registries.ITEM,
                anvilItemKey,
                new BlockItem(CHAIN_ANVIL_BLOCK, new Item.Settings().registryKey(anvilItemKey).useBlockPrefixedTranslationKey())
        );

        RegistryKey<Item> smelterItemKey = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("cosmic_smelter"));
        COSMIC_SMELTER_ITEM = Registry.register(
                Registries.ITEM,
                smelterItemKey,
                new BlockItem(COSMIC_SMELTER_BLOCK, new Item.Settings().registryKey(smelterItemKey).useBlockPrefixedTranslationKey())
        );

        // C. Register Block Entities
        CHAIN_FORGE_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                ChainOresMod.id("chain_forge"),
                FabricBlockEntityTypeBuilder.create(ChainForgeBlockEntity::new, CHAIN_FORGE_BLOCK).build()
        );

        COSMIC_SMELTER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                ChainOresMod.id("cosmic_smelter"),
                FabricBlockEntityTypeBuilder.create(CosmicSmelterBlockEntity::new, COSMIC_SMELTER_BLOCK).build()
        );

        // D. Register Screen Handlers
        CHAIN_FORGE_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                ChainOresMod.id("chain_forge"),
                new ScreenHandlerType<>(ChainForgeScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        COSMIC_SMELTER_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                ChainOresMod.id("cosmic_smelter"),
                new ScreenHandlerType<>(CosmicSmelterScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        ChainAnvilBlock.registerInteractionCallback();
        ChainOresMod.LOGGER.info("Registered Chain Ores Workstations: Chain Forge, Chain Anvil, and Cosmic Smelter!");
    }
}
