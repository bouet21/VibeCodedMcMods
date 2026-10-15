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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CosmicSovereignEntity extends HostileEntity {
    private final ServerBossBar bossBar;
    private int thunderCooldown = 70;
    private int meteorCooldown = 110;
    private boolean isEnraged = false;

    public CosmicSovereignEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.cosmic_sovereign").formatted(Formatting.GOLD, Formatting.BOLD),
                BossBar.Color.YELLOW,
                BossBar.Style.NOTCHED_20
        );
        this.bossBar.setDarkenSky(true);
        this.bossBar.setThickenFog(true);
        this.experiencePoints = 10000;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 1500.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.ATTACK_DAMAGE, 55.0)
                .add(EntityAttributes.ARMOR, 25.0)
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
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.4, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Immunity to projectiles (cosmic kinetic shield)
        if (source.getSource() instanceof ProjectileEntity) {
            world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK.value(), SoundCategory.HOSTILE, 2.0f, 1.8f);
            world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.5, this.getZ(), 1, 0, 0, 0, 0);
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.5f, 1.4f);
            target.addVelocity(0.0, 0.8, 0.0);
            target.velocityDirty = true;

            // Summon lightning on hit
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
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Ambient celestial sparkles
        world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.5, this.getZ(), 2, 0.6, 0.8, 0.6, 0.03);
        if (world.random.nextInt(5) == 0) {
            world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.4, 0.5, 0.4, 0.05);
        }

        // Float / Hovering mechanism
        if (this.getVelocity().y < -0.15) {
            this.setVelocity(this.getVelocity().x, -0.15, this.getVelocity().z);
        }

        // Enrage Trigger at 40% health
        if (!isEnraged && this.getHealth() < this.getMaxHealth() * 0.4f) {
            isEnraged = true;
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 999999, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 999999, 0));

            world.playSound(null, this.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.HOSTILE, 2.5f, 0.8f);
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.0f, 0.7f);

            Text enrageMsg = Text.translatable("message.chainores.cosmic_enrage").formatted(Formatting.GOLD, Formatting.BOLD);
            for (ServerPlayerEntity serverPlayer : world.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(enrageMsg, false);
            }
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player && player.isDead()) {
            healToFull(world);
        }
        if (target != null && target.isAlive()) {
            // 1. Divine Lightning Strike
            if (thunderCooldown > 0) {
                thunderCooldown--;
            } else {
                strikeDivineThunder(world, target);
                thunderCooldown = isEnraged ? 45 : 70;
            }

            // 2. Cosmic Meteor Blast
            if (meteorCooldown > 0) {
                meteorCooldown--;
            } else {
                castCosmicBurst(world, target);
                meteorCooldown = isEnraged ? 70 : 110;
            }
        }
    }

    private void strikeDivineThunder(ServerWorld world, LivingEntity target) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 2.0f, 1.2f);
        for (int i = 0; i < 2; i++) {
            double ox = target.getX() + (world.random.nextDouble() - 0.5) * 3.0;
            double oz = target.getZ() + (world.random.nextDouble() - 0.5) * 3.0;

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

        target.damage(world, this.getDamageSources().magic(), 45.0f);
        target.addVelocity(dir.x * 1.8, 0.7, dir.z * 1.8);
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
