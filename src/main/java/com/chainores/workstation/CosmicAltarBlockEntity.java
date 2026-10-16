package com.chainores.workstation;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CosmicAltarBlockEntity extends BlockEntity {
    private boolean arenaActive = false;
    private int currentWave = 0;
    private int waveTimer = 0;
    private final List<UUID> activeWaveMobs = new ArrayList<>();

    public CosmicAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModWorkstations.COSMIC_ALTAR_BLOCK_ENTITY, pos, state);
    }

    public boolean isArenaActive() {
        return arenaActive;
    }

    public boolean startArena(PlayerEntity initiator) {
        if (arenaActive) return false;
        if (!(world instanceof ServerWorld serverWorld)) return false;

        this.arenaActive = true;
        this.currentWave = 1;
        this.waveTimer = 60; // 3 seconds before wave 1 spawns
        this.activeWaveMobs.clear();
        this.markDirty();

        world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.5f, 1.0f);
        world.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.2f, 0.9f);
        serverWorld.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 60, 0.5, 1.0, 0.5, 0.2);

        broadcastToArena(serverWorld, Text.literal("✦ ¡DESAFÍO DE LAS OLAS ASTRALES INICIADO! ✦").formatted(Formatting.GOLD, Formatting.BOLD));
        broadcastToArena(serverWorld, Text.literal("Ola 1/3 comenzando en 3 segundos... ¡Prepárate!").formatted(Formatting.YELLOW));
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
            if (altar.currentWave < 3) {
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
                broadcastToArena(world, Text.literal("⚔ Ola 1: Campeones Ígneos y del Sculk").formatted(Formatting.RED, Formatting.BOLD));
                for (int i = 0; i < 6; i++) {
                    double angle = (Math.PI * 2 / 6) * i;
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
                            hp.setBaseValue(120.0);
                            zombie.setHealth(120.0f);
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
                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 / 4) * i;
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
                            hp.setBaseValue(180.0);
                            skeleton.setHealth(180.0f);
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
                        hp.setBaseValue(500.0);
                        avatar.setHealth(500.0f);
                    }
                    var dmg = avatar.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
                    if (dmg != null) dmg.setBaseValue(35.0);
                    avatar.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 1, false, false));
                    avatar.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 12000, 0, false, false));
                    world.spawnEntity(avatar);
                    activeWaveMobs.add(avatar.getUuid());
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
            chest.setStack(0, new ItemStack(ModGems.BLOOD_GEM_3, 1));
            chest.setStack(1, new ItemStack(ModGems.VOID_GEM_3, 1));
            chest.setStack(2, new ItemStack(ModGems.CORE_GEM_3, 1));
            chest.setStack(3, new ItemStack(ModGems.GENESIS_GEM_3, 1));
            chest.setStack(4, new ItemStack(ModBossItems.COSMIC_SINGULARITY, 2));
            if (OreRegistryData.TIERS.size() >= 150) {
                chest.setStack(12, new ItemStack(OreRegistryData.TIERS.get(149).ingotItem, 4));
            }
            chest.setStack(14, new ItemStack(ModItems.CHAIN_AMULET, 1));
            chest.setStack(22, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 3));
        }

        // Spawn XP
        ExperienceOrbEntity.spawn(world, net.minecraft.util.math.Vec3d.ofCenter(chestPos), 5000);

        broadcastToArena(world, Text.literal("✦✦✦ ¡VICTORIA SUPREMA! ✦✦✦").formatted(Formatting.GOLD, Formatting.BOLD));
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
    }

    @Override
    protected void readData(ReadView readView) {
        super.readData(readView);
        this.arenaActive = readView.getBoolean("arenaActive", false);
        this.currentWave = readView.getInt("currentWave", 0);
        this.waveTimer = readView.getInt("waveTimer", 0);
    }
}
