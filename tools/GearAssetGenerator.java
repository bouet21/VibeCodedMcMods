package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GearAssetGenerator {

    public static class GearDef {
        public final int tier;
        public final String enName;
        public final String esName;
        public final int colorRgb;

        public GearDef(int tier, String enName, String esName, int colorRgb) {
            this.tier = tier;
            this.enName = enName;
            this.esName = esName;
            this.colorRgb = colorRgb;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Gear Asset Generation & UTF-8 Spanish Lang Fix...");
        Path resources = Paths.get("src/main/resources");
        Path assets = resources.resolve("assets/chainores");
        Path data = resources.resolve("data/chainores");

        Path equipmentDir = assets.resolve("equipment");
        Path humanoidDir = assets.resolve("textures/entity/equipment/humanoid");
        Path humanoidLeggingsDir = assets.resolve("textures/entity/equipment/humanoid_leggings");

        Files.createDirectories(assets.resolve("textures/item"));
        Files.createDirectories(assets.resolve("models/item"));
        Files.createDirectories(assets.resolve("items"));
        Files.createDirectories(equipmentDir);
        Files.createDirectories(humanoidDir);
        Files.createDirectories(humanoidLeggingsDir);
        Files.createDirectories(data.resolve("recipe"));

        // Load Authentic Minecraft UV & Shading Templates
        BufferedImage swordTmpl = ImageIO.read(new File("tools/templates/diamond_sword.png"));
        BufferedImage pickTmpl = ImageIO.read(new File("tools/templates/diamond_pickaxe.png"));
        BufferedImage helmTmpl = ImageIO.read(new File("tools/templates/diamond_helmet.png"));
        BufferedImage chestTmpl = ImageIO.read(new File("tools/templates/diamond_chestplate.png"));
        BufferedImage legsTmpl = ImageIO.read(new File("tools/templates/diamond_leggings.png"));
        BufferedImage bootsTmpl = ImageIO.read(new File("tools/templates/diamond_boots.png"));
        BufferedImage armor1Tmpl = ImageIO.read(new File("tools/templates/armor_layer_1.png"));
        BufferedImage armor2Tmpl = ImageIO.read(new File("tools/templates/armor_layer_2.png"));

        List<GearDef> gears = List.of(
            new GearDef(1, "Copperite", "Cobreprita", 0xD97843),
            new GearDef(10, "Invar", "Invar", 0x7E8582),
            new GearDef(20, "Absolute Zero", "Cero Absoluto", 0x00E1D9),
            new GearDef(30, "Solarite", "Solarita", 0xFFCC00),
            new GearDef(40, "Chlorophyte", "Clorofita", 0x22E84D),
            new GearDef(50, "Runite", "Runita", 0x24C7BA),
            new GearDef(60, "Coreium", "Mineral del Núcleo", 0xFF5522),
            new GearDef(70, "Primordial Sculk", "Sculk Primordial", 0x00FFD5),
            new GearDef(80, "Infernal Core", "Núcleo Infernal", 0xFF6B1A),
            new GearDef(90, "Soul Gem", "Gema del Purgatorio", 0x16A694),
            new GearDef(100, "Nether Heart", "Corazón del Nether", 0xD61142),
            new GearDef(115, "Dragon Breath", "Aliento de Dragón", 0xDE3CE3),
            new GearDef(125, "Galacticite", "Galactita", 0x483296),
            new GearDef(135, "Infinity Shard", "Fragmento Infinito", 0xEB5CE8),
            new GearDef(140, "Genesis", "Génesis", 0xE8F768),
            new GearDef(150, "Godmetal", "Metal de los Dioses", 0xFFE885)
        );

        StringBuilder enGearLang = new StringBuilder();
        StringBuilder esGearLang = new StringBuilder();

        for (int i = 0; i < gears.size(); i++) {
            GearDef g = gears.get(i);
            String swordId = "sword_tier_" + g.tier;
            String pickaxeId = "pickaxe_tier_" + g.tier;
            String helmetId = "helmet_tier_" + g.tier;
            String chestId = "chestplate_tier_" + g.tier;
            String legsId = "leggings_tier_" + g.tier;
            String bootsId = "boots_tier_" + g.tier;

            // 1. High-Quality Beautiful Item Textures with Shading and Specular Highlights
            ImageIO.write(renderThemedItem(swordTmpl, g.colorRgb, true, g.tier), "png", assets.resolve("textures/item/" + swordId + ".png").toFile());
            ImageIO.write(renderThemedItem(pickTmpl, g.colorRgb, true, g.tier), "png", assets.resolve("textures/item/" + pickaxeId + ".png").toFile());
            ImageIO.write(renderThemedItem(helmTmpl, g.colorRgb, false, g.tier), "png", assets.resolve("textures/item/" + helmetId + ".png").toFile());
            ImageIO.write(renderThemedItem(chestTmpl, g.colorRgb, false, g.tier), "png", assets.resolve("textures/item/" + chestId + ".png").toFile());
            ImageIO.write(renderThemedItem(legsTmpl, g.colorRgb, false, g.tier), "png", assets.resolve("textures/item/" + legsId + ".png").toFile());
            ImageIO.write(renderThemedItem(bootsTmpl, g.colorRgb, false, g.tier), "png", assets.resolve("textures/item/" + bootsId + ".png").toFile());

            // 2. 3rd-Person Worn Armor Textures & 1.21.2+ Equipment Asset JSON
            writeEquipmentAsset(equipmentDir, g.tier);
            ImageIO.write(renderThemedArmorLayer(armor1Tmpl, g.colorRgb, g.tier), "png", humanoidDir.resolve("armor_tier_" + g.tier + ".png").toFile());
            ImageIO.write(renderThemedArmorLayer(armor2Tmpl, g.colorRgb, g.tier), "png", humanoidLeggingsDir.resolve("armor_tier_" + g.tier + ".png").toFile());

            // 2. Models & 1.21.2+ Items definitions
            String[] itemIds = {swordId, pickaxeId, helmetId, chestId, legsId, bootsId};
            for (String itemId : itemIds) {
                String parent = (itemId.startsWith("sword_") || itemId.startsWith("pickaxe_")) ? "minecraft:item/handheld" : "minecraft:item/generated";
                Files.writeString(assets.resolve("models/item/" + itemId + ".json"),
                    "{\"parent\": \"" + parent + "\", \"textures\": {\"layer0\": \"chainores:item/" + itemId + "\"}}",
                    StandardCharsets.UTF_8);

                Files.writeString(assets.resolve("items/" + itemId + ".json"),
                    "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"chainores:item/" + itemId + "\"}}",
                    StandardCharsets.UTF_8);
            }

            // 3. Recipes (Smithing Upgrades from previous gear milestone)
            String prevSword = (i == 0) ? "minecraft:iron_sword" : "chainores:sword_tier_" + gears.get(i - 1).tier;
            String prevPick = (i == 0) ? "minecraft:iron_pickaxe" : "chainores:pickaxe_tier_" + gears.get(i - 1).tier;
            String prevHelm = (i == 0) ? "minecraft:iron_helmet" : "chainores:helmet_tier_" + gears.get(i - 1).tier;
            String prevChest = (i == 0) ? "minecraft:iron_chestplate" : "chainores:chestplate_tier_" + gears.get(i - 1).tier;
            String prevLegs = (i == 0) ? "minecraft:iron_leggings" : "chainores:leggings_tier_" + gears.get(i - 1).tier;
            String prevBoots = (i == 0) ? "minecraft:iron_boots" : "chainores:boots_tier_" + gears.get(i - 1).tier;

            String currentIngot = "chainores:ingot_tier_" + g.tier;

            writeSmithing(data, "smithing_" + swordId, prevSword, currentIngot, "chainores:" + swordId);
            writeSmithing(data, "smithing_" + pickaxeId, prevPick, currentIngot, "chainores:" + pickaxeId);
            writeSmithing(data, "smithing_" + helmetId, prevHelm, currentIngot, "chainores:" + helmetId);
            writeSmithing(data, "smithing_" + chestId, prevChest, currentIngot, "chainores:" + chestId);
            writeSmithing(data, "smithing_" + legsId, prevLegs, currentIngot, "chainores:" + legsId);
            writeSmithing(data, "smithing_" + bootsId, prevBoots, currentIngot, "chainores:" + bootsId);

            // 4. Direct Crafting Table Recipes for ALL 16 Gear Tiers
            // Sword: 2 ingots + 1 stick
            Files.writeString(data.resolve("recipe/craft_" + swordId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"I\",\"I\",\"S\"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"},\"S\":{\"item\":\"minecraft:stick\"}},\"result\":{\"id\":\"chainores:" + swordId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);
            // Pickaxe: 3 ingots + 2 sticks
            Files.writeString(data.resolve("recipe/craft_" + pickaxeId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"III\",\" S \",\" S \"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"},\"S\":{\"item\":\"minecraft:stick\"}},\"result\":{\"id\":\"chainores:" + pickaxeId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);
            // Helmet: 5 ingots
            Files.writeString(data.resolve("recipe/craft_" + helmetId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"III\",\"I I\"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"}},\"result\":{\"id\":\"chainores:" + helmetId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);
            // Chestplate: 8 ingots
            Files.writeString(data.resolve("recipe/craft_" + chestId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"I I\",\"III\",\"III\"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"}},\"result\":{\"id\":\"chainores:" + chestId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);
            // Leggings: 7 ingots
            Files.writeString(data.resolve("recipe/craft_" + legsId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"III\",\"I I\",\"I I\"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"}},\"result\":{\"id\":\"chainores:" + legsId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);
            // Boots: 4 ingots
            Files.writeString(data.resolve("recipe/craft_" + bootsId + ".json"),
                "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"equipment\",\"pattern\":[\"I I\",\"I I\"],\"key\":{\"I\":{\"item\":\"" + currentIngot + "\"}},\"result\":{\"id\":\"chainores:" + bootsId + "\",\"count\":1}}",
                StandardCharsets.UTF_8);

            // 5. Crafting Table Upgrades (Shapeless in regular Crafting Table: Previous piece + 1 current ingot -> Upgraded piece)
            writeCraftUpgrade(data, "craft_upgrade_" + swordId, prevSword, currentIngot, "chainores:" + swordId);
            writeCraftUpgrade(data, "craft_upgrade_" + pickaxeId, prevPick, currentIngot, "chainores:" + pickaxeId);
            writeCraftUpgrade(data, "craft_upgrade_" + helmetId, prevHelm, currentIngot, "chainores:" + helmetId);
            writeCraftUpgrade(data, "craft_upgrade_" + chestId, prevChest, currentIngot, "chainores:" + chestId);
            writeCraftUpgrade(data, "craft_upgrade_" + legsId, prevLegs, currentIngot, "chainores:" + legsId);
            writeCraftUpgrade(data, "craft_upgrade_" + bootsId, prevBoots, currentIngot, "chainores:" + bootsId);

            // Lang strings
            enGearLang.append("  \"item.chainores.").append(swordId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Sword\",\n");
            enGearLang.append("  \"item.chainores.").append(pickaxeId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Pickaxe\",\n");
            enGearLang.append("  \"item.chainores.").append(helmetId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Helmet\",\n");
            enGearLang.append("  \"item.chainores.").append(chestId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Chestplate\",\n");
            enGearLang.append("  \"item.chainores.").append(legsId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Leggings\",\n");
            enGearLang.append("  \"item.chainores.").append(bootsId).append("\": \"T").append(g.tier).append(" ").append(g.enName).append(" Boots\",\n");

            esGearLang.append("  \"item.chainores.").append(swordId).append("\": \"T").append(g.tier).append(" Espada de ").append(g.esName).append("\",\n");
            esGearLang.append("  \"item.chainores.").append(pickaxeId).append("\": \"T").append(g.tier).append(" Pico de ").append(g.esName).append("\",\n");
            esGearLang.append("  \"item.chainores.").append(helmetId).append("\": \"T").append(g.tier).append(" Casco de ").append(g.esName).append("\",\n");
            esGearLang.append("  \"item.chainores.").append(chestId).append("\": \"T").append(g.tier).append(" Pechera de ").append(g.esName).append("\",\n");
            esGearLang.append("  \"item.chainores.").append(legsId).append("\": \"T").append(g.tier).append(" Pantalones de ").append(g.esName).append("\",\n");
            esGearLang.append("  \"item.chainores.").append(bootsId).append("\": \"T").append(g.tier).append(" Botas de ").append(g.esName).append("\",\n");
        }

        // Fix Spanish Accents and add Gear into es_es.json in UTF-8
        String esContent = Files.readString(assets.resolve("lang/es_es.json"), StandardCharsets.UTF_8);
        // Replace known corrupted glyphs
        esContent = esContent.replace("Nǧcleo", "Núcleo")
                             .replace("Titǭn", "Titán")
                             .replace("?pex", "Ápex")
                             .replace("?pex", "Ápex")
                             .replace("GǸnesis", "Génesis")
                             .replace("G\u01f8nesis", "Génesis")
                             .replace("Estaǫo", "Estaño")
                             .replace("Esta\u01eb", "Estaño")
                             .replace("CiǸnaga", "Ciénaga")
                             .replace("Ci\u01f8naga", "Ciénaga")
                             .replace("FǸngica", "Fúngica")
                             .replace("F\u01f8ngica", "Fúngica")
                             .replace("Cuǭntica", "Cuántica")
                             .replace("Cu\u01fdntica", "Cuántica")
                             .replace("Tǭrtaro", "Tártaro")
                             .replace("T\u01fdrtaro", "Tártaro")
                             .replace("Corazǫn", "Corazón")
                             .replace("Coraz\u01ebn", "Corazón")
                             .replace("ǭmbar", "Ámbar")
                             .replace("\u01fdmbar", "Ámbar");

        // Insert gear strings before the closing bracket if not already added
        if (!esContent.contains("item.chainores.sword_tier_1")) {
            int lastBrace = esContent.lastIndexOf('}');
            if (lastBrace != -1) {
                esContent = esContent.substring(0, lastBrace).trim();
                if (!esContent.endsWith(",")) esContent += ",";
                esContent += "\n" + esGearLang.toString().trim();
                if (esContent.endsWith(",")) esContent = esContent.substring(0, esContent.length() - 1);
                esContent += "\n}\n";
            }
            Files.writeString(assets.resolve("lang/es_es.json"), esContent, StandardCharsets.UTF_8);
        }

        // Update en_us.json as well if not already added
        String enContent = Files.readString(assets.resolve("lang/en_us.json"), StandardCharsets.UTF_8);
        if (!enContent.contains("item.chainores.sword_tier_1")) {
            int enLastBrace = enContent.lastIndexOf('}');
            if (enLastBrace != -1) {
                enContent = enContent.substring(0, enLastBrace).trim();
                if (!enContent.endsWith(",")) enContent += ",";
                enContent += "\n" + enGearLang.toString().trim();
                if (enContent.endsWith(",")) enContent = enContent.substring(0, enContent.length() - 1);
                enContent += "\n}\n";
            }
            Files.writeString(assets.resolve("lang/en_us.json"), enContent, StandardCharsets.UTF_8);
        }

        // 6. Early-game recipe for Chain Smithing Template and Duplication
        Files.writeString(data.resolve("recipe/chain_smithing_template.json"),
            "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"misc\",\"pattern\":[\" C \",\"CSC\",\" C \"],\"key\":{\"C\":{\"item\":\"minecraft:copper_ingot\"},\"S\":{\"item\":\"minecraft:cobblestone\"}},\"result\":{\"id\":\"chainores:chain_smithing_template\",\"count\":1}}",
            StandardCharsets.UTF_8);
        Files.writeString(data.resolve("recipe/chain_smithing_template_duplicate.json"),
            "{\"type\":\"minecraft:crafting_shaped\",\"category\":\"misc\",\"pattern\":[\"#T#\",\"#C#\",\"###\"],\"key\":{\"#\":{\"item\":\"minecraft:copper_ingot\"},\"T\":{\"item\":\"chainores:chain_smithing_template\"},\"C\":{\"item\":\"minecraft:cobblestone\"}},\"result\":{\"id\":\"chainores:chain_smithing_template\",\"count\":2}}",
            StandardCharsets.UTF_8);

        // 7. All 150 Tiers: 1-to-1 Shapeless Ingot Crafting and Smelting/Blasting for Silk Touch Ore Blocks
        for (int t = 1; t <= 150; t++) {
            String prevIngot = (t == 1) ? "minecraft:copper_ingot" : "chainores:ingot_tier_" + (t - 1);
            String rawItem = "chainores:raw_tier_" + t;
            String ingotItem = "chainores:ingot_tier_" + t;
            String oreBlock = "chainores:ore_tier_" + t;

            // 1-to-1 shapeless crafting (Works in 2x2 player inventory or 3x3 crafting table)
            Files.writeString(data.resolve("recipe/simple_crafting_tier_" + t + ".json"),
                "{\"type\":\"minecraft:crafting_shapeless\",\"category\":\"misc\",\"ingredients\":[{\"item\":\"" + prevIngot + "\"},{\"item\":\"" + rawItem + "\"}],\"result\":{\"id\":\"" + ingotItem + "\",\"count\":1}}",
                StandardCharsets.UTF_8);

            // Smelting ore block in Furnace -> raw ore
            Files.writeString(data.resolve("recipe/smelt_ore_tier_" + t + ".json"),
                "{\"type\":\"minecraft:smelting\",\"category\":\"misc\",\"cookingtime\":200,\"experience\":0.5,\"ingredient\":{\"item\":\"" + oreBlock + "\"},\"result\":{\"id\":\"" + rawItem + "\"}}",
                StandardCharsets.UTF_8);

            // Blasting ore block in Blast Furnace -> raw ore
            Files.writeString(data.resolve("recipe/blast_ore_tier_" + t + ".json"),
                "{\"type\":\"minecraft:blasting\",\"category\":\"misc\",\"cookingtime\":100,\"experience\":0.5,\"ingredient\":{\"item\":\"" + oreBlock + "\"},\"result\":{\"id\":\"" + rawItem + "\"}}",
                StandardCharsets.UTF_8);
        }

        System.out.println("Gear generation, UTF-8 Spanish translation fixes, and complete recipe generation completed successfully!");
    }

    private static void writeCraftUpgrade(Path data, String name, String base, String ingot, String result) throws IOException {
        String json = "{\n" +
                      "  \"type\": \"minecraft:crafting_shapeless\",\n" +
                      "  \"category\": \"equipment\",\n" +
                      "  \"ingredients\": [\n" +
                      "    {\"item\": \"" + base + "\"},\n" +
                      "    {\"item\": \"" + ingot + "\"}\n" +
                      "  ],\n" +
                      "  \"result\": {\"id\": \"" + result + "\", \"count\": 1}\n" +
                      "}";
        Files.writeString(data.resolve("recipe/" + name + ".json"), json, StandardCharsets.UTF_8);
    }

    private static void writeSmithing(Path data, String name, String base, String addition, String result) throws IOException {
        String json = "{\n" +
                      "  \"type\": \"minecraft:smithing_transform\",\n" +
                      "  \"template\": {\"item\": \"chainores:chain_smithing_template\"},\n" +
                      "  \"base\": {\"item\": \"" + base + "\"},\n" +
                      "  \"addition\": {\"item\": \"" + addition + "\"},\n" +
                      "  \"result\": {\"id\": \"" + result + "\", \"count\": 1}\n" +
                      "}";
        Files.writeString(data.resolve("recipe/" + name + ".json"), json, StandardCharsets.UTF_8);
    }

    private static void writeEquipmentAsset(Path equipmentDir, int tier) throws IOException {
        String json = "{\n" +
                      "  \"layers\": {\n" +
                      "    \"humanoid\": [\n" +
                      "      {\n" +
                      "        \"texture\": \"chainores:armor_tier_" + tier + "\"\n" +
                      "      }\n" +
                      "    ],\n" +
                      "    \"humanoid_leggings\": [\n" +
                      "      {\n" +
                      "        \"texture\": \"chainores:armor_tier_" + tier + "\"\n" +
                      "      }\n" +
                      "    ]\n" +
                      "  }\n" +
                      "}";
        Files.writeString(equipmentDir.resolve("armor_tier_" + tier + ".json"), json, StandardCharsets.UTF_8);
    }

    private static BufferedImage renderThemedItem(BufferedImage template, int rgb, boolean isWeaponOrTool, int tier) {
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

                // Handle stick / grip for swords and pickaxes
                if (isWeaponOrTool && r > 60 && g > 40 && b < 60 && (r - b) > 25) {
                    if (tier >= 100) {
                        // High tier legendary weapons: dark obsidian grip with ornate golden pommel
                        if (x <= 3 && y >= 12) {
                            out.setRGB(x, y, 0xFF000000 | 0xFFD700);
                        } else {
                            out.setRGB(x, y, 0xFF000000 | 0x221B26);
                        }
                    } else {
                        out.setRGB(x, y, argb);
                    }
                    continue;
                }

                // Metal / gem pixel: calculate luminosity curve from template
                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int color;
                if (lum < 0.24f) {
                    color = adjustBrightness(rgb, -0.65f); // Crisp dark outline
                } else if (lum < 0.44f) {
                    color = adjustBrightness(rgb, -0.35f); // Deep bevel shadow
                } else if (lum < 0.70f) {
                    color = rgb;                           // Vibrant rich body
                } else if (lum < 0.86f) {
                    color = adjustBrightness(rgb, +0.38f); // Smooth light reflection
                } else {
                    // Brilliant specular glint
                    if (tier >= 140) {
                        color = 0xFFFFFF;
                    } else if (tier >= 90) {
                        color = adjustBrightness(rgb, +0.82f);
                    } else {
                        color = adjustBrightness(rgb, +0.60f);
                    }
                }

                // Unique thematic nuances for milestone materials
                if (tier == 20) {
                    // Absolute Zero: Glacial crystalline sheen
                    if (lum >= 0.80f) color = 0xEAFFFF;
                } else if (tier == 70) {
                    // Primordial Sculk: Dark midnight body with bright warden-soul cyan veins
                    if (lum < 0.50f) color = adjustBrightness(0x0A1822, (lum - 0.35f));
                    else if (lum >= 0.75f) color = 0x00FFD5;
                } else if (tier == 80) {
                    // Infernal Core: Volcanic magma crevice glow
                    if (lum < 0.30f) color = 0x470800;
                    else if (lum >= 0.85f) color = 0xFFEE55;
                } else if (tier == 100) {
                    // Nether Heart: Deep ruby shimmer
                    if (lum < 0.30f) color = 0x4A0012;
                    else if (lum >= 0.85f) color = 0xFFAEC4;
                } else if (tier == 125) {
                    // Galacticite: Cosmic deep violet with stardust sparkles
                    if (lum >= 0.85f) color = 0xE6DAFF;
                } else if (tier == 150) {
                    // Godmetal: Radiant Godly Gold with diamond white sparkles
                    if (lum >= 0.80f) color = 0xFFFFFF;
                }

                out.setRGB(x, y, (a << 24) | (color & 0x00FFFFFF));
            }
        }
        return out;
    }

    private static BufferedImage renderThemedArmorLayer(BufferedImage template, int rgb, int tier) {
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

                float lum = (r * 0.299f + g * 0.587f + b * 0.114f) / 255.0f;

                int color;
                if (lum < 0.26f) {
                    color = adjustBrightness(rgb, -0.65f); // Dark outer seam
                } else if (lum < 0.46f) {
                    color = adjustBrightness(rgb, -0.35f); // Armor plate shadow
                } else if (lum < 0.70f) {
                    color = rgb;                           // Base metallic armor plate
                } else if (lum < 0.86f) {
                    color = adjustBrightness(rgb, +0.38f); // Armor specular reflection
                } else {
                    color = (tier >= 125) ? 0xFFFFFF : adjustBrightness(rgb, +0.70f);
                }

                if (tier == 70) {
                    if (lum < 0.50f) color = adjustBrightness(0x0A1822, (lum - 0.35f));
                    else if (lum >= 0.75f) color = 0x00FFD5;
                } else if (tier == 80) {
                    if (lum < 0.35f) color = 0x470800;
                    else if (lum >= 0.85f) color = 0xFFEE55;
                } else if (tier == 150) {
                    if (lum >= 0.80f) color = 0xFFFFFF;
                }

                out.setRGB(x, y, (a << 24) | (color & 0x00FFFFFF));
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
