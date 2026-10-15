package com.chainores.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChainOreBlock extends Block {
    private final int tier;

    public ChainOreBlock(int tier, Settings settings) {
        super(settings);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        // Only spawn particles for special/higher tier milestone ores
        ParticleEffect particle = getAmbientParticle(tier);
        if (particle == null) return;

        // Chance to spawn based on tier (higher tier = more frequent)
        int chance = (tier >= 140) ? 2 : (tier >= 100) ? 3 : 5;
        if (random.nextInt(chance) != 0) return;

        Direction direction = Direction.random(random);
        if (direction == Direction.UP || !world.getBlockState(pos.offset(direction)).isOpaque()) {
            double x = pos.getX() + 0.5 + (direction.getOffsetX() == 0 ? (random.nextDouble() - 0.5) * 0.8 : direction.getOffsetX() * 0.55);
            double y = pos.getY() + 0.5 + (direction.getOffsetY() == 0 ? (random.nextDouble() - 0.5) * 0.8 : direction.getOffsetY() * 0.55);
            double z = pos.getZ() + 0.5 + (direction.getOffsetZ() == 0 ? (random.nextDouble() - 0.5) * 0.8 : direction.getOffsetZ() * 0.55);

            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = random.nextDouble() * 0.04;
            double vz = (random.nextDouble() - 0.5) * 0.02;

            world.addParticleClient(particle, x, y, z, vx, vy, vz);
        }
    }

    private ParticleEffect getAmbientParticle(int tier) {
        if (tier >= 150) return ParticleTypes.TOTEM_OF_UNDYING;
        if (tier >= 140) return ParticleTypes.HAPPY_VILLAGER;
        if (tier >= 135) return ParticleTypes.ENCHANT;
        if (tier >= 125) return ParticleTypes.END_ROD;
        if (tier >= 115) return ParticleTypes.PORTAL;
        if (tier >= 100) return ParticleTypes.CRIMSON_SPORE;
        if (tier >= 90) return ParticleTypes.SOUL_FIRE_FLAME;
        if (tier >= 80) return ParticleTypes.FLAME;
        if (tier >= 70) return ParticleTypes.SCULK_SOUL;
        if (tier >= 50) return ParticleTypes.WITCH;
        if (tier >= 40 && tier < 50) return ParticleTypes.SPORE_BLOSSOM_AIR;
        if (tier == 30) return ParticleTypes.FLAME;
        if (tier >= 11 && tier <= 20) return ParticleTypes.SNOWFLAKE;
        return null;
    }
}
