package com.chainores.world.feature;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModWorkstations;
import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class NetherShrineFeature extends Feature<DefaultFeatureConfig> {
    public NetherShrineFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        // Check height suitable for Nether
        if (origin.getY() < 25 || origin.getY() > 100) {
            return false;
        }

        // Carve and construct 5x5x5 Magma Chamber
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Floor
                BlockPos floorPos = origin.add(x, 0, z);
                world.setBlockState(floorPos, (x == 0 && z == 0) ? Blocks.MAGMA_BLOCK.getDefaultState() : Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);

                // Ceiling
                BlockPos ceilPos = origin.add(x, 4, z);
                world.setBlockState(ceilPos, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);

                // Walls & Corner Pillars
                for (int y = 1; y <= 3; y++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean isCorner = Math.abs(x) == 2 && Math.abs(z) == 2;
                    boolean isWall = Math.abs(x) == 2 || Math.abs(z) == 2;

                    if (isCorner) {
                        world.setBlockState(p, Blocks.POLISHED_BASALT.getDefaultState(), 2);
                    } else if (isWall) {
                        world.setBlockState(p, random.nextBoolean() ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState() : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        // Soul Lanterns on walls
        world.setBlockState(origin.add(-1, 2, -1), Blocks.SOUL_LANTERN.getDefaultState(), 2);
        world.setBlockState(origin.add(1, 2, -1), Blocks.SOUL_LANTERN.getDefaultState(), 2);

        // Center Cosmic Smelter
        world.setBlockState(origin.add(0, 1, 0), ModWorkstations.COSMIC_SMELTER_BLOCK.getDefaultState(), 2);

        // Loot Chest
        BlockPos chestPos = origin.add(0, 1, 1);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.FLAME_HEART, 1));
            chest.setStack(4, new ItemStack(Items.BLAZE_ROD, 3 + random.nextInt(3)));
            chest.setStack(13, new ItemStack(Items.LAVA_BUCKET, 1));
            chest.setStack(22, new ItemStack(Items.MAGMA_BLOCK, 4 + random.nextInt(4)));
            if (OreRegistryData.TIERS.size() >= 100) {
                chest.setStack(10, new ItemStack(OreRegistryData.TIERS.get(95).ingotItem, 2 + random.nextInt(4)));
            }
        }

        return true;
    }
}
