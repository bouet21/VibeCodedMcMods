package com.chainores.util;

import com.chainores.config.ChainOresConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Utility to verify block modifications against server protections:
 * - FTB Chunks
 * - Open Parties and Claims
 * - FLAN / GommeHD
 * - Vanilla spawn protection
 * - Adventure mode constraints
 */
public class ServerProtectionHelper {

    /**
     * Checks if a player is permitted to break a block at the specified position.
     * Triggers Fabric's PlayerBlockBreakEvents.BEFORE to ensure full mod claim compatibility.
     */
    public static boolean canBreakBlock(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        if (!ChainOresConfig.get().enableClaimProtectionCheck) {
            return true;
        }
        if (player == null || world == null || pos == null) {
            return false;
        }

        // 1. Vanilla spawn protection & adventure mode check
        if (!player.canModifyBlocks()) {
            return false;
        }
        if (world.getServer() != null && world.getServer().isSpawnProtected(world, pos, player)) {
            return false;
        }

        // 2. Fabric API PlayerBlockBreakEvents check (respected by FTB Chunks, FLAN, etc.)
        return PlayerBlockBreakEvents.BEFORE.invoker()
                .beforeBlockBreak(world, player, pos, state, world.getBlockEntity(pos));
    }

    /**
     * Checks if a player is permitted to interact with/modify a block at the specified position
     * (e.g. tilling soil into farmland or flattening paths).
     */
    public static boolean canModifyAt(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        if (!ChainOresConfig.get().enableClaimProtectionCheck) {
            return true;
        }
        if (player == null || world == null || pos == null) {
            return false;
        }
        if (!player.canModifyBlocks()) {
            return false;
        }
        if (world.getServer() != null && world.getServer().isSpawnProtected(world, pos, player)) {
            return false;
        }
        return true;
    }
}
