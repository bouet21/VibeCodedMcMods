package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.item.boss.BossSummonerItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class ModBossItems {
    // 1. Invocadores de Jefes y Mini-Jefes
    public static final Item GLACIAL_HORN = register("glacial_horn", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("glacial_horn"))),
            () -> ModEntities.FROSTWARDEN,
            World.OVERWORLD,
            -64, 320,
            "message.chainores.summon_frostwarden",
            Formatting.AQUA,
            SoundEvents.ENTITY_WARDEN_SONIC_BOOM,
            ParticleTypes.SNOWFLAKE
    ));

    public static final Item VERDANT_HEART = register("verdant_heart", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("verdant_heart"))),
            () -> ModEntities.SYLVA_ANCIENT,
            World.OVERWORLD,
            -64, 320,
            "message.chainores.summon_sylva_ancient",
            Formatting.DARK_GREEN,
            SoundEvents.ENTITY_RAVAGER_ROAR,
            ParticleTypes.HAPPY_VILLAGER
    ));

    public static final Item ABYSSAL_EYE = register("abyssal_eye", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("abyssal_eye"))),
            () -> ModEntities.ABYSSAL_GUARDIAN,
            World.OVERWORLD,
            -64, 0,
            "message.chainores.summon_abyssal",
            Formatting.DARK_AQUA,
            SoundEvents.ENTITY_WARDEN_ROAR,
            ParticleTypes.SCULK_SOUL
    ));

    public static final Item FLAME_HEART = register("flame_heart", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("flame_heart"))),
            () -> ModEntities.NETHER_BEHEMOTH,
            World.NETHER,
            0, 128,
            "message.chainores.summon_nether",
            Formatting.DARK_RED,
            SoundEvents.ENTITY_BLAZE_SHOOT,
            ParticleTypes.FLAME
    ));

    public static final Item VOID_BEACON = register("void_beacon", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_beacon"))),
            () -> ModEntities.VOID_COLOSSUS,
            World.END,
            0, 256,
            "message.chainores.summon_void",
            Formatting.DARK_PURPLE,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            ParticleTypes.PORTAL
    ));

    public static final Item COSMIC_ALTAR_CORE = register("cosmic_altar_core", new BossSummonerItem(
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("cosmic_altar_core"))),
            () -> ModEntities.COSMIC_SOVEREIGN,
            null, // Any dimension
            0, 320,
            "message.chainores.summon_cosmic",
            Formatting.GOLD,
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            ParticleTypes.END_ROD
    ));

    // 2. Catalizadores de Transición de Tier
    public static final Item GLACIAL_CATALYST = register("glacial_catalyst", new Item(
            new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("glacial_catalyst")))
    ));

    public static final Item SYLVA_CATALYST = register("sylva_catalyst", new Item(
            new Item.Settings().maxCount(64).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("sylva_catalyst")))
    ));

    public static final Item ABYSSAL_CATALYST = register("abyssal_catalyst", new Item(
            new Item.Settings().maxCount(64).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("abyssal_catalyst")))
    ));

    public static final Item NETHER_CORE = register("nether_core", new Item(
            new Item.Settings().maxCount(64).rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("nether_core")))
    ));

    public static final Item VOID_SHARD = register("void_shard", new Item(
            new Item.Settings().maxCount(64).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("void_shard")))
    ));

    public static final Item COSMIC_SINGULARITY = register("cosmic_singularity", new Item(
            new Item.Settings().maxCount(16).rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("cosmic_singularity")))
    ));

    // 3. Armas Míticas de Jefes con Habilidades Activas
    public static final Item GLACIAL_BLADE = register("glacial_blade", new com.chainores.gear.GlacialBladeItem(
            new Item.Settings().rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("glacial_blade")))
    ));

    public static final Item BRAMBLE_WHIP = register("bramble_whip", new com.chainores.gear.BrambleWhipItem(
            new Item.Settings().rarity(Rarity.RARE)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("bramble_whip")))
    ));

    public static final Item ABYSSAL_TRIDENT = register("abyssal_trident", new com.chainores.gear.AbyssalTridentItem(
            new Item.Settings().rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("abyssal_trident")))
    ));

    public static final Item INFERNAL_WARAXE = register("infernal_waraxe", new com.chainores.gear.InfernalWaraxeItem(
            new Item.Settings().rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("infernal_waraxe")))
    ));

    public static final Item SINGULARITY_BLADE = register("singularity_blade", new com.chainores.gear.SingularityBladeItem(
            new Item.Settings().rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("singularity_blade")))
    ));

    public static final Item SUPERNOVA_SCEPTER = register("supernova_scepter", new com.chainores.gear.SupernovaScepterItem(
            new Item.Settings().rarity(Rarity.EPIC)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id("supernova_scepter")))
    ));

    // 4. Trofeos Colocables 3D con Aura de Baliza de Base
    public static final Item GLACIAL_TROPHY = registerTrophyItem("glacial_trophy", ModBlocks.GLACIAL_TROPHY_BLOCK, Rarity.RARE);
    public static final Item SYLVA_TROPHY = registerTrophyItem("sylva_trophy", ModBlocks.SYLVA_TROPHY_BLOCK, Rarity.RARE);
    public static final Item ABYSSAL_TROPHY = registerTrophyItem("abyssal_trophy", ModBlocks.ABYSSAL_TROPHY_BLOCK, Rarity.EPIC);
    public static final Item INFERNAL_TROPHY = registerTrophyItem("infernal_trophy", ModBlocks.INFERNAL_TROPHY_BLOCK, Rarity.EPIC);
    public static final Item VOID_TROPHY = registerTrophyItem("void_trophy", ModBlocks.VOID_TROPHY_BLOCK, Rarity.EPIC);
    public static final Item COSMIC_TROPHY = registerTrophyItem("cosmic_trophy", ModBlocks.COSMIC_TROPHY_BLOCK, Rarity.EPIC);

    private static Item registerTrophyItem(String name, net.minecraft.block.Block block, Rarity rarity) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, ChainOresMod.id(name));
        return Registry.register(Registries.ITEM, key, new net.minecraft.item.BlockItem(block,
                new Item.Settings().registryKey(key).maxCount(16).rarity(rarity).useBlockPrefixedTranslationKey()));
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ChainOresMod.id(name), item);
    }

    public static void initialize() {
        ChainOresMod.LOGGER.info("Registered Boss Summoners, Mythic Weapons, and 3D Trophies for Chain Ores.");
    }
}
