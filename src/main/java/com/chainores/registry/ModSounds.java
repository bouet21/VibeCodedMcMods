package com.chainores.registry;

import com.chainores.ChainOresMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent BOSS_SPAWN = register("boss.spawn");
    public static final SoundEvent BOSS_DEATH = register("boss.death");
    public static final SoundEvent GLACIAL_BLADE = register("weapon.glacial_blade");
    public static final SoundEvent BRAMBLE_WHIP = register("weapon.bramble_whip");
    public static final SoundEvent ABYSSAL_TRIDENT = register("weapon.abyssal_trident");
    public static final SoundEvent INFERNAL_WARAXE = register("weapon.infernal_waraxe");
    public static final SoundEvent SINGULARITY_BLADE = register("weapon.singularity_blade");
    public static final SoundEvent SUPERNOVA_SCEPTER = register("weapon.supernova_scepter");
    public static final SoundEvent TROPHY_BEACON = register("block.trophy_beacon");

    private static SoundEvent register(String name) {
        Identifier id = ChainOresMod.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered custom sound events for Chain Ores.");
    }
}
