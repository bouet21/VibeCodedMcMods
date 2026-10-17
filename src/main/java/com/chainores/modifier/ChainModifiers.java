package com.chainores.modifier;

import com.chainores.gear.ChainArmorItem;
import com.chainores.gear.ChainAxeItem;
import com.chainores.gear.ChainHoeItem;
import com.chainores.gear.ChainPickaxeItem;
import com.chainores.gear.ChainShovelItem;
import com.chainores.gear.ChainSwordItem;
import com.chainores.gear.DragonBreathBowItem;
import com.chainores.gear.GalacticCrossbowItem;
import com.chainores.gear.GodOmnitoolItem;
import com.chainores.gear.ChainShieldItem;
import com.chainores.item.ChainTrinketItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChainModifiers {
    public static final String KEY_GEM = "chainores_gem";
    public static final String KEY_GEM_COUNT = "chainores_gem_count";
    public static final String KEY_REFORGE = "chainores_reforge";

    // Base Gem IDs
    public static final String GEM_BLOOD = "blood_gem";
    public static final String GEM_VOID = "void_gem";
    public static final String GEM_CORE = "core_gem";
    public static final String GEM_GENESIS = "genesis_gem";
    public static final String GEM_EARTH = "earth_gem";
    public static final String GEM_ECHO = "echo_gem";
    public static final String GEM_MAGNET = "magnet_gem";
    public static final String GEM_CHRONO = "chrono_gem";

    // Reforge Prefix IDs
    public static final String REFORGE_DIVINE = "divine";
    public static final String REFORGE_LEGENDARY = "legendary";
    public static final String REFORGE_FIERCE = "fierce";
    public static final String REFORGE_SWIFT = "swift";
    public static final String REFORGE_TITANIC = "titanic";

    public static int getGemTier(String gemId) {
        if (gemId == null) return 1;
        if (gemId.endsWith("_3")) return 3;
        if (gemId.endsWith("_2")) return 2;
        return 1;
    }

    public static String getBaseGemType(String gemId) {
        if (gemId == null) return "";
        if (gemId.startsWith(GEM_BLOOD)) return GEM_BLOOD;
        if (gemId.startsWith(GEM_VOID)) return GEM_VOID;
        if (gemId.startsWith(GEM_CORE)) return GEM_CORE;
        if (gemId.startsWith(GEM_GENESIS)) return GEM_GENESIS;
        if (gemId.startsWith(GEM_EARTH)) return GEM_EARTH;
        if (gemId.startsWith(GEM_ECHO)) return GEM_ECHO;
        if (gemId.startsWith(GEM_MAGNET)) return GEM_MAGNET;
        if (gemId.startsWith(GEM_CHRONO)) return GEM_CHRONO;
        return gemId;
    }

    public static boolean isSocketableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof ChainArmorItem ||
               item instanceof com.chainores.gear.MiningExoskeletonItem ||
               item instanceof com.chainores.gear.LumberjackHauberkItem ||
               item instanceof com.chainores.gear.CosmicElytraChestplateItem ||
               item instanceof ChainPickaxeItem ||
               item instanceof ChainSwordItem ||
               item instanceof ChainAxeItem ||
               item instanceof ChainShovelItem ||
               item instanceof ChainHoeItem ||
               item instanceof ChainShieldItem ||
               item instanceof DragonBreathBowItem ||
               item instanceof GalacticCrossbowItem ||
               item instanceof GodOmnitoolItem ||
               item instanceof ChainTrinketItem;
    }

    public static int getMaxSockets(ItemStack stack) {
        if (stack.isEmpty() || !isSocketableItem(stack)) return 0;
        Item item = stack.getItem();

        if (item instanceof ChainTrinketItem trinket) {
            return trinket.getMaxSockets();
        }

        if (item instanceof com.chainores.gear.MiningExoskeletonItem) {
            return 3;
        }

        if (item instanceof com.chainores.gear.LumberjackHauberkItem) {
            return 3;
        }

        if (item instanceof com.chainores.gear.CosmicElytraChestplateItem) {
            return 4;
        }

        if (item instanceof ChainArmorItem armor) {
            int t = armor.getTier();
            if (t <= 1) return 0;       // T1: Demasiado débil para gemas
            if (t <= 30) return 1;      // T10 - T30: 1 slot
            if (t <= 70) return 2;      // T40 - T70: 2 slots
            if (t <= 115) return 3;     // T80 - T115: 3 slots
            return 4;                   // T125, T135, T140, T150 (Las mejores): 4 slots
        }

        if (item instanceof ChainPickaxeItem pickaxe) {
            int t = pickaxe.getTier();
            if (t <= 1) return 0;
            if (t <= 30) return 1;
            if (t <= 115) return 2;
            return 3;
        }

        if (item instanceof ChainAxeItem axe) {
            int t = axe.getTier();
            if (t <= 1) return 0;
            if (t <= 30) return 1;
            if (t <= 115) return 2;
            return 3;
        }

        if (item instanceof ChainShovelItem shovel) {
            int t = shovel.getTier();
            if (t <= 1) return 0;
            if (t <= 30) return 1;
            if (t <= 115) return 2;
            return 3;
        }

        if (item instanceof ChainHoeItem hoe) {
            int t = hoe.getTier();
            if (t <= 1) return 0;
            if (t <= 30) return 1;
            if (t <= 115) return 2;
            return 3;
        }

        if (item instanceof ChainShieldItem) {
            return 2;
        }

        if (item instanceof ChainSwordItem sword) {
            int t = sword.getTier();
            if (t <= 1) return 0;       // T1: Demasiado débil
            if (t <= 30) return 1;      // T10 - T30: 1 slot
            if (t <= 115) return 2;     // T40 - T115: 2 slots
            return 3;                   // T125 - T150: 3 slots
        }

        if (item instanceof DragonBreathBowItem || item instanceof GalacticCrossbowItem) {
            return 3;
        }

        if (item instanceof GodOmnitoolItem) {
            return 4;
        }

        return 0;
    }

    public static List<String> getGems(ItemStack stack) {
        List<String> list = new ArrayList<>();
        if (stack.isEmpty()) return list;

        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return list;
        NbtCompound nbt = custom.copyNbt();

        int count = nbt.getInt(KEY_GEM_COUNT).orElse(0);
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                nbt.getString("chainores_gem_" + i).ifPresent(list::add);
            }
        } else if (nbt.contains(KEY_GEM)) {
            nbt.getString(KEY_GEM).ifPresent(list::add);
        }
        return list;
    }

    public static boolean canAddGem(ItemStack stack) {
        int max = getMaxSockets(stack);
        if (max <= 0) return false;
        return getGems(stack).size() < max;
    }

    public static boolean addGem(ItemStack stack, String gemId) {
        if (stack.isEmpty() || gemId == null) return false;
        int max = getMaxSockets(stack);
        List<String> list = getGems(stack);
        if (list.size() >= max) return false;

        list.add(gemId);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt(KEY_GEM_COUNT, list.size());
            for (int i = 0; i < list.size(); i++) {
                nbt.putString("chainores_gem_" + i, list.get(i));
            }
            nbt.putString(KEY_GEM, list.get(0));
        });
        return true;
    }

    public static void setGem(ItemStack stack, String gemId) {
        if (stack.isEmpty()) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putInt(KEY_GEM_COUNT, 1);
            nbt.putString("chainores_gem_0", gemId);
            nbt.putString(KEY_GEM, gemId);
        });
    }

    public static String getGem(ItemStack stack) {
        List<String> gems = getGems(stack);
        return gems.isEmpty() ? null : gems.get(0);
    }

    public static String getReforge(ItemStack stack) {
        if (stack.isEmpty()) return null;
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return null;
        NbtCompound nbt = custom.copyNbt();
        return nbt.contains(KEY_REFORGE) ? nbt.getString(KEY_REFORGE).orElse(null) : null;
    }

    public static void setReforge(ItemStack stack, String reforgeId) {
        if (stack.isEmpty()) return;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putString(KEY_REFORGE, reforgeId);
        });
    }

    public static String rollRandomReforge(Random random) {
        int roll = random.nextInt(100);
        if (roll < 8) return REFORGE_DIVINE;      // 8% Divine
        if (roll < 22) return REFORGE_LEGENDARY;  // 14% Legendary
        if (roll < 47) return REFORGE_FIERCE;     // 25% Fierce
        if (roll < 73) return REFORGE_SWIFT;      // 26% Swift
        return REFORGE_TITANIC;                   // 27% Titanic
    }

    public static float getDamageMultiplier(ItemStack stack) {
        float mult = 1.0f;
        String reforge = getReforge(stack);
        com.chainores.config.ChainOresConfig cfg = com.chainores.config.ChainOresConfig.get();
        if (reforge != null) {
            mult = switch (reforge) {
                case REFORGE_DIVINE -> cfg.reforgeDivineDamageBoost;
                case REFORGE_FIERCE -> cfg.reforgeFierceDamageBoost;
                case REFORGE_LEGENDARY -> cfg.reforgeLegendaryDamageBoost;
                case REFORGE_SWIFT -> 0.90f;
                case REFORGE_TITANIC -> 1.10f;
                default -> 1.0f;
            };
        }

        // Also scale with Void and Genesis gems
        for (String gem : getGems(stack)) {
            String base = getBaseGemType(gem);
            int tier = getGemTier(gem);
            if (GEM_VOID.equals(base)) mult += 0.15f * tier;
            if (GEM_GENESIS.equals(base)) mult += 0.25f * tier;
        }

        return Math.min(mult, cfg.maxCumulativeDamageMultiplier);
    }

    public static float getMiningSpeedMultiplier(ItemStack stack) {
        com.chainores.config.ChainOresConfig cfg = com.chainores.config.ChainOresConfig.get();
        float mult = 1.0f;
        List<String> gems = getGems(stack);
        for (String gem : gems) {
            String base = getBaseGemType(gem);
            int tier = getGemTier(gem);
            if (GEM_CORE.equals(base)) mult += 0.35f * tier;
            if (GEM_GENESIS.equals(base)) mult += cfg.genesisGemMiningBoost * tier;
            if (GEM_EARTH.equals(base)) mult += 0.40f * tier;
            if (GEM_ECHO.equals(base)) mult += 0.30f * tier;
            if (GEM_MAGNET.equals(base)) mult += 0.35f * tier;
            if (GEM_CHRONO.equals(base)) mult += 0.25f * tier;
        }

        String reforge = getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case REFORGE_DIVINE -> mult += 0.50f;
                case REFORGE_SWIFT -> mult += 0.40f;
                case REFORGE_LEGENDARY -> mult += 0.30f;
                case REFORGE_TITANIC -> mult -= 0.10f;
            }
        }
        return Math.min(mult, cfg.maxMiningSpeedMultiplier);
    }

    public static float getEarthGemDoubleDropChance(ItemStack stack) {
        float chance = 0.0f;
        for (String gem : getGems(stack)) {
            if (GEM_EARTH.equals(getBaseGemType(gem))) {
                chance += 0.25f * getGemTier(gem);
            }
        }
        return Math.min(1.0f, chance);
    }

    public static boolean isReforgableGear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof ChainSwordItem ||
               item instanceof ChainPickaxeItem ||
               item instanceof ChainAxeItem ||
               item instanceof ChainShovelItem ||
               item instanceof ChainHoeItem ||
               item instanceof ChainArmorItem ||
               item instanceof GodOmnitoolItem ||
               item instanceof DragonBreathBowItem ||
               item instanceof GalacticCrossbowItem ||
               item instanceof ChainShieldItem ||
               item instanceof ChainTrinketItem ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.SWORDS) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.AXES) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.HOES) ||
               stack.isIn(net.minecraft.registry.tag.ItemTags.ARMOR_ENCHANTABLE) ||
               stack.isOf(net.minecraft.item.Items.SHIELD) ||
               stack.isOf(net.minecraft.item.Items.BOW) ||
               stack.isOf(net.minecraft.item.Items.CROSSBOW);
    }

    public static boolean hasGem(ItemStack stack, String baseGem) {
        if (stack == null || stack.isEmpty() || baseGem == null) return false;
        for (String g : getGems(stack)) {
            if (baseGem.equals(getBaseGemType(g))) return true;
        }
        return false;
    }

    public static int getGemHighestTier(ItemStack stack, String baseGem) {
        if (stack == null || stack.isEmpty() || baseGem == null) return 0;
        int max = 0;
        for (String g : getGems(stack)) {
            if (baseGem.equals(getBaseGemType(g))) {
                max = Math.max(max, getGemTier(g));
            }
        }
        return max;
    }

    public static ItemStack applyMagnetAutoSmelt(ItemStack original) {
        if (original == null || original.isEmpty()) return original;
        Item item = original.getItem();
        int count = original.getCount();

        if (item == net.minecraft.item.Items.RAW_IRON) return new ItemStack(net.minecraft.item.Items.IRON_INGOT, count);
        if (item == net.minecraft.item.Items.RAW_GOLD) return new ItemStack(net.minecraft.item.Items.GOLD_INGOT, count);
        if (item == net.minecraft.item.Items.RAW_COPPER) return new ItemStack(net.minecraft.item.Items.COPPER_INGOT, count);
        if (item == net.minecraft.item.Items.ANCIENT_DEBRIS) return new ItemStack(net.minecraft.item.Items.NETHERITE_SCRAP, count);
        if (item == net.minecraft.item.Items.COBBLESTONE) return new ItemStack(net.minecraft.item.Items.STONE, count);
        if (item == net.minecraft.item.Items.COBBLED_DEEPSLATE) return new ItemStack(net.minecraft.item.Items.DEEPSLATE, count);
        if (item == net.minecraft.item.Items.SAND || item == net.minecraft.item.Items.RED_SAND) return new ItemStack(net.minecraft.item.Items.GLASS, count);
        if (item == net.minecraft.item.Items.CLAY_BALL) return new ItemStack(net.minecraft.item.Items.BRICK, count);
        if (item == net.minecraft.item.Items.WET_SPONGE) return new ItemStack(net.minecraft.item.Items.SPONGE, count);

        // Chain Ores progressive raw smelting
        for (com.chainores.ore.OreTier ore : com.chainores.ore.OreRegistryData.TIERS) {
            if (item == ore.rawItem) {
                return new ItemStack(ore.ingotItem, count);
            }
        }

        return original;
    }

    public static void applyReforgeHeldBuffs(net.minecraft.entity.Entity entity, ItemStack stack) {
        if (entity.getEntityWorld().isClient() || !(entity instanceof net.minecraft.entity.player.PlayerEntity player)) return;
        String reforge = getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case REFORGE_DIVINE ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 40, 0, true, false));
                case REFORGE_SWIFT ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SPEED, 40, 0, true, false));
                case REFORGE_FIERCE ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.STRENGTH, 40, 0, true, false));
                case REFORGE_TITANIC ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 40, 0, true, false));
                case REFORGE_LEGENDARY ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.LUCK, 40, 0, true, false));
            }
        }

        for (String gem : getGems(stack)) {
            String base = getBaseGemType(gem);
            int tier = getGemTier(gem);
            if (GEM_EARTH.equals(base)) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.HASTE, 40, tier - 1, true, false));
            } else if (GEM_CHRONO.equals(base)) {
                int interval = switch (tier) {
                    case 3 -> 15;
                    case 2 -> 30;
                    default -> 60;
                };
                if (player.age % interval == 0 && stack.isDamaged()) {
                    stack.setDamage(Math.max(0, stack.getDamage() - 1));
                }
            }
        }
    }

    public static void applyArmorGemAndReforgeBuffs(net.minecraft.server.network.ServerPlayerEntity player, ItemStack stack) {
        List<String> gems = getGems(stack);
        int bloodCount = 0;
        int voidCount = 0;
        int coreCount = 0;
        int genesisCount = 0;
        int earthCount = 0;
        int echoCount = 0;
        int magnetCount = 0;
        int chronoCount = 0;

        for (String gem : gems) {
            String base = getBaseGemType(gem);
            int tier = getGemTier(gem);
            switch (base) {
                case GEM_BLOOD -> bloodCount += tier;
                case GEM_VOID -> voidCount += tier;
                case GEM_CORE -> coreCount += tier;
                case GEM_GENESIS -> genesisCount += tier;
                case GEM_EARTH -> earthCount += tier;
                case GEM_ECHO -> echoCount += tier;
                case GEM_MAGNET -> magnetCount += tier;
                case GEM_CHRONO -> chronoCount += tier;
            }
        }

        if (bloodCount > 0) {
            int amp = Math.min(bloodCount - 1, 2);
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.REGENERATION, 40, amp, true, false));
        }
        if (voidCount > 0) {
            if (player.fallDistance > 1.5f && !player.isOnGround()) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOW_FALLING, 40, 0, true, false));
                player.fallDistance = 0;
            }
        }
        if (coreCount > 0) {
            int amp = Math.min(coreCount - 1, 2);
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SPEED, 40, amp, true, false));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE, 40, 0, true, false));
        }
        if (genesisCount > 0) {
            if (!player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.ABSORPTION)) {
                int amp = Math.min(genesisCount, 3);
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.ABSORPTION, 100, amp, true, false));
            }
        }
        if (earthCount > 0) {
            int luckAmp = Math.min(earthCount - 1, 2);
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.LUCK, 40, luckAmp, true, false));
            if (player.getY() < 60) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, 240, 0, true, false));
            }
            if (earthCount >= 2) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.HERO_OF_THE_VILLAGE, 40, 0, true, false));
            }
        }
        if (echoCount > 0) {
            if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS)) {
                player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.DARKNESS);
            }
            if (player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)) {
                player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS);
            }
            if (player.getY() < 60) {
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, 240, 0, true, false));
            }
            if (player.age % 80 == 0) {
                ServerWorld sw = (ServerWorld) player.getEntityWorld();
                double r = 8.0 + 6.0 * echoCount;
                net.minecraft.util.math.Box box = player.getBoundingBox().expand(r);
                for (net.minecraft.entity.mob.HostileEntity mob : sw.getEntitiesByClass(net.minecraft.entity.mob.HostileEntity.class, box, e -> e.isAlive())) {
                    mob.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.GLOWING, 60, 0, true, false));
                }
            }
        }
        if (magnetCount > 0) {
            ServerWorld sw = (ServerWorld) player.getEntityWorld();
            double r = 6.0 + 5.0 * magnetCount;
            net.minecraft.util.math.Box box = player.getBoundingBox().expand(r);
            for (net.minecraft.entity.ItemEntity item : sw.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, box, e -> !e.cannotPickup() && !e.isRemoved())) {
                net.minecraft.util.math.Vec3d pull = player.getEntityPos().add(0, 0.5, 0).subtract(item.getEntityPos()).normalize().multiply(0.38);
                item.setVelocity(pull);
                item.velocityDirty = true;
            }
            for (net.minecraft.entity.ExperienceOrbEntity orb : sw.getEntitiesByClass(net.minecraft.entity.ExperienceOrbEntity.class, box, e -> !e.isRemoved())) {
                net.minecraft.util.math.Vec3d pull = player.getEntityPos().add(0, 0.5, 0).subtract(orb.getEntityPos()).normalize().multiply(0.42);
                orb.setVelocity(pull);
                orb.velocityDirty = true;
            }
            if (magnetCount >= 2 && player.age % 5 == 0) {
                net.minecraft.util.math.Box deflectorBox = player.getBoundingBox().expand(3.5);
                for (net.minecraft.entity.projectile.ProjectileEntity proj : sw.getEntitiesByClass(net.minecraft.entity.projectile.ProjectileEntity.class, deflectorBox, e -> e.getOwner() != player && !e.isRemoved())) {
                    net.minecraft.util.math.Vec3d push = proj.getEntityPos().subtract(player.getEntityPos()).normalize().multiply(0.85);
                    proj.setVelocity(push);
                    proj.velocityDirty = true;
                }
            }
        }
        if (chronoCount > 0) {
            int speedAmp = Math.min(chronoCount - 1, 1);
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SPEED, 40, speedAmp, true, false));
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.HASTE, 40, speedAmp, true, false));

            if (player.age % 40 == 0 && stack.isDamaged()) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
            }

            if (player.age % 20 == 0) {
                ServerWorld sw = (ServerWorld) player.getEntityWorld();
                int cropRadius = 2 + chronoCount;
                int rx = player.getBlockX() + player.getRandom().nextInt(cropRadius * 2 + 1) - cropRadius;
                int rz = player.getBlockZ() + player.getRandom().nextInt(cropRadius * 2 + 1) - cropRadius;
                for (int dy = -2; dy <= 2; dy++) {
                    net.minecraft.util.math.BlockPos cp = new net.minecraft.util.math.BlockPos(rx, player.getBlockY() + dy, rz);
                    net.minecraft.block.BlockState cState = sw.getBlockState(cp);
                    if (cState.getBlock() instanceof net.minecraft.block.CropBlock ||
                        cState.getBlock() instanceof net.minecraft.block.SaplingBlock ||
                        cState.getBlock() instanceof net.minecraft.block.SugarCaneBlock ||
                        cState.getBlock() instanceof net.minecraft.block.NetherWartBlock ||
                        cState.getBlock() instanceof net.minecraft.block.StemBlock) {
                        cState.randomTick(sw, cp, player.getRandom());
                        sw.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, cp.getX() + 0.5, cp.getY() + 0.5, cp.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
                        break;
                    }
                }
            }
        }

        String reforge = getReforge(stack);
        if (reforge != null) {
            switch (reforge) {
                case REFORGE_DIVINE ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 40, 0, true, false));
                case REFORGE_SWIFT ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SPEED, 40, 0, true, false));
                case REFORGE_FIERCE ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.STRENGTH, 40, 0, true, false));
                case REFORGE_TITANIC ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.RESISTANCE, 40, 0, true, false));
                case REFORGE_LEGENDARY ->
                    player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.LUCK, 40, 0, true, false));
            }
        }
    }

    public static Text getReforgeName(String reforge) {
        if (reforge == null) return Text.empty();
        return switch (reforge) {
            case REFORGE_DIVINE -> Text.translatable("reforge.chainores.divine").formatted(Formatting.AQUA, Formatting.BOLD);
            case REFORGE_LEGENDARY -> Text.translatable("reforge.chainores.legendary").formatted(Formatting.GOLD, Formatting.BOLD);
            case REFORGE_FIERCE -> Text.translatable("reforge.chainores.fierce").formatted(Formatting.RED, Formatting.BOLD);
            case REFORGE_SWIFT -> Text.translatable("reforge.chainores.swift").formatted(Formatting.GREEN, Formatting.BOLD);
            case REFORGE_TITANIC -> Text.translatable("reforge.chainores.titanic").formatted(Formatting.GRAY, Formatting.BOLD);
            default -> Text.empty();
        };
    }

    public static List<Text> getReforgeDescriptionLines(String reforge) {
        List<Text> lines = new ArrayList<>();
        if (reforge == null) return lines;
        switch (reforge) {
            case REFORGE_DIVINE -> {
                lines.add(Text.translatable("reforge.chainores.divine.desc1").formatted(Formatting.DARK_AQUA));
                lines.add(Text.translatable("reforge.chainores.divine.desc2").formatted(Formatting.AQUA));
            }
            case REFORGE_LEGENDARY -> {
                lines.add(Text.translatable("reforge.chainores.legendary.desc1").formatted(Formatting.GOLD));
                lines.add(Text.translatable("reforge.chainores.legendary.desc2").formatted(Formatting.YELLOW));
            }
            case REFORGE_FIERCE -> {
                lines.add(Text.translatable("reforge.chainores.fierce.desc1").formatted(Formatting.DARK_RED));
                lines.add(Text.translatable("reforge.chainores.fierce.desc2").formatted(Formatting.RED));
            }
            case REFORGE_SWIFT -> {
                lines.add(Text.translatable("reforge.chainores.swift.desc1").formatted(Formatting.DARK_GREEN));
                lines.add(Text.translatable("reforge.chainores.swift.desc2").formatted(Formatting.GREEN));
            }
            case REFORGE_TITANIC -> {
                lines.add(Text.translatable("reforge.chainores.titanic.desc1").formatted(Formatting.GRAY));
                lines.add(Text.translatable("reforge.chainores.titanic.desc2").formatted(Formatting.WHITE));
            }
        }
        return lines;
    }

    public static void appendModifierTooltips(ItemStack stack, Consumer<Text> textConsumer) {
        List<Text> lines = new ArrayList<>();
        appendModifierTooltips(stack, lines);
        for (Text line : lines) {
            textConsumer.accept(line);
        }
    }

    public static void appendModifierTooltips(ItemStack stack, List<Text> tooltip) {
        String reforge = getReforge(stack);
        if (reforge != null) {
            boolean hasReforgeInfo = tooltip.stream().anyMatch(t -> t.getString().contains("⚡ "));
            if (!hasReforgeInfo) {
                Text reforgeText = getReforgeName(reforge);
                if (!reforgeText.getString().isEmpty()) {
                    tooltip.add(Text.literal("⚡ ").formatted(Formatting.YELLOW).append(reforgeText));
                    for (Text desc : getReforgeDescriptionLines(reforge)) {
                        tooltip.add(desc);
                    }
                }
            }
        }

        // Prevent duplicate socket info if called multiple times
        boolean hasSocketInfo = tooltip.stream().anyMatch(t -> t.getString().contains("Ranuras de Gemas"));
        if (hasSocketInfo) {
            return;
        }

        if (!isSocketableItem(stack)) {
            return;
        }

        int maxSockets = getMaxSockets(stack);
        if (maxSockets <= 0) {
            tooltip.add(Text.literal("Ranuras de Gemas: ").formatted(Formatting.GRAY)
                    .append(Text.literal("[Sin Ranuras]").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(" (Tier inicial demasiado débil)").formatted(Formatting.DARK_RED, Formatting.ITALIC)));
        } else {
            List<String> gems = getGems(stack);
            tooltip.add(Text.literal("Ranuras de Gemas [" + gems.size() + "/" + maxSockets + "]:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

            MutableText socketLine = Text.literal("  ");
            for (int i = 0; i < maxSockets; i++) {
                if (i < gems.size()) {
                    String g = gems.get(i);
                    String base = getBaseGemType(g);
                    int tier = getGemTier(g);
                    String roman = switch (tier) {
                        case 2 -> " II";
                        case 3 -> " III";
                        default -> " I";
                    };

                    switch (base) {
                        case GEM_BLOOD -> socketLine.append(Text.literal("[◆ Carmesí" + roman + "] ").formatted(Formatting.DARK_RED, Formatting.BOLD));
                        case GEM_VOID -> socketLine.append(Text.literal("[◆ Vacío" + roman + "] ").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
                        case GEM_CORE -> socketLine.append(Text.literal("[◆ Núcleo" + roman + "] ").formatted(Formatting.GOLD, Formatting.BOLD));
                        case GEM_GENESIS -> socketLine.append(Text.literal("[◆ Génesis" + roman + "] ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
                        case GEM_EARTH -> socketLine.append(Text.literal("[◆ Tierra" + roman + "] ").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
                        case GEM_ECHO -> socketLine.append(Text.literal("[◆ Eco" + roman + "] ").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
                        case GEM_MAGNET -> socketLine.append(Text.literal("[◆ Imán" + roman + "] ").formatted(Formatting.BLUE, Formatting.BOLD));
                        case GEM_CHRONO -> socketLine.append(Text.literal("[◆ Crono" + roman + "] ").formatted(Formatting.GOLD, Formatting.BOLD));
                        default -> socketLine.append(Text.literal("[◆ Gema" + roman + "] ").formatted(Formatting.AQUA));
                    }
                } else {
                    socketLine.append(Text.literal("[◇ Vacío] ").formatted(Formatting.DARK_GRAY));
                }
            }
            tooltip.add(socketLine);
        }
    }
}
