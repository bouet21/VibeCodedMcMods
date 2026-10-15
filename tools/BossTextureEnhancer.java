package tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BossTextureEnhancer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting High-Fidelity Boss Texture Enhancement...");

        File tmplDir = new File("tools/templates");
        File entityDir = new File("src/main/resources/assets/chainores/textures/entity");
        entityDir.mkdirs();

        BufferedImage zombieTmpl = ImageIO.read(new File(tmplDir, "zombie.png"));
        BufferedImage piglinTmpl = ImageIO.read(new File(tmplDir, "piglin_brute.png"));
        BufferedImage witherTmpl = ImageIO.read(new File(tmplDir, "wither_skeleton.png"));

        // 1. Abyssal Guardian (Sculk Knight / Warden-infused Titan)
        BufferedImage abyssal = generateAbyssalGuardian(zombieTmpl);
        ImageIO.write(abyssal, "PNG", new File(entityDir, "abyssal_guardian.png"));
        System.out.println("Generated Abyssal Guardian (Sculk/Warden) texture!");

        // 2. Nether Behemoth (Magma / Netherite Warlord)
        BufferedImage nether = generateNetherBehemoth(piglinTmpl);
        ImageIO.write(nether, "PNG", new File(entityDir, "nether_behemoth.png"));
        System.out.println("Generated Nether Behemoth (Magma/Piglin Brute) texture!");

        // 3. Void Colossus (Astral Void Lich)
        BufferedImage voidCol = generateVoidColossus(witherTmpl);
        ImageIO.write(voidCol, "PNG", new File(entityDir, "void_colossus.png"));
        System.out.println("Generated Void Colossus (Astral Void) texture!");

        // 4. Cosmic Sovereign (Celestial Genesis Deity)
        BufferedImage cosmic = generateCosmicSovereign(witherTmpl);
        ImageIO.write(cosmic, "PNG", new File(entityDir, "cosmic_sovereign.png"));
        System.out.println("Generated Cosmic Sovereign (Genesis Deity) texture!");

        System.out.println("All boss textures upgraded to legendary visual quality!");
    }

    private static BufferedImage generateAbyssalGuardian(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int sculkBase = 0x08151D;
        int sculkGlow = 0xFF00FFD5;
        int sculkCore = 0xFF55FFFF;
        int boneWhite = 0xFFD8D2C5;
        int deepDark = 0xFF030A0E;

        // Recolor base model shading to Deepslate / Sculk
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(sculkBase, (lum - 0.5f) * 1.5f);
                img.setRGB(x, y, 0xFF000000 | c);
            }
        }

        // 1. Head Details (Face at x: 8..15, y: 8..15)
        // Glowing cyan blind eyes
        img.setRGB(9, 12, sculkGlow); img.setRGB(10, 12, sculkCore);
        img.setRGB(13, 12, sculkCore); img.setRGB(14, 12, sculkGlow);
        // Terrifying jagged mouth
        for (int x = 9; x <= 14; x++) {
            img.setRGB(x, 14, deepDark);
            if (x % 2 == 0) img.setRGB(x, 13, boneWhite);
        }

        // 2. Warden Tendrils / Horns on Head Overlay (x: 32..63, y: 0..15)
        for (int y = 0; y < 8; y++) {
            img.setRGB(34, y, sculkGlow); img.setRGB(35, y, sculkBase);
            img.setRGB(60, y, sculkBase); img.setRGB(61, y, sculkGlow);
        }

        // 3. Torso Ribcage & Soul Heart (Front: x: 20..27, y: 20..31)
        // Pulsating Sculk Heart
        for (int y = 23; y <= 27; y++) {
            for (int x = 22; x <= 25; x++) {
                img.setRGB(x, y, sculkGlow);
            }
        }
        img.setRGB(23, 24, sculkCore); img.setRGB(24, 24, sculkCore);
        img.setRGB(23, 25, sculkCore); img.setRGB(24, 25, sculkCore);
        img.setRGB(23, 25, 0xFFFFFFFF); // Heart shine

        // Bone Ribs across chest
        img.setRGB(20, 22, boneWhite); img.setRGB(21, 22, boneWhite); img.setRGB(26, 22, boneWhite); img.setRGB(27, 22, boneWhite);
        img.setRGB(20, 25, boneWhite); img.setRGB(21, 25, boneWhite); img.setRGB(26, 25, boneWhite); img.setRGB(27, 25, boneWhite);
        img.setRGB(21, 28, boneWhite); img.setRGB(26, 28, boneWhite);

        // 4. Claws and Gauntlets on Arms (Right arm: 40..47, Left arm: 32..39)
        for (int y = 28; y <= 31; y++) {
            img.setRGB(42, y, sculkGlow); img.setRGB(45, y, sculkGlow);
            img.setRGB(34, y + 24, sculkGlow); img.setRGB(37, y + 24, sculkGlow);
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

        // 1. Head: Burning yellow eyes & tusks
        // Eyes at 9,11 and 14,11
        img.setRGB(9, 11, magmaOrange); img.setRGB(10, 11, magmaYellow);
        img.setRGB(13, 11, magmaYellow); img.setRGB(14, 11, magmaOrange);
        // Molten golden tusks
        img.setRGB(8, 14, gold); img.setRGB(8, 13, 0xFFFFFFFF);
        img.setRGB(15, 14, gold); img.setRGB(15, 13, 0xFFFFFFFF);

        // 2. Chest: Netherite Battle Armor & Magma Core
        // Armor plates
        for (int y = 20; y <= 31; y++) {
            img.setRGB(20, y, 0xFF18181C); img.setRGB(27, y, 0xFF18181C);
        }
        for (int x = 20; x <= 27; x++) {
            img.setRGB(x, 20, 0xFF282830);
            img.setRGB(x, 31, gold); // Golden belt
        }
        // Molten core
        for (int y = 22; y <= 27; y++) {
            for (int x = 22; x <= 25; x++) {
                double d = Math.hypot(x - 23.5, y - 24.5);
                if (d <= 1.0) img.setRGB(x, y, magmaYellow);
                else if (d <= 1.8) img.setRGB(x, y, magmaOrange);
                else img.setRGB(x, y, magmaRed);
            }
        }

        // 3. Magma Cracks on Arms & Legs
        for (int i = 0; i < 4; i++) {
            img.setRGB(42 + i, 24 + i, magmaOrange);
            img.setRGB(5 + i, 22 + i, magmaRed);
            img.setRGB(22 + i, 52 + i, magmaYellow);
        }

        return img;
    }

    private static BufferedImage generateVoidColossus(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int voidBone = 0x18102B;
        int nebulaDark = 0xFF4B0082;
        int nebulaPurple = 0xFF8A2BE2;
        int starMagenta = 0xFFFF33FF;
        int starWhite = 0xFFFFFFFF;

        // Base wither skeleton mapped to 64x64
        for (int y = 0; y < base.getHeight(); y++) {
            for (int x = 0; x < base.getWidth(); x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(voidBone, (lum - 0.5f) * 1.6f);
                img.setRGB(x, y, 0xFF000000 | c);
                // Also mirror to 64x64 bottom half for limbs
                if (y >= 16) {
                    img.setRGB(x, y + 16, 0xFF000000 | c);
                }
            }
        }

        // 1. Head: Astral Crown & Magenta Eye Flares
        // Skull eyes at (9,11)-(10,12) and (13,11)-(14,12)
        img.setRGB(9, 11, starMagenta); img.setRGB(10, 11, starWhite);
        img.setRGB(13, 11, starWhite); img.setRGB(14, 11, starMagenta);
        img.setRGB(10, 12, starMagenta); img.setRGB(13, 12, starMagenta);

        // Astral Crown spikes on top
        for (int x = 8; x <= 15; x++) {
            if (x % 2 == 1) {
                img.setRGB(x, 7, nebulaPurple);
                img.setRGB(x, 6, starMagenta);
            }
        }

        // 2. Torso: Swirling Black Hole / Void Singularity in Ribcage
        for (int y = 22; y <= 28; y++) {
            for (int x = 21; x <= 26; x++) {
                double dist = Math.hypot(x - 23.5, y - 25.0);
                if (dist <= 1.0) img.setRGB(x, y, 0xFF000000); // Event horizon
                else if (dist <= 2.0) img.setRGB(x, y, starMagenta); // Accretion disk
                else if (dist <= 2.8) img.setRGB(x, y, nebulaPurple);
                else img.setRGB(x, y, nebulaDark);
            }
        }
        img.setRGB(23, 24, starWhite); // Singularity twinkle

        // 3. Spine and floating astral runes
        img.setRGB(23, 29, nebulaPurple); img.setRGB(24, 29, nebulaPurple);
        img.setRGB(23, 30, starMagenta); img.setRGB(24, 30, starMagenta);

        return img;
    }

    private static BufferedImage generateCosmicSovereign(BufferedImage base) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int marble = 0xEBE8F5;
        int gold = 0xFFFFD700;
        int goldLight = 0xFFFFF2A8;
        int goldDark = 0xFF8A6B00;
        int genesisCyan = 0xFF00FFD5;
        int genesisWhite = 0xFFFFFFFF;

        // Base divine platinum/marble biped
        for (int y = 0; y < base.getHeight(); y++) {
            for (int x = 0; x < base.getWidth(); x++) {
                int argb = base.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int c = adjustBrightness(marble, (lum - 0.5f) * 1.3f);
                img.setRGB(x, y, 0xFF000000 | c);
                if (y >= 16) {
                    img.setRGB(x, y + 16, 0xFF000000 | c);
                }
            }
        }

        // 1. Head: Royal Golden Halo Crown & Third Eye of Genesis
        for (int x = 7; x <= 16; x++) {
            img.setRGB(x, 6, gold);
            img.setRGB(x, 7, goldLight);
        }
        img.setRGB(11, 4, genesisCyan); img.setRGB(12, 4, genesisWhite); // Spire top
        img.setRGB(11, 5, gold); img.setRGB(12, 5, gold);

        // Third Eye of Genesis (center forehead 11,8 and 12,8)
        img.setRGB(11, 8, genesisCyan); img.setRGB(12, 8, genesisWhite);
        // Divine glowing golden blindfold/eyes
        for (int x = 9; x <= 14; x++) {
            img.setRGB(x, 11, gold);
            img.setRGB(x, 12, goldLight);
        }
        img.setRGB(10, 11, genesisWhite); img.setRGB(13, 11, genesisWhite);

        // 2. Torso: Celestial Cuirass & Sun Core of Genesis
        for (int x = 20; x <= 27; x++) {
            img.setRGB(x, 20, gold);
            img.setRGB(x, 21, goldLight);
            img.setRGB(x, 30, goldDark);
            img.setRGB(x, 31, gold);
        }
        for (int y = 20; y <= 31; y++) {
            img.setRGB(20, y, gold);
            img.setRGB(27, y, gold);
        }

        // Blazing Genesis Star in chest
        for (int y = 23; y <= 28; y++) {
            for (int x = 22; x <= 25; x++) {
                double dist = Math.hypot(x - 23.5, y - 25.5);
                if (dist <= 0.8) img.setRGB(x, y, genesisWhite);
                else if (dist <= 1.6) img.setRGB(x, y, genesisCyan);
                else img.setRGB(x, y, goldLight);
            }
        }

        // 3. Golden Bracers & Wing Epaulets on Shoulders
        for (int x = 40; x <= 47; x++) {
            img.setRGB(x, 20, gold);
            img.setRGB(x, 28, goldLight);
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
