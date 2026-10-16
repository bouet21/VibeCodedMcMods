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
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    public static int getEquippedPieceCount(ServerPlayerEntity player, int tier) {
        int count = 0;
        if (player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof ChainArmorItem h && h.getTier() == tier) count++;
        if (player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ChainArmorItem c && c.getTier() == tier) count++;
        if (player.getEquippedStack(EquipmentSlot.LEGS).getItem() instanceof ChainArmorItem l && l.getTier() == tier) count++;
        if (player.getEquippedStack(EquipmentSlot.FEET).getItem() instanceof ChainArmorItem b && b.getTier() == tier) count++;
        return count;
    }

    public static boolean hasFullSet(ServerPlayerEntity player, int tier) {
        return getEquippedPieceCount(player, tier) >= 4;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("Nivel de Progresión: Tier " + tier).formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        textConsumer.accept(Text.literal("✦ Bonus Parcial (2/4): ").formatted(Formatting.AQUA, Formatting.BOLD)
                .append(Text.literal(getArmor2PieceDesc(tier)).formatted(Formatting.WHITE)));
        textConsumer.accept(Text.literal("✦ Bonus Parcial (3/4): ").formatted(Formatting.GREEN, Formatting.BOLD)
                .append(Text.literal(getArmor3PieceDesc(tier)).formatted(Formatting.WHITE)));
        textConsumer.accept(Text.literal("★ Set Completo (4/4): ").formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal(getArmorPerkDesc(tier)).formatted(Formatting.YELLOW)));
        textConsumer.accept(Text.literal("⚡ Habilidad Activa: ").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal("Tecla [Dash] activa Dash Impulso (CD: " + com.chainores.config.ChainOresConfig.get().dashCooldownSeconds + "s)").formatted(Formatting.AQUA, Formatting.ITALIC)));
    }

    public static String getArmor2PieceDesc(int tier) {
        return switch (tier) {
            case 1 -> "Velocidad I y -25% caída";
            case 10 -> "Resistencia I";
            case 20 -> "Inmunidad a congelación";
            case 30 -> "Visión Nocturna";
            case 40 -> "Regeneración I";
            case 50 -> "Prisa Minera I";
            case 60 -> "Impulso de Salto I";
            case 70 -> "Sigilo Sculk y Visión Nocturna";
            case 80 -> "Protección Ígnea I";
            case 90 -> "Velocidad I y purga leve";
            case 100 -> "+2 Corazones Máximos";
            case 115 -> "Caída Lenta pasiva";
            case 125 -> "Resistencia I y Fuerza I";
            case 135 -> "Resistencia II";
            case 140 -> "+4 Corazones Máximos";
            case 150 -> "+6 Corazones y Resistencia II";
            default -> "Bonus básico";
        };
    }

    public static String getArmor3PieceDesc(int tier) {
        return switch (tier) {
            case 1 -> "Velocidad I y -50% caída";
            case 10 -> "Resistencia I y anti-empuje";
            case 20 -> "Prisa Minera I y respiración acuática";
            case 30 -> "Inmunidad a Fuego/Lava";
            case 40 -> "Regeneración I y saturación periódica";
            case 50 -> "Prisa II y Suerte I";
            case 60 -> "Salto II y anti-caída";
            case 70 -> "Absorción I continua";
            case 80 -> "Espinas Ígneas a enemigos";
            case 90 -> "Velocidad II y purga de venenos";
            case 100 -> "+4 Corazones y Furia Carmesí";
            case 115 -> "Salto III y Planeo Dracónico";
            case 125 -> "Escudo Gravitacional";
            case 135 -> "Bucle Temporal y rescate de caída";
            case 140 -> "+8 Corazones y sin hambre";
            case 150 -> "+10 Corazones, Fuerza II y Modo Divino";
            default -> "Bonus intermedio";
        };
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

    private static final Map<UUID, Integer> ACTIVE_SETS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> DASH_COOLDOWNS = new ConcurrentHashMap<>();

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (entity instanceof ServerPlayerEntity player) {
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
                applyGemAndReforgeArmorBuffs(player, stack);
            }

            // Apply perks from the chestplate slot once per tick to avoid quadruplicating
            if (slot == EquipmentSlot.CHEST && this.equipmentType == EquipmentType.CHESTPLATE) {
                Map<Integer, Integer> tierCounts = new HashMap<>();
                for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    if (player.getEquippedStack(s).getItem() instanceof ChainArmorItem cai) {
                        tierCounts.put(cai.getTier(), tierCounts.getOrDefault(cai.getTier(), 0) + 1);
                    }
                }

                for (Map.Entry<Integer, Integer> entry : tierCounts.entrySet()) {
                    int t = entry.getKey();
                    int count = entry.getValue();

                    if (count >= 2) {
                        apply2PiecePerk(player, t);
                    }
                    if (count >= 3) {
                        apply3PiecePerk(player, t);
                    }
                    if (count >= 4) {
                        Integer prev = ACTIVE_SETS.get(player.getUuid());
                        if (prev == null || prev != t) {
                            ACTIVE_SETS.put(player.getUuid(), t);
                            String title = getArmorPerkDesc(t);
                            if (title.contains(":")) title = title.split(":")[0];
                            player.sendMessage(Text.literal("✦ Set Activado: " + title + " ✦").formatted(Formatting.GOLD, Formatting.BOLD), true);
                            player.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.8f, 1.4f);
                        }
                        applyFullSetPerks(player, t);
                    }
                }

                if (!tierCounts.containsKey(tier) || tierCounts.get(tier) < 4) {
                    if (ACTIVE_SETS.containsKey(player.getUuid()) && ACTIVE_SETS.get(player.getUuid()) == tier) {
                        ACTIVE_SETS.remove(player.getUuid());
                    }
                }

                // Check Active Dash Ability
                handleDashAbility(player, world);
            }
        }
    }

    public static boolean hasAnyChainArmor(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (entity.getEquippedStack(slot).getItem() instanceof ChainArmorItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean performDash(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) return false;

        if (!hasAnyChainArmor(player)) {
            return false;
        }

        long now = System.currentTimeMillis();
        long cdMillis = com.chainores.config.ChainOresConfig.get().dashCooldownSeconds * 1000L;
        long lastDash = DASH_COOLDOWNS.getOrDefault(player.getUuid(), 0L);

        if (now - lastDash >= cdMillis) {
            DASH_COOLDOWNS.put(player.getUuid(), now);

            Vec3d look = player.getRotationVector();
            Vec3d horizontal = new Vec3d(look.x, 0, look.z);
            if (horizontal.lengthSquared() < 0.001) {
                horizontal = Vec3d.fromPolar(0, player.getYaw());
            }
            horizontal = horizontal.normalize();

            Vec3d dashVelocity = new Vec3d(horizontal.x * 1.85, 0.32, horizontal.z * 1.85);
            player.setVelocity(dashVelocity);
            player.velocityDirty = true;
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
            player.fallDistance = 0;

            ServerWorld world = (ServerWorld) player.getEntityWorld();
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_BREEZE_WIND_BURST, net.minecraft.sound.SoundCategory.PLAYERS, 1.2f, 1.2f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, net.minecraft.sound.SoundCategory.PLAYERS, 0.8f, 1.5f);

            world.spawnParticles(net.minecraft.particle.ParticleTypes.GUST, player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.4, 0.2, 0.4, 0.05);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 12, 0.5, 0.2, 0.5, 0.05);

            player.sendMessage(Text.literal("⚡ ¡DASH IMPULSO!").formatted(Formatting.AQUA, Formatting.BOLD), true);
            return true;
        }
        return false;
    }

    private void handleDashAbility(ServerPlayerEntity player, ServerWorld world) {
        if (com.chainores.config.ChainOresConfig.get().enableSprintSneakDash) {
            if (player.isSprinting() && player.isSneaking()) {
                performDash(player);
            }
        }
    }

    private void applyGemAndReforgeArmorBuffs(ServerPlayerEntity player, ItemStack stack) {
        java.util.List<String> gems = com.chainores.modifier.ChainModifiers.getGems(stack);
        int bloodCount = 0;
        int voidCount = 0;
        int coreCount = 0;
        int genesisCount = 0;

        for (String gem : gems) {
            switch (gem) {
                case com.chainores.modifier.ChainModifiers.GEM_BLOOD -> bloodCount++;
                case com.chainores.modifier.ChainModifiers.GEM_VOID -> voidCount++;
                case com.chainores.modifier.ChainModifiers.GEM_CORE -> coreCount++;
                case com.chainores.modifier.ChainModifiers.GEM_GENESIS -> genesisCount++;
            }
        }

        if (bloodCount > 0) {
            int amp = Math.min(bloodCount - 1, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, amp, true, false));
        }
        if (voidCount > 0) {
            if (player.fallDistance > 1.5f && !player.isOnGround()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0, true, false));
                player.fallDistance = 0;
            }
        }
        if (coreCount > 0) {
            int amp = Math.min(coreCount - 1, 2);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, amp, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));
        }
        if (genesisCount > 0) {
            if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                int amp = Math.min(genesisCount, 3);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, amp, true, false));
            }
        }

        String reforge = com.chainores.modifier.ChainModifiers.getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case com.chainores.modifier.ChainModifiers.REFORGE_DIVINE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                case com.chainores.modifier.ChainModifiers.REFORGE_SWIFT ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false));
                case com.chainores.modifier.ChainModifiers.REFORGE_FIERCE ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, true, false));
                case com.chainores.modifier.ChainModifiers.REFORGE_TITANIC ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
                case com.chainores.modifier.ChainModifiers.REFORGE_LEGENDARY ->
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 0, true, false));
            }
        }
    }

    private void apply2PiecePerk(ServerPlayerEntity player, int tier) {
        int d = 60;
        switch (tier) {
            case 1 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, d, 0, true, false));
            case 10 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, d, 0, true, false));
            case 20 -> player.setFrozenTicks(0);
            case 30 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, d, 0, true, false));
            case 40 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, d, 0, true, false));
            case 50 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, d, 0, true, false));
            case 60 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, d, 0, true, false));
            case 70 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, d, 0, true, false));
            case 80 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, d, 0, true, false));
            case 90 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, d, 0, true, false));
            case 100, 115, 125, 135, 140, 150 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, d, 1, true, false));
        }
    }

    private void apply3PiecePerk(ServerPlayerEntity player, int tier) {
        int d = 60;
        switch (tier) {
            case 1 -> {
                if (player.fallDistance > 0) player.fallDistance *= 0.5f;
            }
            case 10 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, d, 1, true, false));
            case 20 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, d, 0, true, false));
            case 30 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, d, 0, true, false));
                player.extinguish();
            }
            case 40 -> player.setAir(player.getMaxAir());
            case 50 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, d, 0, true, false));
            case 60 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, d, 0, true, false));
                player.fallDistance = 0;
            }
            case 70 -> {
                if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 0, true, false));
                }
            }
            case 80 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, d, 1, true, false));
            case 90 -> {
                if (player.hasStatusEffect(StatusEffects.POISON)) player.removeStatusEffect(StatusEffects.POISON);
                if (player.hasStatusEffect(StatusEffects.WITHER)) player.removeStatusEffect(StatusEffects.WITHER);
            }
            case 100, 115, 125, 135, 140, 150 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, d, 1, true, false));
        }
    }

    private void applyFullSetPerks(ServerPlayerEntity player, int tier) {
        int duration = 60; // 3 seconds refresh

        switch (tier) {
            case 1 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 0, true, false));
                if (player.fallDistance > 0) player.fallDistance *= 0.5f;
            }
            case 10 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 0, true, false));
            case 20 -> {
                player.setFrozenTicks(0);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, duration, 0, true, false));
            }
            case 30 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 0, true, false));
                player.extinguish();
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0, true, false));
                if (player.getEntityWorld().isDay() && player.getEntityWorld().isSkyVisible(player.getBlockPos())) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0, true, false));
                }
            }
            case 40 -> {
                player.setAir(player.getMaxAir());
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0, true, false));
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
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, duration, 0, true, false));
                if (player.getEntityWorld().getTime() % 120 == 0) {
                    player.addExperience(1);
                }
            }
            case 60 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, true, false));
                player.fallDistance = 0;
            }
            case 70 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, duration, 0, true, false));
                if (!player.hasStatusEffect(StatusEffects.ABSORPTION)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1, true, false));
                }
            }
            case 80 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, duration, 0, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 1, true, false));
                if (player.getEntityWorld().getTime() % 20 == 0) {
                    Box box = player.getBoundingBox().expand(3.5);
                    player.getEntityWorld().getEntitiesByClass(HostileEntity.class, box, LivingEntity::isAlive).forEach(mob -> mob.setOnFireFor(4));
                }
            }
            case 90 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 1, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 1, true, false));
                if (player.hasStatusEffect(StatusEffects.POISON)) player.removeStatusEffect(StatusEffects.POISON);
                if (player.hasStatusEffect(StatusEffects.WITHER)) player.removeStatusEffect(StatusEffects.WITHER);
                if (player.hasStatusEffect(StatusEffects.NAUSEA)) player.removeStatusEffect(StatusEffects.NAUSEA);
                if (player.hasStatusEffect(StatusEffects.SLOWNESS)) player.removeStatusEffect(StatusEffects.SLOWNESS);
                if (player.hasStatusEffect(StatusEffects.WEAKNESS)) player.removeStatusEffect(StatusEffects.WEAKNESS);
                if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
            }
            case 100 -> {
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
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, true, false));
                player.fallDistance = 0;
            }
            case 125 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration, 2, true, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 0, true, false));
                Box arrowBox = player.getBoundingBox().expand(3.0);
                player.getEntityWorld().getEntitiesByClass(PersistentProjectileEntity.class, arrowBox, proj -> proj.getOwner() != player).forEach(proj -> {
                    proj.setVelocity(proj.getVelocity().multiply(-1.2));
                    proj.velocityDirty = true;
                });
            }
            case 135 -> {
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
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 1, true, false));
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(20.0f);
                if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 4, true, false));
                }
            }
            case 150 -> {
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
                if (player.getEntityWorld() instanceof ServerWorld sw && player.age % 10 == 0) {
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.4, 0.6, 0.4, 0.02);
                    sw.spawnParticles(net.minecraft.particle.ParticleTypes.WAX_ON, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.3, 0.4, 0.3, 0.02);
                }
                player.getHungerManager().setSaturationLevel(20.0f);

                if (!player.hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 600, 6, true, false));
                }

                if (player.getY() < -50) {
                    player.requestTeleport(player.getX(), 100, player.getZ());
                    player.setVelocity(0, 0, 0);
                    player.velocityDirty = true;
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0, true, false));
                }

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
