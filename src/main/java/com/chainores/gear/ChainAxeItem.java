package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class ChainAxeItem extends Item {
    private final int tier;
    private static boolean IS_FELLING = false;

    public ChainAxeItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.axe(material, bonusDamage, -3.0f));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        String perk = getAxePerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Habilidad Forestal & Combate: ").formatted(Formatting.GREEN)
                    .append(Text.literal(perk).formatted(Formatting.YELLOW)));
        }
    }

    public static String getAxePerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Corte Conductor: Derriba árboles al agacharse (máx 16) y otorga brotes extra";
            case 10 -> "Impacto Pesado: Rompe escudos con mayor duración y aplica Lentitud";
            case 20 -> "Hacha Criogénica: Congela la corteza y extingue fuego forestal";
            case 30 -> "Hacha Solar: Carbonización instantánea (convierte troncos talados en carbón vegetal)";
            case 40 -> "★ TREECAPITATOR CLOROFITA ★: Derriba todo el árbol (hasta 64 bloques) y duplica manzanas/brotes";
            case 50 -> "Madera Rúnica: Otorga esferas de experiencia por cada tronco talado";
            case 60 -> "Fuerza Tectónica: Tala árboles gigantes y derriba follaje cercano de un impacto";
            case 70 -> "Eco Forestal: Silencia el bosque y extrae catalizadores de experiencia";
            case 80 -> "Corte Ígneo: Carbonización total masiva y daño ígneo en combate";
            case 90 -> "Cosecha Espectral: Destruye hojas instantáneamente y atrae la madera al jugador";
            case 100 -> "Hacha del Verdugo Carmesí: Desangrado masivo a enemigos y tala acelerada 3D";
            case 115 -> "Ráfaga Dracónica: Onda expansiva que despeja hojas y ramas a gran velocidad";
            case 125 -> "Vórtice Gravitacional: Succiona todos los troncos y brotes directamente al inventario";
            case 135 -> "Falla Temporal: Tala instantánea de secuoyas gigantes sin coste de durabilidad adicional";
            case 140 -> "Semilla de la Vida: Cada árbol talado re-planta un brote bendito y suelta manzanas doradas";
            case 150 -> "★ HACHA DEL CREADOR ★: Tala de bosques enteros (hasta 256 bloques), manzanas doradas y Juicio Letal";
            default -> "Tala en cadena al agacharse";
        };
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getEntityWorld() instanceof ServerWorld) {
            if (tier >= 10) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
            }
            if (tier >= 80) {
                target.setOnFireFor(8);
            }
            if (tier >= 100) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
            }
        }
        super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_FELLING && state.isIn(BlockTags.LOGS) && (player.isSneaking() || tier >= 40)) {
                fellTree(serverWorld, player, stack, pos, state);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void fellTree(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos start, BlockState startState) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        int maxLogs = switch (tier) {
            case 1 -> 16;
            case 10, 20 -> 24;
            case 30 -> 32;
            case 40, 50, 60 -> 64;
            case 70, 80, 90, 100 -> 96;
            case 115, 125, 135 -> 160;
            default -> 256;
        };

        IS_FELLING = true;
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
                                if (bs.isIn(BlockTags.LOGS) || (tier >= 60 && bs.isIn(BlockTags.LEAVES))) {
                                    visited.add(neighbor);
                                    queue.add(neighbor);
                                    world.breakBlock(neighbor, true, player);
                                    if (bs.isIn(BlockTags.LOGS)) {
                                        count++;
                                        if (tier < 135) {
                                            stack.damage(1, player, EquipmentSlot.MAINHAND);
                                        }
                                    }
                                    if (count >= maxLogs || stack.isEmpty()) break;
                                }
                            }
                        }
                    }
                }
            }

            // Tier 30 / 80: Carbonización instantánea (madera -> carbón vegetal)
            if (tier == 30 || tier >= 80) {
                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(10.0), e -> true).forEach(item -> {
                    ItemStack drop = item.getStack();
                    if (drop.isIn(net.minecraft.registry.tag.ItemTags.LOGS)) {
                        item.setStack(new ItemStack(Items.CHARCOAL, drop.getCount()));
                    }
                });
            }

            // Tier 40 / 140 / 150: Replantar y manzanas doradas
            if (tier >= 140) {
                world.setBlockState(start, Blocks.OAK_SAPLING.getDefaultState());
                world.spawnEntity(new ItemEntity(world, start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5, new ItemStack(Items.GOLDEN_APPLE, 1)));
            }

            // Tier 125+: Imán de drops al inventario
            if (tier >= 125) {
                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(12.0), e -> true).forEach(e -> {
                    e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
                });
            }

            if (count > 0) {
                player.playSound(SoundEvents.BLOCK_WOOD_BREAK, 1.0f, 0.8f);
                player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
            }
        } finally {
            IS_FELLING = false;
        }
    }
}
