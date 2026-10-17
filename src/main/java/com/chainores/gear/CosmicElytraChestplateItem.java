package com.chainores.gear;

import com.chainores.ChainOresMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CosmicElytraChestplateItem extends Item {

    public static final ArmorMaterial MATERIAL = new ArmorMaterial(
            12000,
            Map.of(EquipmentType.CHESTPLATE, 14),
            30,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            8.0f,
            0.4f,
            ItemTags.CHEST_ARMOR,
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, ChainOresMod.id("cosmic_elytra_chestplate"))
    );

    private static final Map<UUID, Long> LAST_BOOST = new ConcurrentHashMap<>();

    public CosmicElytraChestplateItem(Settings settings) {
        super(settings.armor(MATERIAL, EquipmentType.CHESTPLATE)
                .component(DataComponentTypes.GLIDER, Unit.INSTANCE)
                .maxDamage(12000)
                .fireproof());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (slot != EquipmentSlot.CHEST || !(entity instanceof ServerPlayerEntity player)) return;

        // Apply Gem & Reforge Buffs
        com.chainores.modifier.ChainModifiers.applyArmorGemAndReforgeBuffs(player, stack);

        // In flight effects
        if (player.isGliding()) {
            // Trail particles
            if (player.age % 2 == 0) {
                world.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 0.3, player.getZ(), 2, 0.2, 0.2, 0.2, 0.05);
            }

            // Celestial Jet Thrusters: crouch/sneak while gliding grants instant rocket boost every 3s
            if (player.isSneaking()) {
                long now = System.currentTimeMillis();
                long last = LAST_BOOST.getOrDefault(player.getUuid(), 0L);
                if (now - last > 3000L) {
                    LAST_BOOST.put(player.getUuid(), now);

                    Vec3d look = player.getRotationVector();
                    player.setVelocity(player.getVelocity().add(look.multiply(0.75)));
                    player.velocityDirty = true;
                    player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));

                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.2f, 1.4f);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_BREEZE_WIND_BURST, SoundCategory.PLAYERS, 1.0f, 1.8f);

                    world.spawnParticles(net.minecraft.particle.ParticleTypes.FIREWORK,
                            player.getX(), player.getY(), player.getZ(), 16, 0.3, 0.3, 0.3, 0.1);
                }
            }
        }

        // Void Rescue: Teleports back up if falling into void in End or Overworld with cooldown
        if (player.getY() < -40) {
            com.chainores.util.VoidRescueHelper.tryVoidRescue(player, 90.0);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        textConsumer.accept(Text.literal("★ Armadura de Especialista: Élitros Cósmicos").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        textConsumer.accept(Text.literal("✦ Fusión Élitros + Coraza Pesada: Vuelo infinito con protección total (+14 Armadura)").formatted(Formatting.AQUA));
        textConsumer.accept(Text.literal("✦ Propulsores Celestiales: Agacharse en pleno vuelo activa impulso de cohete (CD: 3s)").formatted(Formatting.YELLOW));
        textConsumer.accept(Text.literal("✦ Amortiguación Cinética: -100% de daño al chocar contra paredes").formatted(Formatting.GREEN));
        textConsumer.accept(Text.literal("✦ Rescate del Vacío: Teletransporte automático de salvamento ante caídas al vacío").formatted(Formatting.GOLD));
    }
}
