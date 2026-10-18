package com.chainores.entity;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AbyssalGuardianEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int sonicCooldown = 100;
    private int chargeTimer = 0;
    private int slamCooldown = 120;
    private int dashCooldown = 80;
    private boolean isEnraged = false;
    private int scaledPlayerCount = 1;
    private BlockPos domainOrigin = null;
    private boolean isDying = false;
    private int deathTicks = 0;

    public AbyssalGuardianEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.abyssal_guardian").formatted(Formatting.DARK_AQUA, Formatting.BOLD),
                BossBar.Color.BLUE,
                BossBar.Style.NOTCHED_10
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 800;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 650.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.ATTACK_DAMAGE, 32.0)
                .add(EntityAttributes.ARMOR, 20.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isDying) {
            return false;
        }
        if (amount >= this.getHealth()) {
            this.isDying = true;
            this.setHealth(1.0f);
            this.setInvulnerable(true);
            this.bossBar.setPercent(0.0f);
            world.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_WARDEN_DEATH, SoundCategory.HOSTILE, 1.8f, 0.7f);
            return true;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 1), this);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.HOSTILE, 1.4f, 0.8f);

            // Lifesteal: heals 25% of attack damage
            float lifesteal = (float) (this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) * 0.25);
            this.heal(lifesteal);
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.2, this.getZ(), 8, 0.4, 0.5, 0.4, 0.05);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);

        if (this.domainOrigin == null) {
            this.domainOrigin = this.getBlockPos();
        }

        if (this.isDying) {
            this.deathTicks++;
            BossCinematicsHelper.tickBossDeath(this, world, this.deathTicks, ParticleTypes.SCULK_SOUL, () -> {
                dropLootAndFinish(world);
            });
            return;
        }

        BossArenaHelper.enforceDomain(this, world, this.domainOrigin, BossArenaHelper.DEFAULT_RADIUS, ParticleTypes.SCULK_SOUL);
        this.scaledPlayerCount = BossScalingHelper.updateBossScaling(this, world, 650.0, 32.0, this.scaledPlayerCount);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Auto-prune excess duplicate bosses from old unspaced world generation
        if (this.age % 40 == 0 && world.getRegistryKey().equals(World.OVERWORLD)) {
            if (!this.getCommandTags().contains("custom_boss") &&
                !com.chainores.world.StructureLocator.isNearRegisteredStructure(world, com.chainores.world.StructureLocator.TYPE_FORGE, this.getBlockPos(), 80.0)) {
                this.bossBar.clearPlayers();
                this.discard();
                return;
            }
        }

        // Ambient sculk particles
        if (world.random.nextInt(3) == 0) {
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.4, 0.4, 0.4, 0.02);
        }

        // Passive Sculk Regeneration (continuous healing in combat or ambient)
        if (this.age % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            float regen = isEnraged ? 6.0f : 3.0f;
            this.heal(regen);
            if (this.age % 40 == 0) {
                world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.3, 0.3, 0.3, 0.03);
            }
        }

        // Constant Status Buffs (Resistance & Strength, enhanced in Enrage)
        if (this.age % 20 == 0) {
            if (isEnraged) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false, true)); // Resistance II (-40% damage)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, false, false, true));   // Strength II
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true));      // Speed II
            } else {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, false, false, true)); // Resistance I (-20% damage)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false, true));   // Strength I
            }
        }

        // Enrage Trigger at 50% health
        if (!isEnraged && this.getHealth() < this.getMaxHealth() * 0.5f) {
            isEnraged = true;
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 2.5f, 0.7f);
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 2.0f, 0.8f);
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.5, this.getZ(), 50, 0.8, 1.2, 0.8, 0.1);

            Text enrageMsg = Text.literal("⚡ ¡El Guardián Abisal se enfurece! Ruge con furia sónica, aumentando su velocidad, resistencia y fuerza.")
                    .formatted(Formatting.DARK_AQUA, Formatting.BOLD);
            for (ServerPlayerEntity p : this.bossBar.getPlayers()) {
                p.sendMessage(enrageMsg, false);
            }
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player && player.isDead()) {
            healToFull(world);
        }
        if (target != null && target.isAlive()) {
            double distanceSq = this.squaredDistanceTo(target);

            // 1. Sonic Shockwave Beam (Distance 5 - 22 blocks)
            int effectiveSonicCd = isEnraged ? 60 : 120;
            if (sonicCooldown > 0) {
                sonicCooldown--;
            } else if (distanceSq > 25.0 && distanceSq < 484.0) {
                chargeTimer++;
                if (chargeTimer == 1) {
                    world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.HOSTILE, 1.6f, 1.0f);
                } else if (chargeTimer >= (isEnraged ? 15 : 25)) {
                    fireSonicWave(world, target);
                    chargeTimer = 0;
                    sonicCooldown = effectiveSonicCd;
                }
            } else {
                chargeTimer = 0;
            }

            // 2. Abyssal Ground Slam (Close Range AOE within 7 blocks)
            if (slamCooldown > 0) {
                slamCooldown--;
            } else if (distanceSq <= 49.0) {
                executeGroundSlam(world);
                slamCooldown = isEnraged ? 70 : 120; // 3.5s or 6s cooldown
            }

            // 3. Shadow Surge / Gap Closer (When target is far: 9 to 24 blocks)
            if (dashCooldown > 0) {
                dashCooldown--;
            } else if (distanceSq > 81.0 && distanceSq < 576.0) {
                executeShadowDash(world, target);
                dashCooldown = isEnraged ? 60 : 100;
            }
        } else {
            chargeTimer = 0;
        }
    }

    private void executeGroundSlam(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.HOSTILE, 2.0f, 0.7f);
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.4f, 1.2f);
        BossCinematicsHelper.triggerScreenShake(world, this.getEntityPos(), 24.0, 0.28);

        // Ring of sculk soul particles
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            double px = this.getX() + Math.cos(rad) * 5.0;
            double pz = this.getZ() + Math.sin(rad) * 5.0;
            world.spawnParticles(ParticleTypes.SCULK_SOUL, px, this.getY() + 0.3, pz, 3, 0.2, 0.3, 0.2, 0.04);
            world.spawnParticles(ParticleTypes.SONIC_BOOM, px, this.getY() + 0.5, pz, 1, 0, 0, 0, 0);
        }

        Box aoe = this.getBoundingBox().expand(6.0, 3.0, 6.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, aoe, e -> e != this && e.isAlive());
        for (LivingEntity enemy : enemies) {
            if (!(enemy instanceof AbyssalGuardianEntity)) {
                enemy.damage(world, this.getDamageSources().magic(), 24.0f);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 160, 0), this);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2), this);
                enemy.addVelocity(0.0, 0.65, 0.0);
                enemy.velocityDirty = true;
            }
        }
    }

    private void executeShadowDash(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d dir = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 1.8f, 1.1f);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 2, false, false, true));

        // Burst forward
        this.setVelocity(dir.x * 1.4, 0.25, dir.z * 1.4);
        this.velocityDirty = true;

        for (int i = 0; i < 20; i++) {
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX() + (world.random.nextDouble() - 0.5) * 1.5,
                    this.getY() + world.random.nextDouble() * 2.0,
                    this.getZ() + (world.random.nextDouble() - 0.5) * 1.5,
                    1, 0, 0, 0, 0.05);
        }
    }

    private void fireSonicWave(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d direction = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 2.0f, 1.0f);
        BossCinematicsHelper.triggerScreenShake(world, this.getEntityPos(), 26.0, 0.30);

        // Particle beam
        double distance = origin.distanceTo(targetPos);
        for (int i = 1; i <= Math.min((int) distance, 24); i++) {
            Vec3d point = origin.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }

        // Deal damage and knockback
        DamageSource source = this.getDamageSources().magic();
        target.damage(world, source, isEnraged ? 42.0f : 32.0f);
        target.addVelocity(direction.x * 1.6, 0.65, direction.z * 1.6);
        target.velocityDirty = true;
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

    private void dropLootAndFinish(ServerWorld world) {
        if (this.domainOrigin != null) {
            BossArenaHelper.breakDomain(world, this.domainOrigin, BossArenaHelper.DEFAULT_RADIUS, ParticleTypes.SCULK_SOUL);
        }
        this.dropStack(world, new ItemStack(ModBossItems.ABYSSAL_CATALYST, 2));
        this.dropStack(world, new ItemStack(ModBossItems.ABYSSAL_TRIDENT, 1));
        this.dropStack(world, new ItemStack(ModBossItems.ABYSSAL_TROPHY, 1));
        if (OreRegistryData.TIERS.size() >= 70) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(69).ingotItem, 6));
        }
        this.bossBar.clearPlayers();
        this.discard();
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
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.5, this.getZ(), 50, 0.6, 1.0, 0.6, 0.2);
            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, 1.4f, 0.7f);
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
