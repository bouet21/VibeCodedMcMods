package com.chainores.world;

import com.chainores.ChainOresMod;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import java.util.function.Predicate;

public class ModWorldGen {

    public static void initialize() {
        ModFeatures.initialize();

        // 1. Structure Features
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES,
                ModFeatures.ABANDONED_FORGE_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheNether(),
                GenerationStep.Feature.SURFACE_STRUCTURES,
                ModFeatures.NETHER_SHRINE_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheEnd(),
                GenerationStep.Feature.SURFACE_STRUCTURES,
                ModFeatures.COSMIC_ALTAR_PLACED
        );

        // 2. All 150 Ore Features
        for (OreTier ore : OreRegistryData.TIERS) {
            Predicate<BiomeSelectionContext> selector = getSelectorFor(ore);
            BiomeModifications.addFeature(
                    selector,
                    GenerationStep.Feature.UNDERGROUND_ORES,
                    ore.placedFeatureKey
            );
        }

        ChainOresMod.LOGGER.info("Registered world generation features for all 150 ores and 3 custom structures across biomes and dimensions.");
    }

    private static Predicate<BiomeSelectionContext> getSelectorFor(OreTier ore) {
        return switch (ore.biomeGroup) {
            case PLAINS_FOREST -> BiomeSelectors.tag(BiomeTags.IS_FOREST)
                    .or(BiomeSelectors.includeByKey(
                            BiomeKeys.PLAINS,
                            BiomeKeys.MEADOW,
                            BiomeKeys.CHERRY_GROVE,
                            BiomeKeys.SUNFLOWER_PLAINS,
                            BiomeKeys.BIRCH_FOREST,
                            BiomeKeys.DARK_FOREST,
                            BiomeKeys.OLD_GROWTH_BIRCH_FOREST
                    ));

            case MOUNTAINS -> BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN)
                    .or(BiomeSelectors.tag(BiomeTags.IS_HILL))
                    .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))
                    .or(BiomeSelectors.includeByKey(
                            BiomeKeys.JAGGED_PEAKS,
                            BiomeKeys.FROZEN_PEAKS,
                            BiomeKeys.STONY_PEAKS,
                            BiomeKeys.SNOWY_SLOPES,
                            BiomeKeys.GROVE,
                            BiomeKeys.SNOWY_PLAINS,
                            BiomeKeys.ICE_SPIKES
                    ));

            case DESERT_BADLANDS -> BiomeSelectors.tag(BiomeTags.IS_BADLANDS)
                    .or(BiomeSelectors.includeByKey(
                            BiomeKeys.DESERT,
                            BiomeKeys.SAVANNA,
                            BiomeKeys.SAVANNA_PLATEAU,
                            BiomeKeys.WINDSWEPT_SAVANNA
                    ));

            case SWAMP_JUNGLE -> BiomeSelectors.tag(BiomeTags.IS_JUNGLE)
                    .or(BiomeSelectors.tag(BiomeTags.IS_OCEAN))
                    .or(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN))
                    .or(BiomeSelectors.includeByKey(
                            BiomeKeys.SWAMP,
                            BiomeKeys.MANGROVE_SWAMP,
                            BiomeKeys.LUSH_CAVES,
                            BiomeKeys.WARM_OCEAN
                    ));

            case DEEPSLATE_CAVES -> BiomeSelectors.foundInOverworld();

            case DEEP_DARK -> BiomeSelectors.includeByKey(BiomeKeys.DEEP_DARK);

            case NETHER_WASTES -> BiomeSelectors.includeByKey(BiomeKeys.NETHER_WASTES);

            case SOUL_SAND_VALLEY -> BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY);

            case CRIMSON_WARPED -> BiomeSelectors.includeByKey(
                    BiomeKeys.CRIMSON_FOREST,
                    BiomeKeys.WARPED_FOREST
            );

            case BASALT_DELTAS -> BiomeSelectors.includeByKey(BiomeKeys.BASALT_DELTAS);

            case END_MIDLANDS -> BiomeSelectors.includeByKey(
                    BiomeKeys.THE_END,
                    BiomeKeys.END_MIDLANDS
            );

            case END_HIGHLANDS -> BiomeSelectors.includeByKey(BiomeKeys.END_HIGHLANDS);

            case END_ISLANDS -> BiomeSelectors.includeByKey(
                    BiomeKeys.SMALL_END_ISLANDS,
                    BiomeKeys.END_BARRENS
            );

            case COSMIC -> BiomeSelectors.includeByKey(
                    BiomeKeys.END_HIGHLANDS,
                    BiomeKeys.SMALL_END_ISLANDS
            );
        };
    }
}
