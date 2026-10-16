package com.chainores.item;

import com.chainores.modifier.ChainModifiers;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.function.Consumer;

public abstract class ChainTrinketItem extends Item {
    private final int maxSockets;

    public ChainTrinketItem(int maxSockets, Settings settings) {
        super(settings);
        this.maxSockets = maxSockets;
    }

    public int getMaxSockets() {
        return maxSockets;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        // Apply passives every second (20 ticks)
        if (player.age % 20 != 0) return;

        applyTrinketPassives(stack, player);
    }

    public static void applyTrinketPassives(ItemStack stack, PlayerEntity player) {
        List<String> gems = ChainModifiers.getGems(stack);
        int bloodPower = 0;
        int voidPower = 0;
        int corePower = 0;
        int genesisPower = 0;

        for (String gem : gems) {
            String base = ChainModifiers.getBaseGemType(gem);
            int tier = ChainModifiers.getGemTier(gem);
            switch (base) {
                case ChainModifiers.GEM_BLOOD -> bloodPower += tier;
                case ChainModifiers.GEM_VOID -> voidPower += tier;
                case ChainModifiers.GEM_CORE -> corePower += tier;
                case ChainModifiers.GEM_GENESIS -> genesisPower += tier;
            }
        }

        // 1. Blood Gem: Regeneration
        if (bloodPower > 0) {
            int amp = Math.min((bloodPower - 1) / 2, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, amp, true, false, true));
        }

        // 2. Void Gem: Fall damage immunity & slow falling glide
        if (voidPower > 0) {
            if (player.fallDistance > 1.2f && !player.isOnGround()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, true, false, true));
                player.fallDistance = 0;
            }
            if (voidPower >= 4 && player.isSneaking() && !player.isOnGround()) {
                // Ethereal glide
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10, 0, true, false, false));
            }
        }

        // 3. Core Gem: Fire Immunity & Speed
        if (corePower > 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
            int speedAmp = Math.min((corePower - 1) / 2, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, speedAmp, true, false, true));
            if (corePower >= 3) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 0, true, false, true));
            }
        }

        // 4. Genesis Gem: Bonus Absorption Hearts & Strength
        if (genesisPower > 0) {
            if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                int absorbAmp = Math.min(genesisPower, 4); // +4 to +10 hearts
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120, absorbAmp, true, false, true));
            }
            if (genesisPower >= 2) {
                int strAmp = Math.min((genesisPower - 2) / 2, 1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, strAmp, true, false, true));
            }
        }

        // 5. Reforge Buffs
        String reforge = ChainModifiers.getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case ChainModifiers.REFORGE_DIVINE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_SWIFT ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_FIERCE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_TITANIC ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_LEGENDARY ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 60, 0, true, false, true));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ Accesorio / Reliquia Arcana ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Otorga sus beneficios pasivos continuos en inventario,").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("mano secundaria o ranuras de Trinkets (Anillo/Amuleto).").formatted(Formatting.GRAY));
        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("⚒ Personalización:").formatted(Formatting.AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • Engarza hasta " + maxSockets + " gemas en el Yunque en Cadena.").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal(" • Reforja con Amatista/Lapis para obtener prefijos legendarios.").formatted(Formatting.DARK_AQUA));
    }
}
