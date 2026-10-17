package com.chainores.compat.emi;

import com.chainores.ChainOresMod;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModGems;
import com.chainores.registry.ModItems;
import com.chainores.registry.ModWorkstations;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class ChainOresEmiPlugin implements EmiPlugin {
    public static final Identifier FORGE_SHEET = Identifier.ofVanilla("textures/gui/container/furnace.png");
    public static final EmiRecipeCategory CHAIN_FORGE_CATEGORY = new EmiRecipeCategory(
            ChainOresMod.id("chain_forge"),
            EmiStack.of(ModWorkstations.CHAIN_FORGE_ITEM),
            new EmiTexture(FORGE_SHEET, 79, 35, 24, 17));

    public static final EmiRecipeCategory COSMIC_SMELTER_CATEGORY = new EmiRecipeCategory(
            ChainOresMod.id("cosmic_smelter"),
            EmiStack.of(ModWorkstations.COSMIC_SMELTER_ITEM),
            new EmiTexture(FORGE_SHEET, 79, 35, 24, 17));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CHAIN_FORGE_CATEGORY);
        registry.addCategory(COSMIC_SMELTER_CATEGORY);
        registry.addWorkstation(CHAIN_FORGE_CATEGORY, EmiStack.of(ModWorkstations.CHAIN_FORGE_ITEM));
        registry.addWorkstation(COSMIC_SMELTER_CATEGORY, EmiStack.of(ModWorkstations.COSMIC_SMELTER_ITEM));

        // 1. 150-Tier Ingot Upgrade Ladder
        for (int i = 0; i < OreRegistryData.TIERS.size() - 1; i++) {
            OreTier current = OreRegistryData.TIERS.get(i);
            OreTier next = OreRegistryData.TIERS.get(i + 1);

            EmiIngredient input = EmiStack.of(current.ingotItem);
            EmiIngredient catalyst;
            if (i == 69) {
                catalyst = EmiStack.of(ModBossItems.ABYSSAL_CATALYST);
            } else if (i == 104) {
                catalyst = EmiStack.of(ModBossItems.NETHER_CORE);
            } else if (i == 134) {
                catalyst = EmiStack.of(ModBossItems.VOID_SHARD);
            } else if (i == 148) {
                catalyst = EmiStack.of(ModBossItems.COSMIC_SINGULARITY);
            } else {
                catalyst = EmiIngredient.of(List.of(
                        EmiStack.of(next.rawItem),
                        EmiStack.of(ModItems.CHAIN_SMITHING_TEMPLATE),
                        EmiStack.of(current.ingotItem)));
            }
            EmiStack output = EmiStack.of(next.ingotItem);

            Identifier id = ChainOresMod.id("/chain_forge/tier_" + current.tier + "_to_" + next.tier);
            registry.addRecipe(new ChainForgeEmiRecipe(id, input, catalyst, output));
        }

        // 2. Raw Ore to Ingot for each tier
        for (OreTier ore : OreRegistryData.TIERS) {
            EmiIngredient input = EmiStack.of(ore.rawItem);
            EmiIngredient catalyst = EmiIngredient.of(List.of(
                    EmiStack.of(ore.rawItem),
                    EmiStack.of(ModItems.CHAIN_SMITHING_TEMPLATE)));
            EmiStack output = EmiStack.of(ore.ingotItem);
            Identifier id = ChainOresMod.id("/chain_forge/smelt_raw_tier_" + ore.tier);
            registry.addRecipe(new ChainForgeEmiRecipe(id, input, catalyst, output));
        }

        // 3. Equipment Creation from Vanilla Gear
        for (int i = 0; i < ModGear.GEAR_SETS.size(); i++) {
            ModGear.GearSet set = ModGear.GEAR_SETS.get(i);
            OreTier ore = OreRegistryData.TIERS.get(set.tierData.tier() - 1);
            EmiIngredient catalyst = EmiStack.of(ore.ingotItem);

            addGearRecipe(registry, set.tierData.tier(), "sword", EmiStack.of(Items.DIAMOND_SWORD), catalyst,
                    EmiStack.of(set.sword));
            addGearRecipe(registry, set.tierData.tier(), "pickaxe", EmiStack.of(Items.DIAMOND_PICKAXE), catalyst,
                    EmiStack.of(set.pickaxe));
            addGearRecipe(registry, set.tierData.tier(), "axe", EmiStack.of(Items.DIAMOND_AXE), catalyst,
                    EmiStack.of(set.axe));
            addGearRecipe(registry, set.tierData.tier(), "shovel", EmiStack.of(Items.DIAMOND_SHOVEL), catalyst,
                    EmiStack.of(set.shovel));
            addGearRecipe(registry, set.tierData.tier(), "hoe", EmiStack.of(Items.DIAMOND_HOE), catalyst,
                    EmiStack.of(set.hoe));
            addGearRecipe(registry, set.tierData.tier(), "helmet", EmiStack.of(Items.DIAMOND_HELMET), catalyst,
                    EmiStack.of(set.helmet));
            addGearRecipe(registry, set.tierData.tier(), "chestplate", EmiStack.of(Items.DIAMOND_CHESTPLATE), catalyst,
                    EmiStack.of(set.chestplate));
            addGearRecipe(registry, set.tierData.tier(), "leggings", EmiStack.of(Items.DIAMOND_LEGGINGS), catalyst,
                    EmiStack.of(set.leggings));
            addGearRecipe(registry, set.tierData.tier(), "boots", EmiStack.of(Items.DIAMOND_BOOTS), catalyst,
                    EmiStack.of(set.boots));
        }

        // 4. Upgrades Between Mod Gear Sets
        for (int i = 0; i < ModGear.GEAR_SETS.size() - 1; i++) {
            ModGear.GearSet curr = ModGear.GEAR_SETS.get(i);
            ModGear.GearSet next = ModGear.GEAR_SETS.get(i + 1);
            OreTier nextOre = OreRegistryData.TIERS.get(next.tierData.tier() - 1);

            EmiIngredient catalyst;
            if (next.tierData.tier() > 70 && curr.tierData.tier() <= 70) {
                catalyst = EmiStack.of(ModBossItems.ABYSSAL_CATALYST);
            } else if (next.tierData.tier() > 105 && curr.tierData.tier() <= 105) {
                catalyst = EmiStack.of(ModBossItems.NETHER_CORE);
            } else if (next.tierData.tier() > 135 && curr.tierData.tier() <= 135) {
                catalyst = EmiStack.of(ModBossItems.VOID_SHARD);
            } else if (next.tierData.tier() == 150) {
                catalyst = EmiStack.of(ModBossItems.COSMIC_SINGULARITY);
            } else {
                catalyst = EmiIngredient
                        .of(List.of(EmiStack.of(nextOre.ingotItem), EmiStack.of(ModItems.CHAIN_SMITHING_TEMPLATE)));
            }

            addGearRecipe(registry, next.tierData.tier(), "up_sword", EmiStack.of(curr.sword), catalyst,
                    EmiStack.of(next.sword));
            addGearRecipe(registry, next.tierData.tier(), "up_pickaxe", EmiStack.of(curr.pickaxe), catalyst,
                    EmiStack.of(next.pickaxe));
            addGearRecipe(registry, next.tierData.tier(), "up_axe", EmiStack.of(curr.axe), catalyst,
                    EmiStack.of(next.axe));
            addGearRecipe(registry, next.tierData.tier(), "up_shovel", EmiStack.of(curr.shovel), catalyst,
                    EmiStack.of(next.shovel));
            addGearRecipe(registry, next.tierData.tier(), "up_hoe", EmiStack.of(curr.hoe), catalyst,
                    EmiStack.of(next.hoe));
            addGearRecipe(registry, next.tierData.tier(), "up_helmet", EmiStack.of(curr.helmet), catalyst,
                    EmiStack.of(next.helmet));
            addGearRecipe(registry, next.tierData.tier(), "up_chestplate", EmiStack.of(curr.chestplate), catalyst,
                    EmiStack.of(next.chestplate));
            addGearRecipe(registry, next.tierData.tier(), "up_leggings", EmiStack.of(curr.leggings), catalyst,
                    EmiStack.of(next.leggings));
            addGearRecipe(registry, next.tierData.tier(), "up_boots", EmiStack.of(curr.boots), catalyst,
                    EmiStack.of(next.boots));
        }

        // 5. Legendary Weapons & Shields
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/dragon_bow"), EmiStack.of(Items.BOW),
                EmiStack.of(OreRegistryData.TIERS.get(114).ingotItem), EmiStack.of(ModGear.DRAGON_BOW)));
        registry.addRecipe(
                new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/galactic_crossbow"), EmiStack.of(Items.CROSSBOW),
                        EmiStack.of(OreRegistryData.TIERS.get(124).ingotItem), EmiStack.of(ModGear.GALACTIC_CROSSBOW)));
        registry.addRecipe(
                new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/sentinel_shield"), EmiStack.of(Items.SHIELD),
                        EmiStack.of(OreRegistryData.TIERS.get(9).ingotItem), EmiStack.of(ModGear.SENTINEL_SHIELD)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/frostguard_shield"),
                EmiStack.of(ModGear.SENTINEL_SHIELD), EmiStack.of(OreRegistryData.TIERS.get(19).ingotItem),
                EmiStack.of(ModGear.FROSTGUARD_SHIELD)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/abyssal_aegis"),
                EmiStack.of(ModGear.FROSTGUARD_SHIELD), EmiStack.of(ModBossItems.ABYSSAL_CATALYST),
                EmiStack.of(ModGear.ABYSSAL_AEGIS)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/infernal_bulwark"),
                EmiStack.of(ModGear.ABYSSAL_AEGIS), EmiStack.of(ModBossItems.NETHER_CORE),
                EmiStack.of(ModGear.INFERNAL_BULWARK)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/aegis_shield"),
                EmiStack.of(ModGear.INFERNAL_BULWARK), EmiStack.of(OreRegistryData.TIERS.get(99).ingotItem),
                EmiStack.of(ModGear.AEGIS_SHIELD)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/cosmic_shield"),
                EmiStack.of(ModGear.AEGIS_SHIELD), EmiStack.of(ModBossItems.COSMIC_SINGULARITY),
                EmiStack.of(ModGear.COSMIC_SHIELD)));

        ModGear.GearSet godSet = ModGear.GEAR_SETS.get(ModGear.GEAR_SETS.size() - 1);
        registry.addRecipe(
                new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/god_omnitool"), EmiStack.of(godSet.pickaxe),
                        EmiStack.of(ModBossItems.COSMIC_SINGULARITY), EmiStack.of(ModGear.GOD_OMNITOOL)));

        // 6. Trinkets
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/chain_ring"),
                EmiStack.of(Items.GOLD_INGOT), EmiStack.of(Items.AMETHYST_SHARD), EmiStack.of(ModItems.CHAIN_RING)));
        registry.addRecipe(new ChainForgeEmiRecipe(ChainOresMod.id("/chain_forge/chain_amulet"),
                EmiStack.of(ModItems.CHAIN_RING), EmiStack.of(Items.DIAMOND), EmiStack.of(ModItems.CHAIN_AMULET)));

        // 7. Gem Fusions
        addGemFusion(registry, "blood_1_to_2", ModGems.BLOOD_GEM, ModGems.BLOOD_GEM_2);
        addGemFusion(registry, "blood_2_to_3", ModGems.BLOOD_GEM_2, ModGems.BLOOD_GEM_3);
        addGemFusion(registry, "void_1_to_2", ModGems.VOID_GEM, ModGems.VOID_GEM_2);
        addGemFusion(registry, "void_2_to_3", ModGems.VOID_GEM_2, ModGems.VOID_GEM_3);
        addGemFusion(registry, "core_1_to_2", ModGems.CORE_GEM, ModGems.CORE_GEM_2);
        addGemFusion(registry, "core_2_to_3", ModGems.CORE_GEM_2, ModGems.CORE_GEM_3);
        addGemFusion(registry, "genesis_1_to_2", ModGems.GENESIS_GEM, ModGems.GENESIS_GEM_2);
        addGemFusion(registry, "genesis_2_to_3", ModGems.GENESIS_GEM_2, ModGems.GENESIS_GEM_3);

        // 8. Cosmic Smelter Duplication (x4)
        for (int i = 0; i < OreRegistryData.TIERS.size(); i++) {
            OreTier ore = OreRegistryData.TIERS.get(i);
            Identifier id = ChainOresMod.id("/cosmic_smelter/tier_" + ore.tier);
            registry.addRecipe(new CosmicSmelterEmiRecipe(
                    id,
                    EmiStack.of(ore.rawItem),
                    EmiStack.of(ore.ingotItem, 4)));
        }
    }

    private void addGearRecipe(EmiRegistry registry, int tier, String type, EmiIngredient input, EmiIngredient catalyst,
            EmiStack output) {
        Identifier id = ChainOresMod.id("/chain_forge/gear_t" + tier + "_" + type);
        registry.addRecipe(new ChainForgeEmiRecipe(id, input, catalyst, output));
    }

    private void addGemFusion(EmiRegistry registry, String name, Item inGem, Item outGem) {
        Identifier id = ChainOresMod.id("/chain_anvil/fuse_" + name);
        registry.addRecipe(new ChainForgeEmiRecipe(id, EmiStack.of(inGem), EmiStack.of(inGem), EmiStack.of(outGem)));
    }

    // =========================================================================
    // EMI Recipe Implementation for Chain Forge
    // =========================================================================
    public static class ChainForgeEmiRecipe implements EmiRecipe {
        private final Identifier id;
        private final EmiIngredient input;
        private final EmiIngredient catalyst;
        private final EmiStack output;

        public ChainForgeEmiRecipe(Identifier id, EmiIngredient input, EmiIngredient catalyst, EmiStack output) {
            this.id = id;
            this.input = input;
            this.catalyst = catalyst;
            this.output = output;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CHAIN_FORGE_CATEGORY;
        }

        @Override
        public @Nullable Identifier getId() {
            return id;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return List.of(input, catalyst);
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(output);
        }

        @Override
        public int getDisplayWidth() {
            return 118;
        }

        @Override
        public int getDisplayHeight() {
            return 36;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(input, 4, 9);
            widgets.addSlot(catalyst, 28, 9);
            widgets.addTexture(new EmiTexture(FORGE_SHEET, 79, 35, 24, 17), 54, 9);
            widgets.addSlot(output, 88, 9).recipeContext(this);
        }
    }

    // =========================================================================
    // EMI Recipe Implementation for Cosmic Smelter (x4)
    // =========================================================================
    public static class CosmicSmelterEmiRecipe implements EmiRecipe {
        private final Identifier id;
        private final EmiIngredient input;
        private final EmiStack output;

        public CosmicSmelterEmiRecipe(Identifier id, EmiIngredient input, EmiStack output) {
            this.id = id;
            this.input = input;
            this.output = output;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return COSMIC_SMELTER_CATEGORY;
        }

        @Override
        public @Nullable Identifier getId() {
            return id;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return List.of(input);
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(output);
        }

        @Override
        public int getDisplayWidth() {
            return 100;
        }

        @Override
        public int getDisplayHeight() {
            return 36;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(input, 6, 9);
            widgets.addTexture(new EmiTexture(FORGE_SHEET, 79, 35, 24, 17), 36, 9);
            widgets.addSlot(output, 70, 9).recipeContext(this);
        }
    }
}
