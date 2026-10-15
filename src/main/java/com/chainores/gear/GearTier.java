package com.chainores.gear;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import java.util.Map;

public record GearTier(
        int tier,
        String id,
        String enName,
        String esName,
        int colorRgb,
        int durability,
        float miningSpeed,
        float swordDamage,
        int helmetDefense,
        int chestDefense,
        int legsDefense,
        int bootsDefense,
        float toughness,
        float knockbackResistance,
        String perkDescription
) {
    public ToolMaterial createToolMaterial() {
        TagKey<Block> incorrectTag = (tier >= 50) ? BlockTags.INCORRECT_FOR_NETHERITE_TOOL
                : (tier >= 20) ? BlockTags.INCORRECT_FOR_DIAMOND_TOOL
                : (tier >= 10) ? BlockTags.INCORRECT_FOR_IRON_TOOL
                : BlockTags.INCORRECT_FOR_STONE_TOOL;

        TagKey<Item> repairTag = ItemTags.SWORDS;
        return new ToolMaterial(incorrectTag, durability, miningSpeed, swordDamage, 20 + tier / 10, repairTag);
    }

    public ArmorMaterial createArmorMaterial() {
        Map<EquipmentType, Integer> defenseMap = Map.of(
                EquipmentType.HELMET, helmetDefense,
                EquipmentType.CHESTPLATE, chestDefense,
                EquipmentType.LEGGINGS, legsDefense,
                EquipmentType.BOOTS, bootsDefense
        );

        RegistryEntry<SoundEvent> sound = (tier >= 70) ? SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE
                : (tier >= 20) ? SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND
                : SoundEvents.ITEM_ARMOR_EQUIP_IRON;

        var assetKey = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, com.chainores.ChainOresMod.id("armor_tier_" + tier));

        return new ArmorMaterial(durability, defenseMap, 20 + tier / 10, sound, toughness, knockbackResistance,
                ItemTags.HEAD_ARMOR, assetKey);
    }
}
