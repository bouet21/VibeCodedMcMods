package com.chainores.gear;

import com.chainores.block.ChainOreBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class ChainPickaxeItem extends Item {
    private final int tier;
    private static final ThreadLocal<Boolean> IS_CHAINING = ThreadLocal.withInitial(() -> false);

    public ChainPickaxeItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.pickaxe(material, bonusDamage, -2.8f));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        return super.getMiningSpeed(stack, state) * com.chainores.modifier.ChainModifiers.getMiningSpeedMultiplier(stack);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        if (attacker.getEntityWorld() instanceof ServerWorld world) {
            String gem = com.chainores.modifier.ChainModifiers.getGem(stack);
            if (com.chainores.modifier.ChainModifiers.GEM_BLOOD.equals(com.chainores.modifier.ChainModifiers.getBaseGemType(gem))) {
                attacker.heal(2.0f * com.chainores.modifier.ChainModifiers.getGemTier(gem));
            }
            float mult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(stack);
            if (mult > 1.0f) {
                target.timeUntilRegen = 0;
                target.damage(world, world.getDamageSources().magic(), (mult - 1.0f) * 12.0f);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            com.chainores.modifier.ChainModifiers.applyReforgeHeldBuffs(entity, stack);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        
        int maxVein = Math.min(64, 4 + (tier / 10) * 4);
        textConsumer.accept(Text.literal("⚡ Minería en Cadena: ").formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal("Hasta " + maxVein + " minerales por veta (Shift al picar)").formatted(Formatting.YELLOW)));

        String perk = getPickaxePerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("✦ Habilidad Especial: ").formatted(Formatting.AQUA)
                    .append(Text.literal(perk).formatted(Formatting.GREEN)));
        }
    }

    public static String getPickaxePerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Magnetismo: Atrae los ítems minados directo hacia ti";
            case 10 -> "Impacto Contundente: Destruye roca y piedra con gran rapidez";
            case 20 -> "Corte Criogénico: Rompe hielo, hielo azul y obsidiana con doble velocidad";
            case 30 -> "Super-Fundición: Auto-funde piedra lisa, ladrillos y cristal al picar";
            case 40 -> "Tala Silvestre: Picar troncos de árboles rompe el árbol entero en cadena";
            case 50 -> "Fortuna Rúnica: Probabilidad del 35% de duplicar minerales y gemas";
            case 60 -> "Túnel Tectónico: Excava automáticamente en túnel vertical 1x2 hacia adelante";
            case 70 -> "Eco Sónico: Minería en cubo 3x3 al picar agachado (Shift)";
            case 80 -> "Extinción Ígnea: Apaga y petrifica la lava cercana al picar";
            case 90 -> "Cosechador Espectral: Otorga el doble de experiencia al picar";
            case 100 -> "Martillo del Coloso: Minería 3x3 reforzada con velocidad titánica";
            case 115 -> "Vínculo de Ender: Los drops se teletransportan a tus pies sin caer al vacío";
            case 125 -> "Agujero Negro: Minería 3x3 hiperveloz con succión de ítems";
            case 135 -> "Falla Dimensional: Minería masiva en cubo 5x5 al picar agachado";
            case 140 -> "Toque de la Creación: Probabilidad del 20% de transmutar a una gema valiosa";
            case 150 -> "★ MARTILLO DE LOS DIOSES ★: Minería 5x5 instantánea, Auto-Smelt, Imán y Fortuna Divina";
            default -> "";
        };
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && miner instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;

            // Prevent recursion when breaking chained blocks
            if (!IS_CHAINING.get()) {
                // 1. Vein Mining on Chain Ores & Vanilla Ores (when player is sneaking or tier >= 50)
                if (com.chainores.config.ChainOresConfig.get().enableVeinMining && (player.isSneaking() || tier >= 50) && (state.getBlock() instanceof ChainOreBlock || isOreBlock(state))) {
                    veinMine(serverWorld, player, stack, pos, state);
                }

                // 2. 5x5 Mining for Tier 135 & 150 (on non-ores when sneaking)
                else if (com.chainores.config.ChainOresConfig.get().enableAreaMining && (tier >= 135) && player.isSneaking() && state.getHardness(world, pos) > 0) {
                    mineArea(serverWorld, player, stack, pos, state, 2);
                }
                // 3. 3x3 Mining for Tier 70, 100, 125 (on non-ores when sneaking)
                else if (com.chainores.config.ChainOresConfig.get().enableAreaMining && (tier >= 70) && player.isSneaking() && state.getHardness(world, pos) > 0) {
                    mineArea(serverWorld, player, stack, pos, state, 1);
                }
            }

            // Magnetism (attracts nearby drop entities)
            if (tier == 1 || tier == 115 || tier == 125 || tier >= 150) {
                attractDrops(serverWorld, player);
            }

            // Super-Smelt block drops (Tier 30 & 150)
            if (tier == 30 || tier >= 150) {
                applySuperSmelt(serverWorld, pos, state);
            }

            // Tier 40: Timber tree feller
            if (tier == 40 && state.isIn(BlockTags.LOGS) && !IS_CHAINING.get() && com.chainores.config.ChainOresConfig.get().enableTreeCapitator) {
                fellTree(serverWorld, player, stack, pos);
            }

            // Tier 50: Runic Fortune extra drop
            if (tier == 50 && (state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.IRON_ORES) || state.getBlock() instanceof ChainOreBlock)) {
                if (world.random.nextFloat() < 0.35f) {
                    state.getBlock().afterBreak(world, player, pos, state, null, stack);
                    player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
                }
            }

            // Tier 60: 1x2 tunnel forward
            if (tier == 60 && !player.isSneaking() && state.getHardness(world, pos) > 0 && !IS_CHAINING.get()) {
                BlockPos above = pos.up();
                BlockState aboveState = world.getBlockState(above);
                if (aboveState.getHardness(world, above) > 0 && com.chainores.util.ServerProtectionHelper.canBreakBlock(serverWorld, player, above, aboveState)) {
                    serverWorld.breakBlock(above, true, player);
                }
            }

            // Tier 80: Extinguish nearby lava into obsidian
            if (tier == 80) {
                for (Direction dir : Direction.values()) {
                    BlockPos p = pos.offset(dir);
                    if (world.getBlockState(p).isOf(Blocks.LAVA) && com.chainores.util.ServerProtectionHelper.canModifyAt(serverWorld, player, p)) {
                        world.setBlockState(p, Blocks.OBSIDIAN.getDefaultState());
                        world.playSound(null, p, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 1.2f);
                    }
                }
            }

            // Tier 140: Creation transmutation (20% chance to drop bonus gem)
            if (tier == 140 && world.random.nextFloat() < 0.20f) {
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(net.minecraft.item.Items.DIAMOND));
                world.spawnEntity(item);
                player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.8f);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private boolean isOreBlock(BlockState state) {
        return state.isIn(BlockTags.COAL_ORES)
                || state.isIn(BlockTags.IRON_ORES)
                || state.isIn(BlockTags.COPPER_ORES)
                || state.isIn(BlockTags.GOLD_ORES)
                || state.isIn(BlockTags.REDSTONE_ORES)
                || state.isIn(BlockTags.LAPIS_ORES)
                || state.isIn(BlockTags.DIAMOND_ORES)
                || state.isIn(BlockTags.EMERALD_ORES);
    }

    /**
     * Vein mining algorithm: BFS to chain connected matching ore blocks.
     * Respects server claims (FTB Chunks, etc.) and configurable limits.
     */
    private void veinMine(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos startPos, BlockState targetState) {
        int configMax = com.chainores.config.ChainOresConfig.get().veinMiningMaxBlocks;
        int maxChain = Math.min(configMax, 4 + (tier / 10) * 4);
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        IS_CHAINING.set(true);
        int brokenCount = 0;

        try {
            while (!queue.isEmpty() && brokenCount < maxChain) {
                BlockPos current = queue.poll();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;

                            BlockPos neighbor = current.add(dx, dy, dz);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);

                                BlockState neighborState = world.getBlockState(neighbor);
                                if (neighborState.isOf(targetState.getBlock())) {
                                    if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, neighbor, neighborState)) {
                                        continue;
                                    }
                                    queue.add(neighbor);
                                    world.breakBlock(neighbor, true, player);
                                    brokenCount++;
                                    stack.damage(1, player, EquipmentSlot.MAINHAND);

                                    if (brokenCount >= maxChain || stack.isEmpty()) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            IS_CHAINING.set(false);
        }

        if (brokenCount > 0) {
            consolidateDrops(world, player, 14.0);
            float pitch = Math.min(2.0f, 1.0f + brokenCount * 0.05f);
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, pitch);
        }
    }

    private void applySuperSmelt(ServerWorld world, BlockPos pos, BlockState state) {
        Box box = new Box(pos).expand(1.5);
        world.getEntitiesByClass(ItemEntity.class, box, e -> true).forEach(item -> {
            ItemStack dropStack = item.getStack();
            if (dropStack.isOf(Blocks.COBBLESTONE.asItem())) {
                item.setStack(new ItemStack(Blocks.STONE, dropStack.getCount()));
            } else if (dropStack.isOf(Blocks.COBBLED_DEEPSLATE.asItem())) {
                item.setStack(new ItemStack(Blocks.DEEPSLATE, dropStack.getCount()));
            } else if (dropStack.isOf(Blocks.SAND.asItem()) || dropStack.isOf(Blocks.RED_SAND.asItem())) {
                item.setStack(new ItemStack(Blocks.GLASS, dropStack.getCount()));
            } else if (dropStack.isOf(net.minecraft.item.Items.CLAY_BALL)) {
                item.setStack(new ItemStack(net.minecraft.item.Items.BRICK, dropStack.getCount()));
            }
        });
    }

    private void attractDrops(ServerWorld world, ServerPlayerEntity player) {
        Box box = player.getBoundingBox().expand(8.0);
        world.getEntitiesByClass(ItemEntity.class, box, e -> !e.isRemoved()).forEach(e -> {
            e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
        });
    }

    private void fellTree(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos start) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        int maxLogs = Math.min(32, com.chainores.config.ChainOresConfig.get().treeCapitatorMaxBlocks);
        IS_CHAINING.set(true);
        int count = 0;
        try {
            while (!queue.isEmpty() && count < maxLogs) {
                BlockPos current = queue.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos neighbor = current.add(dx, dy, dz);
                            BlockState neighborState = world.getBlockState(neighbor);
                            if (!visited.contains(neighbor) && neighborState.isIn(BlockTags.LOGS)) {
                                visited.add(neighbor);
                                if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, neighbor, neighborState)) {
                                    continue;
                                }
                                queue.add(neighbor);
                                world.breakBlock(neighbor, true, player);
                                stack.damage(1, player, EquipmentSlot.MAINHAND);
                                count++;
                                if (count >= maxLogs || stack.isEmpty()) break;
                            }
                        }
                    }
                }
            }
            consolidateDrops(world, player, 12.0);
        } finally {
            IS_CHAINING.set(false);
        }
    }

    private void mineArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, BlockState centerState, int radius) {
        Direction facing = Direction.getEntityFacingOrder(player)[0];
        int effRadius = Math.min(radius, com.chainores.config.ChainOresConfig.get().areaMiningMaxRadius);
        IS_CHAINING.set(true);
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
                    if (!targetState.isAir() && targetState.getHardness(world, targetPos) > 0 && targetState.getHardness(world, targetPos) <= centerState.getHardness(world, center) + 4.0f) {
                        if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, targetPos, targetState)) {
                            continue;
                        }
                        world.breakBlock(targetPos, true, player);
                        stack.damage(1, player, EquipmentSlot.MAINHAND);
                        if (stack.isEmpty()) break;
                    }
                }
            }
            consolidateDrops(world, player, 10.0);
        } finally {
            IS_CHAINING.set(false);
        }
    }

    private void consolidateDrops(ServerWorld world, ServerPlayerEntity player, double radius) {
        java.util.List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(radius), e -> !e.isRemoved());
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
