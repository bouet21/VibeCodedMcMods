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

public class SylvaAncientEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int rootCooldown = 100;
    private int quakeCooldown = 140;
    private int scaledPlayerCount = 1;
    private BlockPos domainOrigin = null;
    private boolean isDying = false;
    private int deathTicks = 0;

    public SylvaAncientEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.sylva_ancient").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
                BossBar.Color.GREEN,
                BossBar.Style.NOTCHED_10
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 650;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 480.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.ATTACK_DAMAGE, 24.0)
                .add(EntityAttributes.ARMOR, 16.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.90)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.75));
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
                    SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5f, 1.0f);
            return true;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1), this);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.3f, 0.8f);
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
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
            BossCinematicsHelper.tickBossDeath(this, world, this.deathTicks, ParticleTypes.HAPPY_VILLAGER, () -> {
                dropLootAndFinish(world);
            });
            return;
        }

        BossArenaHelper.enforceDomain(this, world, this.domainOrigin, BossArenaHelper.DEFAULT_RADIUS, ParticleTypes.HAPPY_VILLAGER);
        this.scaledPlayerCount = BossScalingHelper.updateBossScaling(this, world, 480.0, 24.0, this.scaledPlayerCount);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Photosynthetic healing when wounded
        if (this.age % 60 == 0 && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.heal(12.0f);
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5, this.getZ(), 8, 0.5, 0.5, 0.5, 0.05);
            world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.HOSTILE, 1.0f, 1.2f);
        }

        // Skill 1: Root Entanglement
        if (rootCooldown > 0) {
            rootCooldown--;
        } else {
            LivingEntity target = this.getTarget();
            if (target != null && this.squaredDistanceTo(target) <= 12.0 * 12.0) {
                performRootEntanglement(world);
                rootCooldown = 110 + world.random.nextInt(30);
            }
        }

        // Skill 2: Gaia Quake
        if (quakeCooldown > 0) {
            quakeCooldown--;
        } else {
            performGaiaQuake(world);
            quakeCooldown = 140 + world.random.nextInt(30);
        }
    }

    private void performRootEntanglement(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.HOSTILE, 1.5f, 0.6f);
        Box box = this.getBoundingBox().expand(10.0, 3.0, 10.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != this && (e instanceof PlayerEntity || e instanceof IronGolemEntity) && e.isAlive());

        for (LivingEntity enemy : enemies) {
            enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 3), this);
            enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1), this);
            world.spawnParticles(ParticleTypes.COMPOSTER, enemy.getX(), enemy.getY() + 0.2, enemy.getZ(), 20, 0.4, 0.2, 0.4, 0.05);
        }
    }

    private void performGaiaQuake(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 1.5f, 0.7f);
        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1.2f, 0.5f);
        BossCinematicsHelper.triggerScreenShake(world, this.getEntityPos(), 22.0, 0.25);

        Box box = this.getBoundingBox().expand(8.0, 2.0, 8.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != this && (e instanceof PlayerEntity || e instanceof IronGolemEntity) && e.isAlive());

        for (LivingEntity enemy : enemies) {
            Vec3d knock = new Vec3d(enemy.getX() - this.getX(), enemy.getY() - this.getY(), enemy.getZ() - this.getZ()).normalize().multiply(1.4).add(0, 0.7, 0);
            enemy.setVelocity(knock);
            enemy.velocityDirty = true;
            enemy.damage(world, world.getDamageSources().mobAttack(this), 20.0f);
            world.spawnParticles(ParticleTypes.EXPLOSION, enemy.getX(), enemy.getY() + 0.5, enemy.getZ(), 1, 0, 0, 0, 0);
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

    private void dropLootAndFinish(ServerWorld world) {
        if (this.domainOrigin != null) {
            BossArenaHelper.breakDomain(world, this.domainOrigin, BossArenaHelper.DEFAULT_RADIUS, ParticleTypes.HAPPY_VILLAGER);
        }
        this.dropStack(world, new ItemStack(ModBossItems.SYLVA_CATALYST, 2));
        this.dropStack(world, new ItemStack(ModBossItems.BRAMBLE_WHIP, 1));
        this.dropStack(world, new ItemStack(ModBossItems.SYLVA_TROPHY, 1));
        this.dropStack(world, new ItemStack(com.chainores.registry.ModGems.VENOM_GEM, 2));
        this.dropStack(world, new ItemStack(com.chainores.registry.ModGems.EARTH_GEM, 2));
        if (OreRegistryData.TIERS.size() >= 40) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(39).ingotItem, 6));
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
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossBar.clearPlayers();
    }
}
