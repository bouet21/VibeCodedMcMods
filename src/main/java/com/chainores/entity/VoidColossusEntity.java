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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class VoidColossusEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int teleportCooldown = 100;
    private int bulletCooldown = 80;

    public VoidColossusEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.void_colossus").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                BossBar.Color.PURPLE,
                BossBar.Style.NOTCHED_12
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 2500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 900.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.ATTACK_DAMAGE, 42.0)
                .add(EntityAttributes.ARMOR, 22.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 20.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Deflect projectiles and teleport
        if (source.getSource() instanceof ProjectileEntity projectile) {
            world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK.value(), SoundCategory.HOSTILE, 1.5f, 1.5f);
            teleportRandomly(world);
            return false;
        }

        boolean damaged = super.damage(world, source, amount);
        if (damaged && world.random.nextInt(3) == 0) {
            teleportRandomly(world);
        }
        return damaged;
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 1), this);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.HOSTILE, 1.5f, 0.6f);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Auto-prune excess duplicate bosses from old unspaced world generation
        if (this.age % 40 == 0 && world.getRegistryKey().equals(World.END)) {
            if (!this.getCommandTags().contains("custom_boss") &&
                !com.chainores.world.StructureLocator.isNearRegisteredStructure(world, com.chainores.world.StructureLocator.TYPE_ALTAR, this.getBlockPos(), 80.0)) {
                this.bossBar.clearPlayers();
                this.discard();
                return;
            }
        }

        // Ambient void particles
        if (world.random.nextInt(3) == 0) {
            world.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.5, this.getZ(), 4, 0.5, 0.8, 0.5, 0.1);
            world.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.8, this.getZ(), 2, 0.4, 0.4, 0.4, 0.02);
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player && player.isDead()) {
            healToFull(world);
        }
        if (target != null && target.isAlive()) {
            // Periodic teleportation to reposition
            if (teleportCooldown > 0) {
                teleportCooldown--;
            } else {
                teleportBehindTarget(world, target);
                teleportCooldown = 140; // 7 seconds
            }

            // Homing singularity bullet
            if (bulletCooldown > 0) {
                bulletCooldown--;
            } else {
                world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.HOSTILE, 1.8f, 0.7f);
                ShulkerBulletEntity bullet = new ShulkerBulletEntity(world, this, target, Direction.Axis.Y);
                bullet.setPosition(this.getX(), this.getY() + 2.0, this.getZ());
                world.spawnEntity(bullet);
                bulletCooldown = 100; // 5 seconds
            }
        }
    }

    private void teleportBehindTarget(ServerWorld world, LivingEntity target) {
        double angle = Math.toRadians(target.getYaw());
        double behindX = target.getX() - Math.sin(angle) * 3.5;
        double behindZ = target.getZ() + Math.cos(angle) * 3.5;
        double targetY = target.getY();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.8f, 0.8f);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.6, 1.0, 0.6, 0.2);

        this.refreshPositionAndAngles(behindX, targetY, behindZ, target.getYaw(), 0.0f);

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.8f, 0.8f);
        world.spawnParticles(ParticleTypes.PORTAL, behindX, targetY + 1.0, behindZ, 30, 0.6, 1.0, 0.6, 0.2);
    }

    private void teleportRandomly(ServerWorld world) {
        double nx = this.getX() + (world.random.nextDouble() - 0.5) * 16.0;
        double ny = this.getY() + (world.random.nextInt(5) - 2);
        double nz = this.getZ() + (world.random.nextDouble() - 0.5) * 16.0;

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.5f, 0.9f);
        this.refreshPositionAndAngles(nx, ny, nz, this.getYaw(), this.getPitch());
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.5f, 0.9f);
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
        this.dropStack(world, new ItemStack(ModBossItems.VOID_SHARD, 1 + world.random.nextInt(2)));
        if (OreRegistryData.TIERS.size() >= 135) {
            this.dropStack(world, new ItemStack(OreRegistryData.TIERS.get(134).ingotItem, 4 + world.random.nextInt(5)));
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
