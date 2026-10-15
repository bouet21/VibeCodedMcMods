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
        BlockPos origin = context.getOrigin();
        double distFromCenterSq = (double) origin.getX() * origin.getX() + (double) origin.getZ() * origin.getZ();
        if (distFromCenterSq < 600.0 * 600.0) {
            return false;
        }

        ServerWorld serverWorld = context.getWorld().toServerWorld();
        if (serverWorld != null && StructureLocator.isTooClose(serverWorld, StructureLocator.TYPE_ALTAR, origin, 900)) {
            return false;
        }
        return buildStructure(context.getWorld(), origin, context.getRandom());
    }

    public static boolean buildStructure(StructureWorldAccess world, BlockPos origin, Random random) {
        // Prevent generating on central End island (dragon fight zone)
        double distFromCenterSq = (double) origin.getX() * origin.getX() + (double) origin.getZ() * origin.getZ();
        if (distFromCenterSq < 600.0 * 600.0) {
            return false;
        }

        ServerWorld serverWorld = world.toServerWorld();
        if (serverWorld != null && StructureLocator.isTooClose(serverWorld, StructureLocator.TYPE_ALTAR, origin, 900)) {
            return false;
        }

        // Find solid End ground
        BlockPos basePos = origin;
        while (basePos.getY() > 30 && world.getBlockState(basePos.down()).isAir()) {
            basePos = basePos.down();
        }

        if (basePos.getY() < 30 || basePos.getY() > 120) {
            return false;
        }

        int radius = 14;  // 29x29 Grand Astral Arena
        int skyClear = 16; // 16 blocks vertical clear air for boss combat

        // 1. Foundation downwards
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distSq = x * x + z * z;
                if (distSq <= radius * radius) {
                    for (int y = -1; y >= -5; y--) {
                        BlockPos p = basePos.add(x, y, z);
                        if (world.getBlockState(p).isAir()) {
                            world.setBlockState(p, Blocks.END_STONE.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        // 2. Arena Floor & Sky Clearance
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int distSq = x * x + z * z;
                if (distSq > radius * radius) continue;

                boolean isPerimeter = distSq >= (radius - 1) * (radius - 1);

                // Floor (y = 0)
                BlockPos floorPos = basePos.add(x, 0, z);
                if (isPerimeter) {
                    world.setBlockState(floorPos, Blocks.END_STONE_BRICKS.getDefaultState(), 2);
                } else if (distSq <= 36) {
                    // Inner sacred circle
                    world.setBlockState(floorPos, ((x + z) % 2 == 0) ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.PURPUR_BLOCK.getDefaultState(), 2);
                } else if (distSq <= 100) {
                    // Mid combat ring
                    world.setBlockState(floorPos, ((x * x + z * z) % 3 == 0) ? Blocks.PURPUR_PILLAR.getDefaultState() : Blocks.END_STONE_BRICKS.getDefaultState(), 2);
                } else {
                    world.setBlockState(floorPos, Blocks.END_STONE_BRICKS.getDefaultState(), 2);
                }

                // Clear sky above arena (y = 1 to skyClear)
                for (int y = 1; y <= skyClear; y++) {
                    world.setBlockState(basePos.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                }

                // 2-block high outer safety barrier/parapet (so players don't fly into the void)
                if (isPerimeter) {
                    world.setBlockState(basePos.add(x, 1, z), Blocks.PURPUR_BLOCK.getDefaultState(), 2);
                    world.setBlockState(basePos.add(x, 2, z), Blocks.END_STONE_BRICK_WALL.getDefaultState(), 2);
                    if ((x + z) % 4 == 0) {
                        world.setBlockState(basePos.add(x, 3, z), Blocks.END_ROD.getDefaultState(), 2);
                    }
                }
            }
        }

        // 3. 4 Towering Astral Monoliths (at x = +/-10, z = +/-10)
        int[][] monoliths = {{-10, -10}, {-10, 10}, {10, -10}, {10, 10}};
        for (int[] m : monoliths) {
            for (int y = 1; y <= 11; y++) {
                world.setBlockState(basePos.add(m[0], y, m[1]), (y % 3 == 0) ? Blocks.CRYING_OBSIDIAN.getDefaultState() : Blocks.OBSIDIAN.getDefaultState(), 2);
            }
            world.setBlockState(basePos.add(m[0], 12, m[1]), Blocks.RESPAWN_ANCHOR.getDefaultState(), 2);
            world.setBlockState(basePos.add(m[0], 13, m[1]), Blocks.END_ROD.getDefaultState(), 2);
        }

        // 4. Center Elevated Altar Dais (radius 2, y = 1)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.setBlockState(basePos.add(x, 1, z), (Math.abs(x) == 2 || Math.abs(z) == 2) ? Blocks.PURPUR_BLOCK.getDefaultState() : Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);
            }
        }
        world.setBlockState(basePos.add(0, 2, 0), Blocks.CRYING_OBSIDIAN.getDefaultState(), 2);

        // 5. Cosmic Master Chest atop Altar
        BlockPos chestPos = basePos.add(0, 3, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(ModBossItems.VOID_BEACON, 1));
            chest.setStack(4, new ItemStack(ModBossItems.COSMIC_ALTAR_CORE, 1));
            chest.setStack(11, new ItemStack(Items.SHULKER_SHELL, 6 + random.nextInt(6)));
            chest.setStack(13, new ItemStack(Items.ENDER_EYE, 6 + random.nextInt(6)));
            chest.setStack(15, new ItemStack(Items.END_CRYSTAL, 1));
            if (OreRegistryData.TIERS.size() >= 130) {
                chest.setStack(22, new ItemStack(OreRegistryData.TIERS.get(125).ingotItem, 4 + random.nextInt(4)));
            }
        }

        // 6. Spawn Boss: Coloso del Vacío in Grand Arena
        if (serverWorld != null) {
            VoidColossusEntity boss = ModEntities.VOID_COLOSSUS.create(serverWorld, SpawnReason.STRUCTURE);
            if (boss != null) {
                boss.refreshPositionAndAngles(basePos.getX() + 0.5, basePos.getY() + 2.0, basePos.getZ() + 6.0, random.nextFloat() * 360.0f, 0.0f);
                boss.setPersistent();
                serverWorld.spawnEntity(boss);
                ChainOresMod.LOGGER.info("★ Gran Arena del Altar Cósmico generada en: {}", basePos);
            }
            StructureLocator.record(serverWorld, StructureLocator.TYPE_ALTAR, basePos);
        }

        return true;
    }
}
