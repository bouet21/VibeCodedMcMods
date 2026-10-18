package com.chainores.world.feature;

import com.chainores.ChainOresMod;
import com.chainores.entity.SylvaAncientEntity;
import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
import com.chainores.registry.ModGems;
import com.chainores.registry.ModItems;
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

public class SylvaGroveFeature extends Feature<DefaultFeatureConfig> {
    public SylvaGroveFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        ServerWorld serverWorld = context.getWorld().toServerWorld();
        int minDist = com.chainores.config.ChainOresConfig.get().minStructureDistanceBlocks;
        if (serverWorld != null && StructureLocator.isTooClose(serverWorld, StructureLocator.TYPE_SYLVA, context.getOrigin(), minDist)) {
            return false;
        }
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        ServerWorld serverWorld = world.toServerWorld();

        if (origin.getY() < 55 || origin.getY() > 240) {
            return false;
        }

        BlockPos basePos = origin;
        while (basePos.getY() > 45 && (world.getBlockState(basePos).isAir() || world.getBlockState(basePos).isOf(Blocks.WATER)
                || world.getBlockState(basePos).isIn(net.minecraft.registry.tag.BlockTags.LEAVES)
                || world.getBlockState(basePos).isIn(net.minecraft.registry.tag.BlockTags.LOGS))) {
            basePos = basePos.down();
        }

        int radius = 10;
        int clearRadius = radius + 3; // 13 blocks radius to eliminate any encroaching tree trunks/leaves
        int clearHeight = 18;         // 18 blocks vertical clearance removes all forest canopies

        // 1. Foundation downwards - solid clean base
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    for (int y = -1; y >= -4; y--) {
                        BlockPos p = basePos.add(x, y, z);
                        world.setBlockState(p, Blocks.DIRT.getDefaultState(), 2);
                    }
                }
            }
        }

        // 2. Air & Vegetation clearance - wipe out all trees, branches, foliage and obstructions
        for (int x = -clearRadius; x <= clearRadius; x++) {
            for (int z = -clearRadius; z <= clearRadius; z++) {
                int distSq = x * x + z * z;
                if (distSq <= clearRadius * clearRadius) {
                    for (int y = 1; y <= clearHeight; y++) {
                        world.setBlockState(basePos.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        // 3. Floor placement
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    BlockPos floorPos = basePos.add(x, 0, z);
                    if (distSq <= 9) {
                        world.setBlockState(floorPos, Blocks.MUD_BRICKS.getDefaultState(), 2);
                    } else if (distSq >= (radius - 1) * (radius - 1)) {
                        world.setBlockState(floorPos, Blocks.MOSSY_STONE_BRICKS.getDefaultState(), 2);
                    } else {
                        float r = random.nextFloat();
                        if (r < 0.45f) {
                            world.setBlockState(floorPos, Blocks.MOSS_BLOCK.getDefaultState(), 2);
                        } else if (r < 0.75f) {
                            world.setBlockState(floorPos, Blocks.ROOTED_DIRT.getDefaultState(), 2);
                        } else {
                            world.setBlockState(floorPos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        // 3. 8 Ancient Megalithic Pillars
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI * 2 / 8.0);
            int px = (int) Math.round(Math.cos(angle) * (radius - 1));
            int pz = (int) Math.round(Math.sin(angle) * (radius - 1));
            BlockPos pillarBase = basePos.add(px, 1, pz);

            for (int y = 0; y < 4; y++) {
                world.setBlockState(pillarBase.up(y), Blocks.MOSSY_STONE_BRICKS.getDefaultState(), 2);
            }
            world.setBlockState(pillarBase.up(4), Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState(), 2);
            world.setBlockState(pillarBase.up(5), Blocks.LANTERN.getDefaultState(), 2);
        }

        // 4. Center Altar Dais & Master Verdant Chest
        world.setBlockState(basePos.add(0, 1, 0), Blocks.CHISELED_STONE_BRICKS.getDefaultState(), 2);
        world.setBlockState(basePos.add(1, 1, 0), Blocks.MOSS_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(-1, 1, 0), Blocks.MOSS_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, 1), Blocks.MOSS_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, -1), Blocks.MOSS_BLOCK.getDefaultState(), 2);

        BlockPos chestPos = basePos.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.VERDANT_HEART, 1));
            chest.setStack(4, new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 2 + random.nextInt(2)));
            chest.setStack(11, new ItemStack(ModGems.EARTH_GEM, 1));
            chest.setStack(13, new ItemStack(Items.EMERALD, 4 + random.nextInt(6)));
            chest.setStack(15, new ItemStack(Items.GOLDEN_APPLE, 1 + random.nextInt(2)));
            if (OreRegistryData.TIERS.size() >= 40) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(39).ingotItem, 3 + random.nextInt(3)));
            }
        }

        // 5. Spawn Sylva Ancient mini-boss
        if (serverWorld != null) {
            SylvaAncientEntity boss = ModEntities.SYLVA_ANCIENT.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 3.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Arboleda Silvestre generada en: {}", basePos);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_SYLVA, basePos);
        }

        return true;
    }
}
