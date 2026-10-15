package com.chainores.world.feature;

import com.chainores.ChainOresMod;
import com.chainores.entity.VoidColossusEntity;
import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
import com.chainores.world.StructureLocator;
import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
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
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        // Check if there is End ground underneath
        if (!world.getBlockState(origin.down()).isOf(Blocks.END_STONE)) {
            return false;
        }

        // 9x6x9 Celestial Astral Platform
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos floorPos = origin.add(x, 0, z);
                boolean isOuter = Math.abs(x) == 4 || Math.abs(z) == 4;
                boolean isInner = Math.abs(x) <= 2 && Math.abs(z) <= 2;

                if (isOuter) {
                    world.setBlockState(floorPos, Blocks.END_STONE_BRICKS.getDefaultState(), 2);
                } else if (isInner) {
                    world.setBlockState(floorPos, (x == 0 && z == 0) ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.PURPUR_PILLAR.getDefaultState(), 2);
                } else {
                    world.setBlockState(floorPos, Blocks.PURPUR_BLOCK.getDefaultState(), 2);
                }

                // Clear air dome above platform
                for (int y = 1; y <= 5; y++) {
                    world.setBlockState(origin.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        // 4 Celestial Monolith Pillars
        int[][] corners = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
        for (int[] c : corners) {
            for (int y = 1; y <= 4; y++) {
                world.setBlockState(origin.add(c[0], y, c[1]), (y == 4) ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState(), 2);
            }
            world.setBlockState(origin.add(c[0], 5, c[1]), Blocks.END_ROD.getDefaultState(), 2);
        }

        // Center Pedestal with Crying Obsidian
        world.setBlockState(origin.add(0, 1, 0), Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);

        // Cosmic Treasure Chest on top of pedestal
        BlockPos chestPos = origin.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.VOID_BEACON, 1));
            chest.setStack(4, new ItemStack(ModBossItems.COSMIC_ALTAR_CORE, 1));
            chest.setStack(11, new ItemStack(Items.SHULKER_SHELL, 4 + random.nextInt(4)));
            chest.setStack(13, new ItemStack(Items.ENDER_EYE, 4 + random.nextInt(4)));
            chest.setStack(15, new ItemStack(Items.END_CRYSTAL, 1));
            if (OreRegistryData.TIERS.size() >= 130) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(125).ingotItem, 3 + random.nextInt(4)));
            }
        }

        // SPAWN BOSS: Coloso del Vacío (Void Colossus)
        ServerWorld serverWorld = world.toServerWorld();
        if (serverWorld != null) {
            VoidColossusEntity boss = ModEntities.VOID_COLOSSUS.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 2.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Coloso del Vacío invocado en Altar Cósmico: {}", origin);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_ALTAR, origin);
        }

        return true;
    }
}
