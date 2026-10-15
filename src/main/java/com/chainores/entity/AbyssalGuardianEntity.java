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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AbyssalGuardianEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int sonicCooldown = 100;
    private int chargeTimer = 0;

    public AbyssalGuardianEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.abyssal_guardian").formatted(Formatting.DARK_AQUA, Formatting.BOLD),
                BossBar.Color.BLUE,
                BossBar.Style.NOTCHED_10
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 400.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.ATTACK_DAMAGE, 24.0)
                .add(EntityAttributes.ARMOR, 15.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(EntityAttributes.FOLLOW_RANGE, 36.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200, 0), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1), this);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_WARDEN_ATTACK_IMPACT, SoundCategory.HOSTILE, 1.2f, 0.8f);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
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
        if (world.random.nextInt(4) == 0) {
            world.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.4, 0.4, 0.4, 0.02);
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player && player.isDead()) {
            healToFull(world);
        }
        if (target != null && target.isAlive()) {
            double distanceSq = this.squaredDistanceTo(target);

            if (sonicCooldown > 0) {
                sonicCooldown--;
            } else if (distanceSq > 16.0 && distanceSq < 400.0) { // between 4 and 20 blocks
                chargeTimer++;
                if (chargeTimer == 1) {
                    world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.HOSTILE, 1.5f, 1.0f);
                } else if (chargeTimer >= 25) {
                    // Fire Sonic Shockwave
                    fireSonicWave(world, target);
                    chargeTimer = 0;
                    sonicCooldown = 120; // 6 seconds cooldown
                }
            } else {
                chargeTimer = 0;
            }
        } else {
            chargeTimer = 0;
        }
    }

    private void fireSonicWave(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d direction = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 2.0f, 1.0f);

        // Particle beam
        double distance = origin.distanceTo(targetPos);
        for (int i = 1; i <= Math.min((int) distance, 20); i++) {
            Vec3d point = origin.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }

        // Deal damage and knockback
        DamageSource source = this.getDamageSources().magic();
        target.damage(world, source, 32.0f);
        target.addVelocity(direction.x * 1.5, 0.6, direction.z * 1.5);
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

    @Override
    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(world, damageSource, causedByPlayer);
        // Guaranteed transition drops
        this.dropStack(world, new ItemStack(ModBossItems.ABYSSAL_CATALYST, 1 + world.random.nextInt(2)));
        if (OreRegistryData.TIERS.size() >= 70) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(69).ingotItem, 4 + world.random.nextInt(5)));
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
