package com.chainores.world.feature;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
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

public class CosmicAltarFeature extends Feature<DefaultFeatureConfig> {
    public CosmicAltarFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        // Check if there is End ground underneath
        if (!world.getBlockState(origin.down()).isOf(Blocks.END_STONE)) {
            return false;
        }

        // 7x5x7 Platform
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos floorPos = origin.add(x, 0, z);
                boolean isBorder = Math.abs(x) == 3 || Math.abs(z) == 3;
                world.setBlockState(floorPos, isBorder ? Blocks.END_STONE_BRICKS.getDefaultState() : Blocks.PURPUR_BLOCK.getDefaultState(), 2);

                // Clear air above
                for (int y = 1; y <= 4; y++) {
                    world.setBlockState(origin.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        // 4 Astral Pillars of Obsidian + End Rods
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] c : corners) {
            for (int y = 1; y <= 3; y++) {
                world.setBlockState(origin.add(c[0], y, c[1]), Blocks.OBSIDIAN.getDefaultState(), 2);
            }
            world.setBlockState(origin.add(c[0], 4, c[1]), Blocks.END_ROD.getDefaultState(), 2);
        }

        // Center Pedestal with Crying Obsidian
        world.setBlockState(origin.add(0, 1, 0), Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);

        // Loot Chest on top of pedestal
        BlockPos chestPos = origin.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.VOID_BEACON, 1));
            chest.setStack(4, new ItemStack(ModBossItems.COSMIC_ALTAR_CORE, 1));
            chest.setStack(13, new ItemStack(Items.SHULKER_SHELL, 2 + random.nextInt(3)));
            chest.setStack(22, new ItemStack(Items.ENDER_EYE, 3 + random.nextInt(4)));
            if (OreRegistryData.TIERS.size() >= 130) {
                chest.setStack(10, new ItemStack(OreRegistryData.TIERS.get(125).ingotItem, 2 + random.nextInt(4)));
            }
        }

        if (world.toServerWorld() != null) {
            com.chainores.world.StructureLocator.record(world.toServerWorld(), com.chainores.world.StructureLocator.TYPE_ALTAR, origin);
        }

        return true;
    }
}
