package com.chainores.gear;

import com.chainores.ChainOresMod;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MiningExoskeletonItem extends Item {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
            4500,
            Map.of(EquipmentType.CHESTPLATE, 10),
            25,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            4.0f,
            0.2f,
            ItemTags.CHEST_ARMOR,
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, ChainOresMod.id("mining_exoskeleton"))
    );

    public MiningExoskeletonItem(Settings settings) {
        super(settings.armor(MATERIAL, EquipmentType.CHESTPLATE).maxDamage(4500).fireproof());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot != EquipmentSlot.CHEST || !(entity instanceof ServerPlayerEntity player)) return;

        // Apply Gem & Reforge Buffs
        com.chainores.modifier.ChainModifiers.applyArmorGemAndReforgeBuffs(player, stack);

        // 1. Permanent Miner Buffs: Haste II, Night Vision, Fire Resistance
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 40, 1, true, false, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, false));

        // 2. Passive 6-meter Ore & Drop Magnet
        if (player.age % 4 == 0) {
            List<ItemEntity> items = world.getEntitiesByClass(
                    ItemEntity.class,
                    player.getBoundingBox().expand(6.0),
                    e -> !e.isRemoved() && e.isAlive()
            );
            for (ItemEntity item : items) {
                Vec3d diff = player.getEyePos().subtract(item.getEntityPos());
                if (diff.length() > 0.8) {
                    item.setVelocity(diff.normalize().multiply(0.45));
                }
            }
        }

        // 3. Sonar pulse when sneaking
        if (player.isSneaking() && player.age % 40 == 0) {
            world.spawnParticles(net.minecraft.particle.ParticleTypes.GLOW, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.4, 0.4, 0.4, 0.05);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.6f, 1.8f);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("★ Armadura de Especialista: Minería Profunda").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("✦ Prisa Minera II continua y Visión Nocturna en cavernas").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal("✦ Inmunidad Ígnea total (Fuego y Lava)").formatted(Formatting.RED));
        textConsumer.accept(Text.literal("✦ Imán Espeleológico pasivo: Atrae todos los minerales a 6m").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal("✦ Pulso de Sonar: Emite destellos de ecolocalización al agacharse").formatted(Formatting.LIGHT_PURPLE));
    }
}
