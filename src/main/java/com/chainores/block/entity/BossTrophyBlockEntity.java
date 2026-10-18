package com.chainores.block.entity;

import com.chainores.block.BossTrophyBlock;
import com.chainores.registry.ModBlocks;
import com.chainores.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class BossTrophyBlockEntity extends BlockEntity {

    public BossTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BOSS_TROPHY_BLOCK_ENTITY, pos, state);
    }

    public BossTrophyBlock.TrophyType getTrophyType() {
        if (this.getCachedState().getBlock() instanceof BossTrophyBlock btb) {
            return btb.getTrophyType();
        }
        return BossTrophyBlock.TrophyType.COSMIC;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BossTrophyBlockEntity trophy) {
        BossTrophyBlock.TrophyType type = trophy.getTrophyType();

        if (world.isClient()) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.9;
            double cz = pos.getZ() + 0.5;

            switch (type) {
                case GLACIAL -> world.addParticleClient(ParticleTypes.SNOWFLAKE, cx, cy, cz, 0, 0.04, 0);
                case SYLVA -> world.addParticleClient(ParticleTypes.HAPPY_VILLAGER, cx, cy, cz, (world.random.nextDouble() - 0.5) * 0.1, 0.05, (world.random.nextDouble() - 0.5) * 0.1);
                case ABYSSAL -> world.addParticleClient(ParticleTypes.SCULK_SOUL, cx, cy, cz, 0, 0.03, 0);
                case INFERNAL -> world.addParticleClient(ParticleTypes.FLAME, cx, cy, cz, 0, 0.04, 0);
                case VOID -> world.addParticleClient(ParticleTypes.PORTAL, cx, cy, cz, (world.random.nextDouble() - 0.5) * 0.2, 0.1, (world.random.nextDouble() - 0.5) * 0.2);
                case COSMIC -> {
                    world.addParticleClient(ParticleTypes.END_ROD, cx, cy, cz, 0, 0.05, 0);
                    if (world.random.nextInt(3) == 0) {
                        world.addParticleClient(ParticleTypes.FIREWORK, cx, cy, cz, (world.random.nextDouble() - 0.5) * 0.1, 0.08, (world.random.nextDouble() - 0.5) * 0.1);
                    }
                }
            }
            return;
        }

        // Server-side buff application every 20 ticks (1 second)
        if (world.getTime() % 20 != 0) return;

        double radius = (type == BossTrophyBlock.TrophyType.COSMIC) ? 32.0 : 24.0;
        Box auraBox = new Box(pos).expand(radius);

        if (world instanceof ServerWorld serverWorld) {
            List<ServerPlayerEntity> players = serverWorld.getEntitiesByClass(ServerPlayerEntity.class, auraBox,
                    p -> !p.isSpectator() && p.isAlive());

            for (ServerPlayerEntity player : players) {
                applyTrophyBuff(player, type);
            }

            // Periodic ambient chime
            if (world.getTime() % 100 == 0 && !players.isEmpty()) {
                float pitch = switch (type) {
                    case GLACIAL -> 1.4f;
                    case SYLVA -> 1.2f;
                    case ABYSSAL -> 0.7f;
                    case INFERNAL -> 0.9f;
                    case VOID -> 0.8f;
                    case COSMIC -> 1.5f;
                };
                serverWorld.playSound(null, pos, ModSounds.TROPHY_BEACON, SoundCategory.BLOCKS, 0.6f, pitch);
            }
        }
    }

    private static void applyTrophyBuff(ServerPlayerEntity player, BossTrophyBlock.TrophyType type) {
        switch (type) {
            case GLACIAL -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 0, true, false, true));
                player.setFrozenTicks(0);
            }
            case SYLVA -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 10, 0, true, false, false));
            }
            case ABYSSAL -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, true, false, true));
            }
            case INFERNAL -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 260, 0, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, 0, true, false, true));
            }
            case VOID -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0, true, false, true));
            }
            case COSMIC -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1, true, false, true));
            }
        }
    }
}
