package com.chainores.world.feature;

import com.chainores.ChainOresMod;
import com.chainores.entity.FrostwardenEntity;
import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
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

public class GlacialSanctuaryFeature extends Feature<DefaultFeatureConfig> {
    public GlacialSanctuaryFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        ServerWorld serverWorld = context.getWorld().toServerWorld();
        int minDist = com.chainores.config.ChainOresConfig.get().minStructureDistanceBlocks;
        if (serverWorld != null && StructureLocator.isTooClose(serverWorld, StructureLocator.TYPE_GLACIAL, context.getOrigin(), minDist)) {
            return false;
        }
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        ServerWorld serverWorld = world.toServerWorld();

        if (origin.getY() < 60 || origin.getY() > 250) {
            return false;
        }

        BlockPos basePos = origin;
        while (basePos.getY() > 50 && (world.getBlockState(basePos).isAir() || world.getBlockState(basePos).isOf(Blocks.WATER)
                || world.getBlockState(basePos).isOf(Blocks.SNOW) || world.getBlockState(basePos).isOf(Blocks.POWDER_SNOW)
                || world.getBlockState(basePos).isIn(net.minecraft.registry.tag.BlockTags.LEAVES)
                || world.getBlockState(basePos).isIn(net.minecraft.registry.tag.BlockTags.LOGS))) {
            basePos = basePos.down();
        }

        int radius = 10;
        int clearRadius = radius + 3; // 13 blocks radius to eliminate any surrounding trees/ice spikes
        int clearHeight = 18;         // 18 blocks vertical clearance eliminates all taiga spruce tree canopies

        // 1. Foundation
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    for (int y = -1; y >= -4; y--) {
                        BlockPos p = basePos.add(x, y, z);
                        world.setBlockState(p, Blocks.PACKED_ICE.getDefaultState(), 2);
                    }
                }
            }
        }

        // 2. Air & Vegetation clearance
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
                        world.setBlockState(floorPos, Blocks.BLUE_ICE.getDefaultState(), 2);
                    } else if (distSq >= (radius - 1) * (radius - 1)) {
                        world.setBlockState(floorPos, Blocks.CALCITE.getDefaultState(), 2);
                    } else {
                        world.setBlockState(floorPos, (random.nextFloat() < 0.6f) ? Blocks.PACKED_ICE.getDefaultState() : Blocks.SNOW_BLOCK.getDefaultState(), 2);
                    }
                }
            }
        }

        // 3. 8 Glacial Pillars
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI * 2 / 8.0);
            int px = (int) Math.round(Math.cos(angle) * (radius - 1));
            int pz = (int) Math.round(Math.sin(angle) * (radius - 1));
            BlockPos pillarBase = basePos.add(px, 1, pz);

            for (int y = 0; y < 4; y++) {
                world.setBlockState(pillarBase.up(y), Blocks.QUARTZ_PILLAR.getDefaultState(), 2);
            }
            world.setBlockState(pillarBase.up(4), Blocks.SEA_LANTERN.getDefaultState(), 2);
            world.setBlockState(pillarBase.up(5), Blocks.PACKED_ICE.getDefaultState(), 2);
        }

        // 4. Center Dais & Master Glacial Chest
        world.setBlockState(basePos.add(0, 1, 0), Blocks.CHISELED_QUARTZ_BLOCK.getDefaultState(), 2);
        world.setBlockState(basePos.add(1, 1, 0), Blocks.BLUE_ICE.getDefaultState(), 2);
        world.setBlockState(basePos.add(-1, 1, 0), Blocks.BLUE_ICE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, 1), Blocks.BLUE_ICE.getDefaultState(), 2);
        world.setBlockState(basePos.add(0, 1, -1), Blocks.BLUE_ICE.getDefaultState(), 2);

        BlockPos chestPos = basePos.add(0, 2, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.GLACIAL_HORN, 1));
            chest.setStack(4, new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 2 + random.nextInt(2)));
            chest.setStack(11, new ItemStack(ModItems.PROSPECTOR_ROD, 1));
            chest.setStack(13, new ItemStack(Items.DIAMOND, 2 + random.nextInt(3)));
            chest.setStack(15, new ItemStack(Items.AMETHYST_SHARD, 4 + random.nextInt(6)));
            if (OreRegistryData.TIERS.size() >= 20) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(19).ingotItem, 3 + random.nextInt(3)));
            }
        }

        // 5. Spawn Frostwarden mini-boss
        if (serverWorld != null) {
            FrostwardenEntity boss = ModEntities.FROSTWARDEN.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 3.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Santuario Glacial generado en: {}", basePos);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_GLACIAL, basePos);
        }

        return true;
    }
}
