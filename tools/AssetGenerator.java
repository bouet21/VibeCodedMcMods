package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class AssetGenerator {

    public enum Dimension { OVERWORLD, NETHER, END }
    public enum BaseStone { STONE, DEEPSLATE, NETHERRACK, END_STONE }
    public enum BiomeGroup {
        PLAINS_FOREST, MOUNTAINS, DESERT_BADLANDS, SWAMP_JUNGLE,
        DEEPSLATE_CAVES, DEEP_DARK,
        NETHER_WASTES, SOUL_SAND_VALLEY, CRIMSON_WARPED, BASALT_DELTAS,
        END_MIDLANDS, END_HIGHLANDS, END_ISLANDS, COSMIC
    }

    public static class OreDef {
        public final int tier;
        public final String id;
        public final String enName;
        public final String esName;
        public final Dimension dimension;
        public final BiomeGroup biomeGroup;
        public final BaseStone baseStone;
        public final int minY;
        public final int maxY;
        public final int veinSize;
        public final int veinsPerChunk;
        public final int harvestLevel;
        public final int colorRgb;

        public OreDef(int tier, String id, String enName, String esName, Dimension dimension,
                      BiomeGroup biomeGroup, BaseStone baseStone, int minY, int maxY,
                      int veinSize, int veinsPerChunk, int harvestLevel, int colorRgb) {
            this.tier = tier;
            this.id = id;
            this.enName = enName;
            this.esName = esName;
            this.dimension = dimension;
            this.biomeGroup = biomeGroup;
            this.baseStone = baseStone;
            this.minY = minY;
            this.maxY = maxY;
            this.veinSize = veinSize;
            this.veinsPerChunk = veinsPerChunk;
            this.harvestLevel = harvestLevel;
            this.colorRgb = colorRgb;
        }
    }

    public static List<OreDef> getOres() {
        List<OreDef> list = new ArrayList<>();
        // Tiers 1 - 10: Plains & Forests (Stone)
        list.add(new OreDef(1, "copperite", "Copperite", "Cobreprita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 30, 100, 9, 12, 1, 0xD97843));
        list.add(new OreDef(2, "tin", "Tin", "Estaño", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 25, 90, 8, 10, 1, 0xC0C8D0));
        list.add(new OreDef(3, "bronzeite", "Bronzeite", "Broncita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 20, 80, 8, 9, 1, 0xA07040));
        list.add(new OreDef(4, "galena", "Galena", "Galena", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 15, 75, 7, 9, 1, 0x7A7A85));
        list.add(new OreDef(5, "zincite", "Zincite", "Cincita", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 15, 70, 7, 8, 1, 0x98A890));
        list.add(new OreDef(6, "bismuth", "Bismuth", "Bismuto", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 10, 65, 7, 8, 2, 0xD080A0));
        list.add(new OreDef(7, "silver", "Silver", "Plata", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 10, 60, 6, 7, 2, 0xE6EDF5));
        list.add(new OreDef(8, "electrum", "Electrum", "Electro", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 10, 60, 6, 7, 2, 0xEAD865));
        list.add(new OreDef(9, "nickel", "Nickel", "Níquel", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 5, 55, 6, 6, 2, 0x8C887B));
        list.add(new OreDef(10, "invar", "Invar", "Invar", Dimension.OVERWORLD, BiomeGroup.PLAINS_FOREST, BaseStone.STONE, 0, 50, 6, 6, 2, 0x7E8582));

        // Tiers 11 - 20: Mountains & Snow (Stone)
        list.add(new OreDef(11, "froststeel", "Froststeel", "Acero Glacial", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 50, 180, 8, 8, 2, 0x9BD5FF));
        list.add(new OreDef(12, "cryolite", "Cryolite", "Criolita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 60, 190, 7, 8, 2, 0xC2EBFF));
        list.add(new OreDef(13, "glacite", "Glacite", "Glacita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 40, 150, 7, 7, 2, 0x70C4E8));
        list.add(new OreDef(14, "cobalt", "Cobalt", "Cobalto", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 40, 160, 6, 7, 2, 0x1E56C8));
        list.add(new OreDef(15, "aquamarine", "Aquamarine", "Aguamarina", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 30, 140, 6, 6, 2, 0x33D6C4));
        list.add(new OreDef(16, "sapphire", "Sapphire", "Zafiro", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 50, 180, 5, 6, 3, 0x1B35B8));
        list.add(new OreDef(17, "permafrostium", "Permafrostium", "Permafrostio", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 30, 120, 6, 6, 3, 0x569BB8));
        list.add(new OreDef(18, "borealis", "Borealis", "Borealita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 70, 220, 5, 5, 3, 0x64FFBE));
        list.add(new OreDef(19, "blizzardite", "Blizzardite", "Ventisquita", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 80, 240, 5, 5, 3, 0xD8F0FF));
        list.add(new OreDef(20, "absolute_zero", "Absolute Zero", "Cero Absoluto", Dimension.OVERWORLD, BiomeGroup.MOUNTAINS, BaseStone.STONE, 90, 256, 4, 4, 3, 0x00E1D9));

        // Tiers 21 - 30: Deserts & Badlands (Stone)
        list.add(new OreDef(21, "pyrite", "Pyrite", "Pirita Solar", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 20, 80, 8, 8, 2, 0xCCA625));
        list.add(new OreDef(22, "amber", "Amber", "Ámbar Fósil", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 25, 90, 7, 8, 2, 0xE68A00));
        list.add(new OreDef(23, "sunstone", "Sunstone", "Piedra Solar", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 30, 100, 7, 7, 2, 0xFFA033));
        list.add(new OreDef(24, "desert_topaz", "Desert Topaz", "Topacio Arenoso", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 15, 75, 6, 7, 2, 0xE6C34D));
        list.add(new OreDef(25, "jasper", "Jasper", "Jaspe Rojo", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 35, 110, 6, 6, 2, 0xB33B24));
        list.add(new OreDef(26, "cinnabar", "Cinnabar", "Cinabrio", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 20, 90, 6, 6, 2, 0x8F1D1D));
        list.add(new OreDef(27, "terracotta_ore", "Terracotite", "Terracotita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 40, 120, 7, 7, 2, 0xC96E42));
        list.add(new OreDef(28, "sandstormite", "Sandstormite", "Tormentita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 20, 80, 5, 5, 3, 0xDBB167));
        list.add(new OreDef(29, "solarite", "Solarite", "Solarita", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 30, 100, 5, 5, 3, 0xFFCC00));
        list.add(new OreDef(30, "mirage_core", "Mirage Core", "Espejismo", Dimension.OVERWORLD, BiomeGroup.DESERT_BADLANDS, BaseStone.STONE, 30, 110, 4, 4, 3, 0xE8A2D8));

        // Tiers 31 - 40: Swamps, Jungles & Oceans (Stone / Deepslate)
        list.add(new OreDef(31, "bog_iron", "Bog Iron", "Hierro de Ciénaga", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 10, 60, 8, 8, 2, 0x634E35));
        list.add(new OreDef(32, "malachite", "Malachite", "Malaquita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 15, 75, 7, 8, 2, 0x1FA86A));
        list.add(new OreDef(33, "jade", "Jade", "Jade Imperial", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 10, 70, 7, 7, 2, 0x38D480));
        list.add(new OreDef(34, "serpentine", "Serpentine", "Serpentina", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 0, 50, 6, 7, 2, 0x529148));
        list.add(new OreDef(35, "toxicite", "Toxicite", "Toxicita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, -15, 30, 6, 6, 2, 0x82DE2A));
        list.add(new OreDef(36, "coralium", "Coralium", "Coralio", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.STONE, 10, 50, 6, 6, 2, 0xF24E6B));
        list.add(new OreDef(37, "abyssium", "Abyssium", "Abisio Marino", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, -30, 20, 5, 5, 3, 0x103E78));
        list.add(new OreDef(38, "prismatic_ore", "Prismatite", "Prismatita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, -35, 10, 5, 5, 3, 0x47D4BA));
        list.add(new OreDef(39, "chlorophyte", "Chlorophyte", "Clorofita", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, -40, 0, 5, 5, 3, 0x22E84D));
        list.add(new OreDef(40, "jungle_heart", "Jungle Heart", "Corazón de Jungla", Dimension.OVERWORLD, BiomeGroup.SWAMP_JUNGLE, BaseStone.DEEPSLATE, -45, -10, 4, 4, 3, 0x0FA840));

        // Tiers 41 - 50: Deepslate Caverns (Deepslate)
        list.add(new OreDef(41, "titanium", "Titanium", "Titanio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -20, 10, 6, 7, 3, 0x9EA3A8));
        list.add(new OreDef(42, "platinum", "Platinum", "Platino", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -25, 5, 6, 6, 3, 0xD9E5EB));
        list.add(new OreDef(43, "tungsten", "Tungsten", "Wolframio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -30, 0, 6, 6, 3, 0x4F5959));
        list.add(new OreDef(44, "palladium", "Palladium", "Paladio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -35, -5, 5, 6, 3, 0xDE6D3A));
        list.add(new OreDef(45, "osmium", "Osmium", "Osmio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -40, -10, 5, 5, 3, 0x58788C));
        list.add(new OreDef(46, "iridium", "Iridium", "Iridio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -45, -15, 5, 5, 3, 0xD9D4E8));
        list.add(new OreDef(47, "mythril", "Mythril", "Mithril", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -50, -20, 5, 5, 3, 0x3CA2D9));
        list.add(new OreDef(48, "orichalcum", "Orichalcum", "Oricalco", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -55, -25, 4, 4, 3, 0xE64A7E));
        list.add(new OreDef(49, "adamantite", "Adamantite", "Adamantita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -58, -30, 4, 4, 4, 0xC42323));
        list.add(new OreDef(50, "runite", "Runite", "Runita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -60, -35, 4, 4, 4, 0x24C7BA));

        // Tiers 51 - 60: Deepslate Abyss & Bedrock Layer (Deepslate)
        list.add(new OreDef(51, "darksteel", "Darksteel", "Acero Oscuro", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -40, 5, 5, 4, 0x2B2D33));
        list.add(new OreDef(52, "onyx", "Onyx", "Ónice Profundo", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -45, 4, 5, 4, 0x17171C));
        list.add(new OreDef(53, "bedrockium_dust", "Bedrockium", "Polvo de Lecho", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -55, 4, 4, 4, 0x3D3945));
        list.add(new OreDef(54, "voidstone", "Voidstone", "Piedra Vacía", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -45, 4, 4, 4, 0x412E57));
        list.add(new OreDef(55, "obsidian_infused", "Obsidianite", "Obsidianita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -40, 4, 4, 4, 0x331E54));
        list.add(new OreDef(56, "netherite_subgrade", "Sub-Netherite", "Sub-Netherita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -50, 4, 4, 4, 0x5C463D));
        list.add(new OreDef(57, "abyssalite", "Abyssalite", "Abisalita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -48, 4, 4, 4, 0x1A2B3C));
        list.add(new OreDef(58, "tenebrium", "Tenebrium", "Tenebrio", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -52, 3, 3, 4, 0x231430));
        list.add(new OreDef(59, "gravitite", "Gravitite", "Gravitita", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -54, 3, 3, 4, 0x9952D4));
        list.add(new OreDef(60, "coreium", "Coreium", "Mineral del Núcleo", Dimension.OVERWORLD, BiomeGroup.DEEPSLATE_CAVES, BaseStone.DEEPSLATE, -64, -55, 3, 3, 4, 0xFF5522));

        // Tiers 61 - 70: The Deep Dark (Deepslate)
        list.add(new OreDef(61, "echo_ore", "Echo Ore", "Ecosita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -35, 5, 5, 4, 0x094852));
        list.add(new OreDef(62, "sculk_infused", "Sculkite", "Esculcita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -40, 4, 5, 4, 0x03383B));
        list.add(new OreDef(63, "wardenite", "Wardenite", "Guardianita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -42, 4, 4, 4, 0x0D6970));
        list.add(new OreDef(64, "resonance", "Resonance", "Resonita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -45, 4, 4, 4, 0x1BE3C9));
        list.add(new OreDef(65, "silentium", "Silentium", "Silencio", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -48, 3, 4, 4, 0x14202B));
        list.add(new OreDef(66, "soulbound_ore", "Soulbound", "Almarita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -50, 3, 4, 4, 0x247885));
        list.add(new OreDef(67, "shrieker_crystal", "Shriekerite", "Aullita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -50, 3, 3, 4, 0x3BEBE0));
        list.add(new OreDef(68, "shadow_platinum", "Shadow Platinum", "Platino Sombrío", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -52, 3, 3, 4, 0x577A82));
        list.add(new OreDef(69, "catalyst_ore", "Catalyst Ore", "Catalita", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -55, 3, 3, 4, 0x124742));
        list.add(new OreDef(70, "primordial_sculk", "Primordial Sculk", "Sculk Primordial", Dimension.OVERWORLD, BiomeGroup.DEEP_DARK, BaseStone.DEEPSLATE, -64, -58, 3, 3, 4, 0x00FFD5));

        // Tiers 71 - 80: Nether Wastes (Netherrack)
        list.add(new OreDef(71, "nether_sulfur", "Nether Sulfur", "Azufre Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 10, 110, 8, 8, 3, 0xD4B517));
        list.add(new OreDef(72, "ignisite", "Ignisite", "Ignisita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 15, 105, 7, 7, 3, 0xE65319));
        list.add(new OreDef(73, "cinderstone", "Cinderstone", "Cenizalita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 20, 100, 7, 7, 3, 0x733822));
        list.add(new OreDef(74, "magmacite", "Magmacite", "Magmacita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 10, 80, 6, 7, 3, 0xDE7C12));
        list.add(new OreDef(75, "pyrolite", "Pyrolite", "Pirolita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 25, 95, 6, 6, 3, 0xFA3A14));
        list.add(new OreDef(76, "hellfire_ore", "Hellfire Ore", "Fuego Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 15, 85, 5, 6, 3, 0xFF2200));
        list.add(new OreDef(77, "blazesteel", "Blazesteel", "Acero de Llamas", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 20, 75, 5, 5, 4, 0xF59E0B));
        list.add(new OreDef(78, "caldera_gem", "Caldera Gem", "Gema de Caldera", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 15, 70, 4, 5, 4, 0xE63946));
        list.add(new OreDef(79, "vulcanite", "Vulcanite", "Vulcanita", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 10, 65, 4, 4, 4, 0xB51717));
        list.add(new OreDef(80, "infernal_core", "Infernal Core", "Núcleo Infernal", Dimension.NETHER, BiomeGroup.NETHER_WASTES, BaseStone.NETHERRACK, 10, 50, 4, 4, 4, 0xFF6B1A));

        // Tiers 81 - 90: Soul Sand Valley (Netherrack)
        list.add(new OreDef(81, "ghastite", "Ghastite", "Lagrimita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 20, 100, 6, 6, 3, 0xE0E0E0));
        list.add(new OreDef(82, "soul_gold", "Soul Gold", "Oro de Almas", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 25, 95, 6, 6, 3, 0x429688));
        list.add(new OreDef(83, "spectrite", "Spectrite", "Espectralita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 15, 90, 5, 6, 4, 0x36B09B));
        list.add(new OreDef(84, "phantomite", "Phantomite", "Fantasmita", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 20, 85, 5, 5, 4, 0x599E91));
        list.add(new OreDef(85, "ectoplasm_rock", "Ectoplasmite", "Ectoplasmina", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 15, 80, 5, 5, 4, 0x76D6C6));
        list.add(new OreDef(86, "torment_ore", "Torment Ore", "Tormenta de Almas", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 10, 75, 4, 5, 4, 0x1E5950));
        list.add(new OreDef(87, "bansheeite", "Bansheeite", "Aullido Espectral", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 15, 70, 4, 4, 4, 0x97E0D3));
        list.add(new OreDef(88, "spirit_quartz", "Spirit Quartz", "Cuarzo Espiritual", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 20, 85, 4, 4, 4, 0xC2FAF1));
        list.add(new OreDef(89, "wraithstone", "Wraithstone", "Piedra Espectral", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 10, 60, 4, 4, 4, 0x2A7067));
        list.add(new OreDef(90, "nether_soul_gem", "Nether Soul Gem", "Gema del Purgatorio", Dimension.NETHER, BiomeGroup.SOUL_SAND_VALLEY, BaseStone.NETHERRACK, 10, 50, 3, 3, 4, 0x16A694));

        // Tiers 91 - 100: Crimson & Warped Forests (Netherrack)
        list.add(new OreDef(91, "bloodstone", "Bloodstone", "Sanguinaria", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 20, 100, 6, 6, 4, 0x8C141E));
        list.add(new OreDef(92, "crimsonite", "Crimsonite", "Carmesita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 15, 95, 6, 6, 4, 0xAD1829));
        list.add(new OreDef(93, "twisted_ore", "Twisted Ore", "Retorcidita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 20, 100, 6, 6, 4, 0x177D70));
        list.add(new OreDef(94, "warped_spore_gem", "Warped Gem", "Gema Deformada", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 15, 95, 5, 5, 4, 0x119E8D));
        list.add(new OreDef(95, "tendrilium", "Tendrilium", "Tentaculita", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 15, 85, 5, 5, 4, 0x991938));
        list.add(new OreDef(96, "hoglin_tusk_ore", "Hoglin Ivory", "Marfil del Nether", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 20, 90, 4, 5, 4, 0xD9C0A3));
        list.add(new OreDef(97, "mutagenic_ore", "Mutagenic Ore", "Mutagénica", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 15, 80, 4, 4, 4, 0x25C2B1));
        list.add(new OreDef(98, "sporesteel", "Sporesteel", "Acero Espora", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 10, 70, 4, 4, 4, 0x1F8A7D));
        list.add(new OreDef(99, "fungal_emerald", "Fungal Emerald", "Esmeralda Fúngica", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 10, 60, 3, 4, 4, 0x0EB86B));
        list.add(new OreDef(100, "nether_heart", "Nether Heart", "Corazón del Nether", Dimension.NETHER, BiomeGroup.CRIMSON_WARPED, BaseStone.NETHERRACK, 10, 50, 3, 3, 4, 0xD61142));

        // Tiers 101 - 105: Basalt Deltas (Netherrack)
        list.add(new OreDef(101, "basaltic_iron", "Basaltic Iron", "Hierro Basáltico", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 20, 100, 5, 5, 4, 0x545459));
        list.add(new OreDef(102, "obsidian_core", "Obsidian Core", "Núcleo de Obsidiana", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 15, 90, 5, 5, 4, 0x25163D));
        list.add(new OreDef(103, "ashstone", "Ashstone", "Piedra Ceniza", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 20, 85, 4, 4, 4, 0x8A8379));
        list.add(new OreDef(104, "volcanite_delta", "Delta Volcanite", "Volcanita del Delta", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 15, 75, 4, 4, 4, 0xE04F16));
        list.add(new OreDef(105, "tartarus", "Tartarus", "Tártaro", Dimension.NETHER, BiomeGroup.BASALT_DELTAS, BaseStone.NETHERRACK, 10, 60, 3, 3, 4, 0x4D0E1C));

        // Tiers 106 - 115: Central End & Midlands (End Stone)
        list.add(new OreDef(106, "endium", "Endium", "Endio", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 20, 70, 5, 6, 4, 0x9C7AD1));
        list.add(new OreDef(107, "enderite", "Enderite", "Enderita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 15, 65, 5, 5, 4, 0x22877B));
        list.add(new OreDef(108, "purpurite", "Purpurite", "Purpurita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 20, 60, 4, 5, 4, 0xB34FB8));
        list.add(new OreDef(109, "shulkerite", "Shulkerite", "Shulkerita", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 15, 55, 4, 5, 4, 0x995C99));
        list.add(new OreDef(110, "void_quartz", "Void Quartz", "Cuarzo del Vacío", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 20, 65, 4, 4, 4, 0x563870));
        list.add(new OreDef(111, "teleportium", "Teleportium", "Teleportio", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 15, 60, 4, 4, 4, 0x1ED1B6));
        list.add(new OreDef(112, "chorusite", "Chorusite", "Flor de Coro Cristalina", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 20, 70, 4, 4, 4, 0xD47BD9));
        list.add(new OreDef(113, "void_pearl_ore", "Void Pearl Ore", "Perla del Vacío", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 15, 50, 3, 4, 4, 0x1A5952));
        list.add(new OreDef(114, "dragon_bone_ore", "Dragon Bone Ore", "Hueso de Dragón", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 10, 55, 3, 3, 4, 0xCCC5B4));
        list.add(new OreDef(115, "dragon_breath_gem", "Dragon Breath Gem", "Aliento de Dragón", Dimension.END, BiomeGroup.END_MIDLANDS, BaseStone.END_STONE, 10, 45, 3, 3, 4, 0xDE3CE3));

        // Tiers 116 - 125: Outer End Highlands (End Stone)
        list.add(new OreDef(116, "astralite", "Astralite", "Astralita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 20, 75, 4, 5, 4, 0x6E85E6));
        list.add(new OreDef(117, "cosmic_dust_ore", "Cosmic Dust Ore", "Polvo Cósmico", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 25, 70, 4, 5, 4, 0x9B6EE6));
        list.add(new OreDef(118, "meteorite", "Meteorite", "Meteorita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 20, 65, 4, 4, 4, 0x6E574B));
        list.add(new OreDef(119, "starlight_gem", "Starlight Gem", "Gema Estelar", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 25, 75, 3, 4, 4, 0xFFF37A));
        list.add(new OreDef(120, "nebula_ore", "Nebula Ore", "Nebulita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 15, 65, 3, 4, 4, 0xCF42B9));
        list.add(new OreDef(121, "celestite", "Celestite", "Celestita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 20, 60, 3, 3, 4, 0x73C5E6));
        list.add(new OreDef(122, "eclipse_ore", "Eclipse Ore", "Eclipsio", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 15, 55, 3, 3, 4, 0x36214A));
        list.add(new OreDef(123, "supernova", "Supernova", "Supernova Sólida", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 15, 50, 3, 3, 4, 0xFF8A14));
        list.add(new OreDef(124, "horizon_ore", "Horizon Ore", "Horizonte de Eventos", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 10, 45, 3, 3, 4, 0x1F1F38));
        list.add(new OreDef(125, "galacticite", "Galacticite", "Galactita", Dimension.END, BiomeGroup.END_HIGHLANDS, BaseStone.END_STONE, 10, 40, 3, 3, 4, 0x483296));

        // Tiers 126 - 135: Small End Islands & Void Depths (End Stone)
        list.add(new OreDef(126, "voidwalker_ore", "Voidwalker", "Caminante del Vacío", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 10, 50, 3, 4, 4, 0x22133B));
        list.add(new OreDef(127, "zero_point", "Zero Point", "Punto Cero", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 10, 45, 3, 3, 4, 0x2AE0D1));
        list.add(new OreDef(128, "singularity_ore", "Singularity", "Singularidad", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 10, 40, 3, 3, 4, 0x120D24));
        list.add(new OreDef(129, "dark_matter", "Dark Matter", "Materia Oscura", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 35, 3, 3, 4, 0x18101F));
        list.add(new OreDef(130, "antimatter_ore", "Antimatter", "Antimateria", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 35, 3, 3, 4, 0xE61E53));
        list.add(new OreDef(131, "chronos", "Chronos", "Cronomita", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 30, 2, 3, 4, 0xE6BF33));
        list.add(new OreDef(132, "spacetime_ore", "Spacetime", "Espacio-Tiempo", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 30, 2, 3, 4, 0x613CE0));
        list.add(new OreDef(133, "dimensionite", "Dimensionite", "Dimensionita", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 25, 2, 2, 4, 0x2DE3A3));
        list.add(new OreDef(134, "quantum_ore", "Quantum Ore", "Cuántica", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 25, 2, 2, 4, 0x20C0D4));
        list.add(new OreDef(135, "infinity_shard_ore", "Infinity Shard", "Fragmento Infinito", Dimension.END, BiomeGroup.END_ISLANDS, BaseStone.END_STONE, 5, 20, 2, 2, 4, 0xEB5CE8));

        // Tiers 136 - 150: Ascended & Cosmic Apex (End Stone)
        list.add(new OreDef(136, "aetherium", "Aetherium", "Eterio", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 40, 80, 3, 3, 4, 0x8AE8FF));
        list.add(new OreDef(137, "luminite", "Luminite", "Luminita", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 30, 70, 3, 3, 4, 0x5CFFB0));
        list.add(new OreDef(138, "solaris", "Solaris", "Solaris Divina", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 30, 70, 3, 3, 4, 0xFFD23B));
        list.add(new OreDef(139, "polarium", "Polarium", "Polaris Suprema", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 25, 65, 3, 3, 4, 0x7DF9FF));
        list.add(new OreDef(140, "genesis_ore", "Genesis Ore", "Génesis", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 20, 60, 2, 2, 4, 0xE8F768));
        list.add(new OreDef(141, "omnium", "Omnium", "Omnio", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 15, 55, 2, 2, 4, 0xB66DF0));
        list.add(new OreDef(142, "eternity_gem", "Eternity Gem", "Gema de la Eternidad", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 15, 50, 2, 2, 4, 0x57EDCE));
        list.add(new OreDef(143, "archon_stone", "Archon Stone", "Piedra Arconte", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 10, 45, 2, 2, 4, 0xF05481));
        list.add(new OreDef(144, "chaos_ore", "Chaos Ore", "Mineral del Caos", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 10, 40, 2, 2, 4, 0x871148));
        list.add(new OreDef(145, "order_ore", "Order Ore", "Mineral del Orden", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 10, 40, 2, 2, 4, 0x367FF5));
        list.add(new OreDef(146, "oblivion", "Oblivion", "Olvido Primordial", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 5, 30, 2, 2, 4, 0x211726));
        list.add(new OreDef(147, "creationite", "Creationite", "Creacionita", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 5, 30, 2, 2, 4, 0x48DE7A));
        list.add(new OreDef(148, "nexus_core", "Nexus Core", "Núcleo Nexus", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 5, 25, 2, 2, 4, 0xF2461D));
        list.add(new OreDef(149, "apex_titan", "Apex Titan", "Titán Ápex", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 5, 25, 2, 2, 4, 0xD9A321));
        list.add(new OreDef(150, "godmetal", "Godmetal", "Metal de los Dioses", Dimension.END, BiomeGroup.COSMIC, BaseStone.END_STONE, 5, 20, 2, 2, 4, 0xFFE885));

        return list;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Chain Ores Asset & Data Generation for 150 ores...");
        Path resources = Paths.get("src/main/resources");
        Path assets = resources.resolve("assets/chainores");
        Path data = resources.resolve("data/chainores");

        // Directories
        Files.createDirectories(assets.resolve("textures/block"));
        Files.createDirectories(assets.resolve("textures/item"));
        Files.createDirectories(assets.resolve("models/block"));
        Files.createDirectories(assets.resolve("models/item"));
        Files.createDirectories(assets.resolve("items"));
        Files.createDirectories(assets.resolve("blockstates"));
        Files.createDirectories(assets.resolve("lang"));

        Files.createDirectories(data.resolve("loot_table/blocks"));
        Files.createDirectories(data.resolve("recipe"));
        Files.createDirectories(data.resolve("worldgen/configured_feature"));
        Files.createDirectories(data.resolve("worldgen/placed_feature"));

        List<OreDef> ores = getOres();
        System.out.println("Total ores configured: " + ores.size());

        StringBuilder enLang = new StringBuilder("{\n  \"itemGroup.chainores.main\": \"Chain Ores Progression\",\n");
        StringBuilder esLang = new StringBuilder("{\n  \"itemGroup.chainores.main\": \"Progresión de Minerales en Cadena\",\n");

        enLang.append("  \"item.chainores.guide_book\": \"Chain Ores Codex\",\n");
        enLang.append("  \"item.chainores.guide_book.tooltip1\": \"Comprehensive guide to all 150 progressive ores\",\n");
        enLang.append("  \"item.chainores.guide_book.tooltip2\": \"Right-click to open guide & photos\",\n");
        enLang.append("  \"message.chainores.welcome_book\": \"Welcome to Chain Ores! You received the Ore Codex with guides, photos, biomes & recipes.\",\n");

        esLang.append("  \"item.chainores.guide_book\": \"Códice de Minerales en Cadena\",\n");
        esLang.append("  \"item.chainores.guide_book.tooltip1\": \"Guía completa de los 150 minerales en progresión\",\n");
        esLang.append("  \"item.chainores.guide_book.tooltip2\": \"Clic derecho para abrir guía y fotos\",\n");
        esLang.append("  \"message.chainores.welcome_book\": \"¡Bienvenido a Chain Ores! Has recibido el Códice con fotos, biomas y recetas de los 150 minerales.\",\n");

        // Guide book item model & definition
        writeJson(assets.resolve("models/item/guide_book.json"),
            "{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"chainores:item/guide_book\"}}");
        writeJson(assets.resolve("items/guide_book.json"),
            "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:item/guide_book\"}}");

        // Template icon & item
        generateTemplateTexture(assets.resolve("textures/item/chain_smithing_template.png").toFile());
        writeJson(assets.resolve("models/item/chain_smithing_template.json"),
            "{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"chainores:item/chain_smithing_template\"}}");
        writeJson(assets.resolve("items/chain_smithing_template.json"),
            "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:item/chain_smithing_template\"}}");
        enLang.append("  \"item.chainores.chain_smithing_template\": \"Chain Upgrade Smithing Template\",\n");
        esLang.append("  \"item.chainores.chain_smithing_template\": \"Plantilla de Mejora de Minerales en Cadena\",\n");

        // Crafting recipe for the template
        writeJson(data.resolve("recipe/chain_smithing_template.json"),
            "{\n" +
            "  \"type\": \"minecraft:crafting_shaped\",\n" +
            "  \"pattern\": [\n" +
            "    \"#D#\",\n" +
            "    \"#C#\",\n" +
            "    \"###\"\n" +
            "  ],\n" +
            "  \"key\": {\n" +
            "    \"#\": {\"item\": \"minecraft:diamond\"},\n" +
            "    \"D\": {\"item\": \"minecraft:netherite_upgrade_smithing_template\"},\n" +
            "    \"C\": {\"item\": \"minecraft:copper_ingot\"}\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"id\": \"chainores:chain_smithing_template\",\n" +
            "    \"count\": 2\n" +
            "  }\n" +
            "}");

        // Duplicate recipe for the template (like vanilla netherite upgrade)
        writeJson(data.resolve("recipe/chain_smithing_template_duplicate.json"),
            "{\n" +
            "  \"type\": \"minecraft:crafting_shaped\",\n" +
            "  \"pattern\": [\n" +
            "    \"#T#\",\n" +
            "    \"#C#\",\n" +
            "    \"###\"\n" +
            "  ],\n" +
            "  \"key\": {\n" +
            "    \"#\": {\"item\": \"minecraft:diamond\"},\n" +
            "    \"T\": {\"item\": \"chainores:chain_smithing_template\"},\n" +
            "    \"C\": {\"item\": \"minecraft:cobblestone\"}\n" +
            "  },\n" +
            "  \"result\": {\n" +
            "    \"id\": \"chainores:chain_smithing_template\",\n" +
            "    \"count\": 2\n" +
            "  }\n" +
            "}");

        for (int i = 0; i < ores.size(); i++) {
            OreDef ore = ores.get(i);
            String oreBlockId = "ore_tier_" + ore.tier;
            String storageBlockId = "block_tier_" + ore.tier;
            String rawItemId = "raw_tier_" + ore.tier;
            String ingotItemId = "ingot_tier_" + ore.tier;

            // 1. Textures
            generateOreTexture(assets.resolve("textures/block/" + oreBlockId + ".png").toFile(), ore.baseStone, ore.colorRgb);
            generateStorageTexture(assets.resolve("textures/block/" + storageBlockId + ".png").toFile(), ore.colorRgb);
            generateRawTexture(assets.resolve("textures/item/" + rawItemId + ".png").toFile(), ore.colorRgb);
            generateIngotTexture(assets.resolve("textures/item/" + ingotItemId + ".png").toFile(), ore.colorRgb);

            // 2. Blockstates
            writeJson(assets.resolve("blockstates/" + oreBlockId + ".json"),
                "{\"variants\": {\"\": {\"model\": \"chainores:block/" + oreBlockId + "\"}}}");
            writeJson(assets.resolve("blockstates/" + storageBlockId + ".json"),
                "{\"variants\": {\"\": {\"model\": \"chainores:block/" + storageBlockId + "\"}}}");

            // 3. Block Models
            writeJson(assets.resolve("models/block/" + oreBlockId + ".json"),
                "{\"parent\": \"minecraft:block/cube_all\", \"textures\": {\"all\": \"chainores:block/" + oreBlockId + "\"}}");
            writeJson(assets.resolve("models/block/" + storageBlockId + ".json"),
                "{\"parent\": \"minecraft:block/cube_all\", \"textures\": {\"all\": \"chainores:block/" + storageBlockId + "\"}}");

            // 4. Item Models
            writeJson(assets.resolve("models/item/" + oreBlockId + ".json"),
                "{\"parent\": \"chainores:block/" + oreBlockId + "\"}");
            writeJson(assets.resolve("models/item/" + storageBlockId + ".json"),
                "{\"parent\": \"chainores:block/" + storageBlockId + "\"}");
            writeJson(assets.resolve("models/item/" + rawItemId + ".json"),
                "{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"chainores:item/" + rawItemId + "\"}}");
            writeJson(assets.resolve("models/item/" + ingotItemId + ".json"),
                "{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"chainores:item/" + ingotItemId + "\"}}");

            // 4b. 1.21.2+ Item Definitions (assets/chainores/items/)
            writeJson(assets.resolve("items/" + oreBlockId + ".json"),
                "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:block/" + oreBlockId + "\"}}");
            writeJson(assets.resolve("items/" + storageBlockId + ".json"),
                "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:block/" + storageBlockId + "\"}}");
            writeJson(assets.resolve("items/" + rawItemId + ".json"),
                "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:item/" + rawItemId + "\"}}");
            writeJson(assets.resolve("items/" + ingotItemId + ".json"),
                "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:item/" + ingotItemId + "\"}}");

            // 5. Loot Tables (Silk Touch drops ore, otherwise drops raw item with fortune)
            writeJson(data.resolve("loot_table/blocks/" + oreBlockId + ".json"),
                generateOreLootTable(oreBlockId, rawItemId));
            writeJson(data.resolve("loot_table/blocks/" + storageBlockId + ".json"),
                "{\"type\": \"minecraft:block\", \"pools\": [{\"rolls\": 1.0, \"entries\": [{\"type\": \"minecraft:item\", \"name\": \"chainores:" + storageBlockId + "\"}], \"conditions\": [{\"condition\": \"minecraft:survives_explosion\"}]}]}");

            // 6. Recipes:
            // Storage Block crafting (9 ingots <-> 1 block)
            writeJson(data.resolve("recipe/" + storageBlockId + ".json"),
                "{\n" +
                "  \"type\": \"minecraft:crafting_shaped\",\n" +
                "  \"pattern\": [\"###\", \"###\", \"###\"],\n" +
                "  \"key\": {\"#\": {\"item\": \"chainores:" + ingotItemId + "\"}},\n" +
                "  \"result\": {\"id\": \"chainores:" + storageBlockId + "\", \"count\": 1}\n" +
                "}");
            writeJson(data.resolve("recipe/" + ingotItemId + "_from_block.json"),
                "{\n" +
                "  \"type\": \"minecraft:crafting_shapeless\",\n" +
                "  \"ingredients\": [{\"item\": \"chainores:" + storageBlockId + "\"}],\n" +
                "  \"result\": {\"id\": \"chainores:" + ingotItemId + "\", \"count\": 9}\n" +
                "}");

            // Progression Smithing Table Recipe:
            // Base = previous tier ingot (Tier 1 uses copper_ingot or iron_ingot)
            // Addition = current raw ore
            // Result = current ingot
            String prevIngot = (ore.tier == 1) ? "minecraft:copper_ingot" : "chainores:ingot_tier_" + (ore.tier - 1);
            writeJson(data.resolve("recipe/smithing_tier_" + ore.tier + ".json"),
                "{\n" +
                "  \"type\": \"minecraft:smithing_transform\",\n" +
                "  \"template\": {\"item\": \"chainores:chain_smithing_template\"},\n" +
                "  \"base\": {\"item\": \"" + prevIngot + "\"},\n" +
                "  \"addition\": {\"item\": \"chainores:" + rawItemId + "\"},\n" +
                "  \"result\": {\"id\": \"chainores:" + ingotItemId + "\", \"count\": 1}\n" +
                "}");

            // Secondary Alternative Recipe (Crafting table: previous ingot + 4 raw ores)
            writeJson(data.resolve("recipe/crafting_tier_" + ore.tier + ".json"),
                "{\n" +
                "  \"type\": \"minecraft:crafting_shaped\",\n" +
                "  \"pattern\": [\n" +
                "    \" R \",\n" +
                "    \"RPR\",\n" +
                "    \" R \"\n" +
                "  ],\n" +
                "  \"key\": {\n" +
                "    \"P\": {\"item\": \"" + prevIngot + "\"},\n" +
                "    \"R\": {\"item\": \"chainores:" + rawItemId + "\"}\n" +
                "  },\n" +
                "  \"result\": {\"id\": \"chainores:" + ingotItemId + "\", \"count\": 1}\n" +
                "}");

            // 7. Worldgen (Configured Feature & Placed Feature)
            String targetBlock = switch (ore.baseStone) {
                case STONE -> "minecraft:stone_ore_replaceables";
                case DEEPSLATE -> "minecraft:deepslate_ore_replaceables";
                case NETHERRACK -> "minecraft:netherrack";
                case END_STONE -> "minecraft:base_stone_end";
            };

            writeJson(data.resolve("worldgen/configured_feature/" + oreBlockId + ".json"),
                "{\n" +
                "  \"type\": \"minecraft:ore\",\n" +
                "  \"config\": {\n" +
                "    \"size\": " + ore.veinSize + ",\n" +
                "    \"discard_chance_on_air_exposure\": 0.0,\n" +
                "    \"targets\": [\n" +
                "      {\n" +
                "        \"target\": {\"predicate_type\": \"minecraft:tag_match\", \"tag\": \"" + targetBlock + "\"},\n" +
                "        \"state\": {\"Name\": \"chainores:" + oreBlockId + "\"}\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

            writeJson(data.resolve("worldgen/placed_feature/" + oreBlockId + ".json"),
                "{\n" +
                "  \"feature\": \"chainores:" + oreBlockId + "\",\n" +
                "  \"placement\": [\n" +
                "    {\"type\": \"minecraft:count\", \"count\": " + ore.veinsPerChunk + "},\n" +
                "    {\"type\": \"minecraft:in_square\"},\n" +
                "    {\n" +
                "      \"type\": \"minecraft:height_range\",\n" +
                "      \"height\": {\n" +
                "        \"type\": \"minecraft:uniform\",\n" +
                "        \"min_inclusive\": {\"absolute\": " + ore.minY + "},\n" +
                "        \"max_inclusive\": {\"absolute\": " + ore.maxY + "}\n" +
                "      }\n" +
                "    },\n" +
                "    {\"type\": \"minecraft:biome\"}\n" +
                "  ]\n" +
                "}");

            // 8. Lang
            enLang.append("  \"block.chainores.").append(oreBlockId).append("\": \"T").append(ore.tier).append(" ").append(ore.enName).append(" Ore\",\n");
            enLang.append("  \"item.chainores.").append(oreBlockId).append("\": \"T").append(ore.tier).append(" ").append(ore.enName).append(" Ore\",\n");
            enLang.append("  \"block.chainores.").append(storageBlockId).append("\": \"Block of ").append(ore.enName).append("\",\n");
            enLang.append("  \"item.chainores.").append(storageBlockId).append("\": \"Block of ").append(ore.enName).append("\",\n");
            enLang.append("  \"item.chainores.").append(rawItemId).append("\": \"Raw ").append(ore.enName).append("\",\n");
            enLang.append("  \"item.chainores.").append(ingotItemId).append("\": \"").append(ore.enName).append(" Ingot\"");

            esLang.append("  \"block.chainores.").append(oreBlockId).append("\": \"T").append(ore.tier).append(" Mineral de ").append(ore.esName).append("\",\n");
            esLang.append("  \"item.chainores.").append(oreBlockId).append("\": \"T").append(ore.tier).append(" Mineral de ").append(ore.esName).append("\",\n");
            esLang.append("  \"block.chainores.").append(storageBlockId).append("\": \"Bloque de ").append(ore.esName).append("\",\n");
            esLang.append("  \"item.chainores.").append(storageBlockId).append("\": \"Bloque de ").append(ore.esName).append("\",\n");
            esLang.append("  \"item.chainores.").append(rawItemId).append("\": \"").append(ore.esName).append(" en Bruto\",\n");
            esLang.append("  \"item.chainores.").append(ingotItemId).append("\": \"Lingote de ").append(ore.esName).append("\"");

            if (i < ores.size() - 1) {
                enLang.append(",\n");
                esLang.append(",\n");
            } else {
                enLang.append("\n");
                esLang.append("\n");
            }
        }

        enLang.append("}\n");
        esLang.append("}\n");

        writeString(assets.resolve("lang/en_us.json"), enLang.toString());
        writeString(assets.resolve("lang/es_es.json"), esLang.toString());

        System.out.println("All 150 ore assets and data generation finished successfully!");
    }

    private static String generateOreLootTable(String oreBlockId, String rawItemId) {
        return "{\n" +
               "  \"type\": \"minecraft:block\",\n" +
               "  \"pools\": [\n" +
               "    {\n" +
               "      \"rolls\": 1.0,\n" +
               "      \"bonus_rolls\": 0.0,\n" +
               "      \"entries\": [\n" +
               "        {\n" +
               "          \"type\": \"minecraft:alternatives\",\n" +
               "          \"children\": [\n" +
               "            {\n" +
               "              \"type\": \"minecraft:item\",\n" +
               "              \"name\": \"chainores:" + oreBlockId + "\",\n" +
               "              \"conditions\": [\n" +
               "                {\n" +
               "                  \"condition\": \"minecraft:match_tool\",\n" +
               "                  \"predicate\": {\n" +
               "                    \"predicates\": {\n" +
               "                      \"minecraft:enchantments\": [\n" +
               "                        {\"enchantments\": \"minecraft:silk_touch\", \"levels\": {\"min\": 1}}\n" +
               "                      ]\n" +
               "                    }\n" +
               "                  }\n" +
               "                }\n" +
               "              ]\n" +
               "            },\n" +
               "            {\n" +
               "              \"type\": \"minecraft:item\",\n" +
               "              \"name\": \"chainores:" + rawItemId + "\",\n" +
               "              \"functions\": [\n" +
               "                {\"function\": \"minecraft:apply_bonus\", \"enchantment\": \"minecraft:fortune\", \"formula\": \"minecraft:ore_drops\"},\n" +
               "                {\"function\": \"minecraft:explosion_decay\"}\n" +
               "              ]\n" +
               "            }\n" +
               "          ]\n" +
               "        }\n" +
               "      ]\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }

    private static void writeJson(Path path, String content) throws IOException {
        writeString(path, content);
    }

    private static void writeString(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    // Procedural 16x16 Texture Generators
    private static void generateOreTexture(File out, BaseStone base, int rgb) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        // Base stone colors
        int baseC = switch (base) {
            case STONE -> 0x737373;
            case DEEPSLATE -> 0x38383D;
            case NETHERRACK -> 0x6E2525;
            case END_STONE -> 0xD8DB9C;
        };

        // Fill background noise
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int noise = ((x * 13 + y * 29 + (x ^ y)) % 7) - 3;
                int c = adjustBrightness(baseC, noise * 0.05f);
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // Ore vein pixels (distinctive ore shapes)
        int[][] veinMask = {
            {3,3}, {4,3}, {4,4}, {5,4}, {5,5},
            {10,2}, {11,2}, {11,3}, {12,3},
            {2,9}, {2,10}, {3,10}, {3,11}, {4,11},
            {8,8}, {9,8}, {9,9}, {10,9}, {9,10}, {10,10},
            {12,12}, {13,12}, {13,13}, {14,13},
            {7,13}, {8,13}, {8,14}
        };

        int highlight = adjustBrightness(rgb, 0.40f);
        int shadow = adjustBrightness(rgb, -0.30f);
        int darkBorder = adjustBrightness(rgb, -0.55f);

        for (int[] pt : veinMask) {
            int x = pt[0];
            int y = pt[1];
            if (x >= 0 && x < 16 && y >= 0 && y < 16) {
                // border
                img.setRGB(x, y, 0xFF000000 | rgb);
            }
        }
        // Highlights on top-left of clusters
        img.setRGB(3, 3, 0xFF000000 | highlight);
        img.setRGB(10, 2, 0xFF000000 | highlight);
        img.setRGB(8, 8, 0xFF000000 | highlight);
        img.setRGB(12, 12, 0xFF000000 | highlight);

        // Shadows on bottom-right
        img.setRGB(5, 5, 0xFF000000 | shadow);
        img.setRGB(12, 3, 0xFF000000 | shadow);
        img.setRGB(10, 10, 0xFF000000 | shadow);
        img.setRGB(14, 13, 0xFF000000 | shadow);
        img.setRGB(8, 14, 0xFF000000 | darkBorder);

        ImageIO.write(img, "png", out);
    }

    private static void generateStorageTexture(File out, int rgb) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int highlight = adjustBrightness(rgb, 0.35f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.30f);
        int border = adjustBrightness(rgb, -0.50f);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x == 0 || y == 0) {
                    img.setRGB(x, y, 0xFF000000 | highlight);
                } else if (x == 15 || y == 15) {
                    img.setRGB(x, y, 0xFF000000 | border);
                } else if (x == 1 || y == 1) {
                    img.setRGB(x, y, 0xFF000000 | highlight);
                } else if (x == 14 || y == 14) {
                    img.setRGB(x, y, 0xFF000000 | shadow);
                } else {
                    int pattern = ((x + y) % 4 == 0) ? highlight : (((x - y) % 4 == 0) ? shadow : mid);
                    img.setRGB(x, y, 0xFF000000 | pattern);
                }
            }
        }
        ImageIO.write(img, "png", out);
    }

    private static void generateRawTexture(File out, int rgb) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int highlight = adjustBrightness(rgb, 0.40f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.35f);
        int dark = adjustBrightness(rgb, -0.60f);

        // Raw ore nugget cluster pattern
        int[][] pattern = {
            {5,3,dark},{6,3,dark},{7,3,dark},
            {4,4,dark},{5,4,highlight},{6,4,highlight},{7,4,mid},{8,4,dark},
            {3,5,dark},{4,5,highlight},{5,5,mid},{6,5,mid},{7,5,shadow},{8,5,mid},{9,5,dark},
            {3,6,dark},{4,6,mid},{5,6,mid},{6,6,shadow},{7,6,shadow},{8,6,shadow},{9,6,dark},{10,6,dark},
            {4,7,dark},{5,7,mid},{6,7,highlight},{7,7,mid},{8,7,shadow},{9,7,shadow},{10,7,dark},
            {4,8,dark},{5,8,highlight},{6,8,mid},{7,8,shadow},{8,8,shadow},{9,8,dark},
            {5,9,dark},{6,9,shadow},{7,9,shadow},{8,9,dark},
            {6,10,dark},{7,10,dark}
        };

        for (int[] p : pattern) {
            img.setRGB(p[0], p[1], 0xFF000000 | p[2]);
        }
        ImageIO.write(img, "png", out);
    }

    private static void generateIngotTexture(File out, int rgb) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int h = adjustBrightness(rgb, 0.45f);
        int m = rgb;
        int s = adjustBrightness(rgb, -0.30f);
        int d = adjustBrightness(rgb, -0.55f);

        // Classic Minecraft ingot shape
        int[][] p = {
            // Top bevel
            {4,4,d},{5,4,d},{6,4,d},{7,4,d},{8,4,d},{9,4,d},{10,4,d},
            {3,5,d},{4,5,h},{5,5,h},{6,5,h},{7,5,h},{8,5,h},{9,5,h},{10,5,m},{11,5,d},
            {3,6,d},{4,6,h},{5,6,m},{6,6,m},{7,6,m},{8,6,m},{9,6,s},{10,6,s},{11,6,d},{12,6,d},
            {3,7,d},{4,7,h},{5,7,m},{6,7,m},{7,7,m},{8,7,s},{9,7,s},{10,7,s},{11,7,d},{12,7,d},
            {3,8,d},{4,8,m},{5,8,m},{6,8,m},{7,8,s},{8,8,s},{9,8,s},{10,8,d},{11,8,d},
            {3,9,d},{4,9,m},{5,9,s},{6,9,s},{7,9,s},{8,9,d},{9,9,d},
            {4,10,d},{5,10,d},{6,10,d},{7,10,d},{8,10,d}
        };

        for (int[] item : p) {
            img.setRGB(item[0], item[1], 0xFF000000 | item[2]);
        }
        ImageIO.write(img, "png", out);
    }

    private static void generateTemplateTexture(File out) throws IOException {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int metal = 0x55B5C4;
        int h = 0x93EBF7;
        int s = 0x2A737E;
        int d = 0x144047;

        for (int y = 2; y <= 13; y++) {
            for (int x = 4; x <= 11; x++) {
                if (y == 2 || x == 4) img.setRGB(x, y, 0xFF000000 | h);
                else if (y == 13 || x == 11) img.setRGB(x, y, 0xFF000000 | d);
                else if (y == 3 || x == 5) img.setRGB(x, y, 0xFF000000 | metal);
                else img.setRGB(x, y, 0xFF000000 | s);
            }
        }
        // Notch in template
        img.setRGB(7, 2, 0);
        img.setRGB(8, 2, 0);
        img.setRGB(7, 3, 0xFF000000 | d);
        img.setRGB(8, 3, 0xFF000000 | d);

        ImageIO.write(img, "png", out);
    }

    private static int adjustBrightness(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        if (factor > 0) {
            r = (int) (r + (255 - r) * factor);
            g = (int) (g + (255 - g) * factor);
            b = (int) (b + (255 - b) * factor);
        } else {
            r = (int) (r * (1 + factor));
            g = (int) (g * (1 + factor));
            b = (int) (b * (1 + factor));
        }

        r = Math.clamp(r, 0, 255);
        g = Math.clamp(g, 0, 255);
        b = Math.clamp(b, 0, 255);

        return (r << 16) | (g << 8) | b;
    }
}
