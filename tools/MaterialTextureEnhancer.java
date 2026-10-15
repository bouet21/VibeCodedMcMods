package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import tools.AssetGenerator.BaseStone;
import tools.AssetGenerator.OreDef;

public class MaterialTextureEnhancer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Ultimate Material & Ore Texture Enhancement for 150 Tiers...");

        File blockDir = new File("src/main/resources/assets/chainores/textures/block");
        File itemDir = new File("src/main/resources/assets/chainores/textures/item");
        blockDir.mkdirs();
        itemDir.mkdirs();

        List<OreDef> ores = AssetGenerator.getOres();

        for (OreDef ore : ores) {
            int tier = ore.tier;
            int rgb = getEnhancedColor(tier, ore.colorRgb);

            // 1. Highly Distinct Ore Texture (8 unique vein archetypes)
            BufferedImage oreImg = renderOre(ore.baseStone, rgb, tier);
            ImageIO.write(oreImg, "PNG", new File(blockDir, "ore_tier_" + tier + ".png"));

            // 2. Highly Distinct Storage Block (8 unique architectural tile patterns)
            BufferedImage blockImg = renderStorageBlock(rgb, tier);
            ImageIO.write(blockImg, "PNG", new File(blockDir, "block_tier_" + tier + ".png"));

            // 3. Highly Distinct Ingot Item (6 unique bullion archetypes)
            BufferedImage ingotImg = renderIngot(rgb, tier);
            ImageIO.write(ingotImg, "PNG", new File(itemDir, "ingot_tier_" + tier + ".png"));

            // 4. Highly Distinct Raw Ore Item (6 unique organic crystal/chunk archetypes)
            BufferedImage rawImg = renderRawOre(ore.baseStone, rgb, tier);
            ImageIO.write(rawImg, "PNG", new File(itemDir, "raw_tier_" + tier + ".png"));
        }

        System.out.println("Successfully generated 600 ultra-refined textures across 150 tiers with completely unique silhouettes, styles, and shading!");
    }

    private static int getEnhancedColor(int tier, int defaultRgb) {
        // Hand-crafted distinctive color points ensuring vibrant, non-muddy hues
        return switch (tier) {
            // Plains & Forests (Stone)
            case 1 -> 0xE06836;  // Copperite: bright pure copper orange
            case 2 -> 0xD5E4ED;  // Tin: bright silvery cyan-white
            case 3 -> 0xB87333;  // Bronzeite: deep warm antique bronze
            case 4 -> 0x4B586E;  // Galena: dark gunmetal bluish lead
            case 5 -> 0x48C988;  // Zincite: vibrant mint emerald
            case 6 -> 0xE0409A;  // Bismuth: vivid iridescent magenta
            case 7 -> 0xF4F7FA;  // Silver: gleaming pure platinum white
            case 8 -> 0xFFCC00;  // Electrum: bright electric gold
            case 9 -> 0xAA8E57;  // Nickel: warm antique brass-amber
            case 10 -> 0x364452; // Invar: deep industrial graphite steel

            // Mountains & Snow (Stone)
            case 11 -> 0x75C8FF; // Froststeel: crisp sky ice blue
            case 12 -> 0xE8F8FF; // Cryolite: pure frosted white
            case 13 -> 0x30A8DE; // Glacite: deep glacier cerulean
            case 14 -> 0x004CE6; // Cobalt: rich ultramarine cobalt blue
            case 15 -> 0x00E5BC; // Aquamarine: brilliant tropical sea green
            case 16 -> 0x1A28A8; // Sapphire: deep royal navy sapphire
            case 17 -> 0x5C8E99; // Permafrostium: frosted tundra teal
            case 18 -> 0x39FF94; // Borealis: northern lights neon lime
            case 19 -> 0xBEE5FA; // Blizzardite: howling pale frost
            case 20 -> 0x00FFF0; // Absolute Zero: blinding laser cyan

            // Deserts & Badlands (Stone)
            case 21 -> 0xD9A814; // Pyrite: sparkling fool's gold
            case 22 -> 0xF57C00; // Amber: deep translucent fossil amber
            case 23 -> 0xFF5500; // Sunstone: intense fiery vermilion
            case 24 -> 0xFFDD44; // Desert Topaz: sparkling sunny topaz
            case 25 -> 0xC42323; // Jasper: deep crimson bloodstone
            case 26 -> 0x7E0E24; // Cinnabar: rich wine cinnabar red
            case 27 -> 0xBA6B42; // Terracotite: vibrant terracotta clay
            case 28 -> 0xD6A856; // Sandstormite: desert dune gold
            case 29 -> 0xFFEE11; // Solarite: blazing noon sunlight
            case 30 -> 0xDE5FD5; // Mirage Core: ethereal desert violet

            // Swamps & Jungles (Stone/Deepslate)
            case 31 -> 0x6E4B28; // Bog Iron: rich peat marsh iron
            case 32 -> 0x059955; // Malachite: dark banded forest malachite
            case 33 -> 0x1BD97B; // Jade: imperial luminous jade
            case 34 -> 0x486B00; // Serpentine: venomous olive green
            case 35 -> 0x80FF00; // Toxicite: glowing radioactive lime
            case 36 -> 0xFF3D68; // Coralium: vivid ocean coral pink
            case 37 -> 0x0C344D; // Abyssium: deep ocean oceanic trench
            case 38 -> 0x00F0C0; // Prismatite: bioluminescent prismarine
            case 39 -> 0x22E022; // Chlorophyte: vibrant jungle leaf green
            case 40 -> 0x00FF88; // Jungle Heart: radiant primeval emerald

            // Deepslate & Crystals - Tiers 41-70
            case 45 -> 0xE02244; // Ruby: deep blood ruby
            case 50 -> 0x9400D3; // Amethystine: dark violet crystal
            case 55 -> 0x00E5FF; // Diamondite: brilliant diamond light
            case 60 -> 0x00FF66; // Emeraldite: vivid imperial emerald
            case 65 -> 0xFF7700; // Topaz: warm blazing topaz
            case 70 -> 0x00FFAA; // Sculk Infused: pulsing deep dark warden teal

            // Nether Tiers 71-105
            case 75 -> 0xFF3300; // Magmite: glowing lava molten
            case 80 -> 0x990000; // Crimsonite: deep nether crimson
            case 85 -> 0x008080; // Warpedite: warped fungal cyan
            case 90 -> 0x00F0FF; // Soul Gem: soul fire radiant blue
            case 95 -> 0xFF9900; // Blazeite: blazing gold fire
            case 100 -> 0x332822; // Netherite Infused: obsidian netherite alloy
            case 105 -> 0xFF144D; // Behemoth Core: hyper-dense demon core

            // End Tiers 106-135
            case 110 -> 0xBA55D3; // Purpurite: rich purpur violet
            case 115 -> 0xDA70D6; // Dragon Breath: radiant magenta breath
            case 120 -> 0x4B0082; // Voidite: deep abyss void
            case 125 -> 0x00FFFF; // Enderite: transcendent astral cyan
            case 130 -> 0x9370DB; // Shulkerite: pearlescent shulker shell
            case 135 -> 0xD8BFD8; // Colossus Core: astral singularity purple

            // Cosmic Genesis Tiers 136-150
            case 140 -> 0x00FFD5; // Galactite: cosmic nebula teal
            case 145 -> 0xFF69B4; // Supernova: brilliant stellar pink
            case 148 -> 0xFFD700; // Solar Flare: pure coronal gold
            case 149 -> 0xE0FFFF; // Singularity: hyper-dense starlight white
            case 150 -> 0xFFFFFF; // Genesis: pure transcendent white-gold

            default -> defaultRgb;
        };
    }

    // ==========================================
    // 1. ORE GENERATION WITH 8 DISTINCT VEIN ARCHETYPES
    // ==========================================
    private static BufferedImage renderOre(BaseStone baseStone, int rgb, int tier) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        generateBaseStone(img, baseStone, tier);

        int highlight = adjustBrightness(rgb, 0.45f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.35f);
        int dark = adjustBrightness(rgb, -0.60f);
        int sparkle = 0xFFFFFFFF;

        int archetype = (tier - 1) % 8;

        switch (archetype) {
            case 0 -> {
                // Diagonal Branching Veins (Classic metallic ore)
                int[][] pts = {
                    {2,4}, {3,4}, {3,5}, {4,5}, {4,6}, {5,6},
                    {9,2}, {10,2}, {10,3}, {11,3}, {12,4},
                    {7,8}, {8,8}, {8,9}, {9,9}, {9,10}, {10,10},
                    {1,11}, {2,11}, {2,12}, {3,12}, {3,13},
                    {12,11}, {13,11}, {13,12}, {14,12}, {14,13}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 2, 4, highlight); paintPixel(img, 9, 2, highlight);
                paintPixel(img, 7, 8, highlight); paintPixel(img, 12, 11, highlight);
                paintPixel(img, 5, 6, shadow); paintPixel(img, 12, 4, shadow);
                paintPixel(img, 10, 10, shadow); paintPixel(img, 14, 13, dark);
            }
            case 1 -> {
                // Crystalline Geode Facets (Diamond/Gemstone-like clusters)
                int[][] pts = {
                    {3,3}, {4,3}, {3,4}, {4,4}, {5,4}, {4,5}, {5,5},
                    {11,3}, {12,3}, {10,4}, {11,4}, {12,4}, {11,5},
                    {7,7}, {8,7}, {6,8}, {7,8}, {8,8}, {9,8}, {7,9}, {8,9},
                    {2,11}, {3,11}, {2,12}, {3,12}, {4,12}, {3,13},
                    {11,10}, {12,10}, {11,11}, {12,11}, {13,11}, {12,12}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 4, 4, sparkle); paintPixel(img, 11, 4, sparkle);
                paintPixel(img, 8, 8, sparkle); paintPixel(img, 3, 12, sparkle);
                paintPixel(img, 12, 11, sparkle);
                paintPixel(img, 3, 3, highlight); paintPixel(img, 7, 7, highlight);
                paintPixel(img, 4, 5, shadow); paintPixel(img, 8, 9, shadow);
            }
            case 2 -> {
                // Dense Massive Deposit (Organic rich ore body)
                int[][] pts = {
                    {6,2}, {7,2}, {5,3}, {6,3}, {7,3}, {8,3},
                    {4,4}, {5,4}, {6,4}, {7,4}, {8,4}, {9,4},
                    {5,5}, {6,5}, {7,5}, {8,5}, {9,5}, {10,5},
                    {1,7}, {2,7}, {3,7}, {2,8}, {3,8}, {4,8},
                    {10,7}, {11,7}, {11,8}, {12,8}, {12,9},
                    {5,10}, {6,10}, {7,10}, {5,11}, {6,11}, {7,11}, {8,11}, {6,12}, {7,12}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 6, 2, highlight); paintPixel(img, 5, 3, highlight);
                paintPixel(img, 2, 7, highlight); paintPixel(img, 6, 10, highlight);
                paintPixel(img, 7, 4, shadow); paintPixel(img, 8, 5, shadow);
                paintPixel(img, 4, 8, shadow); paintPixel(img, 7, 12, dark);
            }
            case 3 -> {
                // Undulating Stratified Bands (Sedimentary layers)
                for (int x = 1; x <= 14; x++) {
                    int y1 = 4 + (int) (Math.sin(x * 0.8) * 1.5);
                    int y2 = 10 + (int) (Math.cos(x * 0.8) * 1.5);
                    paintPixel(img, x, y1, (x % 3 == 0) ? highlight : mid);
                    paintPixel(img, x, y1 + 1, (x % 2 == 0) ? shadow : dark);
                    paintPixel(img, x, y2, (x % 4 == 0) ? highlight : mid);
                    paintPixel(img, x, y2 + 1, shadow);
                }
            }
            case 4 -> {
                // Hollow Geode Cavity (Jagged crystal ring)
                int[][] ring = {
                    {6,4}, {7,4}, {8,4}, {9,4},
                    {5,5}, {10,5}, {4,6}, {11,6},
                    {4,7}, {11,7}, {4,8}, {11,8},
                    {5,9}, {10,9}, {6,10}, {7,10}, {8,10}, {9,10}
                };
                for (int[] p : ring) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 6, 4, highlight); paintPixel(img, 7, 4, sparkle);
                paintPixel(img, 10, 5, highlight); paintPixel(img, 4, 6, highlight);
                paintPixel(img, 8, 10, shadow); paintPixel(img, 9, 10, dark);
                paintPixel(img, 7, 7, sparkle); // Center floating spark
            }
            case 5 -> {
                // Vertical Fissure / Chasm (Lightning-shaped jagged crack)
                int[][] fissure = {
                    {7,1}, {8,1}, {7,2}, {8,2},
                    {6,3}, {7,3}, {6,4}, {7,4}, {8,4},
                    {8,5}, {9,5}, {8,6}, {9,6},
                    {7,7}, {8,7}, {9,7}, {6,8}, {7,8}, {8,8},
                    {5,9}, {6,9}, {5,10}, {6,10},
                    {6,11}, {7,11}, {6,12}, {7,12}, {7,13}, {8,13}, {8,14}
                };
                for (int[] p : fissure) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 7, 1, sparkle); paintPixel(img, 7, 3, highlight);
                paintPixel(img, 8, 6, highlight); paintPixel(img, 7, 7, sparkle);
                paintPixel(img, 6, 10, highlight); paintPixel(img, 8, 13, shadow);
                paintPixel(img, 8, 14, dark);
            }
            case 6 -> {
                // Radial Starburst Deposit (Exploding core shards)
                int[][] star = {
                    {7,7}, {8,7}, {7,8}, {8,8},
                    {7,5}, {8,5}, {7,6}, {8,6}, // North
                    {7,9}, {8,9}, {7,10}, {8,10}, // South
                    {5,7}, {6,7}, {5,8}, {6,8}, // West
                    {9,7}, {10,7}, {9,8}, {10,8}, // East
                    {4,4}, {5,5}, {11,4}, {10,5}, {4,11}, {5,10}, {11,11}, {10,10} // Corners
                };
                for (int[] p : star) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 7, 7, sparkle); paintPixel(img, 8, 7, sparkle);
                paintPixel(img, 7, 5, highlight); paintPixel(img, 5, 7, highlight);
                paintPixel(img, 10, 8, shadow); paintPixel(img, 8, 10, shadow);
                paintPixel(img, 11, 11, dark);
            }
            case 7 -> {
                // Alien Geometric Runes (Sculk / Cosmic runic formation)
                int[][] runes = {
                    {3,3}, {4,3}, {5,3}, {3,4}, {5,4}, {3,5}, {4,5}, {5,5},
                    {10,3}, {11,3}, {12,3}, {12,4}, {10,5}, {11,5}, {12,5},
                    {6,7}, {7,7}, {8,7}, {9,7}, {7,8}, {8,8}, {6,9}, {9,9},
                    {3,10}, {5,10}, {3,11}, {4,11}, {5,11}, {3,12}, {5,12},
                    {10,10}, {11,10}, {12,10}, {11,11}, {10,12}, {11,12}, {12,12}
                };
                for (int[] p : runes) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 4, 3, sparkle); paintPixel(img, 11, 3, sparkle);
                paintPixel(img, 7, 7, sparkle); paintPixel(img, 8, 7, sparkle);
                paintPixel(img, 3, 10, highlight); paintPixel(img, 12, 10, highlight);
                paintPixel(img, 5, 12, shadow); paintPixel(img, 12, 12, dark);
            }
        }

        return img;
    }

    private static void generateBaseStone(BufferedImage img, BaseStone base, int tier) {
        int baseC = switch (base) {
            case STONE -> 0x757575;
            case DEEPSLATE -> 0x36363B;
            case NETHERRACK -> 0x6E2525;
            case END_STONE -> 0xD8DB9C;
        };

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int noise = ((x * 17 + y * 31 + tier * 7) % 9) - 4;
                int c = adjustBrightness(baseC, noise * 0.04f);

                if (base == BaseStone.DEEPSLATE && (y % 4 == 0 || y % 5 == 0)) {
                    c = adjustBrightness(c, -0.15f);
                }
                if (base == BaseStone.NETHERRACK && (x * y + x) % 11 == 0) {
                    c = adjustBrightness(c, -0.30f);
                }
                if (base == BaseStone.END_STONE && (x ^ y) % 7 == 0) {
                    c = adjustBrightness(c, -0.12f);
                }

                img.setRGB(x, y, 0xFF000000 | c);
            }
        }
    }

    // ==========================================
    // 2. STORAGE BLOCK GENERATION (8 DISTINCT ARCHITECTURES)
    // ==========================================
    private static BufferedImage renderStorageBlock(int rgb, int tier) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int highlight = adjustBrightness(rgb, 0.45f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.35f);
        int dark = adjustBrightness(rgb, -0.55f);
        int sparkle = 0xFFFFFFFF;

        int style = (tier - 1) % 8;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean border = (x == 0 || y == 0 || x == 15 || y == 15);
                boolean innerBorder = (x == 1 || y == 1 || x == 14 || y == 14);

                switch (style) {
                    case 0 -> {
                        // Riveted Metal Plate with 4 Corner Bolts
                        if (border) {
                            img.setRGB(x, y, (x <= 1 || y <= 1) ? 0xFF000000 | highlight : 0xFF000000 | dark);
                        } else if (innerBorder) {
                            if ((x == 2 || x == 13) && (y == 2 || y == 13)) {
                                img.setRGB(x, y, sparkle); // 4 Bolt rivets
                            } else {
                                img.setRGB(x, y, (x <= 2 || y <= 2) ? 0xFF000000 | highlight : 0xFF000000 | shadow);
                            }
                        } else {
                            int pattern = ((x + y) % 4 == 0) ? highlight : (((x - y) % 4 == 0) ? shadow : mid);
                            img.setRGB(x, y, 0xFF000000 | pattern);
                        }
                    }
                    case 1 -> {
                        // 4 Interlocking Chiseled Cut Gem Tiles
                        if (border || x == 7 || y == 7 || x == 8 || y == 8) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if ((x == 3 && y == 3) || (x == 12 && y == 3) || (x == 3 && y == 12) || (x == 12 && y == 12)) {
                            img.setRGB(x, y, sparkle);
                        } else {
                            boolean topHalf = (y < 7 || (y > 8 && y < 12));
                            boolean leftHalf = (x < 7 || (x > 8 && x < 12));
                            img.setRGB(x, y, (topHalf && leftHalf) ? 0xFF000000 | highlight : 0xFF000000 | shadow);
                        }
                    }
                    case 2 -> {
                        // Smooth Polished Ingot Slab with Glossy Diagonal Sheen
                        if (border) {
                            img.setRGB(x, y, (x == 0 || y == 0) ? 0xFF000000 | highlight : 0xFF000000 | dark);
                        } else if (Math.abs(x - y) <= 1) {
                            img.setRGB(x, y, sparkle); // Center diagonal sheen
                        } else if (Math.abs(x - y) <= 3) {
                            img.setRGB(x, y, 0xFF000000 | highlight);
                        } else {
                            img.setRGB(x, y, 0xFF000000 | mid);
                        }
                    }
                    case 3 -> {
                        // Carved Ancient Monolith with Circular Central Seal
                        double dist = Math.hypot(x - 7.5, y - 7.5);
                        if (border) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if (dist <= 2.0) {
                            img.setRGB(x, y, sparkle); // Glowing eye/seal
                        } else if (dist <= 4.0) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if (dist <= 6.0) {
                            img.setRGB(x, y, 0xFF000000 | highlight);
                        } else {
                            img.setRGB(x, y, 0xFF000000 | shadow);
                        }
                    }
                    case 4 -> {
                        // Nether Citadel Reinforced Bricks with Lava Mortar
                        boolean isMortar = (y == 5 || y == 10 || (y < 5 && x == 8) || (y > 5 && y < 10 && x == 4) || (y > 5 && y < 10 && x == 12) || (y > 10 && x == 8));
                        if (isMortar) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if (border) {
                            img.setRGB(x, y, 0xFF000000 | shadow);
                        } else {
                            int brickNoise = ((x * 7 + y * 13) % 3);
                            img.setRGB(x, y, (brickNoise == 0) ? 0xFF000000 | highlight : 0xFF000000 | mid);
                        }
                    }
                    case 5 -> {
                        // Astral Cosmic Mosaic (Concentric Starry Diamond)
                        int manhattan = Math.abs(x - 7) + Math.abs(y - 7);
                        if (border) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if (manhattan <= 1) {
                            img.setRGB(x, y, sparkle);
                        } else if (manhattan == 3 || manhattan == 4) {
                            img.setRGB(x, y, 0xFF000000 | highlight);
                        } else if (manhattan == 6 || manhattan == 7) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else {
                            img.setRGB(x, y, 0xFF000000 | mid);
                        }
                    }
                    case 6 -> {
                        // Industrial Reinforcing Grate (Cross-Hatch Framework)
                        boolean isBar = (x % 4 == 0 || y % 4 == 0);
                        if (border) {
                            img.setRGB(x, y, (x <= 1 || y <= 1) ? 0xFF000000 | highlight : 0xFF000000 | dark);
                        } else if (isBar) {
                            img.setRGB(x, y, 0xFF000000 | highlight);
                        } else {
                            img.setRGB(x, y, 0xFF000000 | dark); // Recessed vent
                        }
                    }
                    case 7 -> {
                        // Divine Crest Matrix (Filigree corners & Radiant Star)
                        boolean isCornerFiligree = ((x <= 3 || x >= 12) && (y <= 3 || y >= 12));
                        double dist = Math.hypot(x - 7.5, y - 7.5);
                        if (isCornerFiligree) {
                            img.setRGB(x, y, 0xFF000000 | highlight);
                        } else if (dist <= 2.2) {
                            img.setRGB(x, y, sparkle); // Blinding center
                        } else if (x == 7 || x == 8 || y == 7 || y == 8) {
                            img.setRGB(x, y, 0xFF000000 | highlight); // Cross rays
                        } else {
                            img.setRGB(x, y, 0xFF000000 | shadow);
                        }
                    }
                }
            }
        }
        return img;
    }

    // ==========================================
    // 3. INGOT GENERATION (6 DISTINCT BULLION ARCHETYPES)
    // ==========================================
    private static BufferedImage renderIngot(int rgb, int tier) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int highlight = adjustBrightness(rgb, 0.50f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.35f);
        int dark = adjustBrightness(rgb, -0.65f);
        int sparkle = 0xFFFFFFFF;

        int archetype = (tier - 1) % 6;

        switch (archetype) {
            case 0 -> {
                // Classic Heavy Beveled Bullion (Smooth gold/iron style)
                for (int x = 4; x <= 11; x++) {
                    img.setRGB(x, 5, 0xFF000000 | highlight);
                    img.setRGB(x, 6, 0xFF000000 | highlight);
                    img.setRGB(x, 7, 0xFF000000 | mid);
                }
                img.setRGB(5, 5, sparkle); img.setRGB(6, 5, sparkle);

                for (int y = 8; y <= 10; y++) {
                    for (int x = 3; x <= 12; x++) {
                        img.setRGB(x, y, (x <= 5) ? (0xFF000000 | mid) : (0xFF000000 | shadow));
                    }
                }
                img.setRGB(3, 6, 0xFF000000 | highlight);
                img.setRGB(3, 7, 0xFF000000 | mid);
                img.setRGB(2, 8, 0xFF000000 | mid);
                img.setRGB(2, 9, 0xFF000000 | mid);
                img.setRGB(2, 10, 0xFF000000 | shadow);

                img.setRGB(12, 6, 0xFF000000 | shadow);
                img.setRGB(12, 7, 0xFF000000 | shadow);
                img.setRGB(13, 8, 0xFF000000 | dark);
                img.setRGB(13, 9, 0xFF000000 | dark);
                img.setRGB(13, 10, 0xFF000000 | dark);

                for (int x = 3; x <= 13; x++) img.setRGB(x, 11, 0xFF000000 | dark);
            }
            case 1 -> {
                // Faceted Gemstone Crystal Bar (Pointed diamond crystal ingot)
                for (int x = 3; x <= 12; x++) {
                    img.setRGB(x, 6, 0xFF000000 | highlight);
                    img.setRGB(x, 7, (x % 2 == 0) ? sparkle : 0xFF000000 | highlight);
                    img.setRGB(x, 8, 0xFF000000 | mid);
                    img.setRGB(x, 9, 0xFF000000 | shadow);
                }
                // Pointed ends
                img.setRGB(2, 7, sparkle); img.setRGB(2, 8, 0xFF000000 | mid);
                img.setRGB(13, 7, 0xFF000000 | mid); img.setRGB(13, 8, 0xFF000000 | shadow);
                img.setRGB(1, 8, sparkle); img.setRGB(14, 8, 0xFF000000 | dark);
                for (int x = 2; x <= 13; x++) img.setRGB(x, 10, 0xFF000000 | dark);
            }
            case 2 -> {
                // Inscribed Runic Tablet Ingot (Mystical carved tablet)
                for (int y = 4; y <= 11; y++) {
                    for (int x = 4; x <= 11; x++) {
                        boolean topEdge = (y == 4 || x == 4);
                        boolean botEdge = (y == 11 || x == 11);
                        if (topEdge) img.setRGB(x, y, 0xFF000000 | highlight);
                        else if (botEdge) img.setRGB(x, y, 0xFF000000 | dark);
                        else img.setRGB(x, y, 0xFF000000 | mid);
                    }
                }
                // Runic Inscription (Gleaming cross/glyph)
                img.setRGB(6, 6, sparkle); img.setRGB(7, 6, sparkle); img.setRGB(8, 6, sparkle);
                img.setRGB(7, 7, sparkle); img.setRGB(7, 8, sparkle); img.setRGB(7, 9, sparkle);
                img.setRGB(6, 9, 0xFF000000 | highlight); img.setRGB(8, 9, 0xFF000000 | highlight);
            }
            case 3 -> {
                // Notched Futuristic Alloy Ingot (Industrial grip ridges)
                for (int y = 5; y <= 10; y++) {
                    for (int x = 3; x <= 12; x++) {
                        boolean isNotch = (x == 6 || x == 9);
                        if (isNotch) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else if (y <= 6) {
                            img.setRGB(x, y, (x <= 5) ? sparkle : 0xFF000000 | highlight);
                        } else {
                            img.setRGB(x, y, (x >= 10) ? 0xFF000000 | shadow : 0xFF000000 | mid);
                        }
                    }
                }
                for (int x = 3; x <= 12; x++) img.setRGB(x, 11, 0xFF000000 | dark);
            }
            case 4 -> {
                // Heavy Volcanic / Nether Core Ingot (Basalt frame with glowing inner core)
                for (int y = 5; y <= 11; y++) {
                    for (int x = 3; x <= 12; x++) {
                        boolean isInnerCore = (y >= 7 && y <= 9 && x >= 5 && x <= 10);
                        if (isInnerCore) {
                            img.setRGB(x, y, (y == 8) ? sparkle : 0xFF000000 | highlight); // Molten inner stripe
                        } else if (y == 5) {
                            img.setRGB(x, y, 0xFF000000 | shadow);
                        } else if (y == 11 || x == 3 || x == 12) {
                            img.setRGB(x, y, 0xFF000000 | dark);
                        } else {
                            img.setRGB(x, y, 0xFF000000 | mid);
                        }
                    }
                }
            }
            case 5 -> {
                // Celestial Astral Prism Bar (Slender divine tapered bar)
                for (int y = 4; y <= 11; y++) {
                    int w = (y <= 7) ? (y - 1) : (14 - y);
                    for (int x = 8 - w; x <= 8 + w; x++) {
                        if (x == 8 - w) img.setRGB(x, y, sparkle);
                        else if (x == 8) img.setRGB(x, y, 0xFF000000 | highlight);
                        else if (x == 8 + w) img.setRGB(x, y, 0xFF000000 | dark);
                        else img.setRGB(x, y, 0xFF000000 | mid);
                    }
                }
            }
        }

        return img;
    }

    // ==========================================
    // 4. RAW ORE GENERATION (6 DISTINCT ORGANIC CHUNK ARCHETYPES)
    // ==========================================
    private static BufferedImage renderRawOre(BaseStone base, int rgb, int tier) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int highlight = adjustBrightness(rgb, 0.45f);
        int mid = rgb;
        int shadow = adjustBrightness(rgb, -0.35f);
        int dark = adjustBrightness(rgb, -0.65f);
        int sparkle = 0xFFFFFFFF;

        int stoneC = switch (base) {
            case STONE -> 0x757575;
            case DEEPSLATE -> 0x36363B;
            case NETHERRACK -> 0x6E2525;
            case END_STONE -> 0xD8DB9C;
        };

        int archetype = (tier - 1) % 6;

        switch (archetype) {
            case 0 -> {
                // Organic 3-Nodule Clustered Pebble (Classic Raw Iron/Copper style)
                int[][] pts = {
                    {6,3}, {7,3}, {8,3},
                    {5,4}, {6,4}, {7,4}, {8,4}, {9,4},
                    {5,5}, {6,5}, {7,5}, {8,5}, {9,5},
                    {6,6}, {7,6}, {8,6},
                    {3,7}, {4,7}, {3,8}, {4,8}, {5,8}, {3,9}, {4,9}, {5,9}, {4,10},
                    {9,7}, {10,7}, {8,8}, {9,8}, {10,8}, {11,8}, {8,9}, {9,9}, {10,9}, {11,9}, {9,10}, {10,10},
                    {5,10}, {6,10}, {7,10}, {8,10}, {5,11}, {6,11}, {7,11}, {8,11}, {6,12}, {7,12}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 7, 4, sparkle);
                paintPixel(img, 6, 3, highlight); paintPixel(img, 4, 7, highlight); paintPixel(img, 9, 7, highlight);
                paintPixel(img, 9, 5, shadow); paintPixel(img, 11, 9, shadow); paintPixel(img, 8, 11, shadow);
                paintPixel(img, 6, 12, dark); paintPixel(img, 7, 12, dark);
                paintPixel(img, 4, 5, stoneC); paintPixel(img, 8, 7, adjustBrightness(stoneC, -0.2f));
            }
            case 1 -> {
                // Jagged Geode Crystal Shards (3 Spiky Crystals protruding)
                int[][] pts = {
                    // Central tall spire
                    {8,2}, {7,3}, {8,3}, {7,4}, {8,4}, {9,4}, {7,5}, {8,5}, {9,5},
                    {6,6}, {7,6}, {8,6}, {9,6}, {6,7}, {7,7}, {8,7}, {9,7}, {10,7},
                    // Left diagonal shard
                    {4,5}, {5,6}, {4,7}, {5,7}, {3,8}, {4,8}, {5,8}, {3,9}, {4,9}, {5,9},
                    // Right diagonal shard
                    {11,5}, {10,6}, {10,7}, {11,7}, {10,8}, {11,8}, {12,8}, {10,9}, {11,9},
                    // Rock Base
                    {5,10}, {6,10}, {7,10}, {8,10}, {9,10}, {10,10},
                    {6,11}, {7,11}, {8,11}, {9,11}, {7,12}, {8,12}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 8, 2, sparkle); paintPixel(img, 4, 5, sparkle); paintPixel(img, 11, 5, sparkle);
                paintPixel(img, 7, 3, highlight); paintPixel(img, 4, 7, highlight);
                paintPixel(img, 9, 7, shadow); paintPixel(img, 12, 8, dark);
                paintPixel(img, 7, 12, dark); paintPixel(img, 8, 12, dark);
                // Host rock embedding
                paintPixel(img, 6, 10, stoneC); paintPixel(img, 7, 11, stoneC); paintPixel(img, 8, 11, adjustBrightness(stoneC, -0.2f));
            }
            case 2 -> {
                // Veined Ore Spliced Rock (A blocky stone fragment split by rich glowing ore)
                int[][] rockPts = {
                    {4,4}, {5,4}, {6,4}, {7,4}, {8,4}, {9,4}, {10,4},
                    {3,5}, {4,5}, {10,5}, {11,5},
                    {3,6}, {4,6}, {11,6}, {12,6},
                    {2,7}, {3,7}, {11,7}, {12,7},
                    {2,8}, {3,8}, {11,8}, {12,8},
                    {3,9}, {4,9}, {10,9}, {11,9},
                    {4,10}, {5,10}, {9,10}, {10,10},
                    {5,11}, {6,11}, {7,11}, {8,11}
                };
                for (int[] p : rockPts) paintPixel(img, p[0], p[1], stoneC);

                // Thick glowing mineral vein through the center
                int[][] veinPts = {
                    {7,4}, {8,4}, {6,5}, {7,5}, {8,5}, {9,5},
                    {5,6}, {6,6}, {7,6}, {8,6}, {9,6}, {10,6},
                    {5,7}, {6,7}, {7,7}, {8,7}, {9,7}, {10,7},
                    {5,8}, {6,8}, {7,8}, {8,8}, {9,8},
                    {6,9}, {7,9}, {8,9}, {6,10}, {7,10}, {8,10}
                };
                for (int[] p : veinPts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 7, 5, sparkle); paintPixel(img, 6, 6, highlight);
                paintPixel(img, 7, 7, sparkle); paintPixel(img, 8, 8, highlight);
                paintPixel(img, 9, 7, shadow); paintPixel(img, 8, 10, shadow);
            }
            case 3 -> {
                // Spiky Twin Spires Cluster (Aggressive twin crystal peaks)
                int[][] pts = {
                    // Primary tall spire
                    {6,2}, {5,3}, {6,3}, {5,4}, {6,4}, {7,4}, {5,5}, {6,5}, {7,5},
                    {4,6}, {5,6}, {6,6}, {7,6}, {4,7}, {5,7}, {6,7}, {7,7},
                    // Secondary offset spire
                    {10,5}, {9,6}, {10,6}, {9,7}, {10,7}, {11,7},
                    {8,8}, {9,8}, {10,8}, {11,8}, {8,9}, {9,9}, {10,9}, {11,9},
                    // Clustered base
                    {4,8}, {5,8}, {6,8}, {7,8}, {4,9}, {5,9}, {6,9}, {7,9},
                    {5,10}, {6,10}, {7,10}, {8,10}, {9,10}, {10,10},
                    {6,11}, {7,11}, {8,11}, {9,11}, {7,12}, {8,12}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 6, 2, sparkle); paintPixel(img, 10, 5, sparkle);
                paintPixel(img, 5, 4, highlight); paintPixel(img, 9, 7, highlight);
                paintPixel(img, 7, 6, shadow); paintPixel(img, 11, 8, shadow);
                paintPixel(img, 7, 12, dark); paintPixel(img, 8, 12, dark);
                paintPixel(img, 8, 10, stoneC); paintPixel(img, 9, 11, stoneC);
            }
            case 4 -> {
                // Porous Volcanic Slag Clump (Molten pores and irregular rocky slag)
                int[][] pts = {
                    {5,3}, {6,3}, {7,3}, {8,3}, {9,3},
                    {4,4}, {5,4}, {6,4}, {7,4}, {8,4}, {9,4}, {10,4},
                    {3,5}, {4,5}, {5,5}, {6,5}, {7,5}, {8,5}, {9,5}, {10,5}, {11,5},
                    {3,6}, {4,6}, {5,6}, {6,6}, {7,6}, {8,6}, {9,6}, {10,6}, {11,6}, {12,6},
                    {2,7}, {3,7}, {4,7}, {5,7}, {6,7}, {7,7}, {8,7}, {9,7}, {10,7}, {11,7}, {12,7},
                    {3,8}, {4,8}, {5,8}, {6,8}, {7,8}, {8,8}, {9,8}, {10,8}, {11,8},
                    {4,9}, {5,9}, {6,9}, {7,9}, {8,9}, {9,9}, {10,9},
                    {5,10}, {6,10}, {7,10}, {8,10}, {9,10},
                    {6,11}, {7,11}, {8,11}
                };
                for (int[] p : pts) paintPixel(img, p[0], p[1], mid);
                // Glowing volcanic pores / gems
                paintPixel(img, 6, 4, sparkle); paintPixel(img, 9, 6, sparkle);
                paintPixel(img, 5, 7, sparkle); paintPixel(img, 7, 9, sparkle);
                paintPixel(img, 5, 3, highlight); paintPixel(img, 4, 5, highlight);
                paintPixel(img, 11, 6, shadow); paintPixel(img, 10, 8, shadow);
                paintPixel(img, 7, 11, dark); paintPixel(img, 8, 11, dark);
                // Dark porous slag patches
                paintPixel(img, 7, 5, dark); paintPixel(img, 8, 7, dark); paintPixel(img, 6, 8, stoneC);
            }
            case 5 -> {
                // Cosmic Astral Core Shard (Geometric Diamond Core with Floating Satellites)
                // Center Diamond Core
                int[][] diamond = {
                    {7,4}, {8,4},
                    {6,5}, {7,5}, {8,5}, {9,5},
                    {5,6}, {6,6}, {7,6}, {8,6}, {9,6}, {10,6},
                    {4,7}, {5,7}, {6,7}, {7,7}, {8,7}, {9,7}, {10,7}, {11,7},
                    {5,8}, {6,8}, {7,8}, {8,8}, {9,8}, {10,8},
                    {6,9}, {7,9}, {8,9}, {9,9},
                    {7,10}, {8,10}
                };
                for (int[] p : diamond) paintPixel(img, p[0], p[1], mid);
                paintPixel(img, 7, 5, sparkle); paintPixel(img, 8, 5, sparkle);
                paintPixel(img, 7, 6, sparkle); paintPixel(img, 6, 6, highlight);
                paintPixel(img, 9, 8, shadow); paintPixel(img, 10, 7, shadow);
                paintPixel(img, 7, 10, dark); paintPixel(img, 8, 10, dark);

                // 4 Floating Orbital Shards
                paintPixel(img, 7, 2, sparkle); paintPixel(img, 8, 2, highlight);
                paintPixel(img, 7, 12, highlight); paintPixel(img, 8, 12, shadow);
                paintPixel(img, 2, 7, sparkle); paintPixel(img, 2, 8, highlight);
                paintPixel(img, 13, 7, highlight); paintPixel(img, 13, 8, shadow);
            }
        }

        return img;
    }

    private static void paintPixel(BufferedImage img, int x, int y, int rgb) {
        if (x >= 0 && x < 16 && y >= 0 && y < 16) {
            img.setRGB(x, y, 0xFF000000 | rgb);
        }
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
            factor = 1.0f + factor;
            r = (int) (r * factor);
            g = (int) (g * factor);
            b = (int) (b * factor);
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return (r << 16) | (g << 8) | b;
    }
}
