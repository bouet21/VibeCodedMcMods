package com.chainores.gear;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import java.util.function.Consumer;

public class ChainSwordItem extends Item {
    private final int tier;
    private final float bonusDamage;

    public ChainSwordItem(ToolMaterial material, int tier, float bonusDamage, Settings settings) {
        super(settings.sword(material, bonusDamage, -2.4f));
        this.tier = tier;
        this.bonusDamage = bonusDamage;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        String perk = getSwordPerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Habilidad de Combate: ").formatted(Formatting.RED)
                    .append(Text.literal(perk).formatted(Formatting.GOLD)));
        }
    }

    public static String getSwordPerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Descarga Estática: Ralentiza al enemigo y otorga ráfaga de velocidad";
            case 10 -> "Impacto Contundente: Fuerte empuje y aplica Debilidad";
            case 20 -> "Corte Glacial: Congela por completo y aplica Lentitud III";
            case 30 -> "Llamarada Solar: Quema al objetivo y revela con Brillo radiante";
            case 40 -> "Espora Vampírica: Cura 1 corazón e infecta con Veneno II";
            case 50 -> "Cercenador Arcano: Aplica Debilidad y genera orbes de Experiencia";
            case 60 -> "Onda Tectónica: Onda de choque que daña y empuja enemigos cercanos";
            case 70 -> "Aullido Sónico: Provoca Oscuridad profunda, Ceguera y Lentitud IV";
            case 80 -> "Fuego del Averno: Quema infernal de 15s y Wither continuo";
            case 90 -> "Cosecha de Almas: Aplica Wither II, sana vitalidad y restaura hambre";
            case 100 -> "Desangrado Carmesí: Hemorragia prolongada y activa Furia Carmesí";
            case 115 -> "Vuelo Dracónico: Lanza al enemigo por los aires a gran altura";
            case 125 -> "Rayo Orbital: Invoca un rayo cósmico y suspensión por Levitación";
            case 135 -> "Falla Temporal: Detiene al enemigo en el tiempo con Lentitud V extrema";
            case 140 -> "Resonancia Génesis: Cura 3 corazones, otorga Absorción y ciega enemigos";
            case 150 -> "★ JUICIO DIVINO ★: Rayo destructor, Wither IV, curación masiva y ejecución letal";
            default -> "";
        };
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postDamageEntity(stack, target, attacker);

        if (attacker.getEntityWorld().isClient()) return;
        ServerWorld world = (ServerWorld) attacker.getEntityWorld();

        // Multi-Gem Modifiers
        java.util.List<String> gems = com.chainores.modifier.ChainModifiers.getGems(stack);
        for (String gem : gems) {
            String base = com.chainores.modifier.ChainModifiers.getBaseGemType(gem);
            int gemTier = com.chainores.modifier.ChainModifiers.getGemTier(gem);
            if (com.chainores.modifier.ChainModifiers.GEM_BLOOD.equals(base)) {
                attacker.heal(3.0f * gemTier);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HEART, attacker.getX(), attacker.getY() + 1.2, attacker.getZ(), 2 * gemTier, 0.2, 0.2, 0.2, 0.0);
            } else if (com.chainores.modifier.ChainModifiers.GEM_GENESIS.equals(base)) {
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                    lightning.setCosmetic(true);
                    world.spawnEntity(lightning);
                }
                target.damage(world, attacker.getDamageSources().magic(), 20.0f * gemTier);
            } else if (com.chainores.modifier.ChainModifiers.GEM_VOID.equals(base)) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30 + 15 * gemTier, gemTier - 1));
                target.damage(world, attacker.getDamageSources().magic(), 10.0f * gemTier);
            } else if (com.chainores.modifier.ChainModifiers.GEM_CORE.equals(base)) {
                target.setOnFireFor(6 * gemTier);
            } else if (com.chainores.modifier.ChainModifiers.GEM_EARTH.equals(base)) {
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 80, gemTier - 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, gemTier));
            } else if (com.chainores.modifier.ChainModifiers.GEM_ECHO.equals(base)) {
                target.damage(world, attacker.getDamageSources().magic(), 12.0f * gemTier);
                target.takeKnockback(0.7f * gemTier, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
                world.playSound(null, target.getX(), target.getY(), target.getZ(), net.minecraft.sound.SoundEvents.ENTITY_WARDEN_SONIC_BOOM, net.minecraft.sound.SoundCategory.PLAYERS, 0.4f, 1.8f);
            } else if (com.chainores.modifier.ChainModifiers.GEM_MAGNET.equals(base)) {
                target.takeKnockback(-0.6f * gemTier, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            } else if (com.chainores.modifier.ChainModifiers.GEM_CHRONO.equals(base)) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40 * gemTier, 3));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40 * gemTier, 1));
            }
        }

        // Reforge Damage Multiplier
        float dmgMult = com.chainores.modifier.ChainModifiers.getDamageMultiplier(stack);
        if (dmgMult > 1.0f && attacker instanceof PlayerEntity pe) {
            float baseDmg = this.bonusDamage + 4.0f;
            float extra = (dmgMult - 1.0f) * baseDmg;
            target.timeUntilRegen = 0;
            target.damage(world, attacker.getDamageSources().playerAttack(pe), extra);
        }

        switch (tier) {
            case 1 -> {
                // Tier 1 (Cobreprita): Descarga Estática
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0));
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0));
            }
            case 10 -> {
                // Tier 10 (Invar): Impacto Contundente
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0));
                double dx = target.getX() - attacker.getX();
                double dz = target.getZ() - attacker.getZ();
                target.takeKnockback(0.8, -dx, -dz);
            }
            case 20 -> {
                // Tier 20 (Cero Absoluto): Corte Glacial
                target.setFrozenTicks(200);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2));
            }
            case 30 -> {
                // Tier 30 (Solarita): Llamarada Solar
                target.setOnFireFor(6);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
                if (world.isDay() && world.isSkyVisible(target.getBlockPos())) {
                    target.setOnFireFor(12);
                }
            }
            case 40 -> {
                // Tier 40 (Clorofita): Espora Vampírica
                attacker.heal(2.0f);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 1));
            }
            case 50 -> {
                // Tier 50 (Runita): Cercenador Arcano
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 80, 0));
                ExperienceOrbEntity orb = new ExperienceOrbEntity(world, target.getX(), target.getY(), target.getZ(), 4);
                world.spawnEntity(orb);
            }
            case 60 -> {
                // Tier 60 (Mineral del Núcleo): Onda Tectónica AoE
                Box box = target.getBoundingBox().expand(4.0);
                world.getEntitiesByClass(LivingEntity.class, box, e -> e != attacker && e != target && e.isAlive()).forEach(e -> {
                    double dx = e.getX() - target.getX();
                    double dz = e.getZ() - target.getZ();
                    e.takeKnockback(0.7, -dx, -dz);
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
                    if (attacker instanceof ServerPlayerEntity sp) {
                        e.damage(world, sp.getDamageSources().playerAttack(sp), 6.0f);
                    }
                });
            }
            case 70 -> {
                // Tier 70 (Sculk Primordial): Aullido Sónico
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0));
            }
            case 80 -> {
                // Tier 80 (Núcleo Infernal): Fuego del Averno
                target.setOnFireFor(15);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0));
            }
            case 90 -> {
                // Tier 90 (Gema del Purgatorio): Cosecha de Almas
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1));
                attacker.heal(3.0f);
                if (attacker instanceof PlayerEntity player) {
                    player.getHungerManager().add(2, 0.5f);
                }
            }
            case 100 -> {
                // Tier 100 (Corazón del Nether): Desangrado Carmesí
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 1));
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 80, 0));
            }
            case 115 -> {
                // Tier 115 (Aliento de Dragón): Vuelo Dracónico
                target.addVelocity(0, 1.4, 0);
                target.velocityDirty = true;
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30, 1));
            }
            case 125 -> {
                // Tier 125 (Galactita): Rayo Orbital
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                    world.spawnEntity(lightning);
                }
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 0));
            }
            case 135 -> {
                // Tier 135 (Fragmento Infinito): Falla Temporal
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 4));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2));
                target.setVelocity(0, 0, 0);
                target.velocityDirty = true;
            }
            case 140 -> {
                // Tier 140 (Génesis): Resonancia Génesis
                attacker.heal(6.0f);
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
            }
            case 150 -> {
                // Tier 150 (Metal de los Dioses): ★ JUICIO DIVINO ★
                LightningEntity l1 = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                if (l1 != null) {
                    l1.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                    world.spawnEntity(l1);
                }
                world.playSound(null, target.getX(), target.getY(), target.getZ(), net.minecraft.sound.SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.1f);
                attacker.heal(10.0f);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 160, 3));
                world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.1);

                // Execute if below 25% health
                if (target.getHealth() < target.getMaxHealth() * 0.25f) {
                    target.setHealth(0.0f);
                    world.spawnParticles(net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1.0, target.getZ(), 45, 0.5, 0.8, 0.5, 0.2);
                    attacker.playSound(net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.5f);
                }
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
}
