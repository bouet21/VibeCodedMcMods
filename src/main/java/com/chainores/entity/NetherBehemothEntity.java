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
    private int meteorCooldown = 130;
    private int chargeCooldown = 150;
    private boolean isEnraged = false;
    private int scaledPlayerCount = 1;

    public NetherBehemothEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.chainores.nether_behemoth").formatted(Formatting.DARK_RED, Formatting.BOLD),
                BossBar.Color.RED,
                BossBar.Style.NOTCHED_12
        );
        this.bossBar.setDarkenSky(true);
        this.experiencePoints = 1500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 950.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.ATTACK_DAMAGE, 42.0)
                .add(EntityAttributes.ARMOR, 24.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public boolean isFireImmune() {
        return true;
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
        boolean damaged = super.damage(world, source, amount);
        if (damaged && source.getAttacker() instanceof LivingEntity attacker && attacker != this) {
            // Molten Thorns: burns close attackers
            if (this.squaredDistanceTo(attacker) <= 20.0) {
                attacker.setOnFireFor(6.0f);
                attacker.damage(world, this.getDamageSources().onFire(), 8.0f);
                world.playSound(null, attacker.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.2f, 1.0f);
            }
        }
        return damaged;
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);
        if (success && target instanceof LivingEntity living) {
            living.setOnFireFor(12.0f);
            target.addVelocity(0.0, 0.6, 0.0);
            target.velocityDirty = true;
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.2f, 1.2f);

            // Lifesteal: heals 25% of attack damage
            float lifesteal = (float) (this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE) * 0.25);
            this.heal(lifesteal);
            world.spawnParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.4, 0.4, 0.4, 0.05);
        }
        return success;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        this.scaledPlayerCount = BossScalingHelper.updateBossScaling(this, world, 950.0, 42.0, this.scaledPlayerCount);
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Auto-prune excess duplicate bosses from old unspaced world generation
        if (this.age % 40 == 0 && world.getRegistryKey().equals(World.NETHER)) {
            if (!this.getCommandTags().contains("custom_boss") &&
                !com.chainores.world.StructureLocator.isNearRegisteredStructure(world, com.chainores.world.StructureLocator.TYPE_SHRINE, this.getBlockPos(), 80.0)) {
                this.bossBar.clearPlayers();
                this.discard();
                return;
            }
        }

        // Ambient lava & flame aura
        if (world.random.nextInt(3) == 0) {
            world.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 4, 0.6, 0.8, 0.6, 0.05);
            world.spawnParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.5, 0.2, 0.5, 0.0);
        }

        // Passive Magma Healing (extreme in lava/fire)
        if (this.age % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            float baseRegen = isEnraged ? 7.0f : 3.5f;
            if (this.isInLava() || this.isOnFire()) {
                baseRegen += 10.0f; // Rapid lava siphon
            }
            this.heal(baseRegen);
            if (this.age % 40 == 0) {
                world.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.3, 0.4, 0.3, 0.02);
            }
        }

        // Constant Status Buffs (Resistance & Strength, enhanced in Enrage)
        if (this.age % 20 == 0) {
            if (isEnraged) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false, true)); // Resistance II (-40% damage)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, false, false, true));   // Strength II
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, false, true));      // Speed I
            } else {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, false, false, true)); // Resistance I (-20% damage)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, false, false, true));   // Strength I
            }
        }

        // Enrage Trigger at 50% health
        if (!isEnraged && this.getHealth() < this.getMaxHealth() * 0.5f) {
            isEnraged = true;
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 2.5f, 0.7f);
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 2.0f, 0.8f);
            world.spawnParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.5, this.getZ(), 40, 0.8, 1.0, 0.8, 0.1);
            world.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 60, 1.0, 1.2, 1.0, 0.1);

            Text enrageMsg = Text.literal("🔥 ¡El Behemoth del Nether entra en erupción! Llamas incandescentes potencian su fuerza, resistencia y cadencia de fuego.")
                    .formatted(Formatting.DARK_RED, Formatting.BOLD);
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

            // 1. Fireball Barrage (3 normal / 5 enraged)
            int effectiveFireballCd = isEnraged ? 55 : 85;
            if (fireballCooldown > 0) {
                fireballCooldown--;
            } else if (distanceSq > 20.0 && distanceSq < 625.0) {
                fireFireballBarrage(world, target);
                fireballCooldown = effectiveFireballCd;
            }

            // 2. Magma Quake slam (close range AOE within 6 blocks)
            int effectiveQuakeCd = isEnraged ? 70 : 110;
            if (quakeCooldown > 0) {
                quakeCooldown--;
            } else if (distanceSq <= 36.0) {
                executeMagmaQuake(world);
                quakeCooldown = effectiveQuakeCd;
            }

            // 3. Pyroclastic Meteor Rain (Distance 6 - 28 blocks)
            int effectiveMeteorCd = isEnraged ? 80 : 130;
            if (meteorCooldown > 0) {
                meteorCooldown--;
            } else if (distanceSq > 36.0 && distanceSq < 784.0) {
                executeMeteorRain(world, target);
                meteorCooldown = effectiveMeteorCd;
            }

            // 4. Blazing Charge (Distance 8 - 22 blocks)
            if (chargeCooldown > 0) {
                chargeCooldown--;
            } else if (distanceSq > 64.0 && distanceSq < 484.0) {
                executeBlazingCharge(world, target);
                chargeCooldown = isEnraged ? 80 : 140;
            }
        }
    }

    private void fireFireballBarrage(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d diff = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.8f, 0.8f);

        int count = isEnraged ? 2 : 1;
        for (int i = -count; i <= count; i++) {
            Vec3d spread = diff.rotateY((float) Math.toRadians(i * 12));
            SmallFireballEntity fireball = new SmallFireballEntity(world, this, spread);
            fireball.setPosition(origin.x + spread.x * 1.2, origin.y, origin.z + spread.z * 1.2);
            world.spawnEntity(fireball);
        }
    }

    private void executeMeteorRain(ServerWorld world, LivingEntity target) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 2.0f, 0.7f);

        int meteors = isEnraged ? 4 : 2;
        for (int i = 0; i < meteors; i++) {
            double ox = target.getX() + (world.random.nextDouble() - 0.5) * 6.0;
            double oz = target.getZ() + (world.random.nextDouble() - 0.5) * 6.0;
            double oy = target.getY() + 7.0 + world.random.nextDouble() * 2.5;

            Vec3d fallVelocity = new Vec3d((world.random.nextDouble() - 0.5) * 0.1, -1.2, (world.random.nextDouble() - 0.5) * 0.1);
            SmallFireballEntity meteor = new SmallFireballEntity(world, this, fallVelocity);
            meteor.setPosition(ox, oy, oz);
            world.spawnEntity(meteor);

            world.spawnParticles(ParticleTypes.LAVA, ox, oy, oz, 4, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticles(ParticleTypes.FLAME, ox, oy, oz, 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void executeBlazingCharge(ServerWorld world, LivingEntity target) {
        Vec3d origin = this.getEyePos();
        Vec3d targetPos = target.getEyePos();
        Vec3d dir = targetPos.subtract(origin).normalize();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 1.8f, 0.9f);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, true));

        this.setVelocity(dir.x * 1.4, 0.25, dir.z * 1.4);
        this.velocityDirty = true;

        for (int i = 0; i < 20; i++) {
            world.spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5) * 1.8,
                    this.getY() + world.random.nextDouble() * 2.0,
                    this.getZ() + (world.random.nextDouble() - 0.5) * 1.8,
                    1, 0, 0, 0, 0.05);
        }
    }

    private void executeMagmaQuake(ServerWorld world) {
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 2.0f, 0.6f);
        world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.5f, 0.8f);

        // Ring of flame particles
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            double px = this.getX() + Math.cos(rad) * 4.5;
            double pz = this.getZ() + Math.sin(rad) * 4.5;
            world.spawnParticles(ParticleTypes.LAVA, px, this.getY() + 0.2, pz, 2, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticles(ParticleTypes.FLAME, px, this.getY() + 0.5, pz, 3, 0.2, 0.4, 0.2, 0.05);
        }

        // Damage surrounding enemies
        Box aoe = this.getBoundingBox().expand(5.5, 2.5, 5.5);
        List<LivingEntity> enemies = world.getEntitiesByClass(LivingEntity.class, aoe, e -> e != this && e.isAlive());
        for (LivingEntity enemy : enemies) {
            if (!(enemy instanceof NetherBehemothEntity)) {
                enemy.damage(world, this.getDamageSources().onFire(), isEnraged ? 28.0f : 20.0f);
                enemy.setOnFireFor(10.0f);
                enemy.addVelocity(0.0, 0.65, 0.0);
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
