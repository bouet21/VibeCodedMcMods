package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

public class TextureEnhancer {

    static class TierDef {
        final int tier;
        final String name;
        final int rgb;

        TierDef(int tier, String name, int rgb) {
            this.tier = tier;
            this.name = name;
            this.rgb = rgb;
        }
    }

    static final List<TierDef> TIERS = List.of(
            new TierDef(1, "Copperite", 0xD97843),
            new TierDef(10, "Invar", 0x7E8582),
            new TierDef(20, "Absolute Zero", 0x00E1D9),
            new TierDef(30, "Solarite", 0xFFCC00),
            new TierDef(40, "Chlorophyte", 0x22E84D),
            new TierDef(50, "Runite", 0x24C7BA),
            new TierDef(60, "Coreium", 0xFF5522),
            new TierDef(70, "Primordial Sculk", 0x00FFD5),
            new TierDef(80, "Infernal Core", 0xFF6B1A),
            new TierDef(90, "Soul Gem", 0x16A694),
            new TierDef(100, "Nether Heart", 0xD61142),
            new TierDef(115, "Dragon Breath", 0xDE3CE3),
            new TierDef(125, "Galacticite", 0x483296),
            new TierDef(135, "Infinity Shard", 0xEB5CE8),
            new TierDef(140, "Genesis", 0xE8F768),
            new TierDef(150, "Godmetal", 0xFFE885)
    );

