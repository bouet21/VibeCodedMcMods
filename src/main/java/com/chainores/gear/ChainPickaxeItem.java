package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class ChainPickaxeItem extends Item {
    private final int tier;

    public ChainPickaxeItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.pickaxe(material, bonusDamage, -2.8f));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        String perk = getPickaxePerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Habilidad Minera: ").formatted(Formatting.AQUA)
                    .append(Text.literal(perk).formatted(Formatting.GREEN)));
        }
    }

    public static String getPickaxePerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Magnetismo: Atrae los ítems minados directo hacia ti";
            case 10 -> "Impacto Contundente: Destruye roca y piedra con gran rapidez";
            case 20 -> "Corte Criogénico: Rompe hielo, hielo azul y obsidiana con doble velocidad";
            case 30 -> "Super-Fundición: Pica roca como piedra lisa y arena como cristal";
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

            // Tier 1 & Tier 115 & Tier 125 & Tier 150: Magnetism (attracts nearby drop entities)
            if (tier == 1 || tier == 115 || tier == 125 || tier >= 150) {
                attractDrops(serverWorld, player);
            }

            // Tier 30 & 150: Super-Smelt block drops (Cobble -> Stone, Sand -> Glass)
            if ((tier == 30 || tier >= 150) && !player.isSneaking()) {
                if (state.isOf(Blocks.COBBLESTONE)) {
                    world.breakBlock(pos, false);
                    ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Blocks.STONE));
                    world.spawnEntity(item);
                } else if (state.isOf(Blocks.SAND)) {
                    world.breakBlock(pos, false);
                    ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Blocks.GLASS));
                    world.spawnEntity(item);
                }
            }

            // Tier 40: Timber tree feller
            if (tier == 40 && state.isIn(BlockTags.LOGS)) {
                fellTree(serverWorld, player, stack, pos);
            }

            // Tier 50: Runic Fortune extra drop
            if (tier == 50 && (state.isIn(BlockTags.GOLD_ORES) || state.isIn(BlockTags.DIAMOND_ORES) || state.isIn(BlockTags.IRON_ORES))) {
                if (world.random.nextFloat() < 0.35f) {
                    state.getBlock().afterBreak(world, player, pos, state, world.getBlockEntity(pos), stack);
                }
            }

            // Tier 60: 1x2 tunnel forward
            if (tier == 60 && !player.isSneaking() && state.getHardness(world, pos) > 0) {
                BlockPos above = pos.up();
                if (world.getBlockState(above).getHardness(world, above) > 0) {
                    serverWorld.breakBlock(above, true, player);
                }
            }

            // 5x5 Mining for Tier 135 & 150
            if ((tier >= 135) && player.isSneaking() && state.getHardness(world, pos) > 0) {
                mineArea(serverWorld, player, stack, pos, state, 2);
            }
            // 3x3 Mining for Tier 70, 100, 125
            else if ((tier >= 70) && player.isSneaking() && state.getHardness(world, pos) > 0) {
                mineArea(serverWorld, player, stack, pos, state, 1);
            }

            // Tier 80: Extinguish nearby lava into obsidian
            if (tier == 80) {
                for (Direction dir : Direction.values()) {
                    BlockPos p = pos.offset(dir);
                    if (world.getBlockState(p).isOf(Blocks.LAVA)) {
                        world.setBlockState(p, Blocks.OBSIDIAN.getDefaultState());
                    }
                }
            }

            // Tier 140: Creation transmutation (20% chance to drop bonus gem)
            if (tier == 140 && world.random.nextFloat() < 0.20f) {
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(net.minecraft.item.Items.DIAMOND));
                world.spawnEntity(item);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void attractDrops(ServerWorld world, ServerPlayerEntity player) {
        Box box = player.getBoundingBox().expand(8.0);
        world.getEntitiesByClass(ItemEntity.class, box, e -> true).forEach(e -> {
            e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
        });
    }

    private void fellTree(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos start) {
        BlockPos current = start.up();
        int count = 0;
        while (count < 15 && world.getBlockState(current).isIn(BlockTags.LOGS)) {
            world.breakBlock(current, true, player);
            current = current.up();
            count++;
        }
    }

    private void mineArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, BlockState centerState, int radius) {
        Direction facing = Direction.getEntityFacingOrder(player)[0];

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
                if (!targetState.isAir() && targetState.getHardness(world, targetPos) > 0 && targetState.getHardness(world, targetPos) <= centerState.getHardness(world, center) + 4.0f) {
                    world.breakBlock(targetPos, true, player);
                }
            }
        }
    }
}
