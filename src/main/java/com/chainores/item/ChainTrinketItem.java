package com.chainores.item;

import com.chainores.modifier.ChainModifiers;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.function.Consumer;

public abstract class ChainTrinketItem extends Item {
    private final int maxSockets;

    public ChainTrinketItem(int maxSockets, Settings settings) {
        super(settings);
        this.maxSockets = maxSockets;
    }

    public int getMaxSockets() {
        return maxSockets;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        // Apply passives every second (20 ticks)
        if (player.age % 20 != 0) return;

        applyTrinketPassives(stack, player);
    }

    public static void applyTrinketPassives(ItemStack stack, PlayerEntity player) {
        List<String> gems = ChainModifiers.getGems(stack);
        int bloodPower = 0;
        int voidPower = 0;
        int corePower = 0;
        int genesisPower = 0;

        int earthPower = 0;
        int echoPower = 0;
        int magnetPower = 0;
        int chronoPower = 0;
        int frostPower = 0;
        int stormPower = 0;
        int venomPower = 0;
        int solarPower = 0;

        for (String gem : gems) {
            String base = ChainModifiers.getBaseGemType(gem);
            int tier = ChainModifiers.getGemTier(gem);
            switch (base) {
                case ChainModifiers.GEM_BLOOD -> bloodPower += tier;
                case ChainModifiers.GEM_VOID -> voidPower += tier;
                case ChainModifiers.GEM_CORE -> corePower += tier;
                case ChainModifiers.GEM_GENESIS -> genesisPower += tier;
                case ChainModifiers.GEM_EARTH -> earthPower += tier;
                case ChainModifiers.GEM_ECHO -> echoPower += tier;
                case ChainModifiers.GEM_MAGNET -> magnetPower += tier;
                case ChainModifiers.GEM_CHRONO -> chronoPower += tier;
                case ChainModifiers.GEM_FROST -> frostPower += tier;
                case ChainModifiers.GEM_STORM -> stormPower += tier;
                case ChainModifiers.GEM_VENOM -> venomPower += tier;
                case ChainModifiers.GEM_SOLAR -> solarPower += tier;
            }
        }

        // 1. Blood Gem: Regeneration
        if (bloodPower > 0) {
            int amp = Math.min((bloodPower - 1) / 2, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, amp, true, false, true));
        }

