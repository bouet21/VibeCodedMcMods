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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class FrostwardenEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int slamCooldown = 90;
    private int novaCooldown = 130;
    private int scaledPlayerCount = 1;

    public FrostwardenEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.frostwarden").formatted(Formatting.AQUA, Formatting.BOLD),
                BossBar.Color.BLUE,
                BossBar.Style.NOTCHED_10
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 350.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.ATTACK_DAMAGE, 18.0)
                .add(EntityAttributes.ARMOR, 14.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.setFrozenTicks(160);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1), this);
            world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.2f, 1.5f);
            world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.scaledPlayerCount = BossScalingHelper.updateBossScaling(this, world, 350.0, 18.0, this.scaledPlayerCount);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Passive ambient cold
        if (this.age % 10 == 0) {
            world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0, this.getZ(), 4, 0.6, 0.8, 0.6, 0.02);
        }

        // Skill 1: Frost Slam
        if (slamCooldown > 0) {
            slamCooldown--;
        } else {
            LivingEntity target = this.getTarget();
            if (target != null && this.squaredDistanceTo(target) <= 10.0 * 10.0) {
                performFrostSlam(world);
                slamCooldown = 110 + world.random.nextInt(30);
            }
        }

        // Skill 2: Frost Nova
        if (novaCooldown > 0) {
            novaCooldown--;
        } else {
            performFrostNova(world);
            novaCooldown = 140 + world.random.nextInt(40);
        }
    }

    private void performFrostSlam(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 1.2f, 1.4f);
        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.HOSTILE, 1.5f, 0.8f);

        Box impactBox = this.getBoundingBox().expand(7.0, 3.0, 7.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, impactBox,
                e -> e != this && (e instanceof PlayerEntity || e instanceof IronGolemEntity) && e.isAlive());

        for (LivingEntity enemy : enemies) {
            Vec3d knockback = new Vec3d(enemy.getX() - this.getX(), enemy.getY() - this.getY(), enemy.getZ() - this.getZ()).normalize().multiply(1.2).add(0, 0.6, 0);
            enemy.setVelocity(knockback);
            enemy.velocityDirty = true;
            enemy.damage(world, world.getDamageSources().freeze(), 14.0f);
            enemy.setFrozenTicks(200);
            enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2), this);
            world.spawnParticles(ParticleTypes.SNOWFLAKE, enemy.getX(), enemy.getY() + 1.0, enemy.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
        }
    }

    private void performFrostNova(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.HOSTILE, 1.8f, 0.6f);
        double radius = 6.0;
        for (int i = 0; i < 36; i++) {
            double angle = (Math.PI * 2 / 36) * i;
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            world.spawnParticles(ParticleTypes.SNOWFLAKE, px, this.getY() + 0.5, pz, 3, 0.1, 0.3, 0.1, 0.05);
        }

        Box box = this.getBoundingBox().expand(6.0, 2.0, 6.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != this && (e instanceof PlayerEntity || e instanceof IronGolemEntity) && e.isAlive());
        for (LivingEntity enemy : enemies) {
            enemy.damage(world, world.getDamageSources().magic(), 12.0f);
            enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0), this);
            enemy.setFrozenTicks(140);
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
        this.dropStack(world, new ItemStack(ModBossItems.GLACIAL_CATALYST, 1 + world.random.nextInt(2)));
        if (OreRegistryData.TIERS.size() >= 20) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(19).ingotItem, 4 + world.random.nextInt(5)));
        }
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
