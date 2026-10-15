package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BossTextureEnhancer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Master-Quality Boss Texture Enhancement...");

        File tmplDir = new File("tools/templates");
        File entityDir = new File("src/main/resources/assets/chainores/textures/entity");
        entityDir.mkdirs();

        BufferedImage zombieTmpl = ImageIO.read(new File(tmplDir, "zombie.png"));
        BufferedImage piglinTmpl = ImageIO.read(new File(tmplDir, "piglin_brute.png"));

        // 1. Abyssal Guardian (Sculk Knight / Warden-infused Titan) - Elaborate Sculk & Ribs
        BufferedImage abyssal = generateAbyssalGuardian(zombieTmpl);
        ImageIO.write(abyssal, "PNG", new File(entityDir, "abyssal_guardian.png"));
        System.out.println("Generated Abyssal Guardian (Sculk/Warden) texture with intricate bio-veins!");

        // 2. Nether Behemoth (Molten Crimson Warlord) - Piglin Brute
        BufferedImage nether = generateNetherBehemoth(piglinTmpl);
        ImageIO.write(nether, "PNG", new File(entityDir, "nether_behemoth.png"));
        System.out.println("Generated Nether Behemoth (Crimson Molten) texture!");

        // 3. Void Colossus (Astral Void Lich) - Full 64x64 Biped
        BufferedImage voidCol = generateVoidColossus(zombieTmpl);
        ImageIO.write(voidCol, "PNG", new File(entityDir, "void_colossus.png"));
        System.out.println("Generated Void Colossus (Astral Void) texture with Event Horizon Core!");

        // 4. Cosmic Sovereign (Celestial Genesis Deity) - Full 64x64 Biped
        BufferedImage cosmic = generateCosmicSovereign(zombieTmpl);
        ImageIO.write(cosmic, "PNG", new File(entityDir, "cosmic_sovereign.png"));
        System.out.println("Generated Cosmic Sovereign (Genesis Deity) texture with Divine Marble & Gold!");

        System.out.println("All 4 boss textures upgraded to legendary, bug-free visual quality!");
    }

    private static BufferedImage generateAbyssalGuardian(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int deepslate = 0x111822;
        int sculkDark = 0xFF021B24;
        int sculkTeal = 0xFF005B60;
        int sculkGlow = 0xFF00FFD5;
        int sculkBright = 0xFF80FFFF;
        int soulWhite = 0xFFFFFFFF;
        int ribIvory = 0xFFE0D8C8;
        int ribShadow = 0xFF8A8272;

        // Base shading: Deep dark mottled slate
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(deepslate, (lum - 0.5f) * 1.5f);
                // Mottled sculk noise
                if ((x * 7 + y * 13) % 11 == 0) {
                    c = adjustBrightness(sculkDark, (lum - 0.5f) * 1.2f);
                }
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // --- 1. HEAD (Face at x: 8..15, y: 8..15) ---
        // Blind Hollow Soul Eyes with crying sculk tears
        img.setRGB(9, 10, sculkBright); img.setRGB(10, 10, sculkGlow);
        img.setRGB(13, 10, sculkGlow); img.setRGB(14, 10, sculkBright);
        img.setRGB(9, 11, sculkTeal); img.setRGB(14, 11, sculkTeal);
        img.setRGB(9, 12, sculkDark); img.setRGB(14, 12, sculkDark);

        // Jagged Warden teeth & mouth cavity
        for (int x = 9; x <= 14; x++) {
            img.setRGB(x, 14, 0xFF010609); // dark mouth
            img.setRGB(x, 15, 0xFF010609);
            if (x % 2 == 1) {
                img.setRGB(x, 13, ribIvory); // upper fangs
                img.setRGB(x, 15, ribShadow); // lower teeth
            }
        }

        // --- 2. HEAD OVERLAY: Warden Sculk Tendrils/Antennae (x: 32..63, y: 0..15) ---
        for (int y = 0; y <= 7; y++) {
            img.setRGB(34, y, sculkGlow); img.setRGB(35, y, sculkBright); img.setRGB(36, y, sculkTeal);
            img.setRGB(59, y, sculkTeal); img.setRGB(60, y, sculkBright); img.setRGB(61, y, sculkGlow);
        }
        // Tendril branches
        img.setRGB(33, 2, sculkGlow); img.setRGB(32, 1, sculkBright);
        img.setRGB(62, 2, sculkGlow); img.setRGB(63, 1, sculkBright);

        // --- 3. TORSO: Complex Warden Ribcage & Pulsing Soul Core (Front: x: 20..27, y: 20..31) ---
        // Background Sculk cavity inside chest
        for (int y = 21; y <= 29; y++) {
            for (int x = 21; x <= 26; x++) {
                img.setRGB(x, y, sculkDark);
            }
        }
        // Soul Core in center (x: 23..24, y: 24..26)
        img.setRGB(23, 24, sculkBright); img.setRGB(24, 24, soulWhite);
        img.setRGB(23, 25, soulWhite); img.setRGB(24, 25, sculkBright);
        img.setRGB(22, 25, sculkGlow); img.setRGB(25, 25, sculkGlow);
        img.setRGB(23, 26, sculkGlow); img.setRGB(24, 26, sculkGlow);

        // 4 Arches of Bone Ribs wrapping over chest
        int[] ribY = {22, 24, 26, 28};
        for (int ry : ribY) {
            img.setRGB(20, ry, ribShadow); img.setRGB(21, ry, ribIvory);
            img.setRGB(26, ry, ribIvory); img.setRGB(27, ry, ribShadow);
        }
        // Sternum connector
        img.setRGB(23, 22, ribIvory); img.setRGB(24, 22, ribIvory);
        img.setRGB(23, 28, ribIvory); img.setRGB(24, 28, ribIvory);

        // Torso back (x: 32..39, y: 20..31): Spine
        for (int y = 20; y <= 31; y++) {
            img.setRGB(35, y, (y % 2 == 0) ? ribIvory : ribShadow);
            img.setRGB(36, y, (y % 2 == 0) ? ribIvory : ribShadow);
        }

        // --- 4. ARMS: Sculk Veins and Claws ---
        // Right Arm (x: 40..55, y: 20..31), Left Arm (x: 32..47, y: 52..63)
        for (int y = 22; y <= 29; y += 2) {
            img.setRGB(42, y, sculkGlow); img.setRGB(43, y, sculkTeal);
            img.setRGB(34, y + 32, sculkTeal); img.setRGB(35, y + 32, sculkGlow);
        }
        // Glowing Claws on hands
        for (int x = 41; x <= 46; x += 2) {
            img.setRGB(x, 31, sculkBright);
            img.setRGB(x - 8, 63, sculkBright);
        }

        // --- 5. LEGS: Deepslate Greaves with Bio-luminescent cracks ---
        for (int y = 22; y <= 30; y += 3) {
            img.setRGB(4, y, sculkTeal); img.setRGB(5, y, sculkGlow);
            img.setRGB(20, y + 32, sculkGlow); img.setRGB(21, y + 32, sculkTeal);
        }

        return img;
    }

    private static BufferedImage generateNetherBehemoth(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int charred = 0x24110E;
        int magmaRed = 0xFFD61111;
        int magmaOrange = 0xFFFF7700;
        int magmaYellow = 0xFFFFEE00;
        int gold = 0xFFFFD700;

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(charred, (lum - 0.5f) * 1.4f);
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // Head: Burning yellow eyes & tusks
        img.setRGB(9, 11, magmaOrange); img.setRGB(10, 11, magmaYellow);
        img.setRGB(13, 11, magmaYellow); img.setRGB(14, 11, magmaOrange);
        img.setRGB(8, 14, gold); img.setRGB(8, 13, 0xFFFFFFFF);
        img.setRGB(15, 14, gold); img.setRGB(15, 13, 0xFFFFFFFF);

        // Magma Veins across Torso
        img.setRGB(21, 23, magmaRed); img.setRGB(22, 23, magmaOrange);
        img.setRGB(23, 24, magmaYellow); img.setRGB(24, 24, magmaYellow);
        img.setRGB(25, 25, magmaOrange); img.setRGB(26, 25, magmaRed);
        img.setRGB(22, 26, magmaRed); img.setRGB(23, 27, magmaOrange);
        img.setRGB(24, 27, magmaYellow); img.setRGB(25, 28, magmaRed);

        // Netherite Belt
        for (int x = 20; x <= 27; x++) {
            img.setRGB(x, 30, gold);
            img.setRGB(x, 31, 0xFF3D322B);
        }

        return img;
    }

    private static BufferedImage generateVoidColossus(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int voidBase = 0x120822; // Obsidian deep purple
        int nebulaDark = 0xFF3D0C66;
        int nebulaPurple = 0xFF8A2BE2;
        int starMagenta = 0xFFFF2BD6;
        int starCyan = 0xFF00F0FF;
        int starWhite = 0xFFFFFFFF;

        // Base 64x64 biped shading
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(voidBase, (lum - 0.5f) * 1.5f);
                if ((x * 5 + y * 9) % 13 == 0) {
                    c = adjustBrightness(nebulaDark, (lum - 0.5f) * 1.2f);
                }
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // --- 1. HEAD (Face at x: 8..15, y: 8..15) ---
        // Piercing star eyes
        img.setRGB(9, 11, starMagenta); img.setRGB(10, 11, starWhite);
        img.setRGB(13, 11, starWhite); img.setRGB(14, 11, starMagenta);
        img.setRGB(10, 12, starCyan); img.setRGB(13, 12, starCyan);

        // Astral Crown spikes on Head Overlay (x: 32..63, y: 0..15)
        for (int x = 36; x <= 58; x += 3) {
            img.setRGB(x, 1, starWhite);
            img.setRGB(x, 2, starMagenta);
            img.setRGB(x, 3, nebulaPurple);
        }

        // --- 2. TORSO: Swirling Black Hole / Event Horizon Singularity (x: 20..27, y: 20..31) ---
        for (int y = 22; y <= 28; y++) {
            for (int x = 21; x <= 26; x++) {
                double dist = Math.hypot(x - 23.5, y - 25.0);
                if (dist <= 1.0) {
                    img.setRGB(x, y, 0xFF000000); // Absolute Singularity Void
                } else if (dist <= 1.8) {
                    img.setRGB(x, y, starMagenta); // Accretion Flare
                } else if (dist <= 2.6) {
                    img.setRGB(x, y, nebulaPurple);
                } else {
                    img.setRGB(x, y, nebulaDark);
                }
            }
        }
        img.setRGB(23, 24, starWhite); // Singularity twinkle

        // Astral Runic Ribs
        img.setRGB(20, 23, nebulaPurple); img.setRGB(27, 23, nebulaPurple);
        img.setRGB(20, 26, starCyan); img.setRGB(27, 26, starCyan);
        img.setRGB(22, 30, starMagenta); img.setRGB(25, 30, starMagenta);

        // --- 3. ARMS & LEGS: Starlight Constellations ---
        // Arms
        for (int y = 22; y <= 30; y += 2) {
            img.setRGB(42, y, (y % 4 == 0) ? starMagenta : starCyan);
            img.setRGB(34, y + 32, (y % 4 == 0) ? starCyan : starMagenta);
        }
        // Legs
        for (int y = 20; y <= 30; y += 3) {
            img.setRGB(4, y, nebulaPurple); img.setRGB(5, y, starMagenta);
            img.setRGB(20, y + 32, starMagenta); img.setRGB(21, y + 32, nebulaPurple);
        }

        return img;
    }

    private static BufferedImage generateCosmicSovereign(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int marbleBase = 0xE8E6F5; // Divine Platinum Marble
        int goldHighlight = 0xFFFFF1A8;
        int gold = 0xFFFFD700;
        int goldDark = 0xFF9E7B00;
        int genesisCyan = 0xFF00FFD5;
        int genesisWhite = 0xFFFFFFFF;
        int royalPurple = 0xFF6B11A8;

        // Base 64x64 pristine marble biped
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(marbleBase, (lum - 0.5f) * 1.3f);
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // --- 1. HEAD (Face at x: 8..15, y: 8..15) ---
        // Divine Radiant Eyes (Genesis Cyan & White)
        img.setRGB(9, 11, genesisCyan); img.setRGB(10, 11, genesisWhite);
        img.setRGB(13, 11, genesisWhite); img.setRGB(14, 11, genesisCyan);
        // Seraphic Visor / Forehead Gem
        img.setRGB(11, 8, gold); img.setRGB(12, 8, gold);
        img.setRGB(11, 9, genesisCyan); img.setRGB(12, 9, genesisWhite);

        // Radiant Solar Crown on Head Overlay (x: 32..63, y: 0..15)
        for (int x = 36; x <= 58; x += 2) {
            img.setRGB(x, 1, goldHighlight);
            img.setRGB(x, 2, gold);
            img.setRGB(x, 3, goldDark);
        }

        // --- 2. TORSO: Genesis Diamond Star Core & Golden Filigree (x: 20..27, y: 20..31) ---
        // Royal purple sash
        for (int i = 0; i <= 6; i++) {
            img.setRGB(20 + i, 21 + i, royalPurple);
        }

        // Genesis Diamond Star Core (x: 22..25, y: 23..26)
        img.setRGB(23, 23, goldHighlight); img.setRGB(24, 23, goldHighlight);
        img.setRGB(22, 24, goldHighlight); img.setRGB(25, 24, goldHighlight);
        img.setRGB(23, 24, genesisWhite); img.setRGB(24, 24, genesisCyan);
        img.setRGB(23, 25, genesisCyan); img.setRGB(24, 25, genesisWhite);
        img.setRGB(22, 25, gold); img.setRGB(25, 25, gold);
        img.setRGB(23, 26, gold); img.setRGB(24, 26, gold);

        // Golden filigree armor border
        for (int x = 20; x <= 27; x++) {
            img.setRGB(x, 20, gold);
            img.setRGB(x, 31, gold);
        }
        for (int y = 20; y <= 31; y++) {
            img.setRGB(20, y, gold);
            img.setRGB(27, y, gold);
        }

        // --- 3. ARMS: Golden Gauntlets & Celestial Rings ---
        for (int y = 28; y <= 31; y++) {
            for (int x = 41; x <= 46; x++) {
                img.setRGB(x, y, (y == 28) ? goldHighlight : gold);
            }
            for (int x = 33; x <= 38; x++) {
                img.setRGB(x, y + 32, (y == 28) ? goldHighlight : gold);
            }
        }

        // --- 4. LEGS: Royal Gilded Greaves ---
        for (int y = 28; y <= 31; y++) {
            for (int x = 1; x <= 6; x++) {
                img.setRGB(x, y, gold);
            }
            for (int x = 17; x <= 22; x++) {
                img.setRGB(x, y + 32, gold);
            }
        }

        return img;
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
