package com.chainores.config;

import com.chainores.ChainOresMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

/**
 * Central configuration system for Chain Ores.
 * Stores settings in config/chainores.json.
 * Easily modifiable by server owners to balance multipliers, vein mining limits,
 * treecapitator caps, claim protection, transmutation chances, and progression phases.
 */
public class ChainOresConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ChainOresConfig INSTANCE = null;

    // =========================================================================
    // 1. PERFORMANCE & SERVER CLAIM PROTECTION
    // =========================================================================
    /** Maximum blocks that can be mined in a single chain with a pickaxe. */
    public int veinMiningMaxBlocks = 64;

    /** Maximum logs that can be felled at once with TreeCapitator. */
    public int treeCapitatorMaxBlocks = 128;

    /** Maximum area mining radius (1 = 3x3, 2 = 5x5). */
    public int areaMiningMaxRadius = 2;

    /**
     * Whether vein mining, treecapitator, and area tools strictly respect
     * server claim protections (FTB Chunks, Open Parties & Claims, FLAN, WorldGuard, spawn protection).
     */
    public boolean enableClaimProtectionCheck = true;

    // =========================================================================
    // 2. STAT MULTIPLIERS & STACKING CAPS
    // =========================================================================
    /** Maximum total damage multiplier allowed to prevent game-breaking runaway scaling. */
    public float maxCumulativeDamageMultiplier = 2.5f;

    /** Maximum mining speed multiplier allowed from gems and reforge prefixes. */
    public float maxMiningSpeedMultiplier = 4.0f;

    /** Damage multiplier boost for Divine prefix ([DIVINO]). */
    public float reforgeDivineDamageBoost = 1.50f;

    /** Damage multiplier boost for Fierce prefix ([FEROZ]). */
    public float reforgeFierceDamageBoost = 1.35f;

    /** Damage multiplier boost for Legendary prefix ([LEGENDARIO]). */
    public float reforgeLegendaryDamageBoost = 1.25f;

    /** Mining speed bonus for Genesis Gem (Gema del Génesis). */
    public float genesisGemMiningBoost = 0.50f;

    // =========================================================================
    // 3. COSMIC SMELTER PROGRESSION & TRANSMUTATION
    // =========================================================================
    /** Multiplier yield for the Cosmic Smelter (default 4x ingots). */
    public int smelterYield = 4;

    /** Probability of rolling an ingot upgrade during smelting (default 35%). */
    public float smelterTransmutationChance = 0.35f;

    /**
     * When true, prevents the Cosmic Smelter from automatically bypassing
     * boss gate milestones (Tier 70, 105, 135, 149) through transmutation loops.
     */
    public boolean smelterLockBossMilestones = true;

    // =========================================================================
    // 4. PROGRESSION PHASE TOGGLES & ABILITY SWITCHES
    // =========================================================================
    public boolean enablePhaseOverworld = true; // Tiers 1-70
    public boolean enablePhaseNether = true;    // Tiers 71-105
    public boolean enablePhaseEnd = true;       // Tiers 106-135
    public boolean enablePhaseCosmic = true;    // Tiers 136-150

    public boolean enableVeinMining = true;
    public boolean enableTreeCapitator = true;
    public boolean enableAreaMining = true;

    // =========================================================================
    // 5. DASH & HUD CONTROLS
    // =========================================================================
    /**
     * Whether Sprint + Crouch also triggers Dash as an alternate activation method.
     */
    public boolean enableSprintSneakDash = true;

    /** Cooldown in seconds for Dash ability. */
    public int dashCooldownSeconds = 4;

    // =========================================================================
    // 6. STRUCTURE GENERATION
    // =========================================================================
    /** 1 in N chunks chance for Abandoned Forge to spawn naturally. */
    public int abandonedForgeRarity = 120;

    /** 1 in N chunks chance for Nether Shrine to spawn naturally. */
    public int netherShrineRarity = 100;

    /** 1 in N chunks chance for Cosmic Altar to spawn naturally. */
    public int cosmicAltarRarity = 100;

    /** Minimum distance in blocks between naturally generated structures of the same type. */
    public int minStructureDistanceBlocks = 800;

    public static synchronized ChainOresConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static synchronized void load() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File configFile = configDir.resolve("chainores.json").toFile();

            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    ChainOresConfig loaded = GSON.fromJson(reader, ChainOresConfig.class);
                    if (loaded != null) {
                        INSTANCE = loaded;
                        ChainOresMod.LOGGER.info("Successfully loaded configuration from config/chainores.json");
                        return;
                    }
                }
            }
            INSTANCE = new ChainOresConfig();
            save();
            ChainOresMod.LOGGER.info("Created default configuration at config/chainores.json");
        } catch (Exception e) {
            ChainOresMod.LOGGER.error("Failed to load Chain Ores config, using defaults: {}", e.getMessage());
            INSTANCE = new ChainOresConfig();
        }
    }

    public static synchronized void save() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File configFile = configDir.resolve("chainores.json").toFile();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            ChainOresMod.LOGGER.error("Failed to save Chain Ores config: {}", e.getMessage());
        }
    }
}
