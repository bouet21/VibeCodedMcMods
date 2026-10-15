package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class ChainHoeItem extends Item {
    private final int tier;
    private static boolean IS_HARVESTING = false;

    public ChainHoeItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.hoe(material, bonusDamage, -1.0f));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        String perk = getHoePerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Habilidad Agrícola: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(perk).formatted(Formatting.GREEN)));
        }
    }

    public static String getHoePerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Cosecha Fácil: Cosecha y replanta automáticamente el cultivo";
            case 10 -> "Arado Profundo: Convierte tierra en tierra de cultivo";
            case 20 -> "Escarcha Agrícola: Evita el pisoteo accidental de los cultivos";
            case 30 -> "Calor Vital: Acelera la absorción de luz en los sembrados";
            case 40 -> "★ FERTILIDAD CLOROFITA ★: Arado e irrigación 3x3 y cosecha masiva en área con re-plantado";
            case 50 -> "Cosecha Rúnica: Otorga esferas de experiencia por cada cultivo recolectado";
            case 60 -> "Surcos Tectónicos: Arado 3x3 continuo y recolección de raíces";
            case 70 -> "Catalizador Sculk: Transforma cultivos maduros en semillas místicas";
            case 80 -> "Ceniza Volcánica: Fertilizante natural que acelera el crecimiento";
            case 90 -> "Cosecha Espectral: Recolecta cultivos sin tocar el suelo";
            case 100 -> "Arado Titánico 5x5: Crea campos de cultivo gigantes e hidrata automáticamente";
            case 115 -> "Polinización Dracónica: Vuelo rasante que acelera la maduración";
            case 125 -> "Microcosmos Agrícola: Cosecha masiva 5x5 con atracción gravitatoria";
            case 135 -> "Bucle de Cosecha: Los cultivos cosechados re-brotan en fase intermedia";
            case 140 -> "Fruto del Edén: Probabilidad de soltar frutas doradas y semillas ancestrales";
            case 150 -> "★ AZADA DE LA CREACIÓN DIVINA ★: Arado e irrigación 9x9, cosecha instantánea y fertilidad infinita";
            default -> "Auto-replantado y arado en área al agacharse";
        };
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT_PATH)) {
            if (!world.isClient()) {
                int radius = (tier >= 150) ? 4 : (tier >= 100 ? 2 : (tier >= 40 ? 1 : 0));
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos p = pos.add(dx, 0, dz);
                        BlockState bs = world.getBlockState(p);
                        if ((bs.isOf(Blocks.DIRT) || bs.isOf(Blocks.GRASS_BLOCK) || bs.isOf(Blocks.DIRT_PATH)) && world.getBlockState(p.up()).isAir()) {
                            BlockState farmland = Blocks.FARMLAND.getDefaultState();
                            if (tier >= 40) {
                                farmland = farmland.with(FarmlandBlock.MOISTURE, 7); // Hydrated!
                            }
                            world.setBlockState(p, farmland);
                        }
                    }
                }
                world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                if (context.getPlayer() != null) {
                    context.getStack().damage(1, context.getPlayer(), EquipmentSlot.MAINHAND);
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_HARVESTING && state.getBlock() instanceof CropBlock crop) {
                harvestArea(serverWorld, player, stack, pos, crop, state);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void harvestArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center, CropBlock centerCrop, BlockState centerState) {
        int radius = (tier >= 150) ? 4 : (tier >= 100 ? 2 : (tier >= 40 ? 1 : 0));
        IS_HARVESTING = true;
        try {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState bs = world.getBlockState(p);
                    if (bs.getBlock() instanceof CropBlock c && c.isMature(bs)) {
                        world.breakBlock(p, true, player);
                        // Replant
                        int newAge = (tier >= 135) ? 3 : 0;
                        world.setBlockState(p, c.withAge(newAge));

                        if (tier >= 50 && world.random.nextInt(3) == 0) {
                            world.spawnEntity(new net.minecraft.entity.ExperienceOrbEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1));
                        }

                        if (tier < 135) {
                            stack.damage(1, player, EquipmentSlot.MAINHAND);
                        }
                        if (stack.isEmpty()) break;
                    }
                }
                if (stack.isEmpty()) break;
            }

            if (tier >= 125) {
                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(8.0), e -> true).forEach(e -> {
                    e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
                });
            }
        } finally {
            IS_HARVESTING = false;
        }
    }
}
