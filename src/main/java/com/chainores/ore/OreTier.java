package com.chainores.ore;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.PlacedFeature;

public class OreTier {
    public enum Dimension { OVERWORLD, NETHER, END }
    public enum BaseStone { STONE, DEEPSLATE, NETHERRACK, END_STONE }
    public enum BiomeGroup {
        PLAINS_FOREST, MOUNTAINS, DESERT_BADLANDS, SWAMP_JUNGLE,
        DEEPSLATE_CAVES, DEEP_DARK,
        NETHER_WASTES, SOUL_SAND_VALLEY, CRIMSON_WARPED, BASALT_DELTAS,
        END_MIDLANDS, END_HIGHLANDS, END_ISLANDS, COSMIC
    }

    public final int tier;
    public final String id;
    public final String enName;
    public final String esName;
    public final Dimension dimension;
    public final BiomeGroup biomeGroup;
    public final BaseStone baseStone;
    public final int harvestLevel;
    public final int minY;
    public final int maxY;
    public final int veinSize;

    public Block oreBlock;
    public Block storageBlock;
    public Item oreBlockItem;
    public Item storageBlockItem;
    public Item rawItem;
    public Item ingotItem;
    public RegistryKey<PlacedFeature> placedFeatureKey;

    public OreTier(int tier, String id, String enName, String esName, Dimension dimension,
                   BiomeGroup biomeGroup, BaseStone baseStone, int harvestLevel,
                   int minY, int maxY, int veinSize) {
        this.tier = tier;
        this.id = id;
        this.enName = enName;
        this.esName = esName;
        this.dimension = dimension;
        this.biomeGroup = biomeGroup;
        this.baseStone = baseStone;
        this.harvestLevel = harvestLevel;
        this.minY = minY;
        this.maxY = maxY;
        this.veinSize = veinSize;
        this.placedFeatureKey = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("chainores", "ore_tier_" + tier));
    }
}
