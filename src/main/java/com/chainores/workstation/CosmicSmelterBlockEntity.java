package com.chainores.workstation;

import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModWorkstations;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
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
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CosmicSmelterBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private static final int[] TOP_SLOTS = new int[]{INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = new int[]{OUTPUT_SLOT, FUEL_SLOT};
    private static final int[] SIDE_SLOTS = new int[]{FUEL_SLOT, INPUT_SLOT};

    private int burnTime = 0;
    private int fuelTime = 0;
    private int cookTime = 0;
    private final int totalCookTime = 50; // 4x faster smelting!

    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> fuelTime;
                case 2 -> cookTime;
                case 3 -> totalCookTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> fuelTime = value;
                case 2 -> cookTime = value;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };

    public CosmicSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModWorkstations.COSMIC_SMELTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.chainores.cosmic_smelter");
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
        return new CosmicSmelterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(readView, this.inventory);
        this.burnTime = readView.getInt("BurnTime", 0);
        this.fuelTime = readView.getInt("FuelTime", 0);
        this.cookTime = readView.getInt("CookTime", 0);
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        Inventories.writeData(writeView, this.inventory);
        writeView.putInt("BurnTime", this.burnTime);
        writeView.putInt("FuelTime", this.fuelTime);
        writeView.putInt("CookTime", this.cookTime);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CosmicSmelterBlockEntity entity) {
        if (world.isClient()) return;

        boolean wasBurning = entity.burnTime > 0;
        boolean dirty = false;

        if (entity.burnTime > 0) {
            entity.burnTime--;
        }

        ItemStack input = entity.inventory.get(INPUT_SLOT);
        ItemStack fuel = entity.inventory.get(FUEL_SLOT);
        ItemStack outputSlot = entity.inventory.get(OUTPUT_SLOT);

        ItemStack recipeResult = getSmeltingResult(input);

        // Consume fuel if needed and can smelt
        if (entity.burnTime <= 0 && !recipeResult.isEmpty() && canInsertOutput(outputSlot, recipeResult)) {
            int fuelValue = getFuelTime(fuel);
            if (fuelValue > 0) {
                entity.burnTime = fuelValue;
                entity.fuelTime = fuelValue;

                if (fuel.isOf(Items.LAVA_BUCKET)) {
                    entity.inventory.set(FUEL_SLOT, new ItemStack(Items.BUCKET));
                } else if (fuel.isOf(Items.DRAGON_BREATH)) {
                    entity.inventory.set(FUEL_SLOT, new ItemStack(Items.GLASS_BOTTLE));
                } else {
                    fuel.decrement(1);
                }
                dirty = true;
            }
        }

        // Cook progress
        if (entity.burnTime > 0 && !recipeResult.isEmpty() && canInsertOutput(outputSlot, recipeResult)) {
            entity.cookTime++;

            if (entity.cookTime >= entity.totalCookTime) {
                input.decrement(1);

                // Multiplicar lingotes según configuración
                com.chainores.config.ChainOresConfig cfg = com.chainores.config.ChainOresConfig.get();
                int yield = cfg.smelterYield;
                ItemStack out = recipeResult.copy();

                // Probabilidad configurable de transmutar al tier superior (bloqueado en hitos de jefes)
                if (world.random.nextFloat() < cfg.smelterTransmutationChance) {
                    for (int i = 0; i < OreRegistryData.TIERS.size() - 1; i++) {
                        if (recipeResult.isOf(OreRegistryData.TIERS.get(i).ingotItem)) {
                            int currentTier = OreRegistryData.TIERS.get(i).tier;
                            // Bloquear transmutación automática en los hitos que exigen catalizador de jefe
                            boolean isBossMilestone = cfg.smelterLockBossMilestones &&
                                    (currentTier == 70 || currentTier == 105 || currentTier == 135 || currentTier >= 149);

                            if (!isBossMilestone) {
                                out = new ItemStack(OreRegistryData.TIERS.get(i + 1).ingotItem);
                                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.2f, 1.8f);
                                ((ServerWorld) world).spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.1);
                            }
                            break;
                        }
                    }
                }

                if (outputSlot.isEmpty()) {
                    out.setCount(yield);
                    entity.inventory.set(OUTPUT_SLOT, out);
                } else if (ItemStack.areItemsAndComponentsEqual(outputSlot, out)) {
                    outputSlot.increment(yield);
                }

                entity.cookTime = 0;
                dirty = true;
                world.playSound(null, pos, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 0.8f, 1.2f);
            }
        } else {
            if (entity.cookTime > 0) {
                entity.cookTime = Math.max(0, entity.cookTime - 2);
                dirty = true;
            }
        }

        boolean isBurning = entity.burnTime > 0;
        if (wasBurning != isBurning) {
            dirty = true;
            world.setBlockState(pos, state.with(CosmicSmelterBlock.LIT, isBurning), 3);
        }

        if (dirty) {
            markDirty(world, pos, state);
        }
    }

    private static int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (fuel.isOf(Items.DRAGON_BREATH)) return 3000;
        if (fuel.isOf(Items.LAVA_BUCKET)) return 2000;
        if (fuel.isOf(Items.BLAZE_ROD)) return 400;
        if (fuel.isOf(Items.COAL) || fuel.isOf(Items.CHARCOAL)) return 200;
        return 0;
    }

    private static boolean canInsertOutput(ItemStack outputSlot, ItemStack result) {
        if (outputSlot.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(outputSlot, result)) return false;
        int yield = com.chainores.config.ChainOresConfig.get().smelterYield;
        return outputSlot.getCount() + yield <= outputSlot.getMaxCount();
    }

    private static ItemStack getSmeltingResult(ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;

        for (OreTier ore : OreRegistryData.TIERS) {
            if (input.isOf(ore.rawItem) || input.isOf(ore.oreBlockItem)) {
                return new ItemStack(ore.ingotItem);
            }
        }

        if (input.isOf(Items.RAW_IRON) || input.isOf(Items.IRON_ORE) || input.isOf(Items.DEEPSLATE_IRON_ORE)) {
            return new ItemStack(Items.IRON_INGOT);
        }
        if (input.isOf(Items.RAW_GOLD) || input.isOf(Items.GOLD_ORE) || input.isOf(Items.DEEPSLATE_GOLD_ORE)) {
            return new ItemStack(Items.GOLD_INGOT);
        }
        if (input.isOf(Items.RAW_COPPER) || input.isOf(Items.COPPER_ORE) || input.isOf(Items.DEEPSLATE_COPPER_ORE)) {
            return new ItemStack(Items.COPPER_INGOT);
        }
        if (input.isOf(Items.ANCIENT_DEBRIS)) {
            return new ItemStack(Items.NETHERITE_SCRAP);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        if (side == Direction.UP) {
            return TOP_SLOTS;
        }
        return SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == FUEL_SLOT) {
            return getFuelTime(stack) > 0;
        }
        return slot == INPUT_SLOT;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == OUTPUT_SLOT) {
            return true;
        }
        if (dir == Direction.DOWN && slot == FUEL_SLOT) {
            return stack.isOf(Items.BUCKET);
        }
        return false;
    }
}
