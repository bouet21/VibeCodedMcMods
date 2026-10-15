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
        // Must be in deep underground
        if (origin.getY() > -5) {
            return false;
        }

        // Construct 7x5x7 Dungeon Chamber
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                // Floor (y = 0)
                BlockPos floorPos = origin.add(x, 0, z);
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    world.setBlockState(floorPos, Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                } else if ((x + z) % 2 == 0) {
                    world.setBlockState(floorPos, Blocks.DEEPSLATE_BRICKS.getDefaultState(), 2);
                } else {
                    world.setBlockState(floorPos, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), 2);
                }

                // Ceiling (y = 4)
                BlockPos ceilPos = origin.add(x, 4, z);
                world.setBlockState(ceilPos, (x == 0 && z == 0) ? Blocks.CHISELED_DEEPSLATE.getDefaultState() : Blocks.DEEPSLATE_TILES.getDefaultState(), 2);

                // Interior air & walls (y = 1..3)
                for (int y = 1; y <= 3; y++) {
                    BlockPos p = origin.add(x, y, z);
                    boolean isWall = Math.abs(x) == 3 || Math.abs(z) == 3;
                    boolean isCorner = Math.abs(x) == 3 && Math.abs(z) == 3;

                    if (isCorner) {
                        world.setBlockState(p, Blocks.POLISHED_DEEPSLATE.getDefaultState(), 2);
                    } else if (isWall) {
                        if (y == 2 && (Math.abs(x) == 0 || Math.abs(z) == 0)) {
                            world.setBlockState(p, Blocks.IRON_BARS.getDefaultState(), 2);
                        } else {
                            world.setBlockState(p, random.nextBoolean() ? Blocks.DEEPSLATE_BRICKS.getDefaultState() : Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState(), 2);
                        }
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }

        // Hanging Chains & Soul Lanterns from ceiling
        world.setBlockState(origin.add(-1, 3, -1), Blocks.IRON_CHAIN.getDefaultState(), 2);
        world.setBlockState(origin.add(-1, 2, -1), Blocks.SOUL_LANTERN.getDefaultState(), 2);
        world.setBlockState(origin.add(1, 3, 1), Blocks.IRON_CHAIN.getDefaultState(), 2);
        world.setBlockState(origin.add(1, 2, 1), Blocks.SOUL_LANTERN.getDefaultState(), 2);

        // Place Workstations
        world.setBlockState(origin.add(-2, 1, -2), ModWorkstations.CHAIN_FORGE_BLOCK.getDefaultState(), 2);
        world.setBlockState(origin.add(2, 1, -2), ModWorkstations.CHAIN_ANVIL_BLOCK.getDefaultState(), 2);

        // Treasure Loot Chest
        BlockPos chestPos = origin.add(0, 1, 2);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.ABYSSAL_EYE, 1));
            chest.setStack(4, new ItemStack(ModItems.CHAIN_SMITHING_TEMPLATE, 2 + random.nextInt(2)));
            chest.setStack(11, new ItemStack(ModItems.PROSPECTOR_ROD, 1));
            chest.setStack(13, new ItemStack(Items.IRON_CHAIN, 6 + random.nextInt(6)));
            chest.setStack(15, new ItemStack(Items.DIAMOND, 2 + random.nextInt(3)));
            if (OreRegistryData.TIERS.size() >= 50) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(45).ingotItem, 3 + random.nextInt(4)));
            }
        }

        // SPAWN BOSS: Guardián del Abismo (Abyssal Guardian) guarding the chamber
        ServerWorld serverWorld = world.toServerWorld();
        if (serverWorld != null) {
            AbyssalGuardianEntity boss = ModEntities.ABYSSAL_GUARDIAN.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Guardián del Abismo invocado en Forja Abandonada: {}", origin);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_FORGE, origin);
        }

        return true;
    }
}
