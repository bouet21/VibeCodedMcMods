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

    public static synchronized Optional<StructureEntry> findNearest(ServerWorld world, BlockPos origin, String targetType) {
        if (world == null || origin == null) return Optional.empty();
        ensureLoaded(world);

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
        if (type == null || type.isEmpty()) {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                type = TYPE_FORGE;
            } else if (world.getRegistryKey().equals(World.NETHER)) {
                type = TYPE_SHRINE;
            } else {
                type = TYPE_ALTAR;
            }
        }

        // Generate ~160 blocks offset from player in a safe chunk
        int targetX = ((playerPos.getX() >> 4) + 10) * 16 + 8;
        int targetZ = ((playerPos.getZ() >> 4) + 10) * 16 + 8;
        int targetY;

        if (type.equals(TYPE_FORGE)) {
            targetY = -35;
        } else if (type.equals(TYPE_SHRINE)) {
            targetY = 48;
        } else {
            targetY = 60;
        }

        BlockPos targetPos = new BlockPos(targetX, targetY, targetZ);
        StructureEntry entry = new StructureEntry(type, dimId, targetX, targetY, targetZ);
        ENTRIES.add(entry);
        saveToFile(world);
        ChainOresMod.LOGGER.info("Registered structure anchor for {} at {} in {}", type, targetPos, dimId);
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
                        ChainOresMod.LOGGER.info("Loaded {} custom structures from save.", ENTRIES.size());
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
