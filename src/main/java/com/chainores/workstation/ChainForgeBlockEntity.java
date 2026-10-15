package com.chainores.workstation;

import com.chainores.gear.*;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModItems;
import com.chainores.registry.ModWorkstations;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChainForgeBlockEntity extends LockableContainerBlockEntity {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    public static final int INPUT_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private int forgeProgress = 0;
    private final int totalForgeTime = 60; // 3 seconds

    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> forgeProgress;
                case 1 -> totalForgeTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) forgeProgress = value;
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public ChainForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModWorkstations.CHAIN_FORGE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.chainores.chain_forge");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ChainForgeScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(readView, this.inventory);
        this.forgeProgress = readView.getInt("ForgeProgress", 0);
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        Inventories.writeData(writeView, this.inventory);
        writeView.putInt("ForgeProgress", this.forgeProgress);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChainForgeBlockEntity entity) {
        if (world.isClient()) return;

        ItemStack input = entity.inventory.get(INPUT_SLOT);
        ItemStack catalyst = entity.inventory.get(CATALYST_SLOT);
        ItemStack outputSlot = entity.inventory.get(OUTPUT_SLOT);

        ItemStack recipeResult = entity.calculateForgeResult(input, catalyst);

        if (!recipeResult.isEmpty() && canInsertOutput(outputSlot, recipeResult)) {
            entity.forgeProgress++;

            if (entity.forgeProgress % 15 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.7f, 1.3f);
            }

            if (entity.forgeProgress >= entity.totalForgeTime) {
                // Craft item
                input.decrement(1);
                catalyst.decrement(1);

                if (outputSlot.isEmpty()) {
                    entity.inventory.set(OUTPUT_SLOT, recipeResult.copy());
                } else if (ItemStack.areItemsAndComponentsEqual(outputSlot, recipeResult)) {
                    outputSlot.increment(recipeResult.getCount());
                }

                entity.forgeProgress = 0;
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.9f, 1.2f);
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.8f, 1.5f);
                markDirty(world, pos, state);
            }
        } else {
            if (entity.forgeProgress > 0) {
                entity.forgeProgress = Math.max(0, entity.forgeProgress - 2);
                markDirty(world, pos, state);
            }
        }
    }

    private static boolean canInsertOutput(ItemStack outputSlot, ItemStack result) {
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(outputSlot, result)) return false;
        return outputSlot.getCount() + result.getCount() <= outputSlot.getMaxCount();
    }

    private ItemStack calculateForgeResult(ItemStack input, ItemStack catalyst) {
        if (input.isEmpty() || catalyst.isEmpty()) return ItemStack.EMPTY;

        // Catalyst must be Chain Smithing Template or an ingot
        boolean isTemplate = catalyst.isOf(ModItems.CHAIN_SMITHING_TEMPLATE);

        // 1. Ingot Upgrades (Tier N -> Tier N+1)
        for (int i = 0; i < OreRegistryData.TIERS.size(); i++) {
            OreTier ore = OreRegistryData.TIERS.get(i);
            if (input.isOf(ore.ingotItem)) {
                int nextIndex = i + 1;
                if (nextIndex < OreRegistryData.TIERS.size()) {
                    // Milestone dimension boss gates
                    if (i == 69) { // T70 -> T71 requires Abyssal Catalyst
                        if (catalyst.isOf(com.chainores.registry.ModBossItems.ABYSSAL_CATALYST)) {
                            return new ItemStack(OreRegistryData.TIERS.get(nextIndex).ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 104) { // T105 -> T106 requires Nether Core
                        if (catalyst.isOf(com.chainores.registry.ModBossItems.NETHER_CORE)) {
                            return new ItemStack(OreRegistryData.TIERS.get(nextIndex).ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 134) { // T135 -> T136 requires Void Shard
                        if (catalyst.isOf(com.chainores.registry.ModBossItems.VOID_SHARD)) {
                            return new ItemStack(OreRegistryData.TIERS.get(nextIndex).ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 148) { // T149 -> T150 requires Cosmic Singularity
                        if (catalyst.isOf(com.chainores.registry.ModBossItems.COSMIC_SINGULARITY)) {
                            return new ItemStack(OreRegistryData.TIERS.get(nextIndex).ingotItem);
                        }
                        return ItemStack.EMPTY;
                    }

                    if (isTemplate || catalyst.isOf(ore.ingotItem)) {
                        return new ItemStack(OreRegistryData.TIERS.get(nextIndex).ingotItem);
                    }
                }
            }
        }

        // 2. Weapon & Tool Upgrades between the 16 milestones
        for (int i = 0; i < ModGear.GEAR_SETS.size() - 1; i++) {
            ModGear.GearSet current = ModGear.GEAR_SETS.get(i);
            ModGear.GearSet next = ModGear.GEAR_SETS.get(i + 1);
            OreTier nextOre = OreRegistryData.TIERS.get(next.tierData.tier() - 1);

            // Boss milestone gates for gear sets
            int nextTier = next.tierData.tier();
            if (nextTier > 70 && current.tierData.tier() <= 70) {
                if (!catalyst.isOf(com.chainores.registry.ModBossItems.ABYSSAL_CATALYST)) return ItemStack.EMPTY;
            } else if (nextTier > 105 && current.tierData.tier() <= 105) {
                if (!catalyst.isOf(com.chainores.registry.ModBossItems.NETHER_CORE)) return ItemStack.EMPTY;
            } else if (nextTier > 135 && current.tierData.tier() <= 135) {
                if (!catalyst.isOf(com.chainores.registry.ModBossItems.VOID_SHARD)) return ItemStack.EMPTY;
            } else if (nextTier == 150) {
                if (!catalyst.isOf(com.chainores.registry.ModBossItems.COSMIC_SINGULARITY)) return ItemStack.EMPTY;
            } else {
                boolean validCatalyst = isTemplate || catalyst.isOf(nextOre.ingotItem);
                if (!validCatalyst) continue;
            }

            if (input.isOf(current.sword)) return transferGearData(input, new ItemStack(next.sword));
            if (input.isOf(current.pickaxe)) return transferGearData(input, new ItemStack(next.pickaxe));
            if (input.isOf(current.axe)) return transferGearData(input, new ItemStack(next.axe));
            if (input.isOf(current.shovel)) return transferGearData(input, new ItemStack(next.shovel));
            if (input.isOf(current.hoe)) return transferGearData(input, new ItemStack(next.hoe));
            if (input.isOf(current.helmet)) return transferGearData(input, new ItemStack(next.helmet));
            if (input.isOf(current.chestplate)) return transferGearData(input, new ItemStack(next.chestplate));
            if (input.isOf(current.leggings)) return transferGearData(input, new ItemStack(next.leggings));
            if (input.isOf(current.boots)) return transferGearData(input, new ItemStack(next.boots));
        }

        // 3. Special Weapons
        if (input.isOf(net.minecraft.item.Items.BOW) && catalyst.isOf(OreRegistryData.TIERS.get(114).ingotItem)) {
            return new ItemStack(ModGear.DRAGON_BOW);
        }
        if (input.isOf(net.minecraft.item.Items.CROSSBOW) && catalyst.isOf(OreRegistryData.TIERS.get(124).ingotItem)) {
            return new ItemStack(ModGear.GALACTIC_CROSSBOW);
        }
        if (input.isOf(net.minecraft.item.Items.SHIELD) && catalyst.isOf(OreRegistryData.TIERS.get(99).ingotItem)) {
            return new ItemStack(ModGear.AEGIS_SHIELD);
        }
        if (input.isOf(ModGear.GEAR_SETS.get(ModGear.GEAR_SETS.size() - 1).sword) && catalyst.isOf(com.chainores.registry.ModBossItems.COSMIC_SINGULARITY)) {
            return new ItemStack(ModGear.GOD_OMNITOOL);
        }

        // 4. Gem Socketing & Reforging in the Forge
        if (isGear(input.getItem())) {
            if (catalyst.isOf(com.chainores.registry.ModGems.BLOOD_GEM)) {
                ItemStack out = input.copy();
                com.chainores.modifier.ChainModifiers.setGem(out, com.chainores.modifier.ChainModifiers.GEM_BLOOD);
                return out;
            }
            if (catalyst.isOf(com.chainores.registry.ModGems.VOID_GEM)) {
                ItemStack out = input.copy();
                com.chainores.modifier.ChainModifiers.setGem(out, com.chainores.modifier.ChainModifiers.GEM_VOID);
                return out;
            }
            if (catalyst.isOf(com.chainores.registry.ModGems.CORE_GEM)) {
                ItemStack out = input.copy();
                com.chainores.modifier.ChainModifiers.setGem(out, com.chainores.modifier.ChainModifiers.GEM_CORE);
                return out;
            }
            if (catalyst.isOf(com.chainores.registry.ModGems.GENESIS_GEM)) {
                ItemStack out = input.copy();
                com.chainores.modifier.ChainModifiers.setGem(out, com.chainores.modifier.ChainModifiers.GEM_GENESIS);
                return out;
            }
            if (catalyst.isOf(net.minecraft.item.Items.AMETHYST_SHARD) || catalyst.isOf(net.minecraft.item.Items.LAPIS_LAZULI)) {
                ItemStack out = input.copy();
                String rolled = com.chainores.modifier.ChainModifiers.rollRandomReforge(net.minecraft.util.math.random.Random.create());
                com.chainores.modifier.ChainModifiers.setReforge(out, rolled);
                return out;
            }
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack transferGearData(ItemStack input, ItemStack output) {
        if (input.isEmpty() || output.isEmpty()) return output;

        String gem = com.chainores.modifier.ChainModifiers.getGem(input);
        if (gem != null) {
            com.chainores.modifier.ChainModifiers.setGem(output, gem);
        }

        String reforge = com.chainores.modifier.ChainModifiers.getReforge(input);
        if (reforge != null) {
            com.chainores.modifier.ChainModifiers.setReforge(output, reforge);
        }

        if (input.contains(net.minecraft.component.DataComponentTypes.CUSTOM_NAME)) {
            output.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, input.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME));
        }

        return output;
    }

    private boolean isGear(Item item) {
        return item instanceof ChainSwordItem || item instanceof ChainPickaxeItem || item instanceof ChainAxeItem ||
                item instanceof ChainShovelItem || item instanceof ChainHoeItem || item instanceof ChainArmorItem ||
                item instanceof GodOmnitoolItem || item instanceof ChainShieldItem;
    }
}
