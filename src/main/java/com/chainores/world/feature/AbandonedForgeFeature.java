package com.chainores.world.feature;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModItems;
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

public class AbandonedForgeFeature extends Feature<DefaultFeatureConfig> {
    public AbandonedForgeFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        // Must be in deep underground
        if (origin.getY() > -5) {
            return false;
        }

        // Carve and construct 5x4x5 chamber
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Floor
                BlockPos floorPos = origin.add(x, 0, z);
                world.setBlockState(floorPos, random.nextBoolean() ? Blocks.DEEPSLATE_BRICKS.getDefaultState() : Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), 2);

                // Ceiling
                BlockPos ceilPos = origin.add(x, 4, z);
                world.setBlockState(ceilPos, Blocks.DEEPSLATE_BRICKS.getDefaultState(), 2);

                // Interior hollow & walls
                for (int y = 1; y <= 3; y++) {
                    BlockPos p = origin.add(x, y, z);
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        world.setBlockState(p, random.nextInt(3) == 0 ? Blocks.POLISHED_DEEPSLATE.getDefaultState() : Blocks.DEEPSLATE_BRICKS.getDefaultState(), 2);
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        // Hanging lantern in center
        world.setBlockState(origin.add(0, 3, 0), Blocks.IRON_CHAIN.getDefaultState(), 2);
        world.setBlockState(origin.add(0, 2, 0), Blocks.LANTERN.getDefaultState(), 2);

        // Place Chain Forge & Chain Anvil
        world.setBlockState(origin.add(-1, 1, -1), ModWorkstations.CHAIN_FORGE_BLOCK.getDefaultState(), 2);
        world.setBlockState(origin.add(1, 1, -1), ModWorkstations.CHAIN_ANVIL_BLOCK.getDefaultState(), 2);

        // Loot Chest
        BlockPos chestPos = origin.add(0, 1, 1);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.ABYSSAL_EYE, 1));
            chest.setStack(4, new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 1 + random.nextInt(2)));
            chest.setStack(13, new ItemStack(ModItems.PROSPECTOR_ROD, 1));
            chest.setStack(22, new ItemStack(Items.IRON_CHAIN, 3 + random.nextInt(5)));
            if (OreRegistryData.TIERS.size() >= 50) {
                chest.setStack(10, new ItemStack(OreRegistryData.TIERS.get(45).ingotItem, 2 + random.nextInt(4)));
            }
        }

        if (world.toServerWorld() != null) {
            com.chainores.world.StructureLocator.record(world.toServerWorld(), com.chainores.world.StructureLocator.TYPE_FORGE, origin);
        }

        return true;
    }
}
