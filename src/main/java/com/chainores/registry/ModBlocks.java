package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModBlocks {

    public static void initialize() {
        for (OreTier ore : OreRegistryData.TIERS) {
            RegistryKey<Block> oreKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("ore_tier_" + ore.tier));

            // Ore Block
            AbstractBlock.Settings oreSettings = switch (ore.baseStone) {
                case STONE -> AbstractBlock.Settings.copy(Blocks.IRON_ORE);
                case DEEPSLATE -> AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE);
                case NETHERRACK -> AbstractBlock.Settings.copy(Blocks.NETHER_GOLD_ORE);
                case END_STONE -> AbstractBlock.Settings.copy(Blocks.END_STONE).strength(4.5f, 3.0f).requiresTool();
            };

            float strengthBonus = (float) ore.tier * 0.05f;
            oreSettings = oreSettings
                    .registryKey(oreKey)
                    .strength(3.0f + strengthBonus, 3.0f + strengthBonus)
                    .requiresTool();

            Block oreBlock = new Block(oreSettings);
            Registry.register(Registries.BLOCK, oreKey, oreBlock);
            ore.oreBlock = oreBlock;

            // Storage Block
            RegistryKey<Block> storageKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("block_tier_" + ore.tier));
            AbstractBlock.Settings storageSettings = AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
                    .registryKey(storageKey)
                    .strength(5.0f + strengthBonus, 6.0f + strengthBonus)
                    .requiresTool();

            Block storageBlock = new Block(storageSettings);
            Registry.register(Registries.BLOCK, storageKey, storageBlock);
            ore.storageBlock = storageBlock;
        }

        ChainOresMod.LOGGER.info("Registered 300 blocks with required registry keys for 1.21.11.");
    }
}
