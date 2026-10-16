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
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.List;
import java.util.function.Consumer;

public class ChainHoeItem extends Item {
    private final int tier;
    private static final ThreadLocal<Boolean> IS_WORKING = ThreadLocal.withInitial(() -> false);

    public ChainHoeItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.hoe(material, bonusDamage, -1.0f));
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
                target.damage(world, world.getDamageSources().magic(), (mult - 1.0f) * 10.0f);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            com.chainores.modifier.ChainModifiers.applyReforgeHeldBuffs(entity, stack);

            if (entity instanceof ServerPlayerEntity player) {
                // Tier 20+: Escarcha Agrícola (Farmland trampling protection & moisture maintenance)
                if (tier >= 20) {
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

                // Tier 30+: Calor Vital / Polinización (Passive growth pulse nearby)
                if (tier >= 30 && world.getTime() % 40 == 0) {
                    int rx = world.random.nextInt(5) - 2;
                    int rz = world.random.nextInt(5) - 2;
                    BlockPos p = player.getBlockPos().add(rx, 0, rz);
                    fertilizeCrop(world, p, world.getBlockState(p), 1);
                }
            }
        }
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
            case 1 -> "Cosecha Fácil: Clic derecho cosecha y replanta automáticamente";
            case 10 -> "Arado Profundo: Convierte tierra árida y enraizada en cultivo fértil";
            case 20 -> "Escarcha Agrícola: Evita el pisoteo accidental de cultivos e hidrata la tierra";
            case 30 -> "Calor Vital: Acelera el crecimiento de cultivos pasiva y activamente";
            case 40 -> "★ FERTILIDAD CLOROFITA ★: Arado e irrigación 3x3 y cosecha masiva en área con re-plantado";
            case 50 -> "Cosecha Rúnica: Otorga esferas de experiencia por cada cultivo recolectado";
            case 60 -> "Surcos Tectónicos: Arado 3x3 continuo y recolección de raíces";
            case 70 -> "Catalizador Sculk: Probabilidad de extraer semillas místicas y catalizadores";
            case 80 -> "Ceniza Volcánica: Fertilizante natural masivo 3x3 al hacer clic derecho";
            case 90 -> "Cosecha Espectral: Recolecta cultivos sin pisar ni desgastar el terreno";
            case 100 -> "Arado Titánico 5x5: Campos de cultivo gigantes 5x5 e hidratación instantánea";
            case 115 -> "Polinización Dracónica: Fertilizante masivo 5x5 y aceleración biológica";
            case 125 -> "Microcosmos Agrícola: Cosecha masiva 5x5 con atracción magnética de cosechas";
            case 135 -> "Bucle de Cosecha: Los cultivos cosechados re-brotan en fase intermedia";
            case 140 -> "Fruto del Edén: Probabilidad de cosechar manzanas doradas y frutos benditos";
            case 150 -> "★ AZADA DE LA CREACIÓN DIVINA ★: Arado e irrigación 9x9, cosecha instantánea y fertilidad infinita";
            default -> "Auto-replantado y arado en área al hacer clic derecho";
        };
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (world.isClient()) {
            if (isTillable(state) || isAnyCrop(state)) {
                return ActionResult.SUCCESS;
            }
            return super.useOnBlock(context);
        }

        if (!(world instanceof ServerWorld serverWorld) || !(context.getPlayer() instanceof ServerPlayerEntity serverPlayer)) {
            return super.useOnBlock(context);
        }

        // CASE 1: Right-click on a CROP (Harvest or Fertilize)
        if (isAnyCrop(state)) {
            if (isMatureCrop(state)) {
                // Mature: Harvest & Replant in area
                harvestArea(serverWorld, serverPlayer, context.getStack(), pos);
                world.playSound(null, pos, SoundEvents.BLOCK_CROP_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.8f, 1.2f);
                context.getStack().damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                return ActionResult.SUCCESS;
            } else if (tier >= 30) {
                // Immature: Fertilize growth burst!
                int fertRadius = (tier >= 150) ? 4 : (tier >= 115 ? 2 : (tier >= 80 ? 1 : 0));
                int growthPower = (tier >= 150) ? 10 : (tier >= 80 ? 3 : 2);
                for (int dx = -fertRadius; dx <= fertRadius; dx++) {
                    for (int dz = -fertRadius; dz <= fertRadius; dz++) {
                        BlockPos p = pos.add(dx, 0, dz);
                        BlockState bs = world.getBlockState(p);
                        if (isAnyCrop(bs)) {
                            fertilizeCrop(serverWorld, p, bs, growthPower);
                        }
                    }
                }
                world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                context.getStack().damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                return ActionResult.SUCCESS;
            }
        }

        // CASE 2: Right-click on TILLABLE SOIL
        if (isTillable(state)) {
            int radius = (tier >= 150) ? 4 : (tier >= 100 ? 2 : (tier >= 40 ? 1 : 0));
            int tilledCount = 0;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = pos.add(dx, 0, dz);
                    BlockState bs = world.getBlockState(p);

                    if (isTillable(bs)) {
                        BlockPos upPos = p.up();
                        BlockState upState = world.getBlockState(upPos);

                        if (upState.isAir() || upState.isReplaceable()) {
                            if (!com.chainores.util.ServerProtectionHelper.canModifyAt(serverWorld, serverPlayer, p)) {
                                continue;
                            }

                            // Clear non-air replaceable blocks (like short grass or flowers)
                            if (!upState.isAir()) {
                                world.breakBlock(upPos, true, serverPlayer);
                            }

                            if (bs.isOf(Blocks.COARSE_DIRT)) {
                                if (tier >= 10) {
                                    BlockState f = Blocks.FARMLAND.getDefaultState();
                                    if (tier >= 40) f = f.with(FarmlandBlock.MOISTURE, 7);
                                    world.setBlockState(p, f);
                                } else {
                                    world.setBlockState(p, Blocks.DIRT.getDefaultState());
                                }
                            } else if (bs.isOf(Blocks.ROOTED_DIRT)) {
                                BlockState f = (tier >= 10) ? Blocks.FARMLAND.getDefaultState() : Blocks.DIRT.getDefaultState();
                                if (tier >= 40) f = f.with(FarmlandBlock.MOISTURE, 7);
                                world.setBlockState(p, f);
                                world.spawnEntity(new ItemEntity(world, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, new ItemStack(Items.HANGING_ROOTS)));
                            } else {
                                BlockState farmland = Blocks.FARMLAND.getDefaultState();
                                if (tier >= 40) {
                                    farmland = farmland.with(FarmlandBlock.MOISTURE, 7);
                                }
                                world.setBlockState(p, farmland);
                            }

                            tilledCount++;
                        }
                    }
                }
            }

            if (tilledCount > 0) {
                world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                if (tier >= 40) {
                    serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.5, 0.2, 0.5, 0.0);
                }
                context.getStack().damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && miner instanceof ServerPlayerEntity player) {
            if (!IS_WORKING.get() && isAnyCrop(state)) {
                IS_WORKING.set(true);
                try {
                    // Replant the broken central crop
                    replantCrop(serverWorld, pos, state);

                    // If tier >= 40, also harvest and replant neighbor crops
                    int radius = (tier >= 150) ? 4 : (tier >= 100 ? 2 : (tier >= 40 ? 1 : 0));
                    if (radius > 0) {
                        for (int dx = -radius; dx <= radius; dx++) {
                            for (int dz = -radius; dz <= radius; dz++) {
                                if (dx == 0 && dz == 0) continue;
                                BlockPos p = pos.add(dx, 0, dz);
                                BlockState bs = world.getBlockState(p);
                                if (isAnyCrop(bs) && isMatureCrop(bs)) {
                                    harvestAndReplantSingle(serverWorld, player, stack, p, bs);
                                    if (tier < 135) {
                                        stack.damage(1, player, EquipmentSlot.MAINHAND);
                                    }
                                    if (stack.isEmpty()) break;
                                }
                            }
                            if (stack.isEmpty()) break;
                        }
                    }

                    consolidateDrops(serverWorld, player, 8.0);
                    if (tier >= 125) {
                        serverWorld.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(10.0), e -> !e.isRemoved()).forEach(e -> {
                            e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
                        });
                    }
                } finally {
                    IS_WORKING.set(false);
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    private void harvestArea(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos center) {
        int radius = (tier >= 150) ? 4 : (tier >= 100 ? 2 : (tier >= 40 ? 1 : 0));
        IS_WORKING.set(true);
        try {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.add(dx, 0, dz);
                    BlockState bs = world.getBlockState(p);
                    if (isAnyCrop(bs) && isMatureCrop(bs)) {
                        harvestAndReplantSingle(world, player, stack, p, bs);
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
                world.getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(12.0), e -> !e.isRemoved()).forEach(e -> {
                    e.requestTeleport(player.getX(), player.getY() + 0.2, player.getZ());
                });
            }
        } finally {
            IS_WORKING.set(false);
        }
    }

    private void harvestAndReplantSingle(ServerWorld world, ServerPlayerEntity player, ItemStack stack, BlockPos pos, BlockState state) {
        if (!com.chainores.util.ServerProtectionHelper.canBreakBlock(world, player, pos, state)) {
            return;
        }

        if (state.getBlock() instanceof CropBlock crop) {
            world.breakBlock(pos, true, player);
            int newAge = (tier >= 135) ? Math.min(3, crop.getMaxAge() - 1) : 0;
            BlockState soil = world.getBlockState(pos.down());
            if (soil.isOf(Blocks.FARMLAND) || soil.isOf(Blocks.DIRT) || soil.isOf(Blocks.GRASS_BLOCK)) {
                world.setBlockState(pos, crop.withAge(newAge));
            }
        } else if (state.isOf(Blocks.NETHER_WART)) {
            world.breakBlock(pos, true, player);
            BlockState soil = world.getBlockState(pos.down());
            if (soil.isOf(Blocks.SOUL_SAND)) {
                world.setBlockState(pos, Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0));
            }
        } else if (state.isOf(Blocks.COCOA)) {
            world.breakBlock(pos, true, player);
            world.setBlockState(pos, state.with(CocoaBlock.AGE, 0));
        } else if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.SWEET_BERRIES, world.random.nextInt(2) + 2)));
            world.setBlockState(pos, state.with(SweetBerryBushBlock.AGE, 1));
        }

        // Tier 50: Experience orbs
        if (tier >= 50 && world.random.nextFloat() < 0.45f) {
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1));
        }

        // Tier 70: Mystical Seeds & Catalyst bonus drops
        if (tier >= 70 && world.random.nextFloat() < 0.20f) {
            Item bonusItem = world.random.nextBoolean() ? Items.PITCHER_POD : Items.TORCHFLOWER_SEEDS;
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(bonusItem, 1)));
        }

        // Tier 140: Fruit of Eden (Golden Apples / Golden Carrots)
        if (tier >= 140 && world.random.nextFloat() < 0.12f) {
            ItemStack fruit = (world.random.nextFloat() < 0.05f) ? new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
                    : (world.random.nextBoolean() ? new ItemStack(Items.GOLDEN_APPLE) : new ItemStack(Items.GOLDEN_CARROT, 2));
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, fruit));
        }
    }

    private void replantCrop(ServerWorld world, BlockPos pos, BlockState brokenState) {
        if (!world.getBlockState(pos).isAir()) return;

        if (brokenState.getBlock() instanceof CropBlock crop) {
            BlockState soil = world.getBlockState(pos.down());
            if (soil.isOf(Blocks.FARMLAND) || soil.isOf(Blocks.DIRT) || soil.isOf(Blocks.GRASS_BLOCK)) {
                int newAge = (tier >= 135) ? Math.min(3, crop.getMaxAge() - 1) : 0;
                world.setBlockState(pos, crop.withAge(newAge));
            }
        } else if (brokenState.isOf(Blocks.NETHER_WART)) {
            if (world.getBlockState(pos.down()).isOf(Blocks.SOUL_SAND)) {
                world.setBlockState(pos, Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0));
            }
        } else if (brokenState.isOf(Blocks.COCOA)) {
            world.setBlockState(pos, brokenState.with(CocoaBlock.AGE, 0));
        }
    }

    private void fertilizeCrop(ServerWorld world, BlockPos pos, BlockState state, int amount) {
        if (state.getBlock() instanceof CropBlock crop) {
            int currentAge = crop.getAge(state);
            int maxAge = crop.getMaxAge();
            if (currentAge < maxAge) {
                int newAge = Math.min(maxAge, currentAge + amount);
                world.setBlockState(pos, crop.withAge(newAge));
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
            }
        } else if (state.isOf(Blocks.NETHER_WART)) {
            int currentAge = state.get(NetherWartBlock.AGE);
            if (currentAge < 3) {
                world.setBlockState(pos, state.with(NetherWartBlock.AGE, Math.min(3, currentAge + amount)));
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
            }
        } else if (state.isOf(Blocks.COCOA)) {
            int currentAge = state.get(CocoaBlock.AGE);
            if (currentAge < 2) {
                world.setBlockState(pos, state.with(CocoaBlock.AGE, Math.min(2, currentAge + amount)));
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
            }
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