        // 2. Void Gem: Fall damage immunity & slow falling glide
        if (voidPower > 0) {
            if (player.fallDistance > 1.2f && !player.isOnGround()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, true, false, true));
                player.fallDistance = 0;
            }
            if (voidPower >= 4 && player.isSneaking() && !player.isOnGround()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10, 0, true, false, false));
            }
        }

        // 3. Core Gem: Fire Immunity & Speed
        if (corePower > 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
            int speedAmp = Math.min((corePower - 1) / 2, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, speedAmp, true, false, true));
            if (corePower >= 3) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 0, true, false, true));
            }
        }

        // 4. Genesis Gem: Bonus Absorption Hearts & Strength
        if (genesisPower > 0) {
            if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                int absorbAmp = Math.min(genesisPower, 4);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120, absorbAmp, true, false, true));
            }
            if (genesisPower >= 2) {
                int strAmp = Math.min((genesisPower - 2) / 2, 1);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, strAmp, true, false, true));
            }
        }

        // 5. Earth Gem: Haste, Luck & Night Vision
        if (earthPower > 0) {
            int hasteAmp = Math.min((earthPower - 1) / 2, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, hasteAmp, true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 60, Math.min(earthPower - 1, 1), true, false, true));
            if (player.getY() < 60) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, true));
            }
        }

        // 6. Echo Gem: Darkness cure & Cave Vision
        if (echoPower > 0) {
            if (player.hasStatusEffect(StatusEffects.DARKNESS)) player.removeStatusEffect(StatusEffects.DARKNESS);
            if (player.hasStatusEffect(StatusEffects.BLINDNESS)) player.removeStatusEffect(StatusEffects.BLINDNESS);
            if (player.getY() < 60) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, true));
            }
        }

        // 7. Magnet Gem: Item attraction in radius
        if (magnetPower > 0 && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
            double r = 6.0 + 4.0 * magnetPower;
            net.minecraft.util.math.Box box = player.getBoundingBox().expand(r);
            for (net.minecraft.entity.ItemEntity it : sw.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, box, e -> !e.cannotPickup() && !e.isRemoved())) {
                net.minecraft.util.math.Vec3d pull = player.getEntityPos().add(0, 0.5, 0).subtract(it.getEntityPos()).normalize().multiply(0.35);
                it.setVelocity(pull);
                it.velocityDirty = true;
            }
            for (net.minecraft.entity.ExperienceOrbEntity orb : sw.getEntitiesByClass(net.minecraft.entity.ExperienceOrbEntity.class, box, e -> !e.isRemoved())) {
                net.minecraft.util.math.Vec3d pull = player.getEntityPos().add(0, 0.5, 0).subtract(orb.getEntityPos()).normalize().multiply(0.40);
                orb.setVelocity(pull);
                orb.velocityDirty = true;
            }
        }

        // 8. Chrono Gem: Speed & Temporal Mending
        if (chronoPower > 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, Math.min(chronoPower - 1, 1), true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, Math.min(chronoPower - 1, 1), true, false, true));
            if (player.age % 40 == 0 && stack.isDamaged()) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
            }
        }

        // 9. Frost Gem: Absolute Zero Immunity & Frost Aura
        if (frostPower > 0) {
            player.setFrozenTicks(0);
            net.minecraft.util.math.BlockPos under = player.getBlockPos().down();
            if (player.getEntityWorld().getBlockState(under).isOf(net.minecraft.block.Blocks.WATER) && player.isOnGround()) {
                player.getEntityWorld().setBlockState(under, net.minecraft.block.Blocks.FROSTED_ICE.getDefaultState());
            }
            if (frostPower >= 2 && player.age % 40 == 0 && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                net.minecraft.util.math.Box b = player.getBoundingBox().expand(3.0 + frostPower);
                for (net.minecraft.entity.LivingEntity target : sw.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, b, e -> e != player && e.isAlive() && !(e instanceof net.minecraft.entity.player.PlayerEntity))) {
                    target.setFrozenTicks(Math.max(target.getFrozenTicks(), 100));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false));
                }
            }
        }

        // 10. Storm Gem: Lightning Speed & Static Aura
        if (stormPower > 0) {
            int spdAmp = Math.min(stormPower - 1, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, spdAmp, true, false, true));
            if (stormPower >= 2) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 1, true, false, true));
            }
            if (stormPower >= 2 && player.age % 30 == 0 && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                net.minecraft.util.math.Box b = player.getBoundingBox().expand(4.0);
                for (net.minecraft.entity.LivingEntity target : sw.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, b, e -> e != player && e.isAlive() && !(e instanceof net.minecraft.entity.player.PlayerEntity))) {
                    target.damage(sw, player.getDamageSources().magic(), 4.0f * stormPower);
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }

        // 11. Venom Gem: Toxin Immunity & Corrosive Spore Aura
        if (venomPower > 0) {
            if (player.hasStatusEffect(StatusEffects.POISON)) player.removeStatusEffect(StatusEffects.POISON);
            if (player.hasStatusEffect(StatusEffects.HUNGER)) player.removeStatusEffect(StatusEffects.HUNGER);
            if (venomPower >= 2 && player.hasStatusEffect(StatusEffects.WITHER)) player.removeStatusEffect(StatusEffects.WITHER);
            if (player.age % 50 == 0 && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                net.minecraft.util.math.Box b = player.getBoundingBox().expand(3.0 + venomPower);
                for (net.minecraft.entity.LivingEntity target : sw.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, b, e -> e != player && e.isAlive() && !(e instanceof net.minecraft.entity.player.PlayerEntity))) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, Math.min(venomPower - 1, 2), true, false));
                }
            }
        }

        // 12. Solar Gem: Sunfire Photosynthesis & Undead Smite
        if (solarPower > 0) {
            boolean isDay = player.getEntityWorld().isDay();
            boolean isSky = player.getEntityWorld().isSkyVisible(player.getBlockPos());
            if (isDay && isSky) {
                if (player.age % 40 == 0) {
                    player.getHungerManager().add(1, 0.5f);
                }
                if (solarPower >= 2) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0, true, false, true));
                }
            }
            if (solarPower >= 2 && player.getY() < 60) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, true));
            }
            if (solarPower >= 3 && player.age % 40 == 0 && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                net.minecraft.util.math.Box b = player.getBoundingBox().expand(8.0);
                for (net.minecraft.entity.LivingEntity target : sw.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, b, e -> e.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD) && e.isAlive())) {
                    target.setOnFireFor(5);
                    target.damage(sw, player.getDamageSources().onFire(), 8.0f);
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.2, 0.3, 0.2, 0.05);
                }
            }
        }

        // Reforge Buffs
        String reforge = ChainModifiers.getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case ChainModifiers.REFORGE_DIVINE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_SWIFT ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_FIERCE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_TITANIC ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 0, true, false, true));
                case ChainModifiers.REFORGE_LEGENDARY ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 60, 0, true, false, true));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("✦ Accesorio / Reliquia Arcana ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        textConsumer.accept(Text.literal("Otorga sus beneficios pasivos continuos en inventario,").formatted(Formatting.GRAY));
        textConsumer.accept(Text.literal("mano secundaria o ranuras de Trinkets (Anillo/Amuleto).").formatted(Formatting.GRAY));
        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.literal("⚒ Personalización:").formatted(Formatting.AQUA, Formatting.BOLD));
        textConsumer.accept(Text.literal(" • Engarza hasta " + maxSockets + " gemas en el Yunque en Cadena.").formatted(Formatting.DARK_AQUA));
        textConsumer.accept(Text.literal(" • Reforja con Amatista/Lapis para obtener prefijos legendarios.").formatted(Formatting.DARK_AQUA));
    }
}
