package com.chainores.workstation;

import com.chainores.gear.*;
import com.chainores.modifier.ChainModifiers;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModGems;
import com.chainores.registry.ModItems;
import com.chainores.registry.ModWorkstations;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChainForgeBlockEntity extends LockableContainerBlockEntity {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    public static final int INPUT_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private int forgeProgress = 0;
    private final int totalForgeTime = 15; // 0.75 seconds for fast, responsive feedback

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

        ItemStack slot0 = entity.inventory.get(INPUT_SLOT);
        ItemStack slot1 = entity.inventory.get(CATALYST_SLOT);
        ItemStack outputSlot = entity.inventory.get(OUTPUT_SLOT);

        ItemStack recipeResult = entity.calculateForgeResult(slot0, slot1);

        if (!recipeResult.isEmpty() && canInsertOutput(outputSlot, recipeResult)) {
            entity.forgeProgress++;

            if (entity.forgeProgress % 6 == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.6f, 1.3f);
            }

            if (entity.forgeProgress >= entity.totalForgeTime) {
                // Craft item: decrement 1 from both input slots
                slot0.decrement(1);
                slot1.decrement(1);

                if (outputSlot.isEmpty()) {
                    entity.inventory.set(OUTPUT_SLOT, recipeResult.copy());
                } else if (ItemStack.areItemsAndComponentsEqual(outputSlot, recipeResult)) {
                    outputSlot.increment(recipeResult.getCount());
                }

                entity.forgeProgress = 0;
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.85f, 1.2f);
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.7f, 1.5f);
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

    public ItemStack calculateForgeResult(ItemStack slotA, ItemStack slotB) {
        if (slotA.isEmpty() || slotB.isEmpty()) return ItemStack.EMPTY;

        // Try both directions so slots 0 and 1 are 100% interchangeable
        ItemStack res = evaluateRecipe(slotA, slotB);
        if (!res.isEmpty()) return res;
        return evaluateRecipe(slotB, slotA);
    }

    private ItemStack evaluateRecipe(ItemStack input, ItemStack catalyst) {
        boolean isTemplate = catalyst.isOf(ModItems.CHAIN_SMITHING_TEMPLATE);
        OreTier t1 = OreRegistryData.TIERS.get(0); // T1 Copperite

        // -------------------------------------------------------------
        // 0. Template Duplication
        // -------------------------------------------------------------
        if (input.isOf(ModItems.CHAIN_SMITHING_TEMPLATE) && (catalyst.isOf(Items.DIAMOND) || catalyst.isOf(Items.COPPER_INGOT) || catalyst.isOf(t1.ingotItem))) {
            return new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 2);
        }

        // -------------------------------------------------------------
        // 1. Vanilla Copper / Raw Copper -> Tier 1 Copperite Ingot
        // -------------------------------------------------------------
        if (input.isOf(Items.COPPER_INGOT) || input.isOf(Items.RAW_COPPER)) {
            if (isTemplate || catalyst.isOf(Items.COPPER_INGOT) || catalyst.isOf(Items.RAW_COPPER) ||
                    catalyst.isOf(t1.rawItem) || catalyst.isOf(t1.ingotItem)) {
                return new ItemStack(t1.ingotItem);
            }
        }

        // -------------------------------------------------------------
        // 2. Ingot Upgrades (Tier N -> Tier N+1)
        // -------------------------------------------------------------
        for (int i = 0; i < OreRegistryData.TIERS.size(); i++) {
            OreTier ore = OreRegistryData.TIERS.get(i);
            if (input.isOf(ore.ingotItem)) {
                int nextIndex = i + 1;
                if (nextIndex < OreRegistryData.TIERS.size()) {
                    OreTier nextOre = OreRegistryData.TIERS.get(nextIndex);

                    // Boss dimension gates
                    if (i == 69) { // T70 -> T71 requires Abyssal Catalyst
                        if (catalyst.isOf(ModBossItems.ABYSSAL_CATALYST)) {
                            return new ItemStack(nextOre.ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 104) { // T105 -> T106 requires Nether Core
                        if (catalyst.isOf(ModBossItems.NETHER_CORE)) {
                            return new ItemStack(nextOre.ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 134) { // T135 -> T136 requires Void Shard
                        if (catalyst.isOf(ModBossItems.VOID_SHARD)) {
                            return new ItemStack(nextOre.ingotItem);
                        }
                        return ItemStack.EMPTY;
                    } else if (i == 148) { // T149 -> T150 requires Cosmic Singularity
                        if (catalyst.isOf(ModBossItems.COSMIC_SINGULARITY)) {
                            return new ItemStack(nextOre.ingotItem);
                        }
                        return ItemStack.EMPTY;
                    }

                    // Flexible catalysts: Raw ore (guide book), template, duplicate ingot, or next ingot
                    if (isTemplate || catalyst.isOf(nextOre.rawItem) || catalyst.isOf(ore.ingotItem) || catalyst.isOf(nextOre.ingotItem)) {
                        return new ItemStack(nextOre.ingotItem);
                    }
                }
            }

            // Raw Ore in Forge -> Ingot or upgraded Ingot
            if (input.isOf(ore.rawItem)) {
                if (catalyst.isOf(ore.rawItem) || isTemplate) {
                    return new ItemStack(ore.ingotItem);
                }
                int nextIndex = i + 1;
                if (nextIndex < OreRegistryData.TIERS.size()) {
                    OreTier nextOre = OreRegistryData.TIERS.get(nextIndex);
                    if (catalyst.isOf(nextOre.rawItem) && i != 69 && i != 104 && i != 134 && i != 148) {
                        return new ItemStack(nextOre.ingotItem);
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. Equipment Creation from Vanilla Gear (All 16 Gear Sets)
        // -------------------------------------------------------------
        for (int i = 0; i < ModGear.GEAR_SETS.size(); i++) {
            ModGear.GearSet targetSet = ModGear.GEAR_SETS.get(i);
            OreTier targetOre = OreRegistryData.TIERS.get(targetSet.tierData.tier() - 1);
            boolean validCatalyst = (i == 0 && isTemplate) || catalyst.isOf(targetOre.ingotItem) || catalyst.isOf(targetOre.rawItem);

            if (validCatalyst) {
                int targetTier = targetSet.tierData.tier();
                // Check boss gates if crafting higher tier from scratch
                if (targetTier > 70 && !hasBossToken(catalyst, targetTier)) {
                    // Requires boss catalyst
                } else {
                    if (isVanillaSword(input.getItem())) return transferGearData(input, new ItemStack(targetSet.sword));
                    if (isVanillaPickaxe(input.getItem())) return transferGearData(input, new ItemStack(targetSet.pickaxe));
                    if (isVanillaAxe(input.getItem())) return transferGearData(input, new ItemStack(targetSet.axe));
                    if (isVanillaShovel(input.getItem())) return transferGearData(input, new ItemStack(targetSet.shovel));
                    if (isVanillaHoe(input.getItem())) return transferGearData(input, new ItemStack(targetSet.hoe));
                    if (isVanillaHelmet(input.getItem())) return transferGearData(input, new ItemStack(targetSet.helmet));
                    if (isVanillaChestplate(input.getItem())) return transferGearData(input, new ItemStack(targetSet.chestplate));
                    if (isVanillaLeggings(input.getItem())) return transferGearData(input, new ItemStack(targetSet.leggings));
                    if (isVanillaBoots(input.getItem())) return transferGearData(input, new ItemStack(targetSet.boots));
                }
            }
        }

        // -------------------------------------------------------------
        // 4. Weapon & Tool Upgrades Between Mod Gear Sets
        // -------------------------------------------------------------
        for (int i = 0; i < ModGear.GEAR_SETS.size() - 1; i++) {
            ModGear.GearSet current = ModGear.GEAR_SETS.get(i);
            ModGear.GearSet next = ModGear.GEAR_SETS.get(i + 1);
            OreTier nextOre = OreRegistryData.TIERS.get(next.tierData.tier() - 1);

            int nextTier = next.tierData.tier();
            if (nextTier > 70 && current.tierData.tier() <= 70) {
                if (!catalyst.isOf(ModBossItems.ABYSSAL_CATALYST)) continue;
            } else if (nextTier > 105 && current.tierData.tier() <= 105) {
                if (!catalyst.isOf(ModBossItems.NETHER_CORE)) continue;
            } else if (nextTier > 135 && current.tierData.tier() <= 135) {
                if (!catalyst.isOf(ModBossItems.VOID_SHARD)) continue;
            } else if (nextTier == 150) {
                if (!catalyst.isOf(ModBossItems.COSMIC_SINGULARITY)) continue;
            } else {
                boolean validCatalyst = isTemplate || catalyst.isOf(nextOre.ingotItem) || catalyst.isOf(nextOre.rawItem);
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

        // Direct leap to higher milestone with target tier's ingot
        for (int i = 0; i < ModGear.GEAR_SETS.size(); i++) {
            ModGear.GearSet target = ModGear.GEAR_SETS.get(i);
            OreTier targetOre = OreRegistryData.TIERS.get(target.tierData.tier() - 1);
            if (catalyst.isOf(targetOre.ingotItem)) {
                if (input.getItem() instanceof ChainSwordItem && !input.isOf(target.sword)) return transferGearData(input, new ItemStack(target.sword));
                if (input.getItem() instanceof ChainPickaxeItem && !input.isOf(target.pickaxe)) return transferGearData(input, new ItemStack(target.pickaxe));
                if (input.getItem() instanceof ChainAxeItem && !input.isOf(target.axe)) return transferGearData(input, new ItemStack(target.axe));
                if (input.getItem() instanceof ChainShovelItem && !input.isOf(target.shovel)) return transferGearData(input, new ItemStack(target.shovel));
                if (input.getItem() instanceof ChainHoeItem && !input.isOf(target.hoe)) return transferGearData(input, new ItemStack(target.hoe));
                if (input.getItem() instanceof ChainArmorItem armor) {
                    if (isHelmet(armor) && !input.isOf(target.helmet)) return transferGearData(input, new ItemStack(target.helmet));
                    if (isChestplate(armor) && !input.isOf(target.chestplate)) return transferGearData(input, new ItemStack(target.chestplate));
                    if (isLeggings(armor) && !input.isOf(target.leggings)) return transferGearData(input, new ItemStack(target.leggings));
                    if (isBoots(armor) && !input.isOf(target.boots)) return transferGearData(input, new ItemStack(target.boots));
                }
            }
        }

        // -------------------------------------------------------------
        // 5. Legendary Weapons & Shields
        // -------------------------------------------------------------
        // Bows
        if (input.isOf(Items.BOW) && (catalyst.isOf(OreRegistryData.TIERS.get(114).ingotItem) || catalyst.isOf(ModBossItems.NETHER_CORE) || isTemplate)) {
            return transferGearData(input, new ItemStack(ModGear.DRAGON_BOW));
        }
        // Crossbows
        if (input.isOf(Items.CROSSBOW) && (catalyst.isOf(OreRegistryData.TIERS.get(124).ingotItem) || catalyst.isOf(ModBossItems.VOID_SHARD) || isTemplate)) {
            return transferGearData(input, new ItemStack(ModGear.GALACTIC_CROSSBOW));
        }

        // Shields
        if (input.isOf(Items.SHIELD)) {
            if (catalyst.isOf(ModBossItems.COSMIC_SINGULARITY) || catalyst.isOf(OreRegistryData.TIERS.get(149).ingotItem)) return transferGearData(input, new ItemStack(ModGear.COSMIC_SHIELD));
            if (catalyst.isOf(ModBossItems.NETHER_CORE)) return transferGearData(input, new ItemStack(ModGear.INFERNAL_BULWARK));
            if (catalyst.isOf(ModBossItems.ABYSSAL_CATALYST)) return transferGearData(input, new ItemStack(ModGear.ABYSSAL_AEGIS));
            if (catalyst.isOf(OreRegistryData.TIERS.get(99).ingotItem)) return transferGearData(input, new ItemStack(ModGear.AEGIS_SHIELD));
            if (catalyst.isOf(OreRegistryData.TIERS.get(19).ingotItem)) return transferGearData(input, new ItemStack(ModGear.FROSTGUARD_SHIELD));
            if (catalyst.isOf(OreRegistryData.TIERS.get(9).ingotItem) || catalyst.isOf(t1.ingotItem) || isTemplate) return transferGearData(input, new ItemStack(ModGear.SENTINEL_SHIELD));
        }
        if (input.isOf(ModGear.SENTINEL_SHIELD) && (catalyst.isOf(OreRegistryData.TIERS.get(19).ingotItem) || isTemplate)) {
            return transferGearData(input, new ItemStack(ModGear.FROSTGUARD_SHIELD));
        }
        if (input.isOf(ModGear.FROSTGUARD_SHIELD) && (catalyst.isOf(ModBossItems.ABYSSAL_CATALYST) || catalyst.isOf(OreRegistryData.TIERS.get(69).ingotItem))) {
            return transferGearData(input, new ItemStack(ModGear.ABYSSAL_AEGIS));
        }
        if (input.isOf(ModGear.ABYSSAL_AEGIS) && (catalyst.isOf(ModBossItems.NETHER_CORE) || catalyst.isOf(OreRegistryData.TIERS.get(99).ingotItem))) {
            return transferGearData(input, new ItemStack(ModGear.INFERNAL_BULWARK));
        }
        if (input.isOf(ModGear.INFERNAL_BULWARK) && (catalyst.isOf(OreRegistryData.TIERS.get(99).ingotItem) || isTemplate)) {
            return transferGearData(input, new ItemStack(ModGear.AEGIS_SHIELD));
        }
        if ((input.isOf(ModGear.AEGIS_SHIELD) || input.isOf(ModGear.INFERNAL_BULWARK)) && (catalyst.isOf(ModBossItems.COSMIC_SINGULARITY) || catalyst.isOf(OreRegistryData.TIERS.get(149).ingotItem))) {
            return transferGearData(input, new ItemStack(ModGear.COSMIC_SHIELD));
        }

        // God Omnitool
        if (catalyst.isOf(ModBossItems.COSMIC_SINGULARITY)) {
            ModGear.GearSet godSet = ModGear.GEAR_SETS.get(ModGear.GEAR_SETS.size() - 1);
            if (input.isOf(godSet.sword) || input.isOf(godSet.pickaxe) || input.isOf(godSet.axe) || input.isOf(godSet.shovel) || input.isOf(godSet.hoe)) {
                return transferGearData(input, new ItemStack(ModGear.GOD_OMNITOOL));
            }
        }

        // -------------------------------------------------------------
        // 6. Gem Socketing in the Forge
        // -------------------------------------------------------------
        if (ChainModifiers.isSocketableItem(input)) {
            if (catalyst.isOf(ModGems.BLOOD_GEM) && ChainModifiers.canAddGem(input)) {
                ItemStack out = input.copy();
                ChainModifiers.addGem(out, ChainModifiers.GEM_BLOOD);
                return out;
            }
            if (catalyst.isOf(ModGems.VOID_GEM) && ChainModifiers.canAddGem(input)) {
                ItemStack out = input.copy();
                ChainModifiers.addGem(out, ChainModifiers.GEM_VOID);
                return out;
            }
            if (catalyst.isOf(ModGems.CORE_GEM) && ChainModifiers.canAddGem(input)) {
                ItemStack out = input.copy();
                ChainModifiers.addGem(out, ChainModifiers.GEM_CORE);
                return out;
            }
            if (catalyst.isOf(ModGems.GENESIS_GEM) && ChainModifiers.canAddGem(input)) {
                ItemStack out = input.copy();
                ChainModifiers.addGem(out, ChainModifiers.GEM_GENESIS);
                return out;
            }
        }

        // -------------------------------------------------------------
        // 7. Reforging & Repairing in the Forge
        // -------------------------------------------------------------
        if (isGear(input.getItem())) {
            // Reforging with Amethyst, Lapis, or Diamond
            if (catalyst.isOf(Items.AMETHYST_SHARD) || catalyst.isOf(Items.LAPIS_LAZULI) || catalyst.isOf(Items.DIAMOND)) {
                ItemStack out = input.copy();
                String rolled = ChainModifiers.rollRandomReforge(Random.create());
                ChainModifiers.setReforge(out, rolled);
                return out;
            }

            // Repairing damaged gear with matching ingot or template
            if (input.isDamaged()) {
                for (ModGear.GearSet set : ModGear.GEAR_SETS) {
                    if (isItemInGearSet(input.getItem(), set)) {
                        OreTier ore = OreRegistryData.TIERS.get(set.tierData.tier() - 1);
                        if (catalyst.isOf(ore.ingotItem) || catalyst.isOf(ore.rawItem) || isTemplate) {
                            ItemStack repaired = input.copy();
                            repaired.setDamage(0);
                            return repaired;
                        }
                    }
                }
                // Repair special weapons/shields
                if (isSpecialWeapon(input.getItem()) && (isTemplate || catalyst.isOf(Items.DIAMOND) || catalyst.isOf(Items.NETHERITE_INGOT))) {
                    ItemStack repaired = input.copy();
                    repaired.setDamage(0);
                    return repaired;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean hasBossToken(ItemStack catalyst, int tier) {
        if (tier <= 70) return true;
        if (tier <= 105) return catalyst.isOf(ModBossItems.ABYSSAL_CATALYST);
        if (tier <= 135) return catalyst.isOf(ModBossItems.NETHER_CORE);
        return catalyst.isOf(ModBossItems.COSMIC_SINGULARITY);
    }

    private static ItemStack transferGearData(ItemStack input, ItemStack output) {
        if (input.isEmpty() || output.isEmpty()) return output;

        if (input.contains(DataComponentTypes.CUSTOM_DATA)) {
            output.set(DataComponentTypes.CUSTOM_DATA, input.get(DataComponentTypes.CUSTOM_DATA));
        }

        if (input.contains(DataComponentTypes.CUSTOM_NAME)) {
            output.set(DataComponentTypes.CUSTOM_NAME, input.get(DataComponentTypes.CUSTOM_NAME));
        }

        if (input.contains(DataComponentTypes.ENCHANTMENTS)) {
            output.set(DataComponentTypes.ENCHANTMENTS, input.get(DataComponentTypes.ENCHANTMENTS));
        }

        return output;
    }

    private boolean isItemInGearSet(Item item, ModGear.GearSet set) {
        return item == set.sword || item == set.pickaxe || item == set.axe ||
                item == set.shovel || item == set.hoe || item == set.helmet ||
                item == set.chestplate || item == set.leggings || item == set.boots;
    }

    private boolean isSpecialWeapon(Item item) {
        return item == ModGear.DRAGON_BOW || item == ModGear.GALACTIC_CROSSBOW ||
                item == ModGear.AEGIS_SHIELD || item == ModGear.SENTINEL_SHIELD ||
                item == ModGear.FROSTGUARD_SHIELD || item == ModGear.ABYSSAL_AEGIS ||
                item == ModGear.INFERNAL_BULWARK || item == ModGear.COSMIC_SHIELD ||
                item == ModGear.GOD_OMNITOOL;
    }

    private boolean isHelmet(ChainArmorItem armor) {
        return armor.getEquipmentType() == net.minecraft.item.equipment.EquipmentType.HELMET;
    }

    private boolean isChestplate(ChainArmorItem armor) {
        return armor.getEquipmentType() == net.minecraft.item.equipment.EquipmentType.CHESTPLATE;
    }

    private boolean isLeggings(ChainArmorItem armor) {
        return armor.getEquipmentType() == net.minecraft.item.equipment.EquipmentType.LEGGINGS;
    }

    private boolean isBoots(ChainArmorItem armor) {
        return armor.getEquipmentType() == net.minecraft.item.equipment.EquipmentType.BOOTS;
    }

    private boolean isVanillaSword(Item item) {
        return item == Items.IRON_SWORD || item == Items.DIAMOND_SWORD ||
                item == Items.NETHERITE_SWORD || item == Items.GOLDEN_SWORD ||
                item == Items.STONE_SWORD || item == Items.WOODEN_SWORD;
    }

    private boolean isVanillaPickaxe(Item item) {
        return item == Items.IRON_PICKAXE || item == Items.DIAMOND_PICKAXE ||
                item == Items.NETHERITE_PICKAXE || item == Items.GOLDEN_PICKAXE ||
                item == Items.STONE_PICKAXE || item == Items.WOODEN_PICKAXE;
    }

    private boolean isVanillaAxe(Item item) {
        return item == Items.IRON_AXE || item == Items.DIAMOND_AXE ||
                item == Items.NETHERITE_AXE || item == Items.GOLDEN_AXE ||
                item == Items.STONE_AXE || item == Items.WOODEN_AXE;
    }

    private boolean isVanillaShovel(Item item) {
        return item == Items.IRON_SHOVEL || item == Items.DIAMOND_SHOVEL ||
                item == Items.NETHERITE_SHOVEL || item == Items.GOLDEN_SHOVEL ||
                item == Items.STONE_SHOVEL || item == Items.WOODEN_SHOVEL;
    }

    private boolean isVanillaHoe(Item item) {
        return item == Items.IRON_HOE || item == Items.DIAMOND_HOE ||
                item == Items.NETHERITE_HOE || item == Items.GOLDEN_HOE ||
                item == Items.STONE_HOE || item == Items.WOODEN_HOE;
    }

    private boolean isVanillaHelmet(Item item) {
        return item == Items.IRON_HELMET || item == Items.DIAMOND_HELMET ||
                item == Items.NETHERITE_HELMET || item == Items.GOLDEN_HELMET ||
                item == Items.CHAINMAIL_HELMET || item == Items.LEATHER_HELMET;
    }

    private boolean isVanillaChestplate(Item item) {
        return item == Items.IRON_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE || item == Items.GOLDEN_CHESTPLATE ||
                item == Items.CHAINMAIL_CHESTPLATE || item == Items.LEATHER_CHESTPLATE;
    }

    private boolean isVanillaLeggings(Item item) {
        return item == Items.IRON_LEGGINGS || item == Items.DIAMOND_LEGGINGS ||
                item == Items.NETHERITE_LEGGINGS || item == Items.GOLDEN_LEGGINGS ||
                item == Items.CHAINMAIL_LEGGINGS || item == Items.LEATHER_LEGGINGS;
    }

    private boolean isVanillaBoots(Item item) {
        return item == Items.IRON_BOOTS || item == Items.DIAMOND_BOOTS ||
                item == Items.NETHERITE_BOOTS || item == Items.GOLDEN_BOOTS ||
                item == Items.CHAINMAIL_BOOTS || item == Items.LEATHER_BOOTS;
    }

    private boolean isGear(Item item) {
        return item instanceof ChainSwordItem || item instanceof ChainPickaxeItem || item instanceof ChainAxeItem ||
                item instanceof ChainShovelItem || item instanceof ChainHoeItem || item instanceof ChainArmorItem ||
                item instanceof GodOmnitoolItem || item instanceof ChainShieldItem || isSpecialWeapon(item);
    }
}
