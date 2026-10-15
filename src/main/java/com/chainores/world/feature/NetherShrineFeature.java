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
        return buildStructure(context.getWorld(), context.getOrigin(), context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        // Height suitable for Nether cavern floor
        if (origin.getY() < 25 || origin.getY() > 105) {
            return false;
        }

        // Construct 7x6x7 Nether Citadel Shrine
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                // Floor (y = 0)
                BlockPos floorPos = origin.add(x, 0, z);
                if (x == 0 && z == 0) {
                    world.setBlockState(floorPos, Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);
                } else if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    world.setBlockState(floorPos, Blocks.POLISHED_BASALT.getDefaultState(), 2);
                } else if ((x + z) % 2 == 0) {
                    world.setBlockState(floorPos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
                } else {
                    world.setBlockState(floorPos, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                }

                // Ceiling (y = 5)
                BlockPos ceilPos = origin.add(x, 5, z);
                world.setBlockState(ceilPos, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);

                // Interior air & walls (y = 1..4)
                for (int y = 1; y <= 4; y++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean isCorner = Math.abs(x) == 3 && Math.abs(z) == 3;
                    boolean isWall = Math.abs(x) == 3 || Math.abs(z) == 3;

                    if (isCorner) {
                        world.setBlockState(p, Blocks.GILDED_BLACKSTONE.getDefaultState(), 2);
                    } else if (isWall) {
                        if (y == 2 && (Math.abs(x) == 0 || Math.abs(z) == 0)) {
                            world.setBlockState(p, Blocks.LAVA.getDefaultState(), 2);
                        } else {
                            world.setBlockState(p, random.nextBoolean() ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState() : Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 2);
                        }
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        // Corner Soul Torches / Lanterns
        world.setBlockState(origin.add(-2, 1, -2), Blocks.SOUL_FIRE.getDefaultState(), 2);
        world.setBlockState(origin.add(2, 1, -2), Blocks.SOUL_FIRE.getDefaultState(), 2);
        world.setBlockState(origin.add(-2, 1, 2), Blocks.SOUL_FIRE.getDefaultState(), 2);
        world.setBlockState(origin.add(2, 1, 2), Blocks.SOUL_FIRE.getDefaultState(), 2);

        // Center Cosmic Smelter
        world.setBlockState(origin.add(0, 1, -2), ModWorkstations.COSMIC_SMELTER_BLOCK.getDefaultState(), 2);

        // Nether Treasure Chest
        BlockPos chestPos = origin.add(0, 1, 2);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.FLAME_HEART, 1));
            chest.setStack(4, new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(4)));
            chest.setStack(11, new ItemStack(Items.NETHERITE_SCRAP, 1 + random.nextInt(2)));
            chest.setStack(13, new ItemStack(Items.LAVA_BUCKET, 1));
            chest.setStack(15, new ItemStack(Items.MAGMA_BLOCK, 8 + random.nextInt(8)));
            if (OreRegistryData.TIERS.size() >= 100) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(95).ingotItem, 3 + random.nextInt(4)));
            }
        }

        // SPAWN BOSS: Behemoth del Nether (Nether Behemoth)
        ServerWorld serverWorld = world.toServerWorld();
        if (serverWorld != null) {
            NetherBehemothEntity boss = ModEntities.NETHER_BEHEMOTH.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Behemoth del Nether invocado en Santuario del Nether: {}", origin);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_SHRINE, origin);
        }

        return true;
    }
}
