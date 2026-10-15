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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
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

public class NetherBehemothEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int fireballCooldown = 80;
    private int quakeCooldown = 120;

    public NetherBehemothEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.nether_behemoth").formatted(Formatting.DARK_RED, Formatting.BOLD),
                BossBar.Color.RED,
                BossBar.Style.NOTCHED_12
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 1000;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 600.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.ATTACK_DAMAGE, 32.0)
                .add(EntityAttributes.ARMOR, 18.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.setOnFireFor(10.0f);
            target.addVelocity(0.0, 0.5, 0.0);
            target.velocityDirty = true;
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.0f, 1.2f);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Ambient lava & flame aura
        if (world.random.nextInt(3) == 0) {
            world.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 3, 0.6, 0.8, 0.6, 0.05);
            world.spawnParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.5, 0.2, 0.5, 0.0);
        }

        // Lava regeneration
        if (this.isInLava() && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double distanceSq = this.squaredDistanceTo(target);

            // 1. Triple Fireball Barrage
            if (fireballCooldown > 0) {
                fireballCooldown--;
            } else if (distanceSq > 16.0 && distanceSq < 625.0) { // between 4 and 25 blocks
                fireFireballBarrage(world, target);
                fireballCooldown = 90; // 4.5 seconds
            }

            // 2. Magma Quake slam (close range AOE)
            if (quakeCooldown > 0) {
                quakeCooldown--;
            } else if (distanceSq <= 25.0) { // within 5 blocks
                executeMagmaQuake(world);
                quakeCooldown = 110;
            }
        }
    }

    private void fireFireballBarrage(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d diff = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.8f, 0.8f);

        for (int i = -1; i <= 1; i++) {
            Vec3d spread = diff.rotateY((float) Math.toRadians(i * 12));
            SmallFireballEntity fireball = new SmallFireballEntity(world, this, spread);
            fireball.setPosition(origin.x + spread.x * 1.2, origin.y, origin.z + spread.z * 1.2);
            world.spawnEntity(fireball);
        }
    }

    private void executeMagmaQuake(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 2.0f, 0.6f);
        world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.5f, 0.8f);

        // Ring of flame particles
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            double px = this.getX() + Math.cos(rad) * 4.0;
            double pz = this.getZ() + Math.sin(rad) * 4.0;
            world.spawnParticles(ParticleTypes.LAVA, px, this.getY() + 0.2, pz, 2, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticles(ParticleTypes.FLAME, px, this.getY() + 0.5, pz, 3, 0.2, 0.4, 0.2, 0.05);
        }

        // Damage surrounding enemies
        Box aoe = this.getBoundingBox().expand(5.0, 2.0, 5.0);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, aoe, e -> e != this && e.isAlive());
        for (LivingEntity enemy : enemies) {
            if (!(enemy instanceof NetherBehemothEntity)) {
                enemy.damage(world, this.getDamageSources().onFire(), 18.0f);
                enemy.setOnFireFor(8.0f);
                enemy.addVelocity(0.0, 0.6, 0.0);
                enemy.velocityDirty = true;
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
        this.dropStack(world, new ItemStack(ModBossItems.NETHER_CORE, 1 + world.random.nextInt(2)));
        if (OreRegistryData.TIERS.size() >= 105) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(104).ingotItem, 4 + world.random.nextInt(5)));
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
