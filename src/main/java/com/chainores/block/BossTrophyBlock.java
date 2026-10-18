package com.chainores.block;

import com.chainores.block.entity.BossTrophyBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BossTrophyBlock extends Block implements BlockEntityProvider {
    public enum TrophyType {
        GLACIAL,
        SYLVA,
        ABYSSAL,
        INFERNAL,
        VOID,
        COSMIC
    }

    private static final VoxelShape BASE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);
    private static final VoxelShape STEM = Block.createCuboidShape(5.0, 3.0, 5.0, 11.0, 8.0, 11.0);
    private static final VoxelShape CROWN = Block.createCuboidShape(3.0, 8.0, 3.0, 13.0, 15.0, 13.0);
    private static final VoxelShape SHAPE = VoxelShapes.union(BASE, STEM, CROWN);

    private final TrophyType trophyType;

    public BossTrophyBlock(TrophyType trophyType, Settings settings) {
        super(settings);
        this.trophyType = trophyType;
    }

    public TrophyType getTrophyType() {
        return trophyType;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BossTrophyBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof BossTrophyBlockEntity trophy) {
                BossTrophyBlockEntity.tick(world1, pos, state1, trophy);
            }
        };
    }
}
