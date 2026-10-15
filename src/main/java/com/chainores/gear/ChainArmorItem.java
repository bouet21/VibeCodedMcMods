package com.chainores.gear;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.function.Consumer;

public class ChainArmorItem extends Item {
    private final int tier;
    private final EquipmentType equipmentType;

    public ChainArmorItem(ArmorMaterial material, EquipmentType type, int tier, Settings settings) {
        super(settings.armor(material, type));
        this.tier = tier;
        this.equipmentType = type;
    }

    public int getTier() {
        return tier;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    /**
     * Checks if the player has the COMPLETE 4-piece armor set of this specific tier equipped.
     */
    public static boolean hasFullSet(ServerPlayerEntity player, int tier) {
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);

        return head.getItem() instanceof ChainArmorItem h && h.getTier() == tier
            && chest.getItem() instanceof ChainArmorItem c && c.getTier() == tier
            && legs.getItem() instanceof ChainArmorItem l && l.getTier() == tier
            && feet.getItem() instanceof ChainArmorItem b && b.getTier() == tier;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        String perk = getArmorPerkDesc(tier);
        if (!perk.isEmpty()) {
            textConsumer.accept(Text.literal("Efecto de Set Completo (4/4): ").formatted(Formatting.GOLD, Formatting.BOLD)
                    .append(Text.literal(perk).formatted(Formatting.YELLOW)));
            textConsumer.accept(Text.literal("⚡ Requiere equipar el set entero (Casco, Pechera, Pantalones y Botas)").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }

    public static String getArmorPerkDesc(int tier) {
        return switch (tier) {
            case 1 -> "Agilidad Conductora: Velocidad I continua y 50% reducción de daño de caída";
            case 10 -> "Bastión de Invar: Resistencia I e inmunidad al retroceso";
            case 20 -> "Aislamiento Criogénico: Inmunidad a congelación, Prisa Minera y Respiración Acuática";
            case 30 -> "Corona Solar: Inmunidad total a fuego/lava, Visión Nocturna y Regeneración diurna";
            case 40 -> "Fotosíntesis Viva: Regeneración continua, respiración ilimitada y fertilización de cultivos al agacharse";
            case 50 -> "Canalizador Rúnico: Prisa Minera II, Suerte I y generación pasiva de Experiencia";
            case 60 -> "Núcleo Tectónico: Impulso de Salto II, Caída Lenta e inmunidad total a daño de caída";
            case 70 -> "Manto del Warden: Escudo de Absorción II continuo, Visión Nocturna y sigilo Sculk";
            case 80 -> "Espinas Ígneas: Resistencia II, protección ígnea y quema defensiva a enemigos cercanos";
            case 90 -> "Paso Espectral: Velocidad II, Regeneración II y purga instantánea de todo veneno o debuff";
            case 100 -> "Titán Carmesí: +6 Corazones extra permanentemente y Furia Berserker al bajar del 50% de salud";
            case 115 -> "Planeo Dracónico: Salto Aéreo III y planeo suave con Caída Lenta continua";
            case 125 -> "Escudo Gravitacional: Resistencia III, Fuerza I y desvío cinético de flechas enemigas";
            case 135 -> "Bucle Temporal: Resistencia III y teletransporte de salvamento ante caídas al vacío";
            case 140 -> "Árbol de la Vida: +10 Corazones extra, Saturación infinita (sin hambre) y Regeneración vital";
            case 150 -> "★ ASCENSIÓN DIVINA ★: +14 Corazones, Resistencia IV, Fuerza III, Regeneración III, Inmunidad Total, Rescate del Vacío y Castigo Sagrado";
            default -> "";
        };
    }

    private static final java.util.Map<java.util.UUID, Integer> ACTIVE_SETS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (entity instanceof ServerPlayerEntity player) {
            // Apply set perks exclusively from the chestplate slot to avoid duplicate runs per tick
            if (slot == EquipmentSlot.CHEST && this.equipmentType == EquipmentType.CHESTPLATE) {
                if (hasFullSet(player, tier)) {
                    Integer prev = ACTIVE_SETS.get(player.getUuid());
                    if (prev == null || prev != tier) {
                        ACTIVE_SETS.put(player.getUuid(), tier);
                        String title = getArmorPerkDesc(tier);
                        if (title.contains(":")) title = title.split(":")[0];
                        player.sendMessage(Text.literal("✦ Set Activado: " + title + " ✦").formatted(Formatting.GOLD, Formatting.BOLD), true);
                        player.playSound(net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, 0.8f, 1.4f);
                    }
                    applyFullSetPerks(player, tier);
                } else if (ACTIVE_SETS.containsKey(player.getUuid()) && ACTIVE_SETS.get(player.getUuid()) == tier) {
                    ACTIVE_SETS.remove(player.getUuid());
                }
            }
        }
    }

    private void applyFullSetPerks(ServerPlayerEntity player, int tier) {
        int duration = 60; // 3 seconds refresh

        switch (tier) {
            case 1 -> {
                // Tier 1 (Cobreprita): Agilidad Conductora
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 0, true, false));
                if (player.fallDistance > 0) {
                    player.fallDistance *= 0.5f;
                }
            }
            case 10 -> {
                // Tier 10 (Invar): Bastión de Invar
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 0, true, false));
            }
            case 20 -> {
                // Tier 20 (Cero Absoluto): Aislamiento Criogénico
                player.setFrozenTicks(0);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, duration, 0, true, false));
            }
            case 30 -> {
                // Tier 30 (Solarita): Corona Solar
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 0, true, false));
                player.extinguish();
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0, true, false));
                if (player.getEntityWorld().isDay() && player.getEntityWorld().isSkyVisible(player.getBlockPos())) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0, true, false));
                }
            }
            case 40 -> {
                // Tier 40 (Clorofita): Fotosíntesis Viva
                player.setAir(player.getMaxAir());
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0, true, false));
                // Fertilize nearby crops when crouching
                if (player.isSneaking() && player.getEntityWorld().random.nextInt(25) == 0) {
                    int rx = player.getEntityWorld().random.nextInt(5) - 2;
                    int rz = player.getEntityWorld().random.nextInt(5) - 2;
                    BlockPos p = player.getBlockPos().add(rx, 0, rz);
                    BlockState bs = player.getEntityWorld().getBlockState(p);
                    if (bs.getBlock() instanceof CropBlock crop) {
                        int age = crop.getAge(bs);
                        if (age < crop.getMaxAge()) {
                            player.getEntityWorld().setBlockState(p, crop.withAge(age + 1));
                        }
                    }
                }
            }
            case 50 -> {
                // Tier 50 (Runita): Canalizador Rúnico
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, duration, 0, true, false));
                if (player.getEntityWorld().getTime() % 120 == 0) {
                    player.addExperience(1);
                }
            }
            case 60 -> {
                // Tier 60 (Mineral del Núcleo): Núcleo Tectónico
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, true, false));
                player.fallDistance = 0;
            }
            case 70 -> {
                // Tier 70 (Sculk Primordial): Manto del Warden
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0, true, false));
                if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1, true, false));
                }
            }
            case 80 -> {
                // Tier 80 (Núcleo Infernal): Espinas Ígneas
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 1, true, false));
                if (player.getEntityWorld().getTime() % 20 == 0) {
                    Box box = player.getBoundingBox().expand(3.5);
                    player.getEntityWorld().getEntitiesByClass(HostileEntity.class, box, LivingEntity::isAlive).forEach(mob -> {
                        mob.setOnFireFor(4);
                    });
                }
            }
            case 90 -> {
                // Tier 90 (Gema del Purgatorio): Paso Espectral & Purga de Maldiciones
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 1, true, false));
                // Cleanses negative effects automatically
                if (player.hasStatusEffect(StatusEffects.POISON)) player.removeStatusEffect(StatusEffects.POISON);
                if (player.hasStatusEffect(StatusEffects.WITHER)) player.removeStatusEffect(StatusEffects.WITHER);
                if (player.hasStatusEffect(StatusEffects.NAUSEA)) player.removeStatusEffect(StatusEffects.NAUSEA);
                if (player.hasStatusEffect(StatusEffects.SLOWNESS)) player.removeStatusEffect(StatusEffects.SLOWNESS);
                if (player.hasStatusEffect(StatusEffects.WEAKNESS)) player.removeStatusEffect(StatusEffects.WEAKNESS);
                if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
            }
            case 100 -> {
                // Tier 100 (Corazón del Nether): Titán Carmesí (+6 corazones) y Berserker
                if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 2, true, false));
                }
                if (player.getHealth() < player.getMaxHealth() * 0.5f) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 1, true, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 1, true, false));
                } else {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 0, true, false));
                }
            }
            case 115 -> {
                // Tier 115 (Aliento de Dragón): Planeo Dracónico
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, true, false));
                player.fallDistance = 0;
            }
            case 125 -> {
                // Tier 125 (Galactita): Escudo Gravitacional y desvío de flechas
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 0, true, false));

                Box arrowBox = player.getBoundingBox().expand(3.0);
                player.getEntityWorld().getEntitiesByClass(PersistentProjectileEntity.class, arrowBox, proj -> proj.getOwner() != player).forEach(proj -> {
                    proj.setVelocity(proj.getVelocity().multiply(-1.2));
                    proj.velocityDirty = true;
                });
            }
            case 135 -> {
                // Tier 135 (Fragmento Infinito): Bucle Temporal y Rescate del Vacío
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, true, false));
                if (player.getY() < -50) {
                    player.requestTeleport(player.getX(), 90, player.getZ());
                    player.setVelocity(0, 0, 0);
                    player.velocityDirty = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0, true, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1, true, false));
                }
            }
            case 140 -> {
                // Tier 140 (Génesis): Árbol de la Vida (+10 corazones, saturación infinita)
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 1, true, false));
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(20.0f);
                if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 4, true, false));
                }
            }
            case 150 -> {
                // Tier 150 (Metal de los Dioses): ★ ASCENSIÓN DIVINA ★
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 3, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0, true, false));
                player.setAir(player.getMaxAir());
                player.setFrozenTicks(0);
                player.extinguish();
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(20.0f);

                if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 6, true, false));
                }

                // Void Rescue
                if (player.getY() < -50) {
                    player.requestTeleport(player.getX(), 100, player.getZ());
                    player.setVelocity(0, 0, 0);
                    player.velocityDirty = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0, true, false));
                }

                // Periodic Holy Radiance (every 40 ticks = 2 seconds)
                if (player.getEntityWorld().getTime() % 40 == 0) {
                    Box holyBox = player.getBoundingBox().expand(6.0);
                    player.getEntityWorld().getEntitiesByClass(HostileEntity.class, holyBox, LivingEntity::isAlive).forEach(mob -> {
                        mob.setOnFireFor(5);
                        mob.damage((ServerWorld) player.getEntityWorld(), player.getDamageSources().playerAttack(player), 8.0f);
                    });
                }
            }
        }
    }
}
