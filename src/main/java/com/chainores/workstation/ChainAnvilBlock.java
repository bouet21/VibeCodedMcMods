package com.chainores.workstation;

import com.chainores.gear.ChainArmorItem;
import com.chainores.gear.ChainAxeItem;
import com.chainores.gear.ChainHoeItem;
import com.chainores.gear.ChainPickaxeItem;
import com.chainores.gear.ChainShovelItem;
import com.chainores.gear.ChainSwordItem;
import com.chainores.gear.GodOmnitoolItem;
import com.chainores.ore.OreRegistryData;
import com.chainores.ore.OreTier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChainAnvilBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
    private static final VoxelShape MID_SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
    private static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
    private static final VoxelShape ANVIL_SHAPE = VoxelShapes.union(BASE_SHAPE, MID_SHAPE, TOP_SHAPE);

    public ChainAnvilBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().rotateYClockwise());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ANVIL_SHAPE;
    }

    public static void registerInteractionCallback() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ChainAnvilBlock anvil) {
                return anvil.handleAnvilInteraction(world, pos, player, hand);
            }
            return ActionResult.PASS;
        });
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return handleAnvilInteraction(world, pos, player, hand);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return handleAnvilInteraction(world, pos, player, Hand.MAIN_HAND);
    }

    public ActionResult handleAnvilInteraction(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack mainStack = player.getMainHandStack();
        ItemStack offStack = player.getOffHandStack();

        // Determine which stack is gear and which is material/catalyst
        ItemStack gearStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;
        boolean gearInMain = false;

        if (com.chainores.modifier.ChainModifiers.isReforgableGear(mainStack)) {
            gearStack = mainStack;
            materialStack = offStack;
            gearInMain = true;
        } else if (com.chainores.modifier.ChainModifiers.isReforgableGear(offStack)) {
            gearStack = offStack;
            materialStack = mainStack;
            gearInMain = false;
        }

        if (gearStack.isEmpty()) {
            // Check for Gem Fusion (Combining 2 identical gems to upgrade tier)
            if (mainStack.getItem() instanceof com.chainores.item.GemItem) {
                boolean canFuse = (mainStack.getCount() >= 2) || (offStack.isOf(mainStack.getItem()));
                Item nextGem = com.chainores.registry.ModGems.getFusedGem(mainStack.getItem());

                if (canFuse) {
                    if (nextGem != null) {
                        if (!world.isClient()) {
                            ServerWorld sw = (ServerWorld) world;
                            if (!player.getAbilities().creativeMode) {
                                if (mainStack.getCount() >= 2) {
                                    mainStack.decrement(2);
                                } else {
                                    mainStack.decrement(1);
                                    offStack.decrement(1);
                                }
                            }
                            player.getInventory().offerOrDrop(new ItemStack(nextGem, 1));
                            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.2f, 1.3f);
                            world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.5f, 1.5f);
                            sw.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.2);
                            sw.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.1);

                            player.sendMessage(Text.literal("★ ¡FUSIÓN DE GEMAS EXITOSA! Has creado: ").formatted(Formatting.GOLD, Formatting.BOLD)
                                    .append(nextGem.getName()), true);
                        }
                        return ActionResult.SUCCESS;
                    } else {
                        if (!world.isClient()) {
                            player.sendMessage(Text.literal("★ Esta gema ya ha alcanzado el Rango III (Máximo Rango Trascendente).").formatted(Formatting.YELLOW), true);
                            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }

            if (!world.isClient() && hand == Hand.MAIN_HAND) {
                player.sendMessage(Text.literal("ℹ YUNQUE EN CADENA:").formatted(Formatting.GOLD, Formatting.BOLD), false);
                player.sendMessage(Text.literal("  • Fusión de Gemas: Clic con 2 gemas iguales (en mano u offhand) para subir a Rango II o III").formatted(Formatting.LIGHT_PURPLE), false);
                player.sendMessage(Text.literal("  • Reforjar: Shift + Clic (con Amatista, Lapis o Diamante)").formatted(Formatting.AQUA), false);
                player.sendMessage(Text.literal("  • Engarzar: Clic con Gema en mano secundaria y arma/armadura/reliquia en principal").formatted(Formatting.GOLD), false);
                player.sendMessage(Text.literal("  • Reparar: Clic con ítem dañado y su lingote").formatted(Formatting.GREEN), false);
                player.sendMessage(Text.literal("  • Templar: Clic con Redstone o Lapis para Fuerza II y Prisa II (10m)").formatted(Formatting.YELLOW), false);
            }
            return ActionResult.SUCCESS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        // =========================================================================
        // 1. Gem Socketing (if holding a gem in offhand and main hand has socketable gear)
        // =========================================================================
        if (gearInMain && !offStack.isEmpty() && isGemItem(offStack.getItem())) {
            String gemId = getGemId(offStack.getItem());
            if (gemId != null) {
                int maxSockets = com.chainores.modifier.ChainModifiers.getMaxSockets(gearStack);
                if (maxSockets <= 0) {
                    player.sendMessage(Text.literal("✘ Este equipamiento no soporta ranuras de gemas.")
                            .formatted(Formatting.RED), true);
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
                if (!com.chainores.modifier.ChainModifiers.canAddGem(gearStack)) {
                    player.sendMessage(Text.literal("✘ Todas las ranuras de gemas están llenas (" + maxSockets + "/" + maxSockets + ").")
                            .formatted(Formatting.RED), true);
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }

                com.chainores.modifier.ChainModifiers.addGem(gearStack, gemId);
                if (!player.getAbilities().creativeMode) offStack.decrement(1);

                int currentCount = com.chainores.modifier.ChainModifiers.getGems(gearStack).size();
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.2f, 1.3f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                serverWorld.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.1);

                player.sendMessage(Text.literal("◆ ¡Gema engarzada con éxito! Ranuras: [" + currentCount + "/" + maxSockets + "]")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), true);
                return ActionResult.SUCCESS;
            }
        }

        // =========================================================================
        // 2. Reforging (Shift + Click, holding material, or undamaged click with amethyst)
        // =========================================================================
        boolean materialInHand = isReforgeMaterial(materialStack.getItem());
        int invReforgeSlot = -1;
        if (!materialInHand) {
            invReforgeSlot = findSlotWithItem(player.getInventory(), Items.AMETHYST_SHARD);
            if (invReforgeSlot == -1) invReforgeSlot = findSlotWithItem(player.getInventory(), Items.LAPIS_LAZULI);
            if (invReforgeSlot == -1) invReforgeSlot = findSlotWithItem(player.getInventory(), Items.DIAMOND);
        }

        boolean wantsReforge = player.isSneaking() || materialInHand || (!gearStack.isDamaged() && invReforgeSlot != -1 && findSlotWithItem(player.getInventory(), Items.REDSTONE) == -1);

        if (wantsReforge) {
            if (!materialInHand && invReforgeSlot == -1) {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                player.sendMessage(Text.literal("✘ Para reforjar necesitas 1 Fragmento de Amatista, Lapislázuli o Diamante.").formatted(Formatting.RED), true);
                return ActionResult.SUCCESS;
            }

            if (!player.getAbilities().creativeMode) {
                if (materialInHand) {
                    materialStack.decrement(1);
                } else {
                    player.getInventory().getStack(invReforgeSlot).decrement(1);
                }
            }

            String newReforge = com.chainores.modifier.ChainModifiers.rollRandomReforge(world.random);
            com.chainores.modifier.ChainModifiers.setReforge(gearStack, newReforge);

            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.2f, 1.2f);
            world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 0.9f, 1.1f);
            serverWorld.spawnParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 25, 0.4, 0.4, 0.4, 0.1);
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);

            Text reforgeName = com.chainores.modifier.ChainModifiers.getReforgeName(newReforge);
            player.sendMessage(Text.literal("⚡ ¡Ítem reforjado exitosamente! ").formatted(Formatting.GOLD, Formatting.BOLD)
                    .append(reforgeName), true);
            return ActionResult.SUCCESS;
        }

        // =========================================================================
        // 3. Repair Damaged Gear
        // =========================================================================
        if (gearStack.isDamaged()) {
            int tier = getGearTier(gearStack.getItem());
            Item requiredIngot = getRequiredIngot(gearStack.getItem(), tier);
            int slot = findSlotWithItem(player.getInventory(), requiredIngot);
            if (slot == -1 && (tier >= 70 || isSpecialGear(gearStack.getItem()))) {
                slot = findSlotWithItem(player.getInventory(), Items.DIAMOND);
                if (slot == -1) slot = findSlotWithItem(player.getInventory(), Items.NETHERITE_INGOT);
            }

            if (slot != -1) {
                if (!player.getAbilities().creativeMode) {
                    player.getInventory().getStack(slot).decrement(1);
                }
                gearStack.setDamage(0);

                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.1f);
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.8f, 1.3f);
                serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.1);

                player.sendMessage(Text.literal("✔ ¡Ítem reparado al 100% en el Yunque en Cadena!").formatted(Formatting.GREEN, Formatting.BOLD), true);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                player.sendMessage(Text.literal("✘ Requiere 1 " + requiredIngot.getName().getString() + " para reparar.").formatted(Formatting.RED), true);
            }
            return ActionResult.SUCCESS;
        }

        // =========================================================================
        // 4. Undamaged Gear -> Tempering Buff (Redstone or Lapis)
        // =========================================================================
        int redstoneSlot = findSlotWithItem(player.getInventory(), Items.REDSTONE);
        int lapisSlot = findSlotWithItem(player.getInventory(), Items.LAPIS_LAZULI);

        if (redstoneSlot != -1 || lapisSlot != -1) {
            if (!player.getAbilities().creativeMode) {
                if (redstoneSlot != -1) player.getInventory().getStack(redstoneSlot).decrement(1);
                else player.getInventory().getStack(lapisSlot).decrement(1);
            }

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 1, false, true)); // 10 min
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 12000, 1, false, true));

            world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
            serverWorld.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.2);

            player.sendMessage(Text.literal("★ ¡Templado de Yunque Activo: Fuerza II y Prisa II por 10 min!").formatted(Formatting.GOLD, Formatting.BOLD), true);
            return ActionResult.SUCCESS;
        }

        // =========================================================================
        // 5. Default Guide Message
        // =========================================================================
        player.sendMessage(Text.literal("ℹ Usa Shift para Reforjar, sostén una Gema en mano secundaria para engarzar, o Redstone para templar.").formatted(Formatting.AQUA), true);
        return ActionResult.SUCCESS;
    }

    private static boolean isReforgeMaterial(Item item) {
        return item == Items.AMETHYST_SHARD || item == Items.LAPIS_LAZULI || item == Items.DIAMOND;
    }

    private static boolean isGemItem(Item item) {
        return item instanceof com.chainores.item.GemItem;
    }

    private static String getGemId(Item item) {
        return com.chainores.registry.ModGems.getGemId(item);
    }

    private static boolean isSpecialGear(Item item) {
        return item == com.chainores.registry.ModGear.DRAGON_BOW ||
               item == com.chainores.registry.ModGear.GALACTIC_CROSSBOW ||
               item == com.chainores.registry.ModGear.SENTINEL_SHIELD ||
               item == com.chainores.registry.ModGear.FROSTGUARD_SHIELD ||
               item == com.chainores.registry.ModGear.ABYSSAL_AEGIS ||
               item == com.chainores.registry.ModGear.INFERNAL_BULWARK ||
               item == com.chainores.registry.ModGear.AEGIS_SHIELD ||
               item == com.chainores.registry.ModGear.COSMIC_SHIELD ||
               item == com.chainores.registry.ModGear.GOD_OMNITOOL;
    }

    private int getGearTier(Item item) {
        if (item instanceof ChainSwordItem sword) return sword.getTier();
        if (item instanceof ChainPickaxeItem pickaxe) return pickaxe.getTier();
        if (item instanceof ChainAxeItem axe) return axe.getTier();
        if (item instanceof ChainShovelItem shovel) return shovel.getTier();
        if (item instanceof ChainHoeItem hoe) return hoe.getTier();
        if (item instanceof ChainArmorItem armor) return armor.getTier();
        if (item instanceof com.chainores.gear.MiningExoskeletonItem) return 50;
        if (item instanceof com.chainores.gear.LumberjackHauberkItem) return 40;
        if (item instanceof com.chainores.gear.CosmicElytraChestplateItem) return 135;
        if (item instanceof GodOmnitoolItem) return 150;
        if (item == com.chainores.registry.ModGear.DRAGON_BOW) return 115;
        if (item == com.chainores.registry.ModGear.GALACTIC_CROSSBOW) return 125;
        if (item == com.chainores.registry.ModGear.SENTINEL_SHIELD) return 10;
        if (item == com.chainores.registry.ModGear.FROSTGUARD_SHIELD) return 20;
        if (item == com.chainores.registry.ModGear.ABYSSAL_AEGIS) return 70;
        if (item == com.chainores.registry.ModGear.INFERNAL_BULWARK) return 100;
        if (item == com.chainores.registry.ModGear.AEGIS_SHIELD) return 115;
        if (item == com.chainores.registry.ModGear.COSMIC_SHIELD) return 150;
        if (item == com.chainores.registry.ModItems.CHAIN_RING) return 30;
        if (item == com.chainores.registry.ModItems.CHAIN_AMULET) return 70;
        return 1;
    }

    private Item getRequiredIngot(Item item, int tier) {
        if (item == com.chainores.registry.ModItems.CHAIN_RING) return Items.GOLD_INGOT;
        if (item == com.chainores.registry.ModItems.CHAIN_AMULET) return Items.DIAMOND;
        if (item == com.chainores.registry.ModGear.DRAGON_BOW) return OreRegistryData.TIERS.get(114).ingotItem;
        if (item == com.chainores.registry.ModGear.GALACTIC_CROSSBOW) return OreRegistryData.TIERS.get(124).ingotItem;
        if (item == com.chainores.registry.ModGear.SENTINEL_SHIELD) return OreRegistryData.TIERS.get(9).ingotItem;
        if (item == com.chainores.registry.ModGear.FROSTGUARD_SHIELD) return OreRegistryData.TIERS.get(19).ingotItem;
        if (item == com.chainores.registry.ModGear.ABYSSAL_AEGIS) return OreRegistryData.TIERS.get(69).ingotItem;
        if (item == com.chainores.registry.ModGear.INFERNAL_BULWARK) return OreRegistryData.TIERS.get(99).ingotItem;
        if (item == com.chainores.registry.ModGear.AEGIS_SHIELD) return OreRegistryData.TIERS.get(114).ingotItem;
        if (item == com.chainores.registry.ModGear.COSMIC_SHIELD || item == com.chainores.registry.ModGear.GOD_OMNITOOL) {
            return OreRegistryData.TIERS.get(149).ingotItem;
        }

        if (tier <= 0) return Items.COPPER_INGOT;
        int clamped = Math.min(tier, 150) - 1;
        if (clamped < OreRegistryData.TIERS.size()) {
            return OreRegistryData.TIERS.get(clamped).ingotItem;
        }
        return OreRegistryData.TIERS.get(0).ingotItem;
    }

    private int findSlotWithItem(net.minecraft.entity.player.PlayerInventory inv, Item item) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(item)) {
                return i;
            }
        }
        return -1;
    }
}