    public static void main(String[] args) throws Exception {
        System.out.println("Starting High-Fidelity Texture Enhancement for Chain Ores...");

        File tmplDir = new File("tools/templates");
        File itemDir = new File("src/main/resources/assets/chainores/textures/item");
        itemDir.mkdirs();

        // 1. Load vanilla templates
        BufferedImage axeTmpl = ImageIO.read(new File(tmplDir, "diamond_axe.png"));
        BufferedImage shovelTmpl = ImageIO.read(new File(tmplDir, "diamond_shovel.png"));
        BufferedImage hoeTmpl = ImageIO.read(new File(tmplDir, "diamond_hoe.png"));
        BufferedImage bowTmpl = ImageIO.read(new File(tmplDir, "bow.png"));
        BufferedImage crossbowTmpl = ImageIO.read(new File(tmplDir, "crossbow_standby.png"));

        // 2. Enhance 16 Axes, 16 Shovels, 16 Hoes
        for (TierDef t : TIERS) {
            BufferedImage axe = renderThemedTool(axeTmpl, t.rgb, t.tier);
            ImageIO.write(axe, "PNG", new File(itemDir, "axe_tier_" + t.tier + ".png"));

            BufferedImage shovel = renderThemedTool(shovelTmpl, t.rgb, t.tier);
            ImageIO.write(shovel, "PNG", new File(itemDir, "shovel_tier_" + t.tier + ".png"));

            BufferedImage hoe = renderThemedTool(hoeTmpl, t.rgb, t.tier);
            ImageIO.write(hoe, "PNG", new File(itemDir, "hoe_tier_" + t.tier + ".png"));
        }
        System.out.println("Enhanced all 16 Axes, 16 Shovels, and 16 Hoes with 5-tone shading & tier handles!");

        // 3. Dragon Breath Bow
        BufferedImage dragonBow = renderThemedBow(bowTmpl, 0xDE3CE3);
        ImageIO.write(dragonBow, "PNG", new File(itemDir, "dragon_breath_bow.png"));
        System.out.println("Enhanced Dragon Breath Bow texture!");

        // 4. Galactic Crossbow
        BufferedImage galXbow = renderThemedCrossbow(crossbowTmpl, 0x483296);
        ImageIO.write(galXbow, "PNG", new File(itemDir, "galactic_crossbow.png"));
        System.out.println("Enhanced Galactic Crossbow texture!");

        // 5. Aegis Shield
        BufferedImage shield = renderAegisShield();
        ImageIO.write(shield, "PNG", new File(itemDir, "aegis_shield.png"));
        System.out.println("Enhanced Aegis Shield texture!");

        // 6. God Omnitool (Tier 150)
        BufferedImage omnitool = renderGodOmnitool();
        ImageIO.write(omnitool, "PNG", new File(itemDir, "god_omnitool.png"));
        System.out.println("Enhanced God Omnitool (T150) texture!");

        // 7. Prospector Rod & Structure Compass
        BufferedImage rod = renderProspectorRod();
        ImageIO.write(rod, "PNG", new File(itemDir, "prospector_rod.png"));
        System.out.println("Enhanced Prospector Rod texture!");

        BufferedImage compass = renderStructureCompass();
        ImageIO.write(compass, "PNG", new File(itemDir, "structure_compass.png"));
        System.out.println("Enhanced Structure Compass texture!");

        // 8. 4 Sockets Gems
        ImageIO.write(renderFacetedGem(0xE01830, 0x7A0012, 0xFF8090), "PNG", new File(itemDir, "blood_gem.png"));
        ImageIO.write(renderFacetedGem(0x8A2BE2, 0x3E0A70, 0xDDA0DD), "PNG", new File(itemDir, "void_gem.png"));
        ImageIO.write(renderFacetedGem(0xFFB000, 0x8C4B00, 0xFFE070), "PNG", new File(itemDir, "core_gem.png"));
        ImageIO.write(renderFacetedGem(0x00FF88, 0x006633, 0xB0FFD8), "PNG", new File(itemDir, "genesis_gem.png"));
        System.out.println("Enhanced all 4 Faceted Sockets Gems!");

        // 9. Boss Summoners & Catalysts
        ImageIO.write(renderAbyssalEye(), "PNG", new File(itemDir, "abyssal_eye.png"));
        ImageIO.write(renderFlameHeart(), "PNG", new File(itemDir, "flame_heart.png"));
        ImageIO.write(renderVoidBeacon(), "PNG", new File(itemDir, "void_beacon.png"));
        ImageIO.write(renderCosmicAltarCore(), "PNG", new File(itemDir, "cosmic_altar_core.png"));
        ImageIO.write(renderCosmicSingularity(), "PNG", new File(itemDir, "cosmic_singularity.png"));
        ImageIO.write(renderCosmicTrophy(), "PNG", new File(itemDir, "cosmic_trophy.png"));
        System.out.println("Enhanced all Boss Summoners & Artifacts!");

        // 10. Workstation Blocks & Items
        File blockDir = new File("src/main/resources/assets/chainores/textures/block");
        blockDir.mkdirs();

        BufferedImage anvilTmpl = ImageIO.read(new File(tmplDir, "anvil.png"));
        BufferedImage blastFront = ImageIO.read(new File(tmplDir, "blast_furnace_front.png"));
        BufferedImage blastSide = ImageIO.read(new File(tmplDir, "blast_furnace_side.png"));
        BufferedImage blastTop = ImageIO.read(new File(tmplDir, "blast_furnace_top.png"));
        BufferedImage cryingObs = ImageIO.read(new File(tmplDir, "crying_obsidian.png"));

        // Chain Anvil (Item & Block)
        BufferedImage chainAnvil = renderChainAnvil(anvilTmpl);
        ImageIO.write(chainAnvil, "PNG", new File(blockDir, "chain_anvil.png"));
        ImageIO.write(chainAnvil, "PNG", new File(itemDir, "chain_anvil.png"));

        // Chain Forge
        ImageIO.write(renderChainForgeFront(blastFront), "PNG", new File(blockDir, "chain_forge_front.png"));
        ImageIO.write(renderChainForgeSide(blastSide), "PNG", new File(blockDir, "chain_forge_side.png"));
        ImageIO.write(renderChainForgeTop(blastTop), "PNG", new File(blockDir, "chain_forge_top.png"));
        ImageIO.write(renderChainForgeBottom(), "PNG", new File(blockDir, "chain_forge_bottom.png"));

        // Cosmic Smelter
        ImageIO.write(renderCosmicSmelterFront(cryingObs, false), "PNG", new File(blockDir, "cosmic_smelter_front.png"));
        ImageIO.write(renderCosmicSmelterFront(cryingObs, true), "PNG", new File(blockDir, "cosmic_smelter_front_on.png"));
        ImageIO.write(renderCosmicSmelterSide(cryingObs), "PNG", new File(blockDir, "cosmic_smelter_side.png"));
        ImageIO.write(renderCosmicSmelterTop(cryingObs), "PNG", new File(blockDir, "cosmic_smelter_top.png"));
        ImageIO.write(renderCosmicSmelterBottom(), "PNG", new File(blockDir, "cosmic_smelter_bottom.png"));
        System.out.println("Enhanced all Workstations (Chain Forge, Chain Anvil, Cosmic Smelter) textures!");

        System.out.println("All textures upgraded to premium visual quality!");
    }

