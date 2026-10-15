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

import com.chainores.block.ChainOreBlock;

public class ModBlocks {

    public static int getLuminance(int tier) {
        if (tier >= 150) return 15;
        if (tier >= 140) return 13;
        if (tier >= 135) return 12;
        if (tier >= 125) return 11;
        if (tier >= 115) return 9;
        if (tier >= 100) return 10;
        if (tier >= 90) return 8;
        if (tier >= 80) return 10;
        if (tier >= 70) return 7;
        if (tier >= 50) return 5;
        if (tier == 30) return 9;
        if (tier >= 21 && tier < 30) return 3;
        return 0;
    }

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
            int light = getLuminance(ore.tier);

            oreSettings = oreSettings
                    .registryKey(oreKey)
                    .strength(3.0f + strengthBonus, 3.0f + strengthBonus)
                    .requiresTool();

            if (light > 0) {
                oreSettings = oreSettings.luminance(state -> light);
            }

            Block oreBlock = new ChainOreBlock(ore.tier, oreSettings);
            Registry.register(Registries.BLOCK, oreKey, oreBlock);
            ore.oreBlock = oreBlock;

            // Storage Block
            RegistryKey<Block> storageKey = RegistryKey.of(RegistryKeys.BLOCK, ChainOresMod.id("block_tier_" + ore.tier));
            AbstractBlock.Settings storageSettings = AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
                    .registryKey(storageKey)
                    .strength(5.0f + strengthBonus, 6.0f + strengthBonus)
                    .requiresTool();

            if (light > 0) {
                int storageLight = Math.min(15, light + 2);
                storageSettings = storageSettings.luminance(state -> storageLight);
            }

            Block storageBlock = new Block(storageSettings);
            Registry.register(Registries.BLOCK, storageKey, storageBlock);
            ore.storageBlock = storageBlock;
        }

        ChainOresMod.LOGGER.info("Registered 300 blocks with required registry keys, dynamic luminance and particles for 1.21.11.");
    }
}
