package com.chainores.registry;

import com.chainores.ChainOresMod;
import com.chainores.entity.AbyssalGuardianEntity;
import com.chainores.entity.CosmicSovereignEntity;
import com.chainores.entity.NetherBehemothEntity;
import com.chainores.entity.VoidColossusEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {
    public static final RegistryKey<EntityType<?>> ABYSSAL_GUARDIAN_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, ChainOresMod.id("abyssal_guardian"));
    public static final EntityType<AbyssalGuardianEntity> ABYSSAL_GUARDIAN = Registry.register(
            Registries.ENTITY_TYPE,
            ABYSSAL_GUARDIAN_KEY,
            EntityType.Builder.create(AbyssalGuardianEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.4f, 2.8f)
                    .build(ABYSSAL_GUARDIAN_KEY)
    );

    public static final RegistryKey<EntityType<?>> NETHER_BEHEMOTH_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, ChainOresMod.id("nether_behemoth"));
    public static final EntityType<NetherBehemothEntity> NETHER_BEHEMOTH = Registry.register(
            Registries.ENTITY_TYPE,
            NETHER_BEHEMOTH_KEY,
            EntityType.Builder.create(NetherBehemothEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.5f, 3.0f)
                    .makeFireImmune()
                    .build(NETHER_BEHEMOTH_KEY)
    );

    public static final RegistryKey<EntityType<?>> VOID_COLOSSUS_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, ChainOresMod.id("void_colossus"));
    public static final EntityType<VoidColossusEntity> VOID_COLOSSUS = Registry.register(
            Registries.ENTITY_TYPE,
            VOID_COLOSSUS_KEY,
            EntityType.Builder.create(VoidColossusEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.2f, 3.4f)
                    .build(VOID_COLOSSUS_KEY)
    );

    public static final RegistryKey<EntityType<?>> COSMIC_SOVEREIGN_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, ChainOresMod.id("cosmic_sovereign"));
    public static final EntityType<CosmicSovereignEntity> COSMIC_SOVEREIGN = Registry.register(
            Registries.ENTITY_TYPE,
            COSMIC_SOVEREIGN_KEY,
            EntityType.Builder.create(CosmicSovereignEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.8f, 3.8f)
                    .makeFireImmune()
                    .build(COSMIC_SOVEREIGN_KEY)
    );

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(ABYSSAL_GUARDIAN, AbyssalGuardianEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(NETHER_BEHEMOTH, NetherBehemothEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VOID_COLOSSUS, VoidColossusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(COSMIC_SOVEREIGN, CosmicSovereignEntity.createAttributes());

        ChainOresMod.LOGGER.info("Registered 4 Epic Dimension Bosses for Chain Ores Phase 3.");
    }
}
