package com.chainores.gear;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
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
import java.util.function.Consumer;

public class ChainShovelItem extends Item {
    private final int tier;
    private static final ThreadLocal<Boolean> IS_EXCAVATING = ThreadLocal.withInitial(() -> false);

    public ChainShovelItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.shovel(material, bonusDamage, -3.0f));
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
            if (com.chainores.modifier.ChainModifiers.GEM_BLOOD.equals(gem)) {
                attacker.heal(2.0f);
            }
            float mult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(stack);
            if (mult > 1.0f) {
                target.timeUntilRegen = 0;
                target.damage(world, world.getDamageSources().magic(), (mult - 1.0f) * 10.0f);
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
        String perk = getShovelPerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Habilidad de Excavación: ").formatted(Formatting.AQUA)
                    .append(Text.literal(perk).formatted(Formatting.YELLOW)));
        }
    }

    public static String getShovelPerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Excavación Rápida: Velocidad en arena, tierra y grava";
            case 10 -> "Criba de Sílex: 100% de probabilidad de obtener pedernal de la grava";
            case 20 -> "Quitanieves Criogénico: Despeja nieve y hielo instantáneamente";
            case 30 -> "Horno de Excavación: Funde arena en cristal y arcilla en ladrillos al cavar";
            case 40 -> "Excavador 3x3: Cava tierra y arena en área 3x3 al agacharse";
            case 50 -> "Topografía Rúnica: Otorga fragmentos de experiencia al cavar sustrato";
            case 60 -> "Aplanadora Tectónica: Crea caminos de tierra en 3x3 al hacer clic derecho";
            case 70 -> "Sonar Geológico: Revela cavernas subterráneas y absorbe lodo con rapidez";
            case 80 -> "Magma Terrestre: Auto-fundición continua y excavación ígnea";
            case 90 -> "Excavación Espectral: Niega la gravedad de la arena y grava circundante";
            case 100 -> "Pala Titánica: Excavación 3x3 profunda con succión de recursos";
            case 115 -> "Viento Subterráneo: Despeja dunas de arena a velocidad extrema";
            case 125 -> "Gravedad Cero: Cava arena y grava flotante sin que se derrumbe";
            case 135 -> "Excavador 5x5: Excavación masiva 5x5 de biomas desérticos o llanuras";
            case 140 -> "Suelo Fértil: Convierte tierra árida en tierra bendita enriquecida";
            case 150 -> "★ APLANADORA DE LOS DIOSES ★: Nivelación 5x5 total, auto-fundición y atracción gravitacional";
            default -> "Excavación de área al agacharse";
        };
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntityAccessor(context);

        if (context.getPlayer() != null && isPathable(state)) {
            if (!world.isClient() && world instanceof ServerWorld serverWorld && context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
                int radius = (tier >= 135) ? 2 : (tier >= 40 ? 1 : 0);
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos p = pos.add(dx, 0, dz);
                        BlockState bs = world.getBlockState(p);
                        if (isPathable(bs)) {
                            BlockPos up = p.up();
                            BlockState upState = world.getBlockState(up);
                            if (upState.isAir() || upState.isReplaceable()) {
                                if (!com.chainores.util.ServerProtectionHelper.canModifyAt(serverWorld, serverPlayer, p)) {
                                    continue;
                                }
                                if (!upState.isAir()) {
                                    world.breakBlock(up, true, serverPlayer);
                                }
                                world.setBlockState(p, Blocks.DIRT_PATH.getDefaultState());
                            }
                        }
                    }
                }
                world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                context.getStack().damage(1, context.getPlayer(), EquipmentSlot.MAINHAND);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    private boolean isPathable(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK)
                || state.isOf(Blocks.DIRT)
                || state.isOf(Blocks.COARSE_DIRT)
                || state.isOf(Blocks.ROOTED_DIRT)
                || state.isOf(Blocks.PODZOL)
                || state.isOf(Blocks.MYCELIUM);
    }

    private void PlayerEntityAccessor(ItemUsageContext context) {}

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_EXCAVATING.get() && state.isIn(BlockTags.SHOVEL_MINEABLE) && com.chainores.config.ChainOresConfig.get().enableAreaMining && (player.isSneaking() || tier >= 100)) {
                int radius = (tier >= 135) ? 2 : (tier >= 40 ? 1 : 0);
                if (radius > 0) {
                    excavateArea(serverWorld, player, stack, pos, radius);
                }
            }

            // Auto-smelt for T30 / T80 / T150
            if (tier == 30 || tier == 80 || tier == 150) {
                applySuperSmelt(serverWorld, pos);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void excavateArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, int radius) {
        Direction facing = Direction.getEntityFacingOrder(player)[0];
        int effRadius = Math.min(radius, com.chainores.config.ChainOresConfig.get().areaMiningMaxRadius);
        IS_EXCAVATING.set(true);
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
                    if (targetState.isIn(BlockTags.SHOVEL_MINEABLE)) {
                        if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, targetPos, targetState)) {
                            continue;
                        }
                        world.breakBlock(targetPos, true, player);
                        if (tier < 135) {
                            stack.damage(1, player, EquipmentSlot.MAINHAND);
                        }
                        if (stack.isEmpty()) break;
                    }
                }
                if (stack.isEmpty()) break;
            }

            consolidateDrops(world, player, 10.0);
            if (tier >= 125) {
                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(8.0), e -> !e.isRemoved()).forEach(e -> {
                    e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
                });
            }
        } finally {
            IS_EXCAVATING.set(false);
        }
    }

    private void applySuperSmelt(ServerWorld world, BlockPos pos) {
        world.getEntitiesByClass(ItemEntity.class, new net.minecraft.util.math.Box(pos).expand(2.0), e -> !e.isRemoved()).forEach(item -> {
            ItemStack drop = item.getStack();
            if (drop.isOf(Blocks.SAND.asItem()) || drop.isOf(Blocks.RED_SAND.asItem())) {
                item.setStack(new ItemStack(Blocks.GLASS, drop.getCount()));
            } else if (drop.isOf(Items.CLAY_BALL)) {
                item.setStack(new ItemStack(Items.BRICK, drop.getCount()));
            }
        });
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
