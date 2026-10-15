package com.chainores.world;

import com.chainores.ChainOresMod;
import com.chainores.world.feature.AbandonedForgeFeature;
import com.chainores.world.feature.CosmicAltarFeature;
import com.chainores.world.feature.NetherShrineFeature;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> ABANDONED_FORGE = Registry.register(
            Registries.FEATURE,
            ChainOresMod.id("abandoned_forge"),
            new AbandonedForgeFeature(DefaultFeatureConfig.CODEC)
    );

    public static final Feature<DefaultFeatureConfig> NETHER_SHRINE = Registry.register(
            Registries.FEATURE,
            ChainOresMod.id("nether_shrine"),
            new NetherShrineFeature(DefaultFeatureConfig.CODEC)
    );

    public static final Feature<DefaultFeatureConfig> COSMIC_ALTAR = Registry.register(
            Registries.FEATURE,
            ChainOresMod.id("cosmic_altar"),
            new CosmicAltarFeature(DefaultFeatureConfig.CODEC)
    );

    // Placed Feature Keys matching JSON definitions
    public static final RegistryKey<PlacedFeature> ABANDONED_FORGE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, ChainOresMod.id("abandoned_forge"));

    public static final RegistryKey<PlacedFeature> NETHER_SHRINE_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, ChainOresMod.id("nether_shrine"));

    public static final RegistryKey<PlacedFeature> COSMIC_ALTAR_PLACED =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, ChainOresMod.id("cosmic_altar"));

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered Chain Ores Custom Structures and Features.");
    }
}
