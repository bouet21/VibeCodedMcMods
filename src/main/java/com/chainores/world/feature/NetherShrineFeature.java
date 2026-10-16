package com.chainores.world.feature;

import com.chainores.ChainOresMod;
import com.chainores.entity.NetherBehemothEntity;
import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
import com.chainores.registry.ModWorkstations;
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

public class NetherShrineFeature extends Feature<DefaultFeatureConfig> {
    public NetherShrineFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        ServerWorld serverWorld = context.getWorld().toServerWorld();
        int minDist = com.chainores.config.ChainOresConfig.get().minStructureDistanceBlocks;
        if (serverWorld != null && StructureLocator.isTooClose(serverWorld, StructureLocator.TYPE_SHRINE, context.getOrigin(), minDist)) {
            return false;
        }
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        ServerWorld serverWorld = world.toServerWorld();

        // Find solid floor with ceiling clearance in Nether
        BlockPos basePos = null;
        int startY = Math.max(32, Math.min(80, origin.getY()));
        for (int dy = -15; dy <= 20; dy++) {
            BlockPos test = new BlockPos(origin.getX(), startY + dy, origin.getZ());
            if (!world.getBlockState(test.down()).isAir() && !world.getBlockState(test.down()).isOf(Blocks.LAVA)
                    && world.getBlockState(test).isAir() && world.getBlockState(test.up(4)).isAir()) {
                basePos = test;
                break;
            }
        }

        if (basePos == null) {
            basePos = new BlockPos(origin.getX(), startY, origin.getZ());
        }

        int radius = 13; // 27x27 Grand Colosseum
        int height = 11; // 11 blocks high vaulted space

        // 1. Foundation downwards
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y >= -4; y--) {
                    BlockPos p = basePos.add(x, y, z);
                    if (world.getBlockState(p).isAir() || world.getBlockState(p).isOf(Blocks.LAVA)) {
                        world.setBlockState(p, Blocks.POLISHED_BASALT.getDefaultState(), 2);
                    }
                }
            }
        }

        // 2. Colosseum Floor, Interior Air & Vaulted Roof
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distSq = x * x + z * z;
                boolean isBorder = Math.abs(x) == radius || Math.abs(z) == radius;
                boolean isCardGate = (Math.abs(x) == radius && Math.abs(z) <= 3) || (Math.abs(z) == radius && Math.abs(x) <= 3);

                // Floor (y = 0)
                BlockPos floorPos = basePos.add(x, 0, z);
                if (isBorder) {
                    world.setBlockState(floorPos, Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
                } else if (distSq <= 49) {
                    // Center combat ring with magma & crying obsidian inlays
                    if (x == 0 || z == 0) {
                        world.setBlockState(floorPos, Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);
                    } else if ((x + z) % 2 == 0) {
                        world.setBlockState(floorPos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
                    } else {
                        world.setBlockState(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                    }
                } else {
                    world.setBlockState(floorPos, random.nextFloat() < 0.7f ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState() : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                }

                // Clear interior space (y = 1 to height)
                for (int y = 1; y <= height; y++) {
                    BlockPos p = basePos.add(x, y, z);

                    if (isBorder) {
                        if (isCardGate && y <= 6) {
                            // Grand 6-block tall arched openings on all 4 sides
                            world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                        } else if (Math.abs(x) == radius && Math.abs(z) == radius) {
                            world.setBlockState(p, Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
                        } else if (y == 5 && (Math.abs(x) == 6 || Math.abs(z) == 6)) {
                            world.setBlockState(p, Blocks.IRON_BARS.getDefaultState(), 2);
                        } else {
                            world.setBlockState(p, random.nextFloat() < 0.8f ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState() : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                        }
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }

                // Ceiling (y = height + 1)
                BlockPos ceilPos = basePos.add(x, height + 1, z);
                if (distSq <= 16) {
                    world.setBlockState(ceilPos, Blocks.SHROOMLIGHT.getDefaultState(), 2);
                } else {
                    world.setBlockState(ceilPos, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                }
            }
        }

        // 3. 4 Citadel Pillars (at x = +/-8, z = +/-8)
        int[][] pillars = {{-8, -8}, {-8, 8}, {8, -8}, {8, 8}};
        for (int[] p : pillars) {
            for (int y = 1; y <= height; y++) {
                world.setBlockState(basePos.add(p[0], y, p[1]), Blocks.POLISHED_BASALT.getDefaultState(), 2);
            }
            world.setBlockState(basePos.add(p[0], 2, p[1]), Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
            world.setBlockState(basePos.add(p[0], 7, p[1]), Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
            // Soul Fire on top of pillars
            world.setBlockState(basePos.add(p[0], 1, p[1] + (p[1] > 0 ? -1 : 1)), Blocks.SOUL_FIRE.getDefaultState(), 2);
        }

        // 4. Cosmic Smelter Station (North End)
        world.setBlockState(basePos.add(0, 1, -11), ModWorkstations.COSMIC_SMELTER_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(-2, 1, -11), Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
        world.setBlockState(basePos.add(2, 1, -11), Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
        world.setBlockState(basePos.add(-2, 2, -11), Blocks.SOUL_LANTERN.getDefaultState(), 2);
        world.setBlockState(basePos.add(2, 2, -11), Blocks.SOUL_LANTERN.getDefaultState(), 2);

        // 5. Center Raised Altar with Nether Master Chest
        world.setBlockState(basePos.add(0, 1, 0), Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);
        world.setBlockState(basePos.add(1, 1, 0), Blocks.POLISHED_BLACKSTONE.getDefaultState(), 2);
        world.setBlockState(basePos.add(-1, 1, 0), Blocks.POLISHED_BLACKSTONE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, 1), Blocks.POLISHED_BLACKSTONE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, -1), Blocks.POLISHED_BLACKSTONE.getDefaultState(), 2);

        BlockPos chestPos = basePos.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.FLAME_HEART, 1));
            chest.setStack(4, new ItemStack(Items.BLAZE_ROD, 6 + random.nextInt(6)));
            chest.setStack(11, new ItemStack(Items.NETHERITE_SCRAP, 2 + random.nextInt(2)));
            chest.setStack(13, new ItemStack(Items.LAVA_BUCKET, 1));
            chest.setStack(15, new ItemStack(Items.MAGMA_BLOCK, 16 + random.nextInt(16)));
            if (OreRegistryData.TIERS.size() >= 100) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(95).ingotItem, 4 + random.nextInt(4)));
            }
        }

        // 6. Spawn Boss: Behemoth del Nether in Colosseum
        if (serverWorld != null) {
            NetherBehemothEntity boss = ModEntities.NETHER_BEHEMOTH.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 4.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Gran Coliseo del Nether generado en: {}", basePos);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_SHRINE, basePos);
        }

        return true;
    }
}
