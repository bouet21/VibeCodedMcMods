package com.chainores.entity;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BossScalingHelper {
    public static int updateBossScaling(HostileEntity boss, ServerWorld world, double baseHealth, double baseDamage, int lastPlayerCount) {
        if (boss.age % 40 != 0) return lastPlayerCount;

        // Count non-spectator players within 48 blocks
        int count = 0;
        for (ServerPlayerEntity player : world.getPlayers(p -> !p.isSpectator() && p.squaredDistanceTo(boss) <= 48.0 * 48.0)) {
            count++;
        }
        count = Math.max(1, count);

        if (count != lastPlayerCount) {
            float healthRatio = Math.clamp(boss.getHealth() / boss.getMaxHealth(), 0.01f, 1.0f);
            double extraFactor = 0.35 * (count - 1); // +35% HP per additional player
            double dmgFactor = 0.15 * (count - 1);   // +15% DMG per additional player

            double newMaxHealth = baseHealth * (1.0 + extraFactor);
            double newDamage = baseDamage * (1.0 + dmgFactor);

            EntityAttributeInstance healthAttr = boss.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(newMaxHealth);
                boss.setHealth((float) (newMaxHealth * healthRatio));
            }

            EntityAttributeInstance dmgAttr = boss.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (dmgAttr != null) {
                dmgAttr.setBaseValue(newDamage);
            }

            if (count > 1 && count > lastPlayerCount) {
                int hpPercent = (int) (extraFactor * 100);
                int dmgPercent = (int) (dmgFactor * 100);
                Text alert = Text.literal("⚔ ¡DIFICULTAD ADAPTATIVA! ").formatted(Formatting.RED, Formatting.BOLD)
                        .append(boss.getName())
                        .append(Text.literal(" se potencia con " + count + " jugadores (+ " + hpPercent + "% HP, +" + dmgPercent + "% Daño).").formatted(Formatting.GOLD));
                for (ServerPlayerEntity p : world.getPlayers(p -> p.squaredDistanceTo(boss) <= 56.0 * 56.0)) {
                    p.sendMessage(alert, false);
                }
            }
            return count;
        }
        return lastPlayerCount;
    }
}
