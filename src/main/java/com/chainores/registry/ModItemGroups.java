package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> CHAIN_ORES_GROUP_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), ChainOresMod.id("main"));

    public static void initialize() {
        ItemGroup group = FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModItems.GUIDE_BOOK))
                .displayName(Text.translatable("itemGroup.chainores.main"))
                .entries((context, entries) -> {
                    // First entries: Guide Book & Smithing Template & Prospector Rod & Legendary Weapons
                    entries.add(ModItems.GUIDE_BOOK);
                    entries.add(ModItems.CHAIN_SMITHING_TEMPLATE);
                    entries.add(ModItems.PROSPECTOR_ROD);
                    entries.add(ModGear.GOD_OMNITOOL);
                    entries.add(ModGear.DRAGON_BOW);
                    entries.add(ModGear.GALACTIC_CROSSBOW);
                    entries.add(ModGear.AEGIS_SHIELD);
                    entries.add(ModWorkstations.CHAIN_FORGE_ITEM);
                    entries.add(ModWorkstations.CHAIN_ANVIL_ITEM);
                    entries.add(ModWorkstations.COSMIC_SMELTER_ITEM);

                    // Boss Summoners and Milestone Transition Catalysts
                    entries.add(ModBossItems.ABYSSAL_EYE);
                    entries.add(ModBossItems.ABYSSAL_CATALYST);
                    entries.add(ModBossItems.FLAME_HEART);
                    entries.add(ModBossItems.NETHER_CORE);
                    entries.add(ModBossItems.VOID_BEACON);
                    entries.add(ModBossItems.VOID_SHARD);
                    entries.add(ModBossItems.COSMIC_ALTAR_CORE);
                    entries.add(ModBossItems.COSMIC_SINGULARITY);
                    entries.add(ModBossItems.COSMIC_TROPHY);

                    // Weapons, Tools, and Armors (Grouped by milestone sets)
                    for (ModGear.GearSet gear : ModGear.GEAR_SETS) {
                        entries.add(gear.sword);
                        entries.add(gear.pickaxe);
                        entries.add(gear.axe);
                        entries.add(gear.shovel);
                        entries.add(gear.hoe);
                        entries.add(gear.helmet);
                        entries.add(gear.chestplate);
                        entries.add(gear.leggings);
                        entries.add(gear.boots);
                    }

                    // All 150 Ore tiers sequentially: Ore -> Raw -> Ingot -> Storage Block
                    for (OreTier ore : OreRegistryData.TIERS) {
                        entries.add(ore.oreBlockItem);
                        entries.add(ore.rawItem);
                        entries.add(ore.ingotItem);
                        entries.add(ore.storageBlockItem);
                    }
                })
                .build();

        Registry.register(Registries.ITEM_GROUP, CHAIN_ORES_GROUP_KEY, group);
        ChainOresMod.LOGGER.info("Registered Chain Ores Creative Item Group with full progressive arsenal.");
    }
}
