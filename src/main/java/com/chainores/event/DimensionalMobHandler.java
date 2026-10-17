package com.chainores.event;

import com.chainores.ore.OreRegistryData;
import com.chainores.registry.ModBossItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DimensionalMobHandler {

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getEntityWorld() instanceof ServerWorld world)) return;
            if (!(entity instanceof net.minecraft.entity.mob.MobEntity hostile)) return;
            if (entity instanceof EnderDragonEntity || entity instanceof WitherEntity) return;

            // Only trigger if damage was caused by a player
            if (!(damageSource.getAttacker() instanceof PlayerEntity player)) return;

            Random random = world.getRandom();
            double x = hostile.getX();
            double y = hostile.getY() + 0.5;
            double z = hostile.getZ();

            // -------------------------------------------------------------
            // 1. NETHER INFUSED DROPS (Tiers 71 - 105)
            // -------------------------------------------------------------
            if (world.getRegistryKey().equals(World.NETHER)) {
                int dropChance = 35; // 35% base chance

                if (random.nextInt(100) < dropChance) {
                    int minTier = 71;
                    int maxTier = 105;

                    if (hostile instanceof BlazeEntity || hostile instanceof MagmaCubeEntity) {
                        minTier = 71;
                        maxTier = 85;
                    } else if (hostile instanceof WitherSkeletonEntity || hostile instanceof PiglinBruteEntity) {
                        minTier = 86;
                        maxTier = 105;

                        // 5% chance of Nether Core
                        if (random.nextInt(100) < 5) {
                            spawnDrop(world, x, y, z, new ItemStack(ModBossItems.NETHER_CORE, 1));
                            world.playSound(null, hostile.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.2f);
                        }
                    } else if (hostile instanceof GhastEntity) {
                        minTier = 90;
                        maxTier = 105;
                    }

                    int chosenTier = minTier + random.nextInt(Math.max(1, maxTier - minTier + 1));
                    int idx = Math.clamp(chosenTier - 1, 0, OreRegistryData.TIERS.size() - 1);
                    ItemStack dropStack = new ItemStack(OreRegistryData.TIERS.get(idx).rawItem, 1 + random.nextInt(2));

                    spawnDrop(world, x, y, z, dropStack);
                    world.spawnParticles(ParticleTypes.FLAME, x, y, z, 10, 0.3, 0.3, 0.3, 0.05);
                }
            }

            // -------------------------------------------------------------
            // 2. END INFUSED DROPS (Tiers 106 - 150)
            // -------------------------------------------------------------
            else if (world.getRegistryKey().equals(World.END)) {
                int dropChance = 40; // 40% base chance

                if (random.nextInt(100) < dropChance) {
                    int minTier = 106;
                    int maxTier = 150;

                    if (hostile instanceof EndermanEntity) {
                        minTier = 106;
                        maxTier = 130;
                    } else if (hostile instanceof ShulkerEntity) {
                        minTier = 131;
                        maxTier = 149;

                        // 6% chance of Void Shard
                        if (random.nextInt(100) < 6) {
                            spawnDrop(world, x, y, z, new ItemStack(ModBossItems.VOID_SHARD, 1));
                            world.playSound(null, hostile.getBlockPos(), SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 1.0f, 1.2f);
                        }
                    }

                    int chosenTier = minTier + random.nextInt(Math.max(1, maxTier - minTier + 1));
                    int idx = Math.clamp(chosenTier - 1, 0, OreRegistryData.TIERS.size() - 1);
                    ItemStack dropStack = new ItemStack(OreRegistryData.TIERS.get(idx).rawItem, 1 + random.nextInt(2));

                    spawnDrop(world, x, y, z, dropStack);
                    world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 15, 0.3, 0.3, 0.3, 0.1);
                }
            }

            // -------------------------------------------------------------
            // 3. OVERWORLD DEEP DARK / SCULK DEPTHS (Tiers 50 - 70)
            // -------------------------------------------------------------
            else if (world.getRegistryKey().equals(World.OVERWORLD) && hostile.getY() < -30) {
                if (random.nextInt(100) < 25) {
                    int chosenTier = 50 + random.nextInt(21); // T50 - T70
                    int idx = Math.clamp(chosenTier - 1, 0, OreRegistryData.TIERS.size() - 1);
                    ItemStack dropStack = new ItemStack(OreRegistryData.TIERS.get(idx).rawItem, 1);

                    spawnDrop(world, x, y, z, dropStack);
                    world.spawnParticles(ParticleTypes.SCULK_SOUL, x, y, z, 8, 0.2, 0.2, 0.2, 0.05);
                }
            }
        });

        // -------------------------------------------------------------
        // 4. SPECIAL WEAPONS PROJECTILE IMPACT EFFECTS
        // -------------------------------------------------------------
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamageTaken, damageTaken, blocked) -> {
            if (!(entity.getEntityWorld() instanceof ServerWorld world)) return;
            if (damageSource.getSource() instanceof net.minecraft.entity.projectile.ProjectileEntity proj) {
                if (proj.getCommandTags().contains("dragon_arrow")) {
                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.9f, 1.5f);
                    world.spawnParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + 0.8, entity.getZ(), 20, 0.5, 0.5, 0.5, 0.08);
                    world.spawnParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 0.5, entity.getZ(), 15, 0.4, 0.4, 0.4, 0.05);

                    // Residual Dragon AoE damage
                    net.minecraft.util.math.Box aoe = entity.getBoundingBox().expand(3.5);
                    world.getEntitiesByClass(HostileEntity.class, aoe, mob -> mob != entity && mob.isAlive()).forEach(mob -> {
                        mob.damage(world, world.getDamageSources().magic(), 6.0f);
                    });
                } else if (proj.getCommandTags().contains("galactic_arrow")) {
                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.9f, 1.4f);
                    world.spawnParticles(ParticleTypes.SONIC_BOOM, entity.getX(), entity.getY() + 0.8, entity.getZ(), 1, 0, 0, 0, 0);
                    world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 0.8, entity.getZ(), 15, 0.4, 0.4, 0.4, 0.15);

                    // Sonic concussive shockwave
                    net.minecraft.util.math.Box aoe = entity.getBoundingBox().expand(4.0);
                    world.getEntitiesByClass(HostileEntity.class, aoe, mob -> mob != entity && mob.isAlive()).forEach(mob -> {
                        mob.damage(world, world.getDamageSources().magic(), 8.0f);
                        double dx = mob.getX() - entity.getX();
                        double dz = mob.getZ() - entity.getZ();
                        double len = Math.sqrt(dx * dx + dz * dz);
                        if (len > 0.001) {
                            mob.addVelocity((dx / len) * 1.2, 0.35, (dz / len) * 1.2);
                            mob.velocityDirty = true;
                        }
                    });
                }
            }
        });

        // -------------------------------------------------------------
        // 5. KINETIC WALL CRASH IMMUNITY (Cosmic Elytra Chestplate)
        // -------------------------------------------------------------
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, damageSource, amount) -> {
            if (damageSource.isOf(net.minecraft.entity.damage.DamageTypes.FLY_INTO_WALL)) {
                if (entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() instanceof com.chainores.gear.CosmicElytraChestplateItem) {
                    if (entity.getEntityWorld() instanceof ServerWorld sw) {
                        sw.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.ENTITY_BREEZE_WIND_BURST, SoundCategory.PLAYERS, 1.0f, 1.5f);
                        sw.spawnParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                    }
                    return false; // Complete immunity to kinetic crash
                }
            }
            return true;
        });

        // -------------------------------------------------------------
        // 6. FORESTRY TIMBERING (Lumberjack Hauberk)
        // -------------------------------------------------------------
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient() && state.isIn(net.minecraft.registry.tag.BlockTags.LOGS) && player instanceof ServerPlayerEntity serverPlayer) {
                if (player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() instanceof com.chainores.gear.LumberjackHauberkItem) {
                    com.chainores.gear.LumberjackHauberkItem.fellTree((ServerWorld) world, serverPlayer, pos);
                }
            }
            return true;
        });
    }

    private static void spawnDrop(ServerWorld world, double x, double y, double z, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemEntity itemEntity = new ItemEntity(world, x, y, z, stack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }
}
