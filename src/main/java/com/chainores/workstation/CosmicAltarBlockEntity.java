package com.chainores.workstation;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import com.chainores.registry.ModEntities;
import com.chainores.registry.ModGear;
import com.chainores.registry.ModGems;
import com.chainores.registry.ModItems;
import com.chainores.registry.ModWorkstations;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CosmicAltarBlockEntity extends BlockEntity {
    public enum AltarDifficulty {
        NORMAL(3, "Normal", Formatting.GREEN),
        HARD(5, "Difícil", Formatting.GOLD),
        NIGHTMARE(7, "Pesadilla", Formatting.RED);

        public final int maxWaves;
        public final String displayName;
        public final Formatting color;

        AltarDifficulty(int maxWaves, String displayName, Formatting color) {
            this.maxWaves = maxWaves;
            this.displayName = displayName;
            this.color = color;
        }
    }

    private boolean arenaActive = false;
    private int currentWave = 0;
    private int waveTimer = 0;
    private AltarDifficulty difficulty = AltarDifficulty.NORMAL;
    private final List<UUID> activeWaveMobs = new ArrayList<>();

    public CosmicAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModWorkstations.COSMIC_ALTAR_BLOCK_ENTITY, pos, state);
    }

    public boolean isArenaActive() {
        return arenaActive;
    }

    public AltarDifficulty getDifficulty() {
        return difficulty;
    }

    public boolean startArena(PlayerEntity initiator, AltarDifficulty diff) {
        if (arenaActive) return false;
        if (!(world instanceof ServerWorld serverWorld)) return false;

        this.arenaActive = true;
        this.difficulty = diff;
        this.currentWave = 1;
        this.waveTimer = 60; // 3 seconds before wave 1 spawns
        this.activeWaveMobs.clear();
        this.markDirty();

        world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.5f, 1.0f);
        world.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.2f, 0.9f);
        serverWorld.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 60, 0.5, 1.0, 0.5, 0.2);

        broadcastToArena(serverWorld, Text.literal("✦ ¡DESAFÍO DE LAS OLAS ASTRALES [" + diff.displayName.toUpperCase() + "] INICIADO! ✦").formatted(diff.color, Formatting.BOLD));
        broadcastToArena(serverWorld, Text.literal("Ola 1/" + diff.maxWaves + " comenzando en 3 segundos... ¡Prepárate!").formatted(Formatting.YELLOW));
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, CosmicAltarBlockEntity altar) {
        if (world.isClient() || !altar.arenaActive || !(world instanceof ServerWorld serverWorld)) return;

        // Ambient particles while arena is active
        if (altar.arenaActive && serverWorld.random.nextInt(3) == 0) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 4, 0.4, 0.5, 0.4, 0.05);
            serverWorld.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, 2, 0.2, 0.5, 0.2, 0.02);
        }

        if (altar.waveTimer > 0) {
            altar.waveTimer--;
            if (altar.waveTimer == 0) {
                altar.spawnWave(serverWorld, altar.currentWave);
            }
            return;
        }

        // Clean up dead/despawned mobs
        altar.activeWaveMobs.removeIf(uuid -> {
            net.minecraft.entity.Entity e = serverWorld.getEntity(uuid);
            return e == null || !e.isAlive();
        });

        // Check if wave is cleared
        if (altar.activeWaveMobs.isEmpty()) {
            if (altar.currentWave < altar.difficulty.maxWaves) {
                altar.currentWave++;
                altar.waveTimer = 100; // 5 seconds break
                altar.broadcastToArena(serverWorld, Text.literal("✔ ¡Ola completada! Siguiente ola en 5 segundos...").formatted(Formatting.GREEN, Formatting.BOLD));
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.5f, 1.2f);
            } else {
                // Victory!
                altar.completeArena(serverWorld);
            }
        }
    }

    private void spawnWave(ServerWorld world, int wave) {
        activeWaveMobs.clear();
        world.playSound(null, pos, SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 1.5f, 1.0f);

        switch (wave) {
            case 1 -> {
                broadcastToArena(world, Text.literal("⚔ Ola 1: Campeones Ígneos y de la Forja").formatted(Formatting.RED, Formatting.BOLD));
                int count = difficulty == AltarDifficulty.NIGHTMARE ? 8 : 6;
                for (int i = 0; i < count; i++) {
                    double angle = (Math.PI * 2 / count) * i;
                    double sx = pos.getX() + 0.5 + Math.cos(angle) * 8.0;
                    double sz = pos.getZ() + 0.5 + Math.sin(angle) * 8.0;
                    double sy = pos.getY() + 1.0;

                    ZombieEntity zombie = EntityType.ZOMBIE.create(world, SpawnReason.EVENT);
                    if (zombie != null) {
                        zombie.refreshPositionAndAngles(sx, sy, sz, 0, 0);
                        zombie.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                        zombie.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
                        zombie.setCustomName(Text.literal("Campeón de la Forja").formatted(Formatting.GOLD, Formatting.BOLD));
                        zombie.setCustomNameVisible(true);
                        var hp = zombie.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                        if (hp != null) {
                            double health = difficulty == AltarDifficulty.NIGHTMARE ? 180.0 : 120.0;
                            hp.setBaseValue(health);
                            zombie.setHealth((float) health);
                        }
                        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 12000, 1, false, false));
                        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0, false, false));
                        world.spawnEntity(zombie);
                        activeWaveMobs.add(zombie.getUuid());
                    }
                }
            }
            case 2 -> {
                broadcastToArena(world, Text.literal("⚔ Ola 2: Segadores del Vacío y Espectros Corruptos").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
                int count = difficulty == AltarDifficulty.NIGHTMARE ? 6 : 4;
                for (int i = 0; i < count; i++) {
                    double angle = (Math.PI * 2 / count) * i;
                    double sx = pos.getX() + 0.5 + Math.cos(angle) * 9.0;
                    double sz = pos.getZ() + 0.5 + Math.sin(angle) * 9.0;
                    double sy = pos.getY() + 1.0;

                    WitherSkeletonEntity skeleton = EntityType.WITHER_SKELETON.create(world, SpawnReason.EVENT);
                    if (skeleton != null) {
                        skeleton.refreshPositionAndAngles(sx, sy, sz, 0, 0);
                        skeleton.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                        skeleton.setCustomName(Text.literal("Segador del Vacío").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
                        skeleton.setCustomNameVisible(true);
                        var hp = skeleton.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                        if (hp != null) {
                            double health = difficulty == AltarDifficulty.NIGHTMARE ? 240.0 : 180.0;
                            hp.setBaseValue(health);
                            skeleton.setHealth((float) health);
                        }
                        skeleton.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 12000, 1, false, false));
                        world.spawnEntity(skeleton);
                        activeWaveMobs.add(skeleton.getUuid());
                    }
                }
            }
            case 3 -> {
                broadcastToArena(world, Text.literal("☠ Ola 3: Avatar de la Singularidad Cósmica").formatted(Formatting.GOLD, Formatting.BOLD));
                EndermanEntity avatar = EntityType.ENDERMAN.create(world, SpawnReason.EVENT);
                if (avatar != null) {
                    avatar.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 0, 0);
                    avatar.setCustomName(Text.literal("★ Avatar del Cosmos ★").formatted(Formatting.GOLD, Formatting.BOLD));
                    avatar.setCustomNameVisible(true);
                    var hp = avatar.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                    if (hp != null) {
                        double health = difficulty == AltarDifficulty.NIGHTMARE ? 750.0 : 500.0;
                        hp.setBaseValue(health);
                        avatar.setHealth((float) health);
                    }
                    var dmg = avatar.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
                    if (dmg != null) dmg.setBaseValue(38.0);
                    avatar.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 1, false, false));
                    avatar.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 12000, 0, false, false));
                    world.spawnEntity(avatar);
                    activeWaveMobs.add(avatar.getUuid());
                }
            }
            case 4 -> {
                broadcastToArena(world, Text.literal("❄ Ola 4: Dúo Elemental - Guardián Criogénico y Titán Silvestre").formatted(Formatting.AQUA, Formatting.BOLD));
                var frost = ModEntities.FROSTWARDEN.create(world, SpawnReason.EVENT);
                if (frost != null) {
                    frost.refreshPositionAndAngles(pos.getX() - 6.0, pos.getY() + 1.0, pos.getZ(), 0, 0);
                    world.spawnEntity(frost);
                    activeWaveMobs.add(frost.getUuid());
                }
                var sylva = ModEntities.SYLVA_ANCIENT.create(world, SpawnReason.EVENT);
                if (sylva != null) {
                    sylva.refreshPositionAndAngles(pos.getX() + 6.0, pos.getY() + 1.0, pos.getZ(), 0, 0);
                    world.spawnEntity(sylva);
                    activeWaveMobs.add(sylva.getUuid());
                }
            }
            case 5 -> {
                broadcastToArena(world, Text.literal("🔥 Ola 5: Behemoth del Nether & Guardián Abisal").formatted(Formatting.DARK_RED, Formatting.BOLD));
                var abyssal = ModEntities.ABYSSAL_GUARDIAN.create(world, SpawnReason.EVENT);
                if (abyssal != null) {
                    abyssal.refreshPositionAndAngles(pos.getX() - 7.0, pos.getY() + 1.0, pos.getZ(), 0, 0);
                    world.spawnEntity(abyssal);
                    activeWaveMobs.add(abyssal.getUuid());
                }
                var nether = ModEntities.NETHER_BEHEMOTH.create(world, SpawnReason.EVENT);
                if (nether != null) {
                    nether.refreshPositionAndAngles(pos.getX() + 7.0, pos.getY() + 1.0, pos.getZ(), 0, 0);
                    world.spawnEntity(nether);
                    activeWaveMobs.add(nether.getUuid());
                }
            }
            case 6 -> {
                broadcastToArena(world, Text.literal("🌌 Ola 6 (Pesadilla): Coloso del Vacío Primordial").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
                var voidColossus = ModEntities.VOID_COLOSSUS.create(world, SpawnReason.EVENT);
                if (voidColossus != null) {
                    voidColossus.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);
                    world.spawnEntity(voidColossus);
                    activeWaveMobs.add(voidColossus.getUuid());
                }
            }
            case 7 -> {
                broadcastToArena(world, Text.literal("★ OLA FINAL (PESADILLA): APOTEOSIS DEL SOBERANO CÓSMICO ★").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
                var sovereign = ModEntities.COSMIC_SOVEREIGN.create(world, SpawnReason.EVENT);
                if (sovereign != null) {
                    sovereign.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 0, 0);
                    world.spawnEntity(sovereign);
                    activeWaveMobs.add(sovereign.getUuid());
                }
            }
        }
        markDirty();
    }

    private void completeArena(ServerWorld world) {
        this.arenaActive = false;
        this.currentWave = 0;
        this.activeWaveMobs.clear();
        this.markDirty();

        world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 2.0f, 1.0f);
        world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 100, 0.8, 1.0, 0.8, 0.3);

        // Spawn Ascension Chest atop altar
        BlockPos chestPos = pos.up();
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            if (difficulty == AltarDifficulty.NORMAL) {
                chest.setStack(0, new ItemStack(ModGems.BLOOD_GEM_3, 1));
                chest.setStack(1, new ItemStack(ModGems.VOID_GEM_3, 1));
                chest.setStack(2, new ItemStack(ModGems.CORE_GEM_3, 1));
                chest.setStack(3, new ItemStack(ModGems.GENESIS_GEM_3, 1));
                chest.setStack(4, new ItemStack(ModGems.EARTH_GEM_3, 1));
                chest.setStack(5, new ItemStack(ModBossItems.COSMIC_SINGULARITY, 2));
                if (OreRegistryData.TIERS.size() >= 150) {
                    chest.setStack(12, new ItemStack(OreRegistryData.TIERS.get(149).ingotItem, 4));
                }
                chest.setStack(14, new ItemStack(ModItems.CHAIN_AMULET, 1));
                chest.setStack(22, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 3));
                ExperienceOrbEntity.spawn(world, net.minecraft.util.math.Vec3d.ofCenter(chestPos), 5000);
            } else if (difficulty == AltarDifficulty.HARD) {
                chest.setStack(0, new ItemStack(ModGems.BLOOD_GEM_3, 2));
                chest.setStack(1, new ItemStack(ModGems.VOID_GEM_3, 2));
                chest.setStack(2, new ItemStack(ModGems.CORE_GEM_3, 2));
                chest.setStack(3, new ItemStack(ModGems.GENESIS_GEM_3, 2));
                chest.setStack(4, new ItemStack(ModGems.EARTH_GEM_3, 2));
                chest.setStack(5, new ItemStack(ModBossItems.COSMIC_SINGULARITY, 4));
                if (OreRegistryData.TIERS.size() >= 150) {
                    chest.setStack(12, new ItemStack(OreRegistryData.TIERS.get(149).ingotItem, 8));
                }
                chest.setStack(14, new ItemStack(ModGear.COSMIC_ELYTRA_CHESTPLATE, 1));
                chest.setStack(22, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 6));
                ExperienceOrbEntity.spawn(world, net.minecraft.util.math.Vec3d.ofCenter(chestPos), 12000);
            } else { // NIGHTMARE
                chest.setStack(0, new ItemStack(ModGems.BLOOD_GEM_3, 3));
                chest.setStack(1, new ItemStack(ModGems.VOID_GEM_3, 3));
                chest.setStack(2, new ItemStack(ModGems.CORE_GEM_3, 3));
                chest.setStack(3, new ItemStack(ModGems.GENESIS_GEM_3, 3));
                chest.setStack(4, new ItemStack(ModGems.EARTH_GEM_3, 3));
                chest.setStack(5, new ItemStack(ModBossItems.COSMIC_SINGULARITY, 8));
                if (OreRegistryData.TIERS.size() >= 150) {
                    chest.setStack(12, new ItemStack(OreRegistryData.TIERS.get(149).ingotItem, 16));
                }
                chest.setStack(13, new ItemStack(ModGear.GOD_OMNITOOL, 1));
                chest.setStack(14, new ItemStack(ModBossItems.COSMIC_TROPHY, 1));
                chest.setStack(22, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 12));
                ExperienceOrbEntity.spawn(world, net.minecraft.util.math.Vec3d.ofCenter(chestPos), 35000);
            }
        }

        broadcastToArena(world, Text.literal("✦✦✦ ¡VICTORIA SUPREMA [" + difficulty.displayName.toUpperCase() + "]! ✦✦✦").formatted(difficulty.color, Formatting.BOLD));
        broadcastToArena(world, Text.literal("Has superado las Olas Astrales. El Cofre de Ascensión ha descendido sobre el altar.").formatted(Formatting.AQUA, Formatting.BOLD));
    }

    private void broadcastToArena(ServerWorld world, Text message) {
        for (ServerPlayerEntity p : world.getPlayers(player -> player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 64.0 * 64.0)) {
            p.sendMessage(message, false);
        }
    }

    @Override
    protected void writeData(WriteView writeView) {
        super.writeData(writeView);
        writeView.putBoolean("arenaActive", this.arenaActive);
        writeView.putInt("currentWave", this.currentWave);
        writeView.putInt("waveTimer", this.waveTimer);
        writeView.putString("difficulty", this.difficulty.name());
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.arenaActive = readView.getBoolean("arenaActive", false);
        this.currentWave = readView.getInt("currentWave", 0);
        this.waveTimer = readView.getInt("waveTimer", 0);
        String diffName = readView.getString("difficulty", AltarDifficulty.NORMAL.name());
        try {
            this.difficulty = AltarDifficulty.valueOf(diffName);
        } catch (Exception e) {
            this.difficulty = AltarDifficulty.NORMAL;
        }
    }
}
