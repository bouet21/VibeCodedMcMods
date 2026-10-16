package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
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
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class GodOmnitoolItem extends Item {
    private static final ThreadLocal<Boolean> IS_WORKING = ThreadLocal.withInitial(() -> false);

    public GodOmnitoolItem(Settings settings) {
        super(settings.maxDamage(50000).fireproof());
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        return 65.0f * com.chainores.modifier.ChainModifiers.getMiningSpeedMultiplier(stack);
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

            // Gem Modifiers
            for (String gem : com.chainores.modifier.ChainModifiers.getGems(stack)) {
                String base = com.chainores.modifier.ChainModifiers.getBaseGemType(gem);
                int gemTier = com.chainores.modifier.ChainModifiers.getGemTier(gem);
                if (com.chainores.modifier.ChainModifiers.GEM_BLOOD.equals(base)) {
                    attacker.heal(5.0f * gemTier);
                    serverWorld.spawnParticles(ParticleTypes.HEART, attacker.getX(), attacker.getY() + 1.2, attacker.getZ(), 3 * gemTier, 0.2, 0.2, 0.2, 0.0);
                } else if (com.chainores.modifier.ChainModifiers.GEM_GENESIS.equals(base)) {
                    target.damage(serverWorld, serverWorld.getDamageSources().magic(), 30.0f * gemTier);
                } else if (com.chainores.modifier.ChainModifiers.GEM_VOID.equals(base)) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30 + 15 * gemTier, gemTier - 1));
                } else if (com.chainores.modifier.ChainModifiers.GEM_CORE.equals(base)) {
                    target.setOnFireFor(8 * gemTier);
                }
            }

            // Reforge Damage Boost
            float mult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(stack);
            if (mult > 1.0f) {
                target.timeUntilRegen = 0;
                target.damage(serverWorld, serverWorld.getDamageSources().magic(), (mult - 1.0f) * 60.0f);
            }

            attacker.playSound(SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, 1.2f, 1.0f);
        }
        super.postHit(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            com.chainores.modifier.ChainModifiers.applyReforgeHeldBuffs(entity, stack);

            if (entity instanceof ServerPlayerEntity player) {
                // Escarcha Agrícola Divina: Protect farmland & keep hydrated
                BlockPos under = player.getBlockPos().down();
                BlockState bs = world.getBlockState(under);
                if (bs.isOf(Blocks.FARMLAND)) {
                    if (bs.get(FarmlandBlock.MOISTURE) < 7) {
                        world.setBlockState(under, bs.with(FarmlandBlock.MOISTURE, 7));
                    }
                } else if (bs.isOf(Blocks.DIRT) && isAnyCrop(world.getBlockState(player.getBlockPos()))) {
                    world.setBlockState(under, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                }
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ServerPlayerEntity player = context.getPlayer() instanceof ServerPlayerEntity sp ? sp : null;

        if (world.isClient()) {
            if (isAnyCrop(state) || isTillable(state)) return ActionResult.SUCCESS;
            return super.useOnBlock(context);
        }

        if (player != null && world instanceof ServerWorld serverWorld) {
            // CASE 1: CROP RIGHT-CLICK (Eden Harvest or Eden Growth)
            if (isAnyCrop(state)) {
                if (isMatureCrop(state)) {
                    harvestEden(serverWorld, player, context.getStack(), pos);
                    world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    world.playSound(null, pos, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.8f, 1.2f);
                    return ActionResult.SUCCESS;
                } else {
                    // 9x9 Instant Max Maturity
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            BlockPos p = pos.add(dx, 0, dz);
                            BlockState bs = world.getBlockState(p);
                            if (bs.getBlock() instanceof CropBlock crop) {
                                world.setBlockState(p, crop.withAge(crop.getMaxAge()));
                            } else if (bs.isOf(Blocks.NETHER_WART)) {
                                world.setBlockState(p, bs.with(NetherWartBlock.AGE, 3));
                            } else if (bs.isOf(Blocks.COCOA)) {
                                world.setBlockState(p, bs.with(CocoaBlock.AGE, 2));
                            }
                        }
                    }
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 12, 1.0, 0.3, 1.0, 0.0);
                    world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }

            // CASE 2: SOIL RIGHT-CLICK
            if (isTillable(state)) {
                if (player.isSneaking() && state.isOf(Blocks.GRASS_BLOCK)) {
                    // Shift + Click: Path 9x9
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            BlockPos p = pos.add(dx, 0, dz);
                            if (world.getBlockState(p).isOf(Blocks.GRASS_BLOCK)) {
                                BlockPos up = p.up();
                                BlockState upState = world.getBlockState(up);
                                if (upState.isAir() || upState.isReplaceable()) {
                                    if (!com.chainores.util.ServerProtectionHelper.canModifyAt(serverWorld, player, p)) continue;
                                    if (!upState.isAir()) world.breakBlock(up, true, player);
                                    world.setBlockState(p, Blocks.DIRT_PATH.getDefaultState());
                                }
                            }
                        }
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                } else {
                    // Normal Click: Till 9x9 Hydrated Farmland
                    for (int dx = -4; dx <= 4; dx++) {
                        for (int dz = -4; dz <= 4; dz++) {
                            BlockPos p = pos.add(dx, 0, dz);
                            BlockState bs = world.getBlockState(p);
                            if (isTillable(bs)) {
                                BlockPos up = p.up();
                                BlockState upState = world.getBlockState(up);
                                if (upState.isAir() || upState.isReplaceable()) {
                                    if (!com.chainores.util.ServerProtectionHelper.canModifyAt(serverWorld, player, p)) continue;
                                    if (!upState.isAir()) world.breakBlock(up, true, player);
                                    world.setBlockState(p, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
                                }
                            }
                        }
                    }
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 12, 1.0, 0.3, 1.0, 0.0);
                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_WORKING.get()) {
                if (state.isIn(BlockTags.LOGS) && com.chainores.config.ChainOresConfig.get().enableTreeCapitator) {
                    fellFullTree(serverWorld, player, stack, pos);
                } else if (isAnyCrop(state)) {
                    IS_WORKING.set(true);
                    try {
                        // Replant broken central crop
                        replantCrop(serverWorld, pos, state);
                        // Harvest surrounding 9x9
                        harvestEden(serverWorld, player, stack, pos);
                    } finally {
                        IS_WORKING.set(false);
                    }
                } else if (player.isSneaking() && com.chainores.config.ChainOresConfig.get().enableAreaMining) {
                    omniMineArea(serverWorld, player, stack, pos, 2); // 5x5
                }
            }

            // Consolidate & attract drops
            consolidateDrops(serverWorld, player, 16.0);
            serverWorld.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(16.0), e -> !e.isRemoved()).forEach(e -> {
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

        int maxLogs = Math.min(256, com.chainores.config.ChainOresConfig.get().treeCapitatorMaxBlocks);
        IS_WORKING.set(true);
        int count = 0;
        try {
            while (!queue.isEmpty() && count < maxLogs) {
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
                                    if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, neighbor, bs)) {
                                        continue;
                                    }
                                    queue.add(neighbor);
                                    world.breakBlock(neighbor, true, player);
                                    count++;
                                    if (count >= maxLogs) break;
                                }
                            }
                        }
                    }
                }
            }
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        } finally {
            IS_WORKING.set(false);
        }
    }

    private void harvestEden(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center) {
        IS_WORKING.set(true);
        try {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState bs = world.getBlockState(p);
                    if (isAnyCrop(bs) && isMatureCrop(bs)) {
                        if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, p, bs)) {
                            continue;
                        }
                        if (bs.getBlock() instanceof CropBlock c) {
                            world.breakBlock(p, true, player);
                            world.setBlockState(p, c.withAge(Math.min(3, c.getMaxAge() - 1)));
                        } else if (bs.isOf(Blocks.NETHER_WART)) {
                            world.breakBlock(p, true, player);
                            world.setBlockState(p, Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0));
                        } else if (bs.isOf(Blocks.COCOA)) {
                            world.breakBlock(p, true, player);
                            world.setBlockState(p, bs.with(CocoaBlock.AGE, 0));
                        } else if (bs.isOf(Blocks.SWEET_BERRY_BUSH)) {
                            world.spawnEntity(new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, new ItemStack(Items.SWEET_BERRIES, world.random.nextInt(3) + 2)));
                            world.setBlockState(p, bs.with(SweetBerryBushBlock.AGE, 1));
                        }

                        // Fruit of Eden bonus
                        if (world.random.nextFloat() < 0.15f) {
                            ItemStack fruit = (world.random.nextFloat() < 0.08f) ? new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
                                    : (world.random.nextBoolean() ? new ItemStack(Items.GOLDEN_APPLE) : new ItemStack(Items.GOLDEN_CARROT, 2));
                            world.spawnEntity(new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, fruit));
                        }
                    }
                }
            }
        } finally {
            IS_WORKING.set(false);
        }
    }

    private void omniMineArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, int radius) {
        Direction facing = Direction.getEntityFacingOrder(player)[0];
        int effRadius = Math.min(radius, com.chainores.config.ChainOresConfig.get().areaMiningMaxRadius);
        IS_WORKING.set(true);
        try {
            for (int dx = -effRadius; dx <= effRadius; dx++) {
                for (int dy = -effRadius; dy <= effRadius; dy++) {
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
                        if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, targetPos, targetState)) {
                            continue;
                        }
                        world.breakBlock(targetPos, true, player);
                    }
                }
            }
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.8f);
        } finally {
            IS_WORKING.set(false);
        }
    }

    private void replantCrop(ServerWorld world, BlockPos pos, BlockState brokenState) {
        if (!world.getBlockState(pos).isAir()) return;

        if (brokenState.getBlock() instanceof CropBlock crop) {
            BlockState soil = world.getBlockState(pos.down());
            if (soil.isOf(Blocks.FARMLAND) || soil.isOf(Blocks.DIRT) || soil.isOf(Blocks.GRASS_BLOCK)) {
                world.setBlockState(pos, crop.withAge(Math.min(3, crop.getMaxAge() - 1)));
            }
        } else if (brokenState.isOf(Blocks.NETHER_WART)) {
            if (world.getBlockState(pos.down()).isOf(Blocks.SOUL_SAND)) {
                world.setBlockState(pos, Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0));
            }
        } else if (brokenState.isOf(Blocks.COCOA)) {
            world.setBlockState(pos, brokenState.with(CocoaBlock.AGE, 0));
        }
    }

    private boolean isTillable(BlockState state) {
        return state.isOf(Blocks.DIRT)
                || state.isOf(Blocks.GRASS_BLOCK)
                || state.isOf(Blocks.DIRT_PATH)
                || state.isOf(Blocks.COARSE_DIRT)
                || state.isOf(Blocks.ROOTED_DIRT);
    }

    private boolean isAnyCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock
                || state.isOf(Blocks.NETHER_WART)
                || state.isOf(Blocks.COCOA)
                || state.isOf(Blocks.SWEET_BERRY_BUSH);
    }

    private boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (state.isOf(Blocks.NETHER_WART)) {
            return state.get(NetherWartBlock.AGE) >= 3;
        }
        if (state.isOf(Blocks.COCOA)) {
            return state.get(CocoaBlock.AGE) >= 2;
        }
        if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return state.get(SweetBerryBushBlock.AGE) >= 2;
        }
        return false;
    }

    private void consolidateDrops(ServerWorld world, ServerPlayerEntity player, double radius) {
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(radius), e -> !e.isRemoved());
        for (int i = 0; i < items.size(); i++) {
            ItemEntity a = items.get(i);
            if (a.isRemoved()) continue;
            for (int j = i + 1; j < items.size(); j++) {
                ItemEntity b = items.get(j);
                if (b.isRemoved()) continue;
                if (ItemStack.areItemsAndComponentsEqual(a.getStack(), b.getStack())) {
                    int space = a.getStack().getMaxCount() - a.getStack().getCount();
                    if (space > 0) {
                        int toTake = Math.min(space, b.getStack().getCount());
                        a.getStack().increment(toTake);
                        b.getStack().decrement(toTake);
                        if (b.getStack().isEmpty()) {
                            b.discard();
                        }
                    }
                }
            }
        }
    }
}
