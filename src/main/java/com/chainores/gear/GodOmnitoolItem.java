package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class GodOmnitoolItem extends Item {
    private static boolean IS_WORKING = false;

    public GodOmnitoolItem(Settings settings) {
        super(settings.maxDamage(50000).fireproof());
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        return 65.0f; // Universal god mining speed
    }

    @Override
    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        return true; // Drops ANY block in the game!
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("★ HERRAMIENTA UNIVERSAL DE LOS DIOSES ★").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Combina en un solo artefacto:").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal(" • Pico Divino: Minería y excavación masiva 5x5").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal(" • Hacha del Creador: TreeCapitator total de bosques").formatted(Formatting.GREEN));
        textConsumer.accept(Text.literal(" • Pala Cósmica: Nivelación y auto-fundición").formatted(Formatting.GOLD));
        textConsumer.accept(Text.literal(" • Azada del Edén: Arado e hidratación 9x9 instantáneo").formatted(Formatting.LIGHT_PURPLE));
        textConsumer.accept(Text.literal(" • Filo del Juicio: 120 Daño Sagrado y Rayo Divino").formatted(Formatting.RED, Formatting.BOLD));
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getEntityWorld() instanceof ServerWorld serverWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                serverWorld.spawnEntity(lightning);
            }
            target.damage(serverWorld, serverWorld.getDamageSources().magic(), 120.0f);
            attacker.playSound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 1.2f, 1.0f);
        }
        super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ServerPlayerEntity player = context.getPlayer() instanceof ServerPlayerEntity sp ? sp : null;

        if (!world.isClient() && player != null) {
            if (player.isSneaking()) {
                // Shift + Click: Till 9x9 Farmland
                if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT_PATH)) {
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            BlockPos p = pos.add(dx, 0, dz);
                            BlockState bs = world.getBlockState(p);
                            if ((bs.isOf(Blocks.DIRT) || bs.isOf(Blocks.GRASS_BLOCK) || bs.isOf(Blocks.DIRT_PATH)) && world.getBlockState(p.up()).isAir()) {
                                world.setBlockState(p, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                            }
                        }
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            } else {
                // Normal Click: Path 9x9 or strip logs
                if (state.isOf(Blocks.GRASS_BLOCK)) {
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            BlockPos p = pos.add(dx, 0, dz);
                            if (world.getBlockState(p).isOf(Blocks.GRASS_BLOCK) && world.getBlockState(p.up()).isAir()) {
                                world.setBlockState(p, Blocks.DIRT_PATH.getDefaultState());
                            }
                        }
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_WORKING) {
                if (state.isIn(BlockTags.LOGS)) {
                    fellFullTree(serverWorld, player, stack, pos);
                } else if (state.getBlock() instanceof CropBlock crop) {
                    harvestEden(serverWorld, player, stack, pos);
                } else if (player.isSneaking()) {
                    omniMineArea(serverWorld, player, stack, pos, 2); // 5x5
                }
            }

            // Attract drops
            serverWorld.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(16.0), e -> true).forEach(e -> {
                e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
            });
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void fellFullTree(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos start) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        IS_WORKING = true;
        int count = 0;
        try {
            while (!queue.isEmpty() && count < 256) {
                BlockPos current = queue.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.add(dx, dy, dz);
                            if (!visited.contains(neighbor)) {
                                BlockState bs = world.getBlockState(neighbor);
                                if (bs.isIn(BlockTags.LOGS) || bs.isIn(BlockTags.LEAVES)) {
                                    visited.add(neighbor);
                                    queue.add(neighbor);
                                    world.breakBlock(neighbor, true, player);
                                    count++;
                                    if (count >= 256) break;
                                }
                            }
                        }
                    }
                }
            }
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        } finally {
            IS_WORKING = false;
        }
    }

    private void harvestEden(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center) {
        IS_WORKING = true;
        try {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState bs = world.getBlockState(p);
                    if (bs.getBlock() instanceof CropBlock c && c.isMature(bs)) {
                        world.breakBlock(p, true, player);
                        world.setBlockState(p, c.withAge(3));
                    }
                }
            }
        } finally {
            IS_WORKING = false;
        }
    }

    private void omniMineArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, int radius) {
        Direction facing = Direction.getEntityFacingOrder(player)[0];
        IS_WORKING = true;
        try {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    BlockPos targetPos;
                    if (facing.getAxis() == Direction.Axis.Y) {
                        targetPos = center.add(dx, 0, dy);
                    } else if (facing.getAxis() == Direction.Axis.X) {
                        targetPos = center.add(0, dy, dx);
                    } else {
                        targetPos = center.add(dx, dy, 0);
                    }

                    BlockState targetState = world.getBlockState(targetPos);
                    if (!targetState.isAir() && !targetState.isOf(Blocks.BEDROCK)) {
                        world.breakBlock(targetPos, true, player);
                    }
                }
            }
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.8f);
        } finally {
            IS_WORKING = false;
        }
    }
}
