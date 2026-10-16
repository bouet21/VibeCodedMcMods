package com.chainores.world;

import com.chainores.ChainOresMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class StructureLocator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<StructureEntry>>() {}.getType();

    public static final String TYPE_FORGE = "abandoned_forge";
    public static final String TYPE_SHRINE = "nether_shrine";
    public static final String TYPE_ALTAR = "cosmic_altar";

    public record StructureEntry(String type, String dimension, int x, int y, int z) {
        public BlockPos toPos() {
            return new BlockPos(x, y, z);
        }
    }

    private static final List<StructureEntry> ENTRIES = new ArrayList<>();
    private static boolean loaded = false;

    public static synchronized void record(ServerWorld world, String type, BlockPos pos) {
        if (world == null || pos == null) return;
        ensureLoaded(world);

        String dimId = world.getRegistryKey().getValue().toString();
        // Check if already recorded nearby
        for (StructureEntry entry : ENTRIES) {
            if (entry.type.equals(type) && entry.dimension.equals(dimId)) {
                if (Math.abs(entry.x - pos.getX()) < 64 && Math.abs(entry.z - pos.getZ()) < 64) {
                    return; // Already recorded
                }
            }
        }

        StructureEntry newEntry = new StructureEntry(type, dimId, pos.getX(), pos.getY(), pos.getZ());
        ENTRIES.add(newEntry);
        saveToFile(world);
        ChainOresMod.LOGGER.info("Tracked {} at [{}, {}, {}] in {}", type, pos.getX(), pos.getY(), pos.getZ(), dimId);
    }

    public static synchronized boolean isNearRegisteredStructure(ServerWorld world, String type, BlockPos pos, double maxDistance) {
        if (world == null || pos == null) return false;
        ensureLoaded(world);

        String dimId = world.getRegistryKey().getValue().toString();
        double maxDistSq = maxDistance * maxDistance;

        for (StructureEntry entry : ENTRIES) {
            if (entry.dimension().equals(dimId) && entry.type().equalsIgnoreCase(type)) {
                double dx = entry.x() - pos.getX();
                double dz = entry.z() - pos.getZ();
                if ((dx * dx + dz * dz) <= maxDistSq) {
                    return true;
                }
            }
        }
        return false;
    }

    public static synchronized boolean isTooClose(ServerWorld world, String type, BlockPos pos, double minDistance) {
        if (world == null || pos == null) return false;
        ensureLoaded(world);

        String dimId = world.getRegistryKey().getValue().toString();
        double minDistanceSq = minDistance * minDistance;

        for (StructureEntry entry : ENTRIES) {
            if (entry.dimension().equals(dimId) && entry.type().equalsIgnoreCase(type)) {
                double dx = entry.x() - pos.getX();
                double dz = entry.z() - pos.getZ();
                if ((dx * dx + dz * dz) < minDistanceSq) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidDimensionForType(ServerWorld world, String type) {
        if (type == null || type.isEmpty()) return true;
        if (type.equalsIgnoreCase(TYPE_FORGE)) {
            return world.getRegistryKey().equals(World.OVERWORLD);
        } else if (type.equalsIgnoreCase(TYPE_SHRINE)) {
            return world.getRegistryKey().equals(World.NETHER);
        } else if (type.equalsIgnoreCase(TYPE_ALTAR)) {
            return world.getRegistryKey().equals(World.END);
        }
        return false;
    }

    public static synchronized Optional<StructureEntry> findNearest(ServerWorld world, BlockPos origin, String targetType) {
        if (world == null || origin == null) return Optional.empty();
        ensureLoaded(world);

        // Check if target type is valid for current dimension
        if (targetType != null && !targetType.isEmpty() && !isValidDimensionForType(world, targetType)) {
            return Optional.empty();
        }

        String dimId = world.getRegistryKey().getValue().toString();
        StructureEntry closest = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (StructureEntry entry : ENTRIES) {
            if (entry.dimension.equals(dimId)) {
                if (targetType == null || targetType.isEmpty() || entry.type.equalsIgnoreCase(targetType)) {
                    double dx = entry.x - origin.getX();
                    double dz = entry.z - origin.getZ();
                    double distSq = dx * dx + dz * dz;
                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq;
                        closest = entry;
                    }
                }
            }
        }

        // If none found in current dimension, dynamically create a guaranteed structure nearby
        if (closest == null) {
            closest = generateFallback(world, origin, targetType);
        }

        return Optional.ofNullable(closest);
    }

    private static StructureEntry generateFallback(ServerWorld world, BlockPos playerPos, String targetType) {
        String dimId = world.getRegistryKey().getValue().toString();
        String type = targetType;
        if (type == null || type.isEmpty() || !isValidDimensionForType(world, type)) {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                type = TYPE_FORGE;
            } else if (world.getRegistryKey().equals(World.NETHER)) {
                type = TYPE_SHRINE;
            } else {
                type = TYPE_ALTAR;
            }
        }

        // Generate ~450 blocks offset from player in a safe chunk
        int targetX = ((playerPos.getX() >> 4) + 28) * 16 + 8;
        int targetZ = ((playerPos.getZ() >> 4) + 28) * 16 + 8;
        int targetY;

        // Ensure the target chunk is generated and loaded so heightmap calculations and block placement succeed
        world.getChunk(targetX >> 4, targetZ >> 4);

        BlockPos targetPos;
        if (type.equals(TYPE_FORGE)) {
            int surfaceY = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, targetX, targetZ);
            targetY = (surfaceY < 60) ? 64 : surfaceY;
            targetPos = new BlockPos(targetX, targetY, targetZ);
            com.chainores.world.feature.AbandonedForgeFeature.buildStructure(world, targetPos, world.random);
        } else if (type.equals(TYPE_SHRINE)) {
            targetY = 48;
            targetPos = new BlockPos(targetX, targetY, targetZ);
            com.chainores.world.feature.NetherShrineFeature.buildStructure(world, targetPos, world.random);
        } else {
            int endY = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
            targetY = (endY < 45) ? 60 : endY;
            targetPos = new BlockPos(targetX, targetY, targetZ);
            com.chainores.world.feature.CosmicAltarFeature.buildStructure(world, targetPos, world.random);
        }

        StructureEntry entry = new StructureEntry(type, dimId, targetX, targetY, targetZ);
        ENTRIES.add(entry);
        saveToFile(world);
        ChainOresMod.LOGGER.info("Registered and constructed guaranteed structure for {} at {} in {}", type, targetPos, dimId);
        return entry;
    }

    private static synchronized void ensureLoaded(ServerWorld world) {
        if (loaded) return;
        loaded = true;
        try {
            Path savePath = world.getServer().getSavePath(WorldSavePath.ROOT);
            File file = savePath.resolve("chainores_structures.json").toFile();
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    List<StructureEntry> list = GSON.fromJson(reader, LIST_TYPE);
                    if (list != null) {
                        ENTRIES.clear();
                        ENTRIES.addAll(list);
                        // Cleanse corrupted cross-dimension entries
                        ENTRIES.removeIf(entry -> {
                            if (entry.type().equals(TYPE_SHRINE) && !entry.dimension().contains("the_nether")) return true;
                            if (entry.type().equals(TYPE_ALTAR) && !entry.dimension().contains("the_end")) return true;
                            if (entry.type().equals(TYPE_FORGE) && !entry.dimension().contains("overworld")) return true;
                            return false;
                        });

                        // Enforce minimum separation between structures of same type and dimension
                        double minDistance = com.chainores.config.ChainOresConfig.get().minStructureDistanceBlocks;
                        List<StructureEntry> pruned = new ArrayList<>();
                        for (StructureEntry entry : ENTRIES) {
                            boolean duplicate = false;
                            for (StructureEntry kept : pruned) {
                                if (kept.type().equalsIgnoreCase(entry.type()) && kept.dimension().equals(entry.dimension())) {
                                    double dx = kept.x() - entry.x();
                                    double dz = kept.z() - entry.z();
                                    if (dx * dx + dz * dz < minDistance * minDistance) {
                                        duplicate = true;
                                        break;
                                    }
                                }
                            }
                            if (!duplicate) {
                                pruned.add(entry);
                            }
                        }

                        if (pruned.size() != ENTRIES.size()) {
                            ChainOresMod.LOGGER.info("Pruned {} excess clustered structures down to {} properly spaced structures.",
                                    ENTRIES.size() - pruned.size(), pruned.size());
                            ENTRIES.clear();
                            ENTRIES.addAll(pruned);
                            saveToFile(world);
                        } else {
                            ChainOresMod.LOGGER.info("Loaded {} validated custom structures from save.", ENTRIES.size());
                        }
                    }
                }
            }
        } catch (Exception e) {
            ChainOresMod.LOGGER.error("Failed to load structure tracker data: {}", e.getMessage());
        }
    }

    private static synchronized void saveToFile(ServerWorld world) {
        try {
            Path savePath = world.getServer().getSavePath(WorldSavePath.ROOT);
            File file = savePath.resolve("chainores_structures.json").toFile();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(ENTRIES, writer);
            }
        } catch (Exception e) {
            ChainOresMod.LOGGER.error("Failed to save structure tracker data: {}", e.getMessage());
        }
    }

    public static String getLocalizedName(String type) {
        return switch (type) {
            case TYPE_FORGE -> "Forja Abandonada (Abandoned Forge)";
            case TYPE_SHRINE -> "Santuario del Nether (Nether Shrine)";
            case TYPE_ALTAR -> "Altar Cósmico (Cosmic Altar)";
            default -> "Estructura Ancestral";
        };
    }
}
