package com.chainores.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;

public class MiningSatchelItem extends Item {
    public static final int INVENTORY_SIZE = 54;
    private static final String ACTIVE_TAG = "chainores_magnet_active";

    public MiningSatchelItem(Settings settings) {
        super(settings.maxCount(1).fireproof());
    }

    public static boolean isMagnetActive(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyNbt().getBoolean(ACTIVE_TAG, true);
        }
        return true; // Active by default
    }

    public static void setMagnetActive(ItemStack stack, boolean active) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putBoolean(ACTIVE_TAG, active));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            // Shift + Right Click toggles vacuum magnet
            if (!world.isClient()) {
                boolean current = isMagnetActive(stack);
                boolean newState = !current;
                setMagnetActive(stack, newState);

                if (newState) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.5f);
                    user.sendMessage(Text.literal("✔ Imán de Minerales: ").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal("ACTIVADO (Radio 8m)").formatted(Formatting.AQUA)), true);
                } else {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.0f);
                    user.sendMessage(Text.literal("✖ Imán de Minerales: ").formatted(Formatting.RED, Formatting.BOLD)
                            .append(Text.literal("DESACTIVADO").formatted(Formatting.GRAY)), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        // Normal Right Click opens 54-slot satchel inventory
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.literal("Bolsa de Minería en Cadena (54 Ranuras)")
                            .formatted(Formatting.GOLD, Formatting.BOLD);
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    SimpleInventory satchelInv = new SimpleInventory(INVENTORY_SIZE);
                    ContainerComponent comp = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
                    DefaultedList<ItemStack> list = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                    comp.copyTo(list);
                    for (int i = 0; i < INVENTORY_SIZE; i++) {
                        satchelInv.setStack(i, list.get(i));
                    }

                    return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, satchelInv, 6) {
                        @Override
                        public void onClosed(PlayerEntity p) {
                            super.onClosed(p);
                            DefaultedList<ItemStack> updated = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                            for (int i = 0; i < INVENTORY_SIZE; i++) {
                                updated.set(i, satchelInv.getStack(i));
                            }
                            autoCompact(updated);
                            stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(updated));
                        }
                    };
                }
            });
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 1.0f, 1.2f);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (!(entity instanceof PlayerEntity player)) return;

        // Run vacuum check every 4 ticks
        if (player.age % 4 != 0) return;
        if (!isMagnetActive(stack)) return;

        // Attract and vacuum dropped Chain Ores items in an 8-block radius
        List<ItemEntity> items = world.getEntitiesByClass(
                ItemEntity.class,
                player.getBoundingBox().expand(8.0),
                e -> !e.isRemoved() && e.isAlive() && isChainItem(e.getStack())
        );

        if (items.isEmpty()) return;

        boolean absorbedAny = false;
        ContainerComponent comp = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        DefaultedList<ItemStack> satchelItems = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        comp.copyTo(satchelItems);

        for (ItemEntity itemEntity : items) {
            ItemStack dropped = itemEntity.getStack();
            if (dropped.isEmpty()) continue;

            // Pull entity towards player smoothly
            Vec3d diff = player.getEyePos().subtract(itemEntity.getEntityPos());
            double dist = diff.length();

            if (dist > 1.4) {
                Vec3d pull = diff.normalize().multiply(Math.min(0.55, 0.25 + 0.05 * (8.0 - dist)));
                itemEntity.setVelocity(pull);
            } else {
                // Within 1.4m: absorb into satchel
                int remaining = insertIntoSatchel(satchelItems, dropped);
                if (remaining < dropped.getCount()) {
                    absorbedAny = true;
                    if (remaining <= 0) {
                        itemEntity.discard();
                    } else {
                        dropped.setCount(remaining);
                    }
                }
            }
        }

        if (absorbedAny) {
            autoCompact(satchelItems);
            stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(satchelItems));
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.4f, 1.8f);
        }
    }

    public static void autoCompact(DefaultedList<ItemStack> satchelItems) {
        // 1. Chain Ores Tiers 1 - 150 (9 ingots -> 1 storage block)
        for (com.chainores.ore.OreTier ore : com.chainores.ore.OreRegistryData.TIERS) {
            if (ore.ingotItem == null || ore.storageBlockItem == null) continue;
            compactIngredient(satchelItems, ore.ingotItem, ore.storageBlockItem);
        }

        // 2. Vanilla Minerals (9 items -> 1 block)
        compactIngredient(satchelItems, net.minecraft.item.Items.RAW_IRON, net.minecraft.item.Items.RAW_IRON_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.IRON_INGOT, net.minecraft.item.Items.IRON_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.RAW_COPPER, net.minecraft.item.Items.RAW_COPPER_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.COPPER_INGOT, net.minecraft.item.Items.COPPER_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.RAW_GOLD, net.minecraft.item.Items.RAW_GOLD_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.GOLD_INGOT, net.minecraft.item.Items.GOLD_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.DIAMOND, net.minecraft.item.Items.DIAMOND_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.EMERALD, net.minecraft.item.Items.EMERALD_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.COAL, net.minecraft.item.Items.COAL_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.REDSTONE, net.minecraft.item.Items.REDSTONE_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.LAPIS_LAZULI, net.minecraft.item.Items.LAPIS_BLOCK);
        compactIngredient(satchelItems, net.minecraft.item.Items.NETHERITE_INGOT, net.minecraft.item.Items.NETHERITE_BLOCK);
    }

    private static void compactIngredient(DefaultedList<ItemStack> satchelItems, Item ingredient, Item resultBlock) {
        int total = 0;
        for (ItemStack s : satchelItems) {
            if (!s.isEmpty() && s.isOf(ingredient)) {
                total += s.getCount();
            }
        }

        int blocksToMake = total / 9;
        if (blocksToMake <= 0) return;

        int toRemove = blocksToMake * 9;
        for (int i = 0; i < satchelItems.size(); i++) {
            ItemStack s = satchelItems.get(i);
            if (!s.isEmpty() && s.isOf(ingredient)) {
                int take = Math.min(toRemove, s.getCount());
                s.decrement(take);
                toRemove -= take;
                if (s.isEmpty()) {
                    satchelItems.set(i, ItemStack.EMPTY);
                }
                if (toRemove <= 0) break;
            }
        }

        ItemStack blocks = new ItemStack(resultBlock, blocksToMake);
        insertIntoSatchel(satchelItems, blocks);
    }

    private static boolean isChainItem(ItemStack s) {
        if (s.isEmpty()) return false;
        var id = Registries.ITEM.getId(s.getItem());
        if (id.getNamespace().equals("chainores")) return true;
        return s.isIn(net.minecraft.registry.tag.ItemTags.COAL_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.IRON_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.GOLD_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.COPPER_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.DIAMOND_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.REDSTONE_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.LAPIS_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.EMERALD_ORES) ||
                s.isIn(net.minecraft.registry.tag.ItemTags.COALS);
    }

    private static int insertIntoSatchel(DefaultedList<ItemStack> satchelItems, ItemStack incoming) {
        int count = incoming.getCount();

        // 1. Try to merge with existing non-full stacks
        for (int i = 0; i < satchelItems.size(); i++) {
            ItemStack existing = satchelItems.get(i);
            if (!existing.isEmpty() && ItemStack.areItemsAndComponentsEqual(existing, incoming)) {
                int space = existing.getMaxCount() - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, count);
                    existing.increment(toAdd);
                    count -= toAdd;
                    if (count <= 0) return 0;
                }
            }
        }

        // 2. Try to insert into first empty slot
        for (int i = 0; i < satchelItems.size(); i++) {
            if (satchelItems.get(i).isEmpty()) {
                ItemStack newStack = incoming.copy();
                newStack.setCount(count);
                satchelItems.set(i, newStack);
                return 0;
            }
        }

        return count;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);

        boolean active = isMagnetActive(stack);
        textConsumer.accept(Text.literal("Capacidad: 54 Ranuras Profundas de Minería").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Imán de Vértice: ")
                .formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(active ? "✔ ACTIVADO" : "✖ DESACTIVADO")
                        .formatted(active ? Formatting.GREEN : Formatting.RED, Formatting.BOLD)));
        textConsumer.accept(Text.literal("✦ Auto-Compactador: ").formatted(Formatting.AQUA, Formatting.BOLD)
                .append(Text.literal("ACTIVO (9 lingotes ➔ 1 bloque automático)").formatted(Formatting.WHITE)));

        textConsumer.accept(Text.literal("• Clic Derecho: Abrir inventario de la bolsa.").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("• Shift + Clic Derecho: Alternar auto-aspirador de minerales.").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal("• Compatible con ranuras de Trinkets (Cinturón/Pecho).").formatted(Formatting.DARK_PURPLE));

        // Count stored items
        ContainerComponent comp = stack.get(DataComponentTypes.CONTAINER);
        if (comp != null) {
            int filled = (int) comp.streamNonEmpty().count();
            int totalItems = comp.streamNonEmpty().mapToInt(ItemStack::getCount).sum();
            textConsumer.accept(Text.literal("Almacenado: " + filled + "/54 ranuras (" + totalItems + " objetos)")
                    .formatted(Formatting.YELLOW));
        }
    }
}
