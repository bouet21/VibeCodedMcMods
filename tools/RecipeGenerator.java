package tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RecipeGenerator {

    private static final int[] GEAR_TIERS = {
        1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 115, 125, 135, 140, 150
    };

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Complete Recipe Generation for Minecraft 1.21.11...");

        Path recipeDir = Paths.get("src/main/resources/data/chainores/recipe");
        Path recipesDir = Paths.get("src/main/resources/data/chainores/recipes");

        Files.createDirectories(recipeDir);
        Files.createDirectories(recipesDir);

        // 1. Chain Smithing Template & Duplication Recipes
        writeShapedRecipe(recipeDir, recipesDir, "chain_smithing_template", "misc",
                List.of(" C ", "CSC", " C "),
                List.of("C", "minecraft:copper_ingot", "S", "minecraft:cobblestone"),
                "chainores:chain_smithing_template", 1);

        writeShapedRecipe(recipeDir, recipesDir, "chain_smithing_template_duplicate", "misc",
                List.of("#T#", "#C#", "###"),
                List.of("#", "minecraft:copper_ingot", "T", "chainores:chain_smithing_template", "C", "minecraft:cobblestone"),
                "chainores:chain_smithing_template", 2);

        // 2. Recipes for All 16 Gear Milestone Tiers
        for (int i = 0; i < GEAR_TIERS.length; i++) {
            int tier = GEAR_TIERS[i];
            String currentIngot = "chainores:ingot_tier_" + tier;

            String swordId = "sword_tier_" + tier;
            String pickaxeId = "pickaxe_tier_" + tier;
            String helmetId = "helmet_tier_" + tier;
            String chestId = "chestplate_tier_" + tier;
            String legsId = "leggings_tier_" + tier;
            String bootsId = "boots_tier_" + tier;

            // Direct Shaped Crafting (Crafting Table)
            // Sword: 2 ingots + 1 stick
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + swordId, "equipment",
                    List.of("I", "I", "S"),
                    List.of("I", currentIngot, "S", "minecraft:stick"),
                    "chainores:" + swordId, 1);

            // Pickaxe: 3 ingots + 2 sticks
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + pickaxeId, "equipment",
                    List.of("III", " S ", " S "),
                    List.of("I", currentIngot, "S", "minecraft:stick"),
                    "chainores:" + pickaxeId, 1);

            // Helmet: 5 ingots
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + helmetId, "equipment",
                    List.of("III", "I I"),
                    List.of("I", currentIngot),
                    "chainores:" + helmetId, 1);

            // Chestplate: 8 ingots
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + chestId, "equipment",
                    List.of("I I", "III", "III"),
                    List.of("I", currentIngot),
                    "chainores:" + chestId, 1);

            // Leggings: 7 ingots
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + legsId, "equipment",
                    List.of("III", "I I", "I I"),
                    List.of("I", currentIngot),
                    "chainores:" + legsId, 1);

            // Boots: 4 ingots
            writeShapedRecipe(recipeDir, recipesDir, "craft_" + bootsId, "equipment",
                    List.of("I I", "I I"),
                    List.of("I", currentIngot),
                    "chainores:" + bootsId, 1);

            // Previous tier pieces for upgrades
            String prevSword = (i == 0) ? "minecraft:iron_sword" : "chainores:sword_tier_" + GEAR_TIERS[i - 1];
            String prevPick = (i == 0) ? "minecraft:iron_pickaxe" : "chainores:pickaxe_tier_" + GEAR_TIERS[i - 1];
            String prevHelm = (i == 0) ? "minecraft:iron_helmet" : "chainores:helmet_tier_" + GEAR_TIERS[i - 1];
            String prevChest = (i == 0) ? "minecraft:iron_chestplate" : "chainores:chestplate_tier_" + GEAR_TIERS[i - 1];
            String prevLegs = (i == 0) ? "minecraft:iron_leggings" : "chainores:leggings_tier_" + GEAR_TIERS[i - 1];
            String prevBoots = (i == 0) ? "minecraft:iron_boots" : "chainores:boots_tier_" + GEAR_TIERS[i - 1];

            // Shapeless Crafting Table Upgrades (Previous Piece + Current Ingot -> Upgraded Piece)
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + swordId, "equipment",
                    List.of(prevSword, currentIngot), "chainores:" + swordId, 1);
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + pickaxeId, "equipment",
                    List.of(prevPick, currentIngot), "chainores:" + pickaxeId, 1);
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + helmetId, "equipment",
                    List.of(prevHelm, currentIngot), "chainores:" + helmetId, 1);
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + chestId, "equipment",
                    List.of(prevChest, currentIngot), "chainores:" + chestId, 1);
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + legsId, "equipment",
                    List.of(prevLegs, currentIngot), "chainores:" + legsId, 1);
            writeShapelessRecipe(recipeDir, recipesDir, "craft_upgrade_" + bootsId, "equipment",
                    List.of(prevBoots, currentIngot), "chainores:" + bootsId, 1);

            // Smithing Table Upgrades (Template + Previous Piece + Current Ingot -> Upgraded Piece)
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + swordId,
                    "chainores:chain_smithing_template", prevSword, currentIngot, "chainores:" + swordId);
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + pickaxeId,
                    "chainores:chain_smithing_template", prevPick, currentIngot, "chainores:" + pickaxeId);
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + helmetId,
                    "chainores:chain_smithing_template", prevHelm, currentIngot, "chainores:" + helmetId);
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + chestId,
                    "chainores:chain_smithing_template", prevChest, currentIngot, "chainores:" + chestId);
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + legsId,
                    "chainores:chain_smithing_template", prevLegs, currentIngot, "chainores:" + legsId);
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_" + bootsId,
                    "chainores:chain_smithing_template", prevBoots, currentIngot, "chainores:" + bootsId);
        }

        // 3. Recipes for All 150 Progressive Ores & Ingots
        for (int t = 1; t <= 150; t++) {
            String prevIngot = (t == 1) ? "minecraft:copper_ingot" : "chainores:ingot_tier_" + (t - 1);
            String rawItem = "chainores:raw_tier_" + t;
            String ingotItem = "chainores:ingot_tier_" + t;
            String oreBlock = "chainores:ore_tier_" + t;
            String storageBlock = "chainores:block_tier_" + t;

            // Ingot simple craft (prev ingot + raw ore in 2x2 grid)
            writeShapelessRecipe(recipeDir, recipesDir, "simple_crafting_tier_" + t, "misc",
                    List.of(prevIngot, rawItem), ingotItem, 1);

            // Ingot smithing table upgrade
            writeSmithingRecipe(recipeDir, recipesDir, "smithing_tier_" + t,
                    "chainores:chain_smithing_template", prevIngot, rawItem, ingotItem);

            // Ingot 4-raw crafting table alternative
            writeShapedRecipe(recipeDir, recipesDir, "crafting_tier_" + t, "misc",
                    List.of(" R ", "RPR", " R "),
                    List.of("P", prevIngot, "R", rawItem),
                    ingotItem, 1);

            // Storage Block (9 ingots <-> 1 block)
            writeShapedRecipe(recipeDir, recipesDir, "block_tier_" + t, "building",
                    List.of("###", "###", "###"),
                    List.of("#", ingotItem),
                    storageBlock, 1);

            // Ingot from Storage Block (1 block -> 9 ingots)
            writeShapelessRecipe(recipeDir, recipesDir, "ingot_tier_" + t + "_from_block", "misc",
                    List.of(storageBlock), ingotItem, 9);

            // Ore block smelting in Furnace -> raw ore
            writeCookingRecipe(recipeDir, recipesDir, "smelt_ore_tier_" + t, "minecraft:smelting",
                    oreBlock, rawItem, 200, 0.5f);

            // Ore block blasting in Blast Furnace -> raw ore
            writeCookingRecipe(recipeDir, recipesDir, "blast_ore_tier_" + t, "minecraft:blasting",
                    oreBlock, rawItem, 100, 0.5f);

            // Raw ore smelting in Furnace -> ingot
            writeCookingRecipe(recipeDir, recipesDir, "smelt_raw_tier_" + t, "minecraft:smelting",
                    rawItem, ingotItem, 200, 0.7f);

            // Raw ore blasting in Blast Furnace -> ingot
            writeCookingRecipe(recipeDir, recipesDir, "blast_raw_tier_" + t, "minecraft:blasting",
                    rawItem, ingotItem, 100, 0.7f);
        }

        System.out.println("All recipes regenerated in valid Minecraft 1.21.11 format in both recipe/ and recipes/!");
    }

    private static void writeShapedRecipe(Path dir1, Path dir2, String name, String category,
                                          List<String> pattern, List<String> keyPairs, String resultId, int count) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"minecraft:crafting_shaped\",\n");
        sb.append("  \"category\": \"").append(category).append("\",\n");
        sb.append("  \"pattern\": [\n");
        for (int i = 0; i < pattern.size(); i++) {
            sb.append("    \"").append(pattern.get(i)).append("\"");
            if (i < pattern.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"key\": {\n");
        for (int i = 0; i < keyPairs.size(); i += 2) {
            sb.append("    \"").append(keyPairs.get(i)).append("\": \"").append(keyPairs.get(i + 1)).append("\"");
            if (i + 2 < keyPairs.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  },\n");
        sb.append("  \"result\": {\n");
        sb.append("    \"id\": \"").append(resultId).append("\",\n");
        sb.append("    \"count\": ").append(count).append("\n");
        sb.append("  }\n");
        sb.append("}");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(dir1.resolve(name + ".json"), bytes);
        Files.write(dir2.resolve(name + ".json"), bytes);
    }

    private static void writeShapelessRecipe(Path dir1, Path dir2, String name, String category,
                                             List<String> ingredients, String resultId, int count) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"type\": \"minecraft:crafting_shapeless\",\n");
        sb.append("  \"category\": \"").append(category).append("\",\n");
        sb.append("  \"ingredients\": [\n");
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append("    \"").append(ingredients.get(i)).append("\"");
            if (i < ingredients.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"result\": {\n");
        sb.append("    \"id\": \"").append(resultId).append("\",\n");
        sb.append("    \"count\": ").append(count).append("\n");
        sb.append("  }\n");
        sb.append("}");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(dir1.resolve(name + ".json"), bytes);
        Files.write(dir2.resolve(name + ".json"), bytes);
    }

    private static void writeSmithingRecipe(Path dir1, Path dir2, String name,
                                            String template, String base, String addition, String resultId) throws IOException {
        String json = "{\n" +
                      "  \"type\": \"minecraft:smithing_transform\",\n" +
                      "  \"template\": \"" + template + "\",\n" +
                      "  \"base\": \"" + base + "\",\n" +
                      "  \"addition\": \"" + addition + "\",\n" +
                      "  \"result\": {\n" +
                      "    \"id\": \"" + resultId + "\"\n" +
                      "  }\n" +
                      "}";

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        Files.write(dir1.resolve(name + ".json"), bytes);
        Files.write(dir2.resolve(name + ".json"), bytes);
    }

    private static void writeCookingRecipe(Path dir1, Path dir2, String name, String type,
                                           String ingredient, String resultId, int cookingTime, float xp) throws IOException {
        String json = "{\n" +
                      "  \"type\": \"" + type + "\",\n" +
                      "  \"category\": \"misc\",\n" +
                      "  \"cookingtime\": " + cookingTime + ",\n" +
                      "  \"experience\": " + xp + ",\n" +
                      "  \"ingredient\": \"" + ingredient + "\",\n" +
                      "  \"result\": {\n" +
                      "    \"id\": \"" + resultId + "\"\n" +
                      "  }\n" +
                      "}";

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        Files.write(dir1.resolve(name + ".json"), bytes);
        Files.write(dir2.resolve(name + ".json"), bytes);
    }
}
