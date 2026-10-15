package com.chainores.modifier;

import com.chainores.gear.ChainArmorItem;
import com.chainores.gear.ChainAxeItem;
import com.chainores.gear.ChainHoeItem;
import com.chainores.gear.ChainPickaxeItem;
import com.chainores.gear.ChainShovelItem;
import com.chainores.gear.ChainSwordItem;
import com.chainores.gear.DragonBreathBowItem;
import com.chainores.gear.GalacticCrossbowItem;
import com.chainores.gear.GodOmnitoolItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChainModifiers {
    public static final String KEY_GEM = "chainores_gem";
    public static final String KEY_GEM_COUNT = "chainores_gem_count";
    public static final String KEY_REFORGE = "chainores_reforge";

    // Gem IDs
    public static final String GEM_BLOOD = "blood_gem";
    public static final String GEM_VOID = "void_gem";
    public static final String GEM_CORE = "core_gem";
    public static final String GEM_GENESIS = "genesis_gem";

    // Reforge Prefix IDs
    public static final String REFORGE_DIVINE = "divine";
    public static final String REFORGE_LEGENDARY = "legendary";
    public static final String REFORGE_FIERCE = "fierce";
    public static final String REFORGE_SWIFT = "swift";
    public static final String REFORGE_TITANIC = "titanic";

    public static boolean isSocketableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof ChainArmorItem ||
               item instanceof ChainSwordItem ||
               item instanceof DragonBreathBowItem ||
               item instanceof GalacticCrossbowItem ||
               item instanceof GodOmnitoolItem;
    }

    public static int getMaxSockets(ItemStack stack) {
        if (stack.isEmpty() || !isSocketableItem(stack)) return 0;
        Item item = stack.getItem();

        if (item instanceof ChainArmorItem armor) {
            int t = armor.getTier();
            if (t <= 1) return 0;       // T1: Demasiado débil para gemas
            if (t <= 30) return 1;      // T10 - T30: 1 slot
            if (t <= 70) return 2;      // T40 - T70: 2 slots
            if (t <= 115) return 3;     // T80 - T115: 3 slots
            return 4;                   // T125, T135, T140, T150 (Las mejores): 4 slots
        }

        if (item instanceof ChainSwordItem sword) {
            int t = sword.getTier();
            if (t <= 1) return 0;       // T1: Demasiado débil
            if (t <= 30) return 1;      // T10 - T30: 1 slot
            if (t <= 115) return 2;     // T40 - T115: 2 slots
            return 3;                   // T125 - T150: 3 slots
        }

        if (item instanceof DragonBreathBowItem || item instanceof GalacticCrossbowItem) {
            return 3;
        }

        if (item instanceof GodOmnitoolItem) {
            return 4;
        }

        return 0;
    }

    public static List<String> getGems(ItemStack stack) {
        List<String> list = new ArrayList<>();
        if (stack.isEmpty()) return list;

        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return list;
        NbtCompound nbt = custom.copyNbt();

        int count = nbt.getInt(KEY_GEM_COUNT).orElse(0);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                nbt.getString("chainores_gem_" + i).ifPresent(list::add);
            }
        } else if (nbt.contains(KEY_GEM)) {
            nbt.getString(KEY_GEM).ifPresent(list::add);
        }
        return list;
    }

    public static boolean canAddGem(ItemStack stack) {
        int max = getMaxSockets(stack);
        if (max <= 0) return false;
        return getGems(stack).size() < max;
    }

    public static boolean addGem(ItemStack stack, String gemId) {
        if (stack.isEmpty() || gemId == null) return false;
        int max = getMaxSockets(stack);
        List<String> list = getGems(stack);
        if (list.size() >= max) return false;

        list.add(gemId);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt(KEY_GEM_COUNT, list.size());
            for (int i = 0; i < list.size(); i++) {
                nbt.putString("chainores_gem_" + i, list.get(i));
            }
            nbt.putString(KEY_GEM, list.get(0));
        });
        return true;
    }

    public static void setGem(ItemStack stack, String gemId) {
        // Clear previous and set single gem
        if (stack.isEmpty()) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt(KEY_GEM_COUNT, 1);
            nbt.putString("chainores_gem_0", gemId);
            nbt.putString(KEY_GEM, gemId);
        });
    }

    public static String getGem(ItemStack stack) {
        List<String> gems = getGems(stack);
        return gems.isEmpty() ? null : gems.get(0);
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
        List<String> gems = getGems(stack);
        for (String gem : gems) {
            if (GEM_CORE.equals(gem)) mult += 0.35f;
            if (GEM_GENESIS.equals(gem)) mult += 0.50f;
        }

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

    public static void appendModifierTooltips(ItemStack stack, Consumer<Text> textConsumer) {
        List<Text> lines = new ArrayList<>();
        appendModifierTooltips(stack, lines);
        for (Text line : lines) {
            textConsumer.accept(line);
        }
    }

    public static void appendModifierTooltips(ItemStack stack, List<Text> tooltip) {
        // Prevent duplicate tooltip lines if called multiple times
        boolean hasSocketInfo = tooltip.stream().anyMatch(t -> t.getString().contains("Ranuras de Gemas"));
        if (hasSocketInfo) {
            return;
        }

        String reforge = getReforge(stack);
        if (reforge != null) {
            boolean hasReforgeInfo = tooltip.stream().anyMatch(t -> t.getString().contains("⚡ "));
            if (!hasReforgeInfo) {
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
        }

        // Only weapons and armors show gem sockets!
        if (!isSocketableItem(stack)) {
            return;
        }

        int maxSockets = getMaxSockets(stack);
        if (maxSockets <= 0) {
            tooltip.add(Text.literal("Ranuras de Gemas: ").formatted(Formatting.GRAY)
                    .append(Text.literal("[Sin Ranuras]").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(" (Tier inicial demasiado débil)").formatted(Formatting.DARK_RED, Formatting.ITALIC)));
        } else {
            List<String> gems = getGems(stack);
            tooltip.add(Text.literal("Ranuras de Gemas [" + gems.size() + "/" + maxSockets + "]:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

            MutableText socketLine = Text.literal("  ");
            for (int i = 0; i < maxSockets; i++) {
                if (i < gems.size()) {
                    String g = gems.get(i);
                    switch (g) {
                        case GEM_BLOOD -> socketLine.append(Text.literal("[◆ Carmesí] ").formatted(Formatting.DARK_RED, Formatting.BOLD));
                        case GEM_VOID -> socketLine.append(Text.literal("[◆ Vacío] ").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
                        case GEM_CORE -> socketLine.append(Text.literal("[◆ Núcleo] ").formatted(Formatting.GOLD, Formatting.BOLD));
                        case GEM_GENESIS -> socketLine.append(Text.literal("[◆ Génesis] ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
                        default -> socketLine.append(Text.literal("[◆ Gema] ").formatted(Formatting.AQUA));
                    }
                } else {
                    socketLine.append(Text.literal("[◇ Vacío] ").formatted(Formatting.DARK_GRAY));
                }
            }
            tooltip.add(socketLine);
        }
    }
}