    private static BufferedImage renderThemedTool(BufferedImage template, int rgb, int tier) {
        int w = template.getWidth();
        int h = template.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = template.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Handle shaft / grip detection
                // Wood pixels: brownish with r > g > b
                if (r > 60 && g > 40 && b < 65 && (r - b) > 20) {
                    if (tier >= 140) {
                        // Tiers 140-150: Divine Radiant Shaft (Gold with White Highlights)
                        if (x <= 3 && y >= 12) {
                            out.setRGB(x, y, 0xFF000000 | 0xFFFFFF); // Brilliant tip
                        } else if ((x + y) % 2 == 0) {
                            out.setRGB(x, y, 0xFF000000 | 0xFFE885);
                        } else {
                            out.setRGB(x, y, 0xFF000000 | 0xD4AF37);
                        }
                    } else if (tier >= 100) {
                        // Tiers 100-135: Obsidian Netherite Shaft with Golden Pommel
                        if (x <= 3 && y >= 12) {
                            out.setRGB(x, y, 0xFF000000 | 0xFFD700); // Gold pommel
                        } else {
                            // Dark obsidian gradient
                            float stickLum = (r + g + b) / (3.0f * 255.0f);
                            int obs = (stickLum > 0.4f) ? 0x362D3D : 0x1E1724;
                            out.setRGB(x, y, 0xFF000000 | obs);
                        }
                    } else if (tier >= 70) {
                        // Tiers 70-90: Deep Dark Sculk-infused Shaft
                        if (x <= 3 && y >= 12) {
                            out.setRGB(x, y, 0xFF000000 | 0x00FFD5);
                        } else {
                            int sculk = ((x + y) % 3 == 0) ? 0x0D2B33 : 0x051317;
                            out.setRGB(x, y, 0xFF000000 | sculk);
                        }
                    } else {
                        // Tiers 1-60: Rich polished walnut wood with enhanced shading
                        out.setRGB(x, y, argb);
                    }
                    continue;
                }

                // Metal / Mineral Head: 5-tone luminosity mapping
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                int color;
                if (lum < 0.24f) {
                    color = adjustBrightness(rgb, -0.68f); // Deep bevel outline
                } else if (lum < 0.44f) {
                    color = adjustBrightness(rgb, -0.38f); // Core shadow
                } else if (lum < 0.70f) {
                    color = rgb;                           // Base vibrant tone
                } else if (lum < 0.86f) {
                    color = adjustBrightness(rgb, +0.40f); // Bright reflection
                } else {
                    // Specular highlight
                    if (tier >= 140) {
                        color = 0xFFFFFF; // Pure diamond shine
                    } else if (tier >= 90) {
                        color = adjustBrightness(rgb, +0.85f);
                    } else {
                        color = adjustBrightness(rgb, +0.62f);
                    }
                }

                out.setRGB(x, y, 0xFF000000 | color);
            }
        }
        return out;
    }

    private static BufferedImage renderThemedBow(BufferedImage template, int rgb) {
        int w = template.getWidth();
        int h = template.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int main = rgb;
        int dark = adjustBrightness(rgb, -0.55f);
        int light = adjustBrightness(rgb, +0.50f);
        int stringColor = 0xFFF0E0FF; // Glowing celestial bowstring
        int grip = 0xFFFFD700;        // Golden dragon grip

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = template.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Center grip
                if (Math.abs(x - 7) <= 1 && Math.abs(y - 7) <= 1) {
                    out.setRGB(x, y, 0xFF000000 | grip);
                    continue;
                }

                // Bowstring
                if (x == y || Math.abs(x - y) <= 1 && (x <= 4 || x >= 11)) {
                    // String pixel
                    if (r > 150 && g > 150 && b > 150) {
                        out.setRGB(x, y, stringColor);
                        continue;
                    }
                }

                // Limb pixels
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                int col = (lum < 0.35f) ? dark : (lum < 0.70f) ? main : light;
                out.setRGB(x, y, 0xFF000000 | col);
            }
        }
        return out;
    }

    private static BufferedImage renderThemedCrossbow(BufferedImage template, int rgb) {
        int w = template.getWidth();
        int h = template.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int cosmic = rgb;
        int light = adjustBrightness(rgb, +0.60f);
        int dark = adjustBrightness(rgb, -0.50f);
        int gold = 0xFFFFD700;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = template.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Iron / metal parts in vanilla crossbow (front brackets, trigger)
                if (Math.abs(r - g) < 15 && Math.abs(g - b) < 15) {
                    out.setRGB(x, y, 0xFF000000 | gold);
                    continue;
                }

                // Wood / stock parts transformed to Cosmic Crystal
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                int col = (lum < 0.35f) ? dark : (lum < 0.70f) ? cosmic : light;
                out.setRGB(x, y, 0xFF000000 | col);
            }
        }
        return out;
    }

    private static BufferedImage renderAegisShield() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int goldBorder = 0xFFD4AF37;
        int goldHighlight = 0xFFFFF1A8;
        int goldShadow = 0xFF8A6B1A;

        int sculkDark = 0xFF051821;
        int sculkCore = 0xFF0A2E3D;
        int sculkGlow = 0xFF00FFD5;
        int centerEye = 0xFF55FFFF;

        // 1. Ornate Gold Border
        for (int x = 3; x <= 12; x++) {
            img.setRGB(x, 1, goldBorder);
            img.setRGB(x, 2, (x == 3 || x == 12) ? goldShadow : goldHighlight);
        }
        for (int y = 3; y <= 9; y++) {
            img.setRGB(2, y, goldBorder);
            img.setRGB(3, y, goldHighlight);
            img.setRGB(12, y, goldShadow);
            img.setRGB(13, y, goldBorder);
        }
        // Beveled bottom taper
        img.setRGB(3, 10, goldBorder); img.setRGB(12, 10, goldBorder);
        img.setRGB(4, 11, goldBorder); img.setRGB(11, 11, goldBorder);
        img.setRGB(5, 12, goldBorder); img.setRGB(10, 12, goldBorder);
        img.setRGB(6, 13, goldBorder); img.setRGB(9, 13, goldBorder);
        img.setRGB(7, 14, goldBorder); img.setRGB(8, 14, goldBorder);

        // 2. Shield Face (Deep Sculk Crystal)
        for (int y = 3; y <= 9; y++) {
            for (int x = 4; x <= 11; x++) {
                img.setRGB(x, y, sculkCore);
            }
        }
        for (int x = 4; x <= 11; x++) img.setRGB(x, 10, sculkDark);
        for (int x = 5; x <= 10; x++) img.setRGB(x, 11, sculkDark);
        for (int x = 6; x <= 9; x++)  img.setRGB(x, 12, sculkDark);
        for (int x = 7; x <= 8; x++)  img.setRGB(x, 13, sculkDark);

        // 3. Central Eye Emblem
        img.setRGB(7, 5, sculkGlow); img.setRGB(8, 5, sculkGlow);
        img.setRGB(6, 6, sculkGlow); img.setRGB(7, 6, centerEye); img.setRGB(8, 6, centerEye); img.setRGB(9, 6, sculkGlow);
        img.setRGB(7, 7, sculkGlow); img.setRGB(8, 7, sculkGlow);
        img.setRGB(7, 8, 0xFFFFFFFF); // Specular eye glint

        return img;
    }

    private static BufferedImage renderGodOmnitool() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int divineWhite = 0xFFFFFFFF;
        int divineGold = 0xFFFFE885;
        int deepGold = 0xFFD4AF37;
        int shadowGold = 0xFF7D5F00;
        int genesisCyan = 0xFF00FFD5;
        int genesisCore = 0xFF55FFFF;

        // Long Halberd Shaft (Diagonal)
        for (int i = 1; i <= 9; i++) {
            int x = i;
            int y = 15 - i;
            img.setRGB(x, y, divineGold);
            if (x > 1 && y < 15) img.setRGB(x - 1, y, shadowGold);
        }
        // Shaft Pommel
        img.setRGB(1, 15, deepGold);
        img.setRGB(0, 15, shadowGold);

        // Multi-head Socket Hub (Center around 9,6)
        img.setRGB(9, 6, genesisCore);
        img.setRGB(8, 6, genesisCyan);
        img.setRGB(10, 6, genesisCyan);
        img.setRGB(9, 5, genesisCyan);
        img.setRGB(9, 7, genesisCyan);

        // Top Spear Tip (pointing to top right 14,1 and 15,0)
        img.setRGB(11, 4, divineGold);
        img.setRGB(12, 3, divineGold);
        img.setRGB(13, 2, divineWhite);
        img.setRGB(14, 1, divineWhite);
        img.setRGB(15, 0, divineWhite);
        img.setRGB(13, 3, deepGold);
        img.setRGB(14, 2, deepGold);

        // Axe Blade Wing (Upper Left: heavy cutting crescent)
        img.setRGB(7, 4, deepGold);
        img.setRGB(6, 3, divineGold);
        img.setRGB(7, 2, divineGold);
        img.setRGB(8, 2, divineWhite);
        img.setRGB(5, 4, divineWhite);
        img.setRGB(5, 5, divineWhite);
        img.setRGB(6, 6, divineGold);

        // War-Pick / Scythe Hook (Lower Right: curving puncture hook)
        img.setRGB(11, 7, deepGold);
        img.setRGB(12, 7, divineGold);
        img.setRGB(13, 8, divineWhite);
        img.setRGB(14, 9, divineWhite);
        img.setRGB(13, 9, deepGold);

        // Shovel Excavation Collar (Back Flange)
        img.setRGB(8, 7, divineGold);
        img.setRGB(7, 8, divineWhite);
        img.setRGB(8, 8, deepGold);

        return img;
    }

    private static BufferedImage renderProspectorRod() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int brass = 0xFFD4AF37;
        int darkBrass = 0xFF7D5F00;
        int lightBrass = 0xFFFFF1A8;
        int crystal = 0xFF00FFD5;
        int crystalCore = 0xFFFFFFFF;
        int crystalShadow = 0xFF0B6358;

        // Diagonal Surveying Rod Shaft
        for (int i = 2; i <= 10; i++) {
            int x = i;
            int y = 15 - i;
            img.setRGB(x, y, brass);
            if ((x + y) % 3 == 0) img.setRGB(x, y, lightBrass);
            img.setRGB(x - 1, y, darkBrass);
        }
        // Base grip ring
        img.setRGB(2, 14, darkBrass);
        img.setRGB(1, 15, brass);

        // Resonance Crystal Head (multi-faceted gem on tip 12,3 to 15,0)
        img.setRGB(10, 4, darkBrass);
        img.setRGB(11, 5, darkBrass);

        img.setRGB(11, 3, crystal);
        img.setRGB(12, 4, crystal);
        img.setRGB(12, 2, crystal);
        img.setRGB(13, 3, crystalCore);
        img.setRGB(13, 1, crystalCore);
        img.setRGB(14, 2, crystalCore);
        img.setRGB(14, 0, crystal);
        img.setRGB(15, 1, crystal);
        img.setRGB(13, 4, crystalShadow);
        img.setRGB(14, 3, crystalShadow);

        return img;
    }

    private static BufferedImage renderStructureCompass() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int brassHighlight = 0xFFFFF2A8;
        int brassBody = 0xFFD4AF37;
        int brassShadow = 0xFF7D5F00;
        int darkRim = 0xFF423000;
        int dialBg = 0xFF121526;
        int needleRed = 0xFFFF2E4C;
        int needleCyan = 0xFF00F5D4;
        int centerGold = 0xFFFFFFFF;

        // Outer Ring Octagonal / Round (circle r=6 around center 7.5, 7.5)
        for (int y = 1; y <= 14; y++) {
            for (int x = 1; x <= 14; x++) {
                double dx = x - 7.5;
                double dy = y - 7.5;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist <= 6.8 && dist > 5.2) {
                    if (y <= 4 || (y <= 7 && x <= 4)) {
                        img.setRGB(x, y, brassHighlight);
                    } else if (dist > 6.2) {
                        img.setRGB(x, y, darkRim);
                    } else if (y > 10) {
                        img.setRGB(x, y, brassShadow);
                    } else {
                        img.setRGB(x, y, brassBody);
                    }
                } else if (dist <= 5.2) {
                    img.setRGB(x, y, dialBg);
                }
            }
        }

        // Top suspension ring
        img.setRGB(7, 0, brassHighlight); img.setRGB(8, 0, brassHighlight);
        img.setRGB(6, 1, brassBody); img.setRGB(9, 1, brassShadow);

        // Cardinal Insets (North: Ruby, South: Sapphire, West: Amber, East: Emerald)
        img.setRGB(7, 2, 0xFFFF3B56); img.setRGB(8, 2, 0xFFFF7085);
        img.setRGB(7, 13, 0xFF00B4D8); img.setRGB(8, 13, 0xFF90E0EF);
        img.setRGB(2, 7, 0xFFFFBE0B); img.setRGB(2, 8, 0xFFFB5607);
        img.setRGB(13, 7, 0xFF06D6A0); img.setRGB(13, 8, 0xFF118AB2);

        // Compass Needle
        img.setRGB(6, 6, needleRed);
        img.setRGB(5, 5, needleRed);
        img.setRGB(4, 4, 0xFFFF8597);

        img.setRGB(9, 9, needleCyan);
        img.setRGB(10, 10, needleCyan);
        img.setRGB(11, 11, 0xFF99FCF0);

        // Center Pivoting Spindle
        img.setRGB(7, 7, brassBody);
        img.setRGB(8, 7, centerGold);
        img.setRGB(7, 8, brassShadow);
        img.setRGB(8, 8, brassBody);

        return img;
    }

    private static BufferedImage renderFacetedGem(int primary, int shadow, int highlight) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        primary = 0xFF000000 | primary;
        shadow = 0xFF000000 | shadow;
        highlight = 0xFF000000 | highlight;
        int darkOutline = adjustBrightness(shadow & 0x00FFFFFF, -0.60f) | 0xFF000000;

        // Faceted Diamond cut shape
        // Top crown: y=3 to 6
        for (int x = 6; x <= 9; x++) img.setRGB(x, 3, highlight);
        for (int x = 4; x <= 11; x++) img.setRGB(x, 4, (x <= 7) ? highlight : primary);
        for (int x = 3; x <= 12; x++) img.setRGB(x, 5, (x <= 6) ? highlight : (x <= 9) ? primary : shadow);
        for (int x = 2; x <= 13; x++) img.setRGB(x, 6, (x <= 5) ? highlight : (x <= 10) ? primary : shadow);

        // Lower Pavilion: y=7 to 13
        for (int x = 3; x <= 12; x++) img.setRGB(x, 7, (x <= 5) ? highlight : (x <= 9) ? primary : shadow);
        for (int x = 4; x <= 11; x++) img.setRGB(x, 8, (x <= 6) ? primary : shadow);
        for (int x = 5; x <= 10; x++) img.setRGB(x, 9, (x <= 7) ? primary : shadow);
        for (int x = 6; x <= 9; x++)  img.setRGB(x, 10, (x <= 7) ? primary : shadow);
        for (int x = 7; x <= 8; x++)  img.setRGB(x, 11, shadow);
        img.setRGB(7, 12, darkOutline); img.setRGB(8, 12, darkOutline);

        // Crisp dark outline accents
        img.setRGB(5, 3, darkOutline); img.setRGB(10, 3, darkOutline);
        img.setRGB(3, 4, darkOutline); img.setRGB(12, 4, darkOutline);
        img.setRGB(2, 5, darkOutline); img.setRGB(13, 5, darkOutline);
        img.setRGB(1, 6, darkOutline); img.setRGB(14, 6, darkOutline);

        // Brilliant sparkle at (6, 5) and (7, 4)
        img.setRGB(6, 5, 0xFFFFFFFF);
        img.setRGB(7, 4, 0xFFFFFFFF);
        img.setRGB(5, 5, 0xFFFFFFFF);

        return img;
    }

    private static BufferedImage renderAbyssalEye() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int sculk = 0xFF051A24;
        int glow = 0xFF00FFD5;
        int center = 0xFF00222B;
        int pupil = 0xFF55FFFF;

        for (int y = 3; y <= 12; y++) {
            for (int x = 3; x <= 12; x++) {
                double dist = Math.hypot(x - 7.5, y - 7.5);
                if (dist <= 4.8) {
                    if (dist > 3.8) img.setRGB(x, y, sculk);
                    else if (dist > 2.2) img.setRGB(x, y, glow);
                    else if (dist > 1.0) img.setRGB(x, y, center);
                    else img.setRGB(x, y, pupil);
                }
            }
        }
        img.setRGB(6, 6, 0xFFFFFFFF); // Eye highlight
        return img;
    }

    private static BufferedImage renderFlameHeart() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int border = 0xFF540909;
        int fireRed = 0xFFD61111;
        int fireOrange = 0xFFFF7700;
        int fireYellow = 0xFFFFEE00;
        int coreWhite = 0xFFFFFFFF;

        for (int y = 3; y <= 13; y++) {
            for (int x = 3; x <= 12; x++) {
                // Heart shape formula
                double dx = (x - 7.5) / 4.5;
                double dy = (y - 7.0) / 4.5;
                if (Math.pow(dx * dx + dy * dy - 0.7, 3) - dx * dx * dy * dy * dy <= 0.1) {
                    double dist = Math.hypot(x - 7.5, y - 8.0);
                    if (dist > 3.2) img.setRGB(x, y, border);
                    else if (dist > 2.2) img.setRGB(x, y, fireRed);
                    else if (dist > 1.2) img.setRGB(x, y, fireOrange);
                    else if (dist > 0.6) img.setRGB(x, y, fireYellow);
                    else img.setRGB(x, y, coreWhite);
                }
            }
        }
        return img;
    }

    private static BufferedImage renderVoidBeacon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int obsidian = 0xFF1B1424;
        int beam = 0xFFE085FF;
        int beamCore = 0xFFFFFFFF;
        int ring = 0xFF8A30B8;

        for (int y = 2; y <= 13; y++) {
            img.setRGB(7, y, beam);
            img.setRGB(8, y, beamCore);
        }
        for (int x = 4; x <= 11; x++) {
            img.setRGB(x, 13, obsidian);
            img.setRGB(x, 14, obsidian);
        }
        img.setRGB(5, 7, ring); img.setRGB(10, 7, ring);
        img.setRGB(4, 8, ring); img.setRGB(11, 8, ring);
        img.setRGB(5, 9, ring); img.setRGB(10, 9, ring);
        return img;
    }

    private static BufferedImage renderCosmicAltarCore() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int gold = 0xFFFFD700;
        int cosmic = 0xFF8B5CF6;
        int star = 0xFFFFFFFF;

        for (int y = 3; y <= 12; y++) {
            for (int x = 3; x <= 12; x++) {
                double d = Math.hypot(x - 7.5, y - 7.5);
                if (d <= 4.5) {
                    if (d > 3.5) img.setRGB(x, y, gold);
                    else if (d > 1.5) img.setRGB(x, y, cosmic);
                    else img.setRGB(x, y, star);
                }
            }
        }
        // Rays
        img.setRGB(7, 1, star); img.setRGB(8, 1, star);
        img.setRGB(7, 14, star); img.setRGB(8, 14, star);
        img.setRGB(1, 7, star); img.setRGB(1, 8, star);
        img.setRGB(14, 7, star); img.setRGB(14, 8, star);
        return img;
    }

    private static BufferedImage renderCosmicSingularity() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int voidBlack = 0xFF000000;
        int accretion = 0xFFFF77FF;
        int starWhite = 0xFFFFFFFF;

        for (int y = 2; y <= 13; y++) {
            for (int x = 2; x <= 13; x++) {
                double d = Math.hypot(x - 7.5, y - 7.5);
                if (d <= 5.0) {
                    if (d < 2.0) img.setRGB(x, y, voidBlack);
                    else if (d < 3.5) img.setRGB(x, y, accretion);
                    else if (d < 4.8) img.setRGB(x, y, 0xFF4B0082);
                }
            }
        }
        img.setRGB(4, 4, starWhite);
        img.setRGB(11, 11, starWhite);
        return img;
    }

    private static BufferedImage renderCosmicTrophy() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int gold = 0xFFFFD700;
        int light = 0xFFFFF2A8;
        int dark = 0xFF8A6B00;
        int diamond = 0xFF55FFFF;

        // Trophy base
        for (int x = 4; x <= 11; x++) img.setRGB(x, 14, dark);
        for (int x = 5; x <= 10; x++) img.setRGB(x, 13, gold);
        for (int y = 10; y <= 12; y++) {
            img.setRGB(7, y, gold); img.setRGB(8, y, light);
        }
        // Cup bowl
        for (int x = 4; x <= 11; x++) img.setRGB(x, 5, gold);
        for (int x = 4; x <= 11; x++) img.setRGB(x, 6, light);
        for (int x = 5; x <= 10; x++) img.setRGB(x, 7, gold);
        for (int x = 6; x <= 9; x++) img.setRGB(x, 8, gold);
        for (int x = 7; x <= 8; x++) img.setRGB(x, 9, dark);

        // Handles
        img.setRGB(3, 6, gold); img.setRGB(3, 7, dark);
        img.setRGB(12, 6, gold); img.setRGB(12, 7, dark);

        // Crown Jewel
        img.setRGB(7, 3, diamond); img.setRGB(8, 3, diamond);
        img.setRGB(7, 4, diamond); img.setRGB(8, 4, 0xFFFFFFFF);

        return img;
    }

    private static BufferedImage renderChainAnvil(BufferedImage tmpl) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int gold = 0xFFFFD700;
        int goldDark = 0xFF8A6B00;
        int steelTint = 0x3D354A; // Hardened dark steel

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = tmpl.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r + g + b) / (3.0f * 255.0f);

                // Base hardened steel
                int col = adjustBrightness(steelTint, (lum - 0.5f) * 1.2f);
                out.setRGB(x, y, 0xFF000000 | col);
            }
        }

        // Add reinforced golden chain bands across the anvil waist & top edge
        for (int x = 4; x <= 11; x++) {
            out.setRGB(x, 1, ((x % 2 == 0) ? gold : goldDark));
            out.setRGB(x, 4, ((x % 2 == 1) ? gold : goldDark));
        }
        out.setRGB(7, 2, 0xFF00FFD5); // Runic core glow
        out.setRGB(8, 2, 0xFF00FFD5);
        return out;
    }

    private static BufferedImage renderChainForgeFront(BufferedImage tmpl) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int bronze = 0x5C4738;
        int fireOrange = 0xFFFF6600;
        int fireYellow = 0xFFFFDD00;
        int gold = 0xFFFFD700;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = tmpl.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r + g + b) / (3.0f * 255.0f);

                // If furnace grille fire:
                if (r > 160 && g < 140 && b < 80) {
                    out.setRGB(x, y, 0xFF000000 | fireOrange);
                } else if (r > 200 && g > 160 && b < 100) {
                    out.setRGB(x, y, 0xFF000000 | fireYellow);
                } else {
                    int c = adjustBrightness(bronze, (lum - 0.5f) * 1.3f);
                    out.setRGB(x, y, 0xFF000000 | c);
                }
            }
        }
        // Reinforced top & bottom rivet bands
        for (int x = 0; x < 16; x++) {
            if (x % 3 == 0) {
                out.setRGB(x, 1, 0xFF000000 | gold);
                out.setRGB(x, 14, 0xFF000000 | gold);
            }
        }
        return out;
    }

    private static BufferedImage renderChainForgeSide(BufferedImage tmpl) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int bronze = 0x5C4738;
        int gold = 0xFFFFD700;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = tmpl.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r + g + b) / (3.0f * 255.0f);
                int c = adjustBrightness(bronze, (lum - 0.5f) * 1.3f);
                out.setRGB(x, y, 0xFF000000 | c);
            }
        }
        // Golden chain x-brace on side
        for (int i = 3; i <= 12; i++) {
            out.setRGB(i, i, 0xFF000000 | gold);
            out.setRGB(15 - i, i, 0xFF000000 | gold);
        }
        return out;
    }

    private static BufferedImage renderChainForgeTop(BufferedImage tmpl) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int bronze = 0x5C4738;
        int vent = 0x221B17;
        int gold = 0xFFFFD700;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = tmpl.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r + g + b) / (3.0f * 255.0f);
                if (Math.abs(x - 7.5) < 3.5 && Math.abs(y - 7.5) < 3.5) {
                    out.setRGB(x, y, 0xFF000000 | vent);
                } else {
                    int c = adjustBrightness(bronze, (lum - 0.5f) * 1.3f);
                    out.setRGB(x, y, 0xFF000000 | c);
                }
            }
        }
        out.setRGB(4, 4, 0xFF000000 | gold); out.setRGB(11, 4, 0xFF000000 | gold);
        out.setRGB(4, 11, 0xFF000000 | gold); out.setRGB(11, 11, 0xFF000000 | gold);
        return out;
    }

    private static BufferedImage renderChainForgeBottom() {
        BufferedImage out = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int stone = 0x362B22;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int col = adjustBrightness(stone, ((x * y) % 7 - 3) * 0.05f);
                out.setRGB(x, y, 0xFF000000 | col);
            }
        }
        return out;
    }

    private static BufferedImage renderCosmicSmelterFront(BufferedImage tmpl, boolean on) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int gold = 0xFFFFD700;
        int voidPurple = 0xFF4A154B;
        int coreStar = 0xFFFFFFFF;
        int coreGlow = 0xFFFF77FF;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, tmpl.getRGB(x, y));
            }
        }

        // Chamber portal door (center 4 to 11 x, 5 to 11 y)
        for (int y = 5; y <= 11; y++) {
            for (int x = 4; x <= 11; x++) {
                if (x == 4 || x == 11 || y == 5 || y == 11) {
                    out.setRGB(x, y, 0xFF000000 | gold);
                } else {
                    if (on) {
                        double d = Math.hypot(x - 7.5, y - 8.0);
                        if (d <= 1.2) out.setRGB(x, y, coreStar);
                        else if (d <= 2.2) out.setRGB(x, y, coreGlow);
                        else out.setRGB(x, y, voidPurple);
                    } else {
                        out.setRGB(x, y, voidPurple);
                    }
                }
            }
        }
        return out;
    }

    private static BufferedImage renderCosmicSmelterSide(BufferedImage tmpl) {
        return tmpl;
    }

    private static BufferedImage renderCosmicSmelterTop(BufferedImage tmpl) {
        int w = tmpl.getWidth();
        int h = tmpl.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int gold = 0xFFFFD700;
        int purple = 0xFF8A2BE2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, tmpl.getRGB(x, y));
            }
        }
        // Top cosmic intake ring
        for (int i = 4; i <= 11; i++) {
            out.setRGB(i, 4, gold); out.setRGB(i, 11, gold);
            out.setRGB(4, i, gold); out.setRGB(11, i, gold);
        }
        for (int y = 6; y <= 9; y++) {
            for (int x = 6; x <= 9; x++) {
                out.setRGB(x, y, purple);
            }
        }
        return out;
    }

    private static BufferedImage renderCosmicSmelterBottom() {
        BufferedImage out = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int obs = 0x100A1A;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                out.setRGB(x, y, 0xFF000000 | obs);
            }
        }
        return out;
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
