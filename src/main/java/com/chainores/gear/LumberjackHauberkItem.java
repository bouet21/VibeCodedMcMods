package com.chainores.gear;

import com.chainores.ChainOresMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class LumberjackHauberkItem extends Item {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
            4000,
            Map.of(EquipmentType.CHESTPLATE, 9),
            22,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            3.5f,
            0.15f,
            ItemTags.CHEST_ARMOR,
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, ChainOresMod.id("lumberjack_hauberk"))
    );

    private static final ThreadLocal<Boolean> IS_FELLING = ThreadLocal.withInitial(() -> false);

    public LumberjackHauberkItem(Settings settings) {
        super(settings.armor(MATERIAL, EquipmentType.CHESTPLATE).maxDamage(4000));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot != EquipmentSlot.CHEST || !(entity instanceof ServerPlayerEntity player)) return;

        // Apply Gem & Reforge Buffs
        com.chainores.modifier.ChainModifiers.applyArmorGemAndReforgeBuffs(player, stack);

        // 1. Sylva Speed & Leap: Speed II and Jump Boost II
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, true, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 40, 1, true, false, false));

        // 2. Sylva Fertility: every 60 ticks, fertilize nearby crops/saplings in 4-block radius
        if (player.age % 60 == 0) {
            BlockPos playerPos = player.getBlockPos();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = -1; dy <= 2; dy++) {
                        BlockPos target = playerPos.add(dx, dy, dz);
                        BlockState state = world.getBlockState(target);
                        if (state.getBlock() instanceof Fertilizable fertilizable) {
                            if (fertilizable.isFertilizable(world, target, state)) {
                                if (world.random.nextFloat() < 0.35f && fertilizable.canGrow(world, world.random, target, state)) {
                                    fertilizable.grow(world, world.random, target, state);
                                    world.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER,
                                            target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Treecapitator trigger: called from BlockBreak callback when wearing this armor.
     */
    public static void fellTree(ServerWorld world, ServerPlayerEntity player, BlockPos startPos) {
        if (IS_FELLING.get()) return;
        IS_FELLING.set(true);

        try {
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(startPos);
            visited.add(startPos);

            int felled = 0;
            int maxLogs = Math.min(64, com.chainores.config.ChainOresConfig.get().treeCapitatorMaxBlocks);

            while (!queue.isEmpty() && felled < maxLogs) {
                BlockPos current = queue.poll();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos next = current.add(dx, dy, dz);

                            if (!visited.contains(next)) {
                                visited.add(next);
                                BlockState state = world.getBlockState(next);

                                if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES)) {
                                    if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, next, state)) {
                                        continue;
                                    }
                                    queue.add(next);
                                    if (state.isIn(BlockTags.LOGS)) {
                                        world.breakBlock(next, true, player);
                                        felled++;
                                    } else {
                                        // 30% chance to break adjacent leaves
                                        if (world.random.nextFloat() < 0.3f) {
                                            world.breakBlock(next, true, player);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (felled > 0) {
                world.playSound(null, startPos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.2f, 0.8f);
                consolidateDrops(world, player, 16.0);
            }
        } finally {
            IS_FELLING.set(false);
        }
    }

    private static void consolidateDrops(ServerWorld world, ServerPlayerEntity player, double radius) {
        java.util.List<net.minecraft.entity.ItemEntity> items = world.getEntitiesByClass(
                net.minecraft.entity.ItemEntity.class, player.getBoundingBox().expand(radius), e -> !e.isRemoved());
        for (int i = 0; i < items.size(); i++) {
            net.minecraft.entity.ItemEntity a = items.get(i);
            if (a.isRemoved()) continue;
            for (int j = i + 1; j < items.size(); j++) {
                net.minecraft.entity.ItemEntity b = items.get(j);
                if (b.isRemoved()) continue;
                if (ItemStack.areItemsAndComponentsEqual(a.getStack(), b.getStack())) {
                    int space = a.getStack().getMaxCount() - a.getStack().getCount();
                    if (space > 0) {
                        int toTake = Math.min(space, b.getStack().getCount());
                        a.getStack().increment(toTake);
                        b.getStack().decrement(toTake);
                        if (b.getStack().isEmpty()) {
                            b.discard();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("★ Armadura de Especialista: Tala & Naturaleza").formatted(Formatting.GREEN, Formatting.BOLD));
        textConsumer.accept(Text.literal("✦ Agilidad Silvestre: Velocidad II y Salto II continuos").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal("✦ Tala Devastadora: Talar un tronco tala el árbol entero en cadena").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal("✦ Fertilidad de Gaia: Acelera el crecimiento de cultivos y brotes cercanos").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal("✦ Inmunidad a espinas, cactus y arbustos").formatted(Formatting.LIGHT_PURPLE));
    }
}
