package com.chainores.workstation;

import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModWorkstations;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CosmicAltarBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape BASE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);
    private static final VoxelShape PILLAR = Block.createCuboidShape(3.0, 4.0, 3.0, 13.0, 12.0, 13.0);
    private static final VoxelShape TOP = Block.createCuboidShape(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape ALTAR_SHAPE = VoxelShapes.union(BASE, PILLAR, TOP);

    public CosmicAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ALTAR_SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CosmicAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof CosmicAltarBlockEntity altar) {
                CosmicAltarBlockEntity.tick(world1, pos, state1, altar);
            }
        };
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return handleInteraction(world, pos, player, Hand.MAIN_HAND);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return handleInteraction(world, pos, player, hand);
    }

    private ActionResult handleInteraction(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof CosmicAltarBlockEntity altar)) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);

        if (altar.isArenaActive()) {
            if (!world.isClient()) {
                player.sendMessage(Text.literal("⚔ El Desafío de las Olas Astrales ya está en curso. ¡Defiende tu posición!").formatted(Formatting.RED, Formatting.BOLD), true);
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(ModBossItems.COSMIC_TROPHY)) {
            if (!world.isClient()) {
                CosmicAltarBlockEntity.AltarDifficulty diff = player.isSneaking() 
                        ? CosmicAltarBlockEntity.AltarDifficulty.HARD 
                        : CosmicAltarBlockEntity.AltarDifficulty.NORMAL;
                boolean started = altar.startArena(player, diff);
                if (started) {
                    player.sendMessage(Text.literal("★ ¡Has consagrado el Trofeo Cósmico en el Altar en Modo " + diff.displayName + "!").formatted(diff.color, Formatting.BOLD), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        if (stack.isOf(ModBossItems.COSMIC_SINGULARITY)) {
            if (!world.isClient()) {
                boolean started = altar.startArena(player, CosmicAltarBlockEntity.AltarDifficulty.NIGHTMARE);
                if (started) {
                    player.sendMessage(Text.literal("☠ ¡HAS DESPERTADO EL MODO PESADILLA DEL ALTAR CÓSMICO! ☠").formatted(Formatting.RED, Formatting.BOLD), true);
                }
            }
            return ActionResult.SUCCESS;
        }

        if (!world.isClient() && hand == Hand.MAIN_HAND) {
            player.sendMessage(Text.literal("✦ ALTARES CÓSMICOS (DIFICULTAD ESCALABLE) ✦").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
            player.sendMessage(Text.literal("Elige el nivel de desafío consagrando tu ofrenda:").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal(" • Clic derecho con Trofeo Cósmico: ").formatted(Formatting.WHITE)
                    .append(Text.literal("Modo Normal (3 Olas)").formatted(Formatting.GREEN, Formatting.BOLD)), false);
            player.sendMessage(Text.literal(" • Shift + Clic con Trofeo Cósmico: ").formatted(Formatting.WHITE)
                    .append(Text.literal("Modo Difícil (5 Olas, Mini-jefes)").formatted(Formatting.GOLD, Formatting.BOLD)), false);
            player.sendMessage(Text.literal(" • Clic con Singularidad Cósmica: ").formatted(Formatting.WHITE)
                    .append(Text.literal("Modo Pesadilla (7 Olas, Jefes Mayores y Botín Divino)").formatted(Formatting.RED, Formatting.BOLD)), false);
            world.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0f, 0.8f);
        }

        return ActionResult.SUCCESS;
    }
}
