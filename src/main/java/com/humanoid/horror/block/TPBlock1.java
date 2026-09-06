package com.humanoid.horror.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
                    Registries.DIMENSION,
                    new ResourceLocation(
                            "humanoid",
                            "dimension2"
                    )
            );

    private static final ResourceKey<Level> HUMANOID_DIMENSION =
            ResourceKey.create(
                    Registries.DIMENSION,
                    new ResourceLocation(
                            "humanoid",
                            "humanoid_dimension"
                    )
            );

    public TPBlock1(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity
    ) {

        super.entityInside(
                state,
                level,
                pos,
                entity
        );

        if (level.isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.getServer() == null) {
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

        /*
         * Rastgele X / Z
         * Y = 2
         */
        int randomX =
                RANDOM.nextInt(2001) - 1000;

        int randomZ =
                RANDOM.nextInt(2001) - 1000;

        player.teleportTo(
                targetLevel,
                randomX + 0.5D,
                2.0D,
                randomZ + 0.5D,
                player.getYRot(),
                player.getXRot()
        );
    }
}
