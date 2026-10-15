package tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import javax.imageio.ImageIO;

public class ShieldTextureGenerator {

    public static void main(String[] args) throws Exception {
        System.out.println("Generating 5 unique legendary shield textures and 3D models based on Aegis Shield...");

        File templateFile = new File("src/main/resources/assets/chainores/textures/item/aegis_shield.png");
        BufferedImage template = ImageIO.read(templateFile);

        File texDir = new File("src/main/resources/assets/chainores/textures/item");
        File modelItemDir = new File("src/main/resources/assets/chainores/models/item");
        File itemDefDir = new File("src/main/resources/assets/chainores/items");
        texDir.mkdirs();
        modelItemDir.mkdirs();
        itemDefDir.mkdirs();

        String[] shieldIds = {
            "sentinel_shield",
            "frostguard_shield",
            "infernal_bulwark",
            "abyssal_aegis",
            "cosmic_shield"
        };

        // Palettes: {Border[4], Plate[4], Emblem[4]}
        int[][][] palettes = {
            // 1. Sentinel Shield (Bronze / Dark Steel / Electric Amber)
            {
                {0xFFA552, 0xD47833, 0x8C4417, 0x472007},
                {0x4D5663, 0x363D47, 0x22272E, 0x14171C},
                {0xFFFFFF, 0xFFE042, 0xFF9E1B, 0xC46600}
            },
            // 2. Frostguard Shield (Frosted Platinum / Deep Glacier / Laser Cyan)
            {
                {0xFFFFFF, 0xD4E8F5, 0x88A8C2, 0x46657D},
                {0x224869, 0x142F47, 0x0C1F30, 0x050E17},
                {0xFFFFFF, 0x80FFFF, 0x00E1D9, 0x008A85}
            },
            // 3. Infernal Bulwark (Basalt & Gilded Blackstone / Charred Nether / Molten Magma)
            {
                {0xFFCC44, 0x6E5844, 0x423326, 0x241912},
                {0x381B16, 0x26120E, 0x170A08, 0x0A0403},
                {0xFFFFAA, 0xFF7711, 0xD62200, 0x800B00}
            },
            // 4. Abyssal Aegis (Deepslate Obsidian / Deep Dark / Warden Sculk Soul)
            {
                {0x69697A, 0x434352, 0x2A2A33, 0x15151A},
                {0x151D24, 0x0C131A, 0x060B0F, 0x020405},
                {0xE0FFFF, 0x00FFD5, 0x00A89B, 0x004742}
            },
            // 5. Cosmic Sovereign Shield (Solar Gold / Interstellar Violet / Supernova Star)
            {
                {0xFFFFFF, 0xFFD700, 0xC49800, 0x6E5500},
                {0x3D1B5E, 0x2A1042, 0x190829, 0x0A0212},
                {0xFFFFFF, 0xFFAAF5, 0xDE3CE3, 0x8C1291}
            }
        };

        String baseModel = Files.readString(new File(modelItemDir, "aegis_shield.json").toPath());
        String baseBlockingModel = Files.readString(new File(modelItemDir, "aegis_shield_blocking.json").toPath());

        for (int i = 0; i < shieldIds.length; i++) {
            String id = shieldIds[i];
            int[][] pal = palettes[i];

            // 1. Texture
            BufferedImage img = createShieldVariant(template, pal[0], pal[1], pal[2]);
            ImageIO.write(img, "PNG", new File(texDir, id + ".png"));

            // 2. 3D Model Standard
            String modelJson = baseModel.replace("chainores:item/aegis_shield", "chainores:item/" + id);
            Files.writeString(new File(modelItemDir, id + ".json").toPath(), modelJson);

            // 3. 3D Model Blocking
            String blockingJson = baseBlockingModel.replace("chainores:item/aegis_shield", "chainores:item/" + id);
            Files.writeString(new File(modelItemDir, id + "_blocking.json").toPath(), blockingJson);

            // 4. 1.21.2 Item Model Definition
            String itemDef = "{\n" +
                    "  \"model\": {\n" +
                    "    \"type\": \"minecraft:condition\",\n" +
                    "    \"property\": \"minecraft:using_item\",\n" +
                    "    \"on_true\": {\n" +
                    "      \"type\": \"minecraft:model\",\n" +
                    "      \"model\": \"chainores:item/" + id + "_blocking\"\n" +
                    "    },\n" +
                    "    \"on_false\": {\n" +
                    "      \"type\": \"minecraft:model\",\n" +
                    "      \"model\": \"chainores:item/" + id + "\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n";
            Files.writeString(new File(itemDefDir, id + ".json").toPath(), itemDef);
        }

        System.out.println("Generated textures, 3D standard models, 3D blocking models, and item definitions for all 5 new shields!");
    }

    private static BufferedImage createShieldVariant(BufferedImage template, int[] borderGrad, int[] plateGrad, int[] emblemGrad) {
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

                if ((g > 140 && b > 140 && r < 120) || (r > 200 && g > 200 && b > 200)) {
                    float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                    int color = mapGradient(emblemGrad, lum);
                    out.setRGB(x, y, (a << 24) | color);
                } else if (r < 75 && g < 75 && b < 100) {
                    float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 100.0f;
                    int color = mapGradient(plateGrad, lum);
                    out.setRGB(x, y, (a << 24) | color);
                } else {
                    float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;
                    int color = mapGradient(borderGrad, lum);
                    out.setRGB(x, y, (a << 24) | color);
                }
            }
        }
        return out;
    }

    private static int mapGradient(int[] palette, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        if (t >= 0.75f) return palette[0];
        else if (t >= 0.45f) return palette[1];
        else if (t >= 0.20f) return palette[2];
        else return palette[3];
    }
}
