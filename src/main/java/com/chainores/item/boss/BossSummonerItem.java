package com.chainores.item.boss;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.function.Supplier;

public class BossSummonerItem extends Item {
    private final Supplier<EntityType<? extends HostileEntity>> bossTypeSupplier;
    private final RegistryKey<World> requiredDimension;
    private final int maxY;
    private final int minY;
    private final String summonMessageKey;
    private final Formatting messageColor;
    private final SoundEvent summonSound;
    private final ParticleEffect summonParticle;

    public BossSummonerItem(
            Settings settings,
            Supplier<EntityType<? extends HostileEntity>> bossTypeSupplier,
            RegistryKey<World> requiredDimension,
            int minY,
            int maxY,
            String summonMessageKey,
            Formatting messageColor,
            SoundEvent summonSound,
            ParticleEffect summonParticle
    ) {
        super(settings);
        this.bossTypeSupplier = bossTypeSupplier;
        this.requiredDimension = requiredDimension;
        this.minY = minY;
        this.maxY = maxY;
        this.summonMessageKey = summonMessageKey;
        this.messageColor = messageColor;
        this.summonSound = summonSound;
        this.summonParticle = summonParticle;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        PlayerEntity player = context.getPlayer();
        BlockPos clickPos = context.getBlockPos().offset(context.getSide());

        return trySummon(world, player, clickPos, context.getStack());
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockPos spawnPos = user.getBlockPos().add(0, 1, 0);
        return trySummon(world, user, spawnPos, stack);
    }

    private ActionResult trySummon(World world, PlayerEntity player, BlockPos pos, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }

        // Validate dimension
        if (requiredDimension != null && !world.getRegistryKey().equals(requiredDimension)) {
            String dimName = requiredDimension.getValue().getPath();
            if (player != null) {
                player.sendMessage(
                        Text.translatable("message.chainores.summon_wrong_dimension", dimName)
                                .formatted(Formatting.RED, Formatting.BOLD),
                        true
                );
            }
            return ActionResult.FAIL;
        }

        // Validate height limits
        if (pos.getY() < minY || pos.getY() > maxY) {
            if (player != null) {
                player.sendMessage(
                        Text.translatable("message.chainores.summon_wrong_height", minY, maxY)
                                .formatted(Formatting.RED, Formatting.BOLD),
                        true
                );
            }
            return ActionResult.FAIL;
        }

        EntityType<? extends HostileEntity> type = bossTypeSupplier.get();
        if (type == null) {
            return ActionResult.FAIL;
        }

        HostileEntity boss = type.create(serverWorld, SpawnReason.TRIGGERED);
        if (boss == null) {
            return ActionResult.FAIL;
        }

        boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0.0f);
        boss.addCommandTag("custom_boss");

        // Visual and auditory effects
        if (summonParticle != null) {
            serverWorld.spawnParticles(summonParticle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 60, 1.5, 1.5, 1.5, 0.2);
        }

        serverWorld.playSound(null, pos, summonSound != null ? summonSound : SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5f, 0.9f);

        // Cosmetic lightning bolt
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            lightning.setCosmetic(true);
            serverWorld.spawnEntity(lightning);
        }

        serverWorld.spawnEntity(boss);

        // Server-wide broadcast announcement and Cinematic Screen Title
        Text announcement = Text.translatable(summonMessageKey).formatted(messageColor, Formatting.BOLD);
        for (ServerPlayerEntity serverPlayer : serverWorld.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.sendMessage(announcement, false);
        }
        Text title = Text.literal("¡" + boss.getName().getString().toUpperCase() + " HA DESPERTADO!").formatted(messageColor, Formatting.BOLD);
        Text subtitle = Text.translatable(summonMessageKey).formatted(Formatting.ITALIC, Formatting.WHITE);
        com.chainores.entity.BossCinematicsHelper.sendBossSpawnTitle(serverWorld, pos, title, subtitle, summonSound);

        if (player != null && !player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}
