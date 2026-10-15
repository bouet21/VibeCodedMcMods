package com.chainores.world.feature;

import com.chainores.ChainOresMod;
import com.chainores.entity.AbyssalGuardianEntity;
import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
import com.chainores.registry.ModItems;
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

public class AbandonedForgeFeature extends Feature<DefaultFeatureConfig> {
    public AbandonedForgeFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        if (origin.getY() < -50 || origin.getY() > 260) {
            return false;
        }

        // Adjust origin to sit firmly on solid terrain
        BlockPos basePos = origin;
        while (basePos.getY() > -30 && (world.getBlockState(basePos).isAir() || world.getBlockState(basePos).isOf(Blocks.WATER))) {
            basePos = basePos.down();
        }
        if (basePos.getY() <= -30) {
            basePos = origin;
        }

        int radius = 12; // 25x25 Grand Battle Arena
        int height = 9;  // 9 blocks tall interior clearance

        // 1. Foundation downwards
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y >= -4; y--) {
                    BlockPos p = basePos.add(x, y, z);
                    if (world.getBlockState(p).isAir() || world.getBlockState(p).isOf(Blocks.WATER) || world.getBlockState(p).isOf(Blocks.DIRT)) {
                        world.setBlockState(p, Blocks.COBBLED_DEEPSLATE.getDefaultState(), 2);
                    }
                }
            }
        }

        // 2. Floor, Interior & Vaulted Ceiling
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distSq = x * x + z * z;
                boolean isBorder = Math.abs(x) == radius || Math.abs(z) == radius;

                // Floor (y = 0)
                BlockPos floorPos = basePos.add(x, 0, z);
                if (isBorder) {
                    world.setBlockState(floorPos, Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                } else if (distSq <= 36) {
                    // Center fighting ring
                    world.setBlockState(floorPos, ((x + z) % 2 == 0) ? Blocks.DEEPSLATE_TILES.getDefaultState() : Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                } else if (Math.abs(x) == 8 || Math.abs(z) == 8) {
                    world.setBlockState(floorPos, Blocks.CHISELED_DEEPSLATE.getDefaultState(), 2);
                } else {
                    world.setBlockState(floorPos, random.nextBoolean() ? Blocks.DEEPSLATE_BRICKS.getDefaultState() : Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), 2);
                }

                // Clear interior space (y = 1 to height)
                for (int y = 1; y <= height; y++) {
                    BlockPos p = basePos.add(x, y, z);

                    if (isBorder) {
                        // Grand entrance gate on South wall (z == radius, |x| <= 3)
                        if (z == radius && Math.abs(x) <= 3 && y <= 5) {
                            world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                        } else if (y == 4 && (Math.abs(x) == 4 || Math.abs(z) == 4)) {
                            // Iron grated viewing windows
                            world.setBlockState(p, Blocks.IRON_BARS.getDefaultState(), 2);
                        } else if (Math.abs(x) == radius && Math.abs(z) == radius) {
                            world.setBlockState(p, Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                        } else {
                            world.setBlockState(p, random.nextFloat() < 0.8f ? Blocks.DEEPSLATE_BRICKS.getDefaultState() : Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), 2);
                        }
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }

                // Ceiling (y = height + 1)
                BlockPos ceilPos = basePos.add(x, height + 1, z);
                world.setBlockState(ceilPos, isBorder ? Blocks.POLISHED_DEEPSLATE.getDefaultState() : Blocks.DEEPSLATE_TILES.getDefaultState(), 2);
            }
        }

        // 3. 4 Citadel Pillars (at x = +/-7, z = +/-7)
        int[][] pillars = {{-7, -7}, {-7, 7}, {7, -7}, {7, 7}};
        for (int[] p : pillars) {
            for (int y = 1; y <= height; y++) {
                world.setBlockState(basePos.add(p[0], y, p[1]), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
            }
            world.setBlockState(basePos.add(p[0], 2, p[1]), Blocks.CHISELED_DEEPSLATE.getDefaultState(), 2);
            world.setBlockState(basePos.add(p[0], 6, p[1]), Blocks.CHISELED_DEEPSLATE.getDefaultState(), 2);
        }

        // 4. Hanging Chains and Soul Lanterns
        int[][] lightPositions = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}, {0, -5}, {0, 5}};
        for (int[] lp : lightPositions) {
            world.setBlockState(basePos.add(lp[0], height, lp[1]), Blocks.IRON_CHAIN.getDefaultState(), 2);
            world.setBlockState(basePos.add(lp[0], height - 1, lp[1]), Blocks.IRON_CHAIN.getDefaultState(), 2);
            world.setBlockState(basePos.add(lp[0], height - 2, lp[1]), Blocks.SOUL_LANTERN.getDefaultState(), 2);
        }

        // 5. Artisan Workstation Dais (North Wall)
        world.setBlockState(basePos.add(-3, 1, -10), ModWorkstations.CHAIN_FORGE_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(3, 1, -10), ModWorkstations.CHAIN_ANVIL_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(-1, 1, -10), Blocks.BLAST_FURNACE.getDefaultState(), 2);
        world.setBlockState(basePos.add(1, 1, -10), Blocks.BLAST_FURNACE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, -10), Blocks.CAULDRON.getDefaultState(), 2);

        // 6. Center Raised Dais with Master Treasure Chest
        world.setBlockState(basePos.add(0, 1, 0), Blocks.CHISELED_DEEPSLATE.getDefaultState(), 2);
        world.setBlockState(basePos.add(1, 1, 0), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
        world.setBlockState(basePos.add(-1, 1, 0), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, 1), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, -1), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);

        BlockPos chestPos = basePos.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.ABYSSAL_EYE, 1));
            chest.setStack(4, new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 3 + random.nextInt(3)));
            chest.setStack(11, new ItemStack(ModItems.PROSPECTOR_ROD, 1));
            chest.setStack(13, new ItemStack(Items.IRON_CHAIN, 12 + random.nextInt(12)));
            chest.setStack(15, new ItemStack(Items.DIAMOND, 4 + random.nextInt(4)));
            if (OreRegistryData.TIERS.size() >= 50) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(45).ingotItem, 4 + random.nextInt(4)));
            }
        }

        // 7. Spawn Boss in Arena: Guardián del Abismo
        ServerWorld serverWorld = world.toServerWorld();
        if (serverWorld != null) {
            AbyssalGuardianEntity boss = ModEntities.ABYSSAL_GUARDIAN.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 4.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Gran Arena de Forja Abandonada generada en: {}", basePos);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_FORGE, basePos);
        }

        return true;
    }
}
