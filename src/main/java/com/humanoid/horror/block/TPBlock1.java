package com.humanoid.horror.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class TPBlock1 extends Block {

    private static final Random RANDOM = new Random();

    private static final ResourceKey<Level> DIMENSION2 =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    new net.minecraft.resources.ResourceLocation(
                            "humanoid",
                            "dimension2"
                    )
            );

    private static final ResourceKey<Level> HUMANOID_DIMENSION =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    new net.minecraft.resources.ResourceLocation(
                            "humanoid",
                            "humanoid_dimension"
                    )
            );

    public TPBlock1(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(
            Level level,
            BlockPos pos,
            BlockState state,
            Entity entity
    ) {

        super.stepOn(
                level,
                pos,
                state,
                entity
        );

        if (level.isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        /*
         * %50 Dimension2
         * %50 Humanoid Dimension
         */
        ResourceKey<Level> targetKey;

        if (RANDOM.nextBoolean()) {
            targetKey = DIMENSION2;
        } else {
            targetKey = HUMANOID_DIMENSION;
        }

        ServerLevel targetLevel =
                player.getServer()
                        .getLevel(targetKey);

        if (targetLevel == null) {
            return;
        }

        BlockPos spawnPos =
                targetLevel.getSharedSpawnPos();

        player.teleportTo(
                targetLevel,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 1.0D,
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );
    }
}
