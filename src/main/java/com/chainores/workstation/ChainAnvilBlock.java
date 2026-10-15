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

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack held = player.getMainHandStack();

        if (held.isEmpty()) {
            if (!world.isClient()) {
                player.sendMessage(Text.literal("ℹ Sostén una herramienta o armadura de Chain Ores para reparar o templar.")
                        .formatted(Formatting.YELLOW), true);
            }
            return ActionResult.SUCCESS;
        }

        int tier = getGearTier(held.getItem());

        if (tier > 0 || held.getItem() instanceof GodOmnitoolItem) {
            if (world.isClient()) return ActionResult.SUCCESS;

            ServerWorld serverWorld = (ServerWorld) world;

            // Scenario 1: Gem Socketing (if holding a gem in offhand)
            ItemStack offhand = player.getOffHandStack();
            if (!offhand.isEmpty()) {
                String gemId = null;
                if (offhand.isOf(com.chainores.registry.ModGems.BLOOD_GEM)) gemId = com.chainores.modifier.ChainModifiers.GEM_BLOOD;
                else if (offhand.isOf(com.chainores.registry.ModGems.VOID_GEM)) gemId = com.chainores.modifier.ChainModifiers.GEM_VOID;
                else if (offhand.isOf(com.chainores.registry.ModGems.CORE_GEM)) gemId = com.chainores.modifier.ChainModifiers.GEM_CORE;
                else if (offhand.isOf(com.chainores.registry.ModGems.GENESIS_GEM)) gemId = com.chainores.modifier.ChainModifiers.GEM_GENESIS;

                if (gemId != null) {
                    int maxSockets = com.chainores.modifier.ChainModifiers.getMaxSockets(held);
                    if (maxSockets <= 0) {
                        player.sendMessage(Text.literal("✘ Este equipamiento es demasiado frágil para soportar ranuras de gemas.")
                                .formatted(Formatting.RED), true);
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        return ActionResult.SUCCESS;
                    }
                    if (!com.chainores.modifier.ChainModifiers.canAddGem(held)) {
                        player.sendMessage(Text.literal("✘ Todas las ranuras de gemas están llenas (" + maxSockets + "/" + maxSockets + ").")
                                .formatted(Formatting.RED), true);
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        return ActionResult.SUCCESS;
                    }

                    com.chainores.modifier.ChainModifiers.addGem(held, gemId);
                    if (!player.getAbilities().creativeMode) offhand.decrement(1);

                    int currentCount = com.chainores.modifier.ChainModifiers.getGems(held).size();
                    world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.2f, 1.3f);
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                    serverWorld.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.1);

                    player.sendMessage(Text.literal("◆ ¡Gema engarzada con éxito! Ranuras: [" + currentCount + "/" + maxSockets + "]")
                            .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), true);
                    return ActionResult.SUCCESS;
                }
            }

            // Scenario 2: Reforging (Shift-click or holding Amethyst Shard / Lapis Lazuli)
            boolean hasMaterial = (!offhand.isEmpty() && (offhand.isOf(Items.AMETHYST_SHARD) || offhand.isOf(Items.LAPIS_LAZULI)));
            int invSlot = findSlotWithItem(player.getInventory(), Items.AMETHYST_SHARD);
            if (invSlot == -1) invSlot = findSlotWithItem(player.getInventory(), Items.LAPIS_LAZULI);

            if (player.isSneaking() || hasMaterial) {
                if (!player.getAbilities().creativeMode) {
                    if (hasMaterial) {
                        offhand.decrement(1);
                    } else if (invSlot != -1) {
                        player.getInventory().getStack(invSlot).decrement(1);
                    } else {
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        player.sendMessage(Text.literal("✘ Para reforjar necesitas 1 Fragmento de Amatista o Lapislázuli.").formatted(Formatting.RED), true);
                        return ActionResult.SUCCESS;
                    }
                }
                String newReforge = com.chainores.modifier.ChainModifiers.rollRandomReforge(world.random);
                com.chainores.modifier.ChainModifiers.setReforge(held, newReforge);

                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.2f, 1.2f);
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 0.9f, 1.1f);
                serverWorld.spawnParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.1);

                player.sendMessage(Text.literal("⚡ ¡Arma reforjada! Nuevo prefijo: [" + newReforge.toUpperCase() + "]").formatted(Formatting.GOLD, Formatting.BOLD), true);
                return ActionResult.SUCCESS;
            }

            // Scenario 3: Damaged item -> Repair
            if (held.isDamaged()) {
                Item requiredIngot = getRequiredIngot(tier);
                int slot = findSlotWithItem(player.getInventory(), requiredIngot);

                if (slot != -1) {
                    player.getInventory().getStack(slot).decrement(1);
                    held.setDamage(0);

                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.1f);
                    world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.8f, 1.3f);
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);

                    player.sendMessage(Text.literal("✔ ¡Ítem reparado al 100% en el Yunque en Cadena!").formatted(Formatting.GREEN, Formatting.BOLD), true);
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    player.sendMessage(Text.literal("✘ Requiere 1 " + requiredIngot.getName().getString() + " para reparar.").formatted(Formatting.RED), true);
                }
                return ActionResult.SUCCESS;
            }

            // Scenario 4: Undamaged item -> Temple / Sharpening Buff
            int lapisSlot = findSlotWithItem(player.getInventory(), Items.LAPIS_LAZULI);
            int redstoneSlot = findSlotWithItem(player.getInventory(), Items.REDSTONE);

            if (lapisSlot != -1 || redstoneSlot != -1) {
                if (lapisSlot != -1) player.getInventory().getStack(lapisSlot).decrement(1);
                else player.getInventory().getStack(redstoneSlot).decrement(1);

                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 1, false, true)); // 10 min
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 12000, 1, false, true));

                world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                serverWorld.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.2);

                player.sendMessage(Text.literal("★ ¡Templado de Yunque Activo: Fuerza II y Prisa II por 10 min!").formatted(Formatting.GOLD, Formatting.BOLD), true);
            } else {
                player.sendMessage(Text.literal("ℹ Usa Shift para Reforjar, sostén una Gema en mano secundaria para engarzar, o Lapis/Redstone para templar.").formatted(Formatting.AQUA), true);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private int getGearTier(Item item) {
        if (item instanceof ChainSwordItem sword) return sword.getTier();
        if (item instanceof ChainPickaxeItem pickaxe) return pickaxe.getTier();
        if (item instanceof ChainAxeItem axe) return axe.getTier();
        if (item instanceof ChainShovelItem shovel) return shovel.getTier();
        if (item instanceof ChainHoeItem hoe) return hoe.getTier();
        if (item instanceof ChainArmorItem armor) return armor.getTier();
        if (item instanceof GodOmnitoolItem) return 150;
        return 0;
    }

    private Item getRequiredIngot(int tier) {
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
