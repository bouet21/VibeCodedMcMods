package com.chainores.ore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.chainores.ore.OreTier.Dimension;
import com.chainores.ore.OreTier.BiomeGroup;
import com.chainores.ore.OreTier.BaseStone;

public class OreRegistryData {
    public static final List<OreTier> TIERS = new ArrayList<>();

    static {
        // Tiers 1 - 10: Plains & Forests (Stone)
        add(1, "copperite", "Copperite", "Cobreprita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 1, 30, 100, 9);
        add(2, "tin", "Tin", "Estaño", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 1, 25, 90, 8);
        add(3, "bronzeite", "Bronzeite", "Broncita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 1, 20, 80, 8);
        add(4, "galena", "Galena", "Galena", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 1, 15, 75, 7);
        add(5, "zincite", "Zincite", "Cincita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 1, 15, 70, 7);
        add(6, "bismuth", "Bismuth", "Bismuto", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 2, 10, 65, 7);
        add(7, "silver", "Silver", "Plata", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 2, 10, 60, 6);
        add(8, "electrum", "Electrum", "Electro", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 2, 10, 60, 6);
        add(9, "nickel", "Nickel", "Níquel", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 2, 5, 55, 6);
        add(10, "invar", "Invar", "Invar", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 2, 0, 50, 6);

        // Tiers 11 - 20: Mountains & Snow (Stone)
        add(11, "froststeel", "Froststeel", "Acero Glacial", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 2, 50, 180, 8);
        add(12, "cryolite", "Cryolite", "Criolita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 2, 60, 190, 7);
        add(13, "glacite", "Glacite", "Glacita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 2, 40, 150, 7);
        add(14, "cobalt", "Cobalt", "Cobalto", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 2, 40, 160, 6);
        add(15, "aquamarine", "Aquamarine", "Aguamarina", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 2, 30, 140, 6);
        add(16, "sapphire", "Sapphire", "Zafiro", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 3, 50, 180, 5);
        add(17, "permafrostium", "Permafrostium", "Permafrostio", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 3, 30, 120, 6);
        add(18, "borealis", "Borealis", "Borealita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 3, 70, 220, 5);
        add(19, "blizzardite", "Blizzardite", "Ventisquita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 3, 80, 240, 5);
        add(20, "absolute_zero", "Absolute Zero", "Cero Absoluto", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 3, 90, 256, 4);

        // Tiers 21 - 30: Deserts & Badlands (Stone)
        add(21, "pyrite", "Pyrite", "Pirita Solar", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 20, 80, 8);
        add(22, "amber", "Amber", "Ámbar Fósil", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 25, 90, 7);
        add(23, "sunstone", "Sunstone", "Piedra Solar", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 30, 100, 7);
        add(24, "desert_topaz", "Desert Topaz", "Topacio Arenoso", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 15, 75, 6);
        add(25, "jasper", "Jasper", "Jaspe Rojo", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 35, 110, 6);
        add(26, "cinnabar", "Cinnabar", "Cinabrio", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 20, 90, 6);
        add(27, "terracotta_ore", "Terracotite", "Terracotita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 2, 40, 120, 7);
        add(28, "sandstormite", "Sandstormite", "Tormentita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 3, 20, 80, 5);
        add(29, "solarite", "Solarite", "Solarita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 3, 30, 100, 5);
        add(30, "mirage_core", "Mirage Core", "Espejismo", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 3, 30, 110, 4);

        // Tiers 31 - 40: Swamps, Jungles & Oceans (Stone / Deepslate)
        add(31, "bog_iron", "Bog Iron", "Hierro de Ciénaga", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 2, 10, 60, 8);
        add(32, "malachite", "Malachite", "Malaquita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 2, 15, 75, 7);
        add(33, "jade", "Jade", "Jade Imperial", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 2, 10, 70, 7);
        add(34, "serpentine", "Serpentine", "Serpentina", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 2, 0, 50, 6);
        add(35, "toxicite", "Toxicite", "Toxicita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, 2, -15, 30, 6);
        add(36, "coralium", "Coralium", "Coralio", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 2, 10, 50, 6);
        add(37, "abyssium", "Abyssium", "Abisio Marino", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, 3, -30, 20, 5);
        add(38, "prismatic_ore", "Prismatite", "Prismatita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, 3, -35, 10, 5);
        add(39, "chlorophyte", "Chlorophyte", "Clorofita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, 3, -40, 0, 5);
        add(40, "jungle_heart", "Jungle Heart", "Corazón de Jungla", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, 3, -45, -10, 4);

        // Tiers 41 - 50: Deepslate Caverns (Deepslate)
        add(41, "titanium", "Titanium", "Titanio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -20, 10, 6);
        add(42, "platinum", "Platinum", "Platino", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -25, 5, 6);
        add(43, "tungsten", "Tungsten", "Wolframio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -30, 0, 6);
        add(44, "palladium", "Palladium", "Paladio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -35, -5, 5);
        add(45, "osmium", "Osmium", "Osmio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -40, -10, 5);
        add(46, "iridium", "Iridium", "Iridio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -45, -15, 5);
        add(47, "mythril", "Mythril", "Mithril", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -50, -20, 5);
        add(48, "orichalcum", "Orichalcum", "Oricalco", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 3, -55, -25, 4);
        add(49, "adamantite", "Adamantite", "Adamantita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -58, -30, 4);
        add(50, "runite", "Runite", "Runita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -60, -35, 4);

        // Tiers 51 - 60: Deepslate Abyss & Bedrock Layer (Deepslate)
        add(51, "darksteel", "Darksteel", "Acero Oscuro", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -40, 5);
        add(52, "onyx", "Onyx", "Ónice Profundo", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -45, 4);
        add(53, "bedrockium_dust", "Bedrockium", "Polvo de Lecho", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -55, 4);
        add(54, "voidstone", "Voidstone", "Piedra Vacía", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -45, 4);
        add(55, "obsidian_infused", "Obsidianite", "Obsidianita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -40, 4);
        add(56, "netherite_subgrade", "Sub-Netherite", "Sub-Netherita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -50, 4);
        add(57, "abyssalite", "Abyssalite", "Abisalita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -48, 4);
        add(58, "tenebrium", "Tenebrium", "Tenebrio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -52, 3);
        add(59, "gravitite", "Gravitite", "Gravitita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -54, 3);
        add(60, "coreium", "Coreium", "Mineral del Núcleo", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, 4, -64, -55, 3);

        // Tiers 61 - 70: The Deep Dark (Deepslate)
        add(61, "echo_ore", "Echo Ore", "Ecosita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -35, 5);
        add(62, "sculk_infused", "Sculkite", "Esculcita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -40, 4);
        add(63, "wardenite", "Wardenite", "Guardianita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -42, 4);
        add(64, "resonance", "Resonance", "Resonita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -45, 4);
        add(65, "silentium", "Silentium", "Silencio", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -48, 3);
        add(66, "soulbound_ore", "Soulbound", "Almarita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -50, 3);
        add(67, "shrieker_crystal", "Shriekerite", "Aullita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -50, 3);
        add(68, "shadow_platinum", "Shadow Platinum", "Platino Sombrío", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -52, 3);
        add(69, "catalyst_ore", "Catalyst Ore", "Catalita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -55, 3);
        add(70, "primordial_sculk", "Primordial Sculk", "Sculk Primordial", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, 4, -64, -58, 3);

        // Tiers 71 - 80: Nether Wastes (Netherrack)
        add(71, "nether_sulfur", "Nether Sulfur", "Azufre Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 10, 110, 8);
        add(72, "ignisite", "Ignisite", "Ignisita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 15, 105, 7);
        add(73, "cinderstone", "Cinderstone", "Cenizalita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 20, 100, 7);
        add(74, "magmacite", "Magmacite", "Magmacita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 10, 80, 6);
        add(75, "pyrolite", "Pyrolite", "Pirolita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 25, 95, 6);
        add(76, "hellfire_ore", "Hellfire Ore", "Fuego Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 3, 15, 85, 5);
        add(77, "blazesteel", "Blazesteel", "Acero de Llamas", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 4, 20, 75, 5);
        add(78, "caldera_gem", "Caldera Gem", "Gema de Caldera", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 4, 15, 70, 4);
        add(79, "vulcanite", "Vulcanite", "Vulcanita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 4, 10, 65, 4);
        add(80, "infernal_core", "Infernal Core", "Núcleo Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 4, 10, 50, 4);

        // Tiers 81 - 90: Soul Sand Valley (Netherrack)
        add(81, "ghastite", "Ghastite", "Lagrimita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 3, 20, 100, 6);
        add(82, "soul_gold", "Soul Gold", "Oro de Almas", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 3, 25, 95, 6);
        add(83, "spectrite", "Spectrite", "Espectralita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 15, 90, 5);
        add(84, "phantomite", "Phantomite", "Fantasmita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 20, 85, 5);
        add(85, "ectoplasm_rock", "Ectoplasmite", "Ectoplasmina", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 15, 80, 5);
        add(86, "torment_ore", "Torment Ore", "Tormenta de Almas", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 10, 75, 4);
        add(87, "bansheeite", "Bansheeite", "Aullido Espectral", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 15, 70, 4);
        add(88, "spirit_quartz", "Spirit Quartz", "Cuarzo Espiritual", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 20, 85, 4);
        add(89, "wraithstone", "Wraithstone", "Piedra Espectral", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 10, 60, 4);
        add(90, "nether_soul_gem", "Nether Soul Gem", "Gema del Purgatorio", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 4, 10, 50, 3);

        // Tiers 91 - 100: Crimson & Warped Forests (Netherrack)
        add(91, "bloodstone", "Bloodstone", "Sanguinaria", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 20, 100, 6);
        add(92, "crimsonite", "Crimsonite", "Carmesita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 15, 95, 6);
        add(93, "twisted_ore", "Twisted Ore", "Retorcidita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 20, 100, 6);
        add(94, "warped_spore_gem", "Warped Gem", "Gema Deformada", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 15, 95, 5);
        add(95, "tendrilium", "Tendrilium", "Tentaculita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 15, 85, 5);
        add(96, "hoglin_tusk_ore", "Hoglin Ivory", "Marfil del Nether", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 20, 90, 4);
        add(97, "mutagenic_ore", "Mutagenic Ore", "Mutagénica", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 15, 80, 4);
        add(98, "sporesteel", "Sporesteel", "Acero Espora", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 10, 70, 4);
        add(99, "fungal_emerald", "Fungal Emerald", "Esmeralda Fúngica", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 10, 60, 3);
        add(100, "nether_heart", "Nether Heart", "Corazón del Nether", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 4, 10, 50, 3);

        // Tiers 101 - 105: Basalt Deltas (Netherrack)
        add(101, "basaltic_iron", "Basaltic Iron", "Hierro Basáltico", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 4, 20, 100, 5);
        add(102, "obsidian_core", "Obsidian Core", "Núcleo de Obsidiana", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 4, 15, 90, 5);
        add(103, "ashstone", "Ashstone", "Piedra Ceniza", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 4, 20, 85, 4);
        add(104, "volcanite_delta", "Delta Volcanite", "Volcanita del Delta", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 4, 15, 75, 4);
        add(105, "tartarus", "Tartarus", "Tártaro", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 4, 10, 60, 3);

        // Tiers 106 - 115: Central End & Midlands (End Stone)
        add(106, "endium", "Endium", "Endio", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 20, 70, 5);
        add(107, "enderite", "Enderite", "Enderita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 15, 65, 5);
        add(108, "purpurite", "Purpurite", "Purpurita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 20, 60, 4);
        add(109, "shulkerite", "Shulkerite", "Shulkerita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 15, 55, 4);
        add(110, "void_quartz", "Void Quartz", "Cuarzo del Vacío", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 20, 65, 4);
        add(111, "teleportium", "Teleportium", "Teleportio", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 15, 60, 4);
        add(112, "chorusite", "Chorusite", "Flor de Coro Cristalina", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 20, 70, 4);
        add(113, "void_pearl_ore", "Void Pearl Ore", "Perla del Vacío", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 15, 50, 3);
        add(114, "dragon_bone_ore", "Dragon Bone Ore", "Hueso de Dragón", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 10, 55, 3);
        add(115, "dragon_breath_gem", "Dragon Breath Gem", "Aliento de Dragón", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 4, 10, 45, 3);

        // Tiers 116 - 125: Outer End Highlands (End Stone)
        add(116, "astralite", "Astralite", "Astralita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 20, 75, 4);
        add(117, "cosmic_dust_ore", "Cosmic Dust Ore", "Polvo Cósmico", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 25, 70, 4);
        add(118, "meteorite", "Meteorite", "Meteorita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 20, 65, 4);
        add(119, "starlight_gem", "Starlight Gem", "Gema Estelar", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 25, 75, 3);
        add(120, "nebula_ore", "Nebula Ore", "Nebulita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 15, 65, 3);
        add(121, "celestite", "Celestite", "Celestita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 20, 60, 3);
        add(122, "eclipse_ore", "Eclipse Ore", "Eclipsio", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 15, 55, 3);
        add(123, "supernova", "Supernova", "Supernova Sólida", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 15, 50, 3);
        add(124, "horizon_ore", "Horizon Ore", "Horizonte de Eventos", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 10, 45, 3);
        add(125, "galacticite", "Galacticite", "Galactita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 4, 10, 40, 3);

        // Tiers 126 - 135: Small End Islands & Void Depths (End Stone)
        add(126, "voidwalker_ore", "Voidwalker", "Caminante del Vacío", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 10, 50, 3);
        add(127, "zero_point", "Zero Point", "Punto Cero", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 10, 45, 3);
        add(128, "singularity_ore", "Singularity", "Singularidad", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 10, 40, 3);
        add(129, "dark_matter", "Dark Matter", "Materia Oscura", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 35, 3);
        add(130, "antimatter_ore", "Antimatter", "Antimateria", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 35, 3);
        add(131, "chronos", "Chronos", "Cronomita", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 30, 2);
        add(132, "spacetime_ore", "Spacetime", "Espacio-Tiempo", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 30, 2);
        add(133, "dimensionite", "Dimensionite", "Dimensionita", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 25, 2);
        add(134, "quantum_ore", "Quantum Ore", "Cuántica", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 25, 2);
        add(135, "infinity_shard_ore", "Infinity Shard", "Fragmento Infinito", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 4, 5, 20, 2);

        // Tiers 136 - 150: Ascended & Cosmic Apex (End Stone)
        add(136, "aetherium", "Aetherium", "Eterio", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 40, 80, 3);
        add(137, "luminite", "Luminite", "Luminita", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 30, 70, 3);
        add(138, "solaris", "Solaris", "Solaris Divina", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 30, 70, 3);
        add(139, "polarium", "Polarium", "Polaris Suprema", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 25, 65, 3);
        add(140, "genesis_ore", "Genesis Ore", "Génesis", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 20, 60, 2);
        add(141, "omnium", "Omnium", "Omnio", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 15, 55, 2);
        add(142, "eternity_gem", "Eternity Gem", "Gema de la Eternidad", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 15, 50, 2);
        add(143, "archon_stone", "Archon Stone", "Piedra Arconte", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 10, 45, 2);
        add(144, "chaos_ore", "Chaos Ore", "Mineral del Caos", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 10, 40, 2);
        add(145, "order_ore", "Order Ore", "Mineral del Orden", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 10, 40, 2);
        add(146, "oblivion", "Oblivion", "Olvido Primordial", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 5, 30, 2);
        add(147, "creationite", "Creationite", "Creacionita", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 5, 30, 2);
        add(148, "nexus_core", "Nexus Core", "Núcleo Nexus", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 5, 25, 2);
        add(149, "apex_titan", "Apex Titan", "Titán Ápex", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 5, 25, 2);
        add(150, "godmetal", "Godmetal", "Metal de los Dioses", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 4, 5, 20, 2);
    }

    private static void add(int tier, String id, String en, String es, Dimension dim, BiomeGroup bg, BaseStone bs, int harvest, int minY, int maxY, int veinSize) {
        TIERS.add(new OreTier(tier, id, en, es, dim, bg, bs, harvest, minY, maxY, veinSize));
    }
}
