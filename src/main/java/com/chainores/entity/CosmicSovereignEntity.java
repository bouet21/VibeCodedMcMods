package com.chainores.entity;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class CosmicSovereignEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int thunderCooldown = 60;
    private int burstCooldown = 100;
    private int supernovaCooldown = 130;
    private boolean isEnraged = false;
    private int scaledPlayerCount = 1;

    public CosmicSovereignEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.cosmic_sovereign").formatted(Formatting.GOLD, Formatting.BOLD),
                BossBar.Color.YELLOW,
                BossBar.Style.NOTCHED_20
        );
        this.bossBar.setDarkenSky(true);
        this.bossBar.setThickenFog(true);
        this.experiencePoints = 15000;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 2500.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.40)
                .add(EntityAttributes.ATTACK_DAMAGE, 75.0)
                .add(EntityAttributes.ARMOR, 30.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.45, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 36.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Immunity to all projectiles (cosmic kinetic shield)
        if (source.getSource() instanceof ProjectileEntity) {
            world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK.value(), SoundCategory.HOSTILE, 2.0f, 1.8f);
            world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.5, this.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.8f, 1.4f);
            target.addVelocity(0.0, 0.85, 0.0);
            target.velocityDirty = true;

            // Divine Lifesteal: heals 35% of attack damage
            float lifesteal = (float) (this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) * 0.35);
            this.heal(lifesteal);
            world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.5, this.getZ(), 12, 0.4, 0.6, 0.4, 0.1);

            // Strike lightning on hit
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
                lightning.setCosmetic(true);
                world.spawnEntity(lightning);
            }
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.scaledPlayerCount = BossScalingHelper.updateBossScaling(this, world, 2500.0, 75.0, this.scaledPlayerCount);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Ambient celestial sparkles
        world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.5, this.getZ(), 3, 0.6, 0.8, 0.6, 0.03);
        if (world.random.nextInt(4) == 0) {
            world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.4, 0.5, 0.4, 0.05);
        }

        // Float / Hovering mechanism
        if (this.getVelocity().y < -0.15) {
            this.setVelocity(this.getVelocity().x, -0.15, this.getVelocity().z);
        }

        // Passive Divine Regeneration
        if (this.age % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            float regen = isEnraged ? 14.0f : 6.0f;
            this.heal(regen);
            if (this.age % 40 == 0) {
                world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.2, this.getZ(), 4, 0.3, 0.4, 0.3, 0.03);
            }
        }

        // Constant Status Buffs (Resistance & Strength & Speed, enhanced in Enrage)
        if (this.age % 20 == 0) {
            if (isEnraged) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 2, false, false, true)); // Resistance III (-60% damage!)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 3, false, false, true));   // Strength IV
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true));      // Speed II
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, false, false, true)); // Regeneration II
            } else {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false, true)); // Resistance II (-40% damage)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, false, false, true));   // Strength II
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, false, true));      // Speed I
            }
        }

        // Enrage Trigger at 50% health (Cosmic Awakening)
        if (!isEnraged && this.getHealth() < this.getMaxHealth() * 0.5f) {
            isEnraged = true;

            world.playSound(null, this.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.HOSTILE, 2.5f, 0.8f);
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.5f, 0.7f);
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5, this.getZ(), 40, 0.6, 1.0, 0.6, 0.2);
            world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.5, this.getZ(), 50, 1.0, 1.5, 1.0, 0.15);

            // Strike lightning at self
            LightningEntity selfLightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
            if (selfLightning != null) {
                selfLightning.refreshPositionAfterTeleport(this.getX(), this.getY(), this.getZ());
                selfLightning.setCosmetic(true);
                world.spawnEntity(selfLightning);
            }

            Text enrageMsg = Text.literal("✨ ¡El Soberano Cósmico desata el poder del Big Bang! Resistencia suprema, velocidad celestial y cataclismo activados.")
                    .formatted(Formatting.GOLD, Formatting.BOLD);
            for (ServerPlayerEntity serverPlayer : world.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(enrageMsg, false);
            }
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player && player.isDead()) {
            healToFull(world);
        }
        if (target != null && target.isAlive()) {
            double distanceSq = this.squaredDistanceTo(target);

            // 1. Divine Lightning Cataclysm
            int effectiveThunderCd = isEnraged ? 35 : 60;
            if (thunderCooldown > 0) {
                thunderCooldown--;
            } else {
                strikeDivineThunder(world, target);
                thunderCooldown = effectiveThunderCd;
            }

            // 2. Cosmic Ray Burst (Distance 6 - 36 blocks)
            int effectiveBurstCd = isEnraged ? 60 : 100;
            if (burstCooldown > 0) {
                burstCooldown--;
            } else if (distanceSq > 36.0) {
                castCosmicBurst(world, target);
                burstCooldown = effectiveBurstCd;
            }

            // 3. Supernova Pulse (AOE Blast within 12 blocks)
            int effectiveSupernovaCd = isEnraged ? 75 : 130;
            if (supernovaCooldown > 0) {
                supernovaCooldown--;
            } else if (distanceSq <= 144.0) {
                executeSupernova(world);
                supernovaCooldown = effectiveSupernovaCd;
            }
        }
    }

    private void strikeDivineThunder(ServerWorld world, LivingEntity target) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 2.0f, 1.2f);
        int strikes = isEnraged ? 4 : 2;
        for (int i = 0; i < strikes; i++) {
            double ox = target.getX() + (world.random.nextDouble() - 0.5) * 4.0;
            double oz = target.getZ() + (world.random.nextDouble() - 0.5) * 4.0;

            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(ox, target.getY(), oz);
                world.spawnEntity(lightning);
            }
        }
    }

    private void castCosmicBurst(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d dir = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.HOSTILE, 2.0f, 1.1f);

        // Flash beam
        double dist = origin.distanceTo(targetPos);
        for (double d = 1.0; d <= dist; d += 1.5) {
            Vec3d p = origin.add(dir.multiply(d));
            world.spawnParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 3, 0.2, 0.2, 0.2, 0.05);
        }

        target.damage(world, this.getDamageSources().magic(), isEnraged ? 60.0f : 45.0f);
        target.addVelocity(dir.x * 1.8, 0.7, dir.z * 1.8);
        target.velocityDirty = true;
    }

    private void executeSupernova(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 2.5f, 1.2f);
        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.HOSTILE, 2.0f, 0.6f);

        // Supernova ring particles
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            double px = this.getX() + Math.cos(rad) * 6.0;
            double pz = this.getZ() + Math.sin(rad) * 6.0;
            world.spawnParticles(ParticleTypes.END_ROD, px, this.getY() + 1.0, pz, 3, 0.2, 0.4, 0.2, 0.05);
            world.spawnParticles(ParticleTypes.FIREWORK, px, this.getY() + 1.2, pz, 2, 0.1, 0.2, 0.1, 0.05);
        }

        Box blastBox = this.getBoundingBox().expand(12.0, 4.0, 12.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, blastBox, e -> e != this && e.isAlive());
        for (LivingEntity enemy : enemies) {
            if (!(enemy instanceof CosmicSovereignEntity)) {
                Vec3d pushDir = enemy.getEyePos().subtract(this.getEyePos()).normalize();
                enemy.addVelocity(pushDir.x * 2.0, 0.8, pushDir.z * 2.0);
                enemy.velocityDirty = true;
                enemy.damage(world, this.getDamageSources().magic(), isEnraged ? 45.0f : 32.0f);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 0), this);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1), this);
            }
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(world, damageSource, causedByPlayer);
        // Guaranteed ultimate rewards
        this.dropStack(world, new ItemStack(ModBossItems.COSMIC_SINGULARITY, 1));
        this.dropStack(world, new ItemStack(ModBossItems.COSMIC_TROPHY, 1));
        if (OreRegistryData.TIERS.size() >= 150) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(149).ingotItem, 16));
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        this.bossBar.clearPlayers();
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource damageSource) {
        boolean result = super.onKilledOther(world, other, damageSource);
        if (other instanceof ServerPlayerEntity) {
            healToFull(world);
        }
        return result;
    }

    public void healToFull(ServerWorld world) {
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5, this.getZ(), 60, 0.8, 1.2, 0.8, 0.25);
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, 1.5f, 0.7f);
            Text msg = Text.literal("☠ ¡Has caído ante ").formatted(Formatting.DARK_RED, Formatting.BOLD)
                    .append(this.getName())
                    .append(Text.literal("! Ha devorado tu alma y restaurado el 100% de su vida.").formatted(Formatting.RED, Formatting.BOLD));
            for (ServerPlayerEntity p : this.bossBar.getPlayers()) {
                p.sendMessage(msg, false);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.clearPlayers();
    }
}
