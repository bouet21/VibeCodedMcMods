package com.chainores.compat.trinkets;

import com.chainores.ChainOresMod;
import com.chainores.item.ChainTrinketItem;
import com.chainores.registry.ModItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TrinketsCompat {
    private static boolean initialized = false;

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) {
            ChainOresMod.LOGGER.info("[Chain Ores] Trinkets mod not present - accessories work in vanilla inventory/offhand.");
            return;
        }

        try {
            Class<?> trinketClass = Class.forName("dev.emi.trinkets.api.Trinket");
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");

            Object trinketProxy = Proxy.newProxyInstance(
                trinketClass.getClassLoader(),
                new Class<?>[]{trinketClass},
                (proxy, method, args) -> {
                    if ("tick".equals(method.getName()) && args != null && args.length >= 3) {
                        ItemStack stack = (ItemStack) args[0];
                        LivingEntity entity = (LivingEntity) args[2];
                        if (entity instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
                            if (player.age % 20 == 0) {
                                ChainTrinketItem.applyTrinketPassives(stack, player);
                            }
                        }
                    }
                    return null;
                }
            );

            Method registerTrinketMethod = trinketsApiClass.getMethod("registerTrinket", Item.class, trinketClass);
            registerTrinketMethod.invoke(null, ModItems.CHAIN_RING, trinketProxy);
            registerTrinketMethod.invoke(null, ModItems.CHAIN_AMULET, trinketProxy);

            Object satchelProxy = Proxy.newProxyInstance(
                TrinketsCompat.class.getClassLoader(),
                new Class<?>[]{trinketClass},
                (proxy, method, args) -> {
                    if ("tick".equals(method.getName()) && args != null && args.length >= 3) {
                        ItemStack stack = (ItemStack) args[0];
                        LivingEntity entity = (LivingEntity) args[2];
                        if (entity instanceof PlayerEntity player && !player.getEntityWorld().isClient() && player.getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                            ModItems.MINING_SATCHEL.inventoryTick(stack, sw, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
                        }
                    }
                    return null;
                }
            );
            registerTrinketMethod.invoke(null, ModItems.MINING_SATCHEL, satchelProxy);

            initialized = true;
            ChainOresMod.LOGGER.info("[Chain Ores] Successfully integrated with Trinkets! Chain Ring, Amulet, and Mining Satchel can now be equipped in Trinkets slots.");
        } catch (Throwable t) {
            ChainOresMod.LOGGER.warn("[Chain Ores] Failed to dynamically register Trinkets integration: " + t.getMessage());
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
