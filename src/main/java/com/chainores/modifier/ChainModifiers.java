package com.chainores.modifier;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import java.util.List;

public class ChainModifiers {
    public static final String KEY_GEM = "chainores_gem";
    public static final String KEY_REFORGE = "chainores_reforge";

    // Gem IDs
    public static final String GEM_BLOOD = "blood_gem";
    public static final String GEM_VOID = "void_gem";
    public static final String GEM_CORE = "core_gem";
    public static final String GEM_GENESIS = "genesis_gem";

    // Reforge Prefix IDs
    public static final String REFORGE_LEGENDARY = "legendary";
    public static final String REFORGE_FIERCE = "fierce";
    public static final String REFORGE_SWIFT = "swift";
    public static final String REFORGE_TITANIC = "titanic";
    public static final String REFORGE_DIVINE = "divine";

    public static String getGem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return null;
        NbtCompound nbt = custom.copyNbt();
        return nbt.contains(KEY_GEM) ? nbt.getString(KEY_GEM).orElse(null) : null;
    }

    public static void setGem(ItemStack stack, String gemId) {
        if (stack.isEmpty()) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putString(KEY_GEM, gemId);
        });
    }

    public static String getReforge(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return null;
        NbtCompound nbt = custom.copyNbt();
        return nbt.contains(KEY_REFORGE) ? nbt.getString(KEY_REFORGE).orElse(null) : null;
    }

    public static void setReforge(ItemStack stack, String reforgeId) {
        if (stack.isEmpty()) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putString(KEY_REFORGE, reforgeId);
        });
    }

    public static String rollRandomReforge(Random random) {
        int roll = random.nextInt(100);
        if (roll < 8) return REFORGE_DIVINE;      // 8% Divine
        if (roll < 22) return REFORGE_LEGENDARY;  // 14% Legendary
        if (roll < 47) return REFORGE_FIERCE;     // 25% Fierce
        if (roll < 73) return REFORGE_SWIFT;      // 26% Swift
        return REFORGE_TITANIC;                   // 27% Titanic
    }

    public static float getDamageMultiplier(ItemStack stack) {
        String reforge = getReforge(stack);
        if (reforge == null) return 1.0f;
        return switch (reforge) {
            case REFORGE_DIVINE -> 1.50f;
            case REFORGE_FIERCE -> 1.35f;
            case REFORGE_LEGENDARY -> 1.25f;
            case REFORGE_SWIFT -> 0.90f;
            case REFORGE_TITANIC -> 1.10f;
            default -> 1.0f;
        };
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        float mult = 1.0f;
        String gem = getGem(stack);
        if (GEM_CORE.equals(gem)) mult += 0.35f;
        if (GEM_GENESIS.equals(gem)) mult += 0.50f;

        String reforge = getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case REFORGE_DIVINE -> mult += 0.50f;
                case REFORGE_SWIFT -> mult += 0.40f;
                case REFORGE_LEGENDARY -> mult += 0.30f;
                case REFORGE_TITANIC -> mult -= 0.10f;
            }
        }
        return mult;
    }

    public static void appendModifierTooltips(ItemStack stack, List<Text> tooltip) {
        String reforge = getReforge(stack);
        if (reforge != null) {
            Text reforgeText = switch (reforge) {
                case REFORGE_DIVINE -> Text.translatable("reforge.chainores.divine").formatted(Formatting.AQUA, Formatting.BOLD);
                case REFORGE_LEGENDARY -> Text.translatable("reforge.chainores.legendary").formatted(Formatting.GOLD, Formatting.BOLD);
                case REFORGE_FIERCE -> Text.translatable("reforge.chainores.fierce").formatted(Formatting.RED, Formatting.BOLD);
                case REFORGE_SWIFT -> Text.translatable("reforge.chainores.swift").formatted(Formatting.GREEN, Formatting.BOLD);
                case REFORGE_TITANIC -> Text.translatable("reforge.chainores.titanic").formatted(Formatting.GRAY, Formatting.BOLD);
                default -> Text.empty();
            };
            if (!reforgeText.getString().isEmpty()) {
                tooltip.add(Text.literal("⚡ ").formatted(Formatting.YELLOW).append(reforgeText));
            }
        }

        String gem = getGem(stack);
        if (gem != null) {
            Text gemText = switch (gem) {
                case GEM_BLOOD -> Text.translatable("gem.chainores.blood").formatted(Formatting.DARK_RED, Formatting.ITALIC);
                case GEM_VOID -> Text.translatable("gem.chainores.void").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC);
                case GEM_CORE -> Text.translatable("gem.chainores.core").formatted(Formatting.GOLD, Formatting.ITALIC);
                case GEM_GENESIS -> Text.translatable("gem.chainores.genesis").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC);
                default -> Text.empty();
            };
            if (!gemText.getString().isEmpty()) {
                tooltip.add(Text.literal("◆ ").formatted(Formatting.LIGHT_PURPLE).append(gemText));
            }
        }
    }
}
