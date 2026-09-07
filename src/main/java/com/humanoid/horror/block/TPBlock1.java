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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class TPBlock1 extends Block {

    private static final Random RANDOM = new Random();

    /*
     * Aynı oyuncunun aynı anda tekrar tekrar
     * teleport edilmesini engeller.
     */
    private static final Set<UUID> TELEPORTING_PLAYERS =
            new HashSet<>();

    /*
     * Hedef dimensionlar.
     */

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

    /*
     * ---------------------------------------------------------
     * BLOK KIRILMAZ
     * ---------------------------------------------------------
     *
     * Bu özellik BlockBehaviour.Properties tarafında
     * destroy time / resistance ile ayarlanacak.
     *
     * TPBlock1 kayıt edilirken:
     *
     * strength(-1.0F, 3600000.0F)
     *
     * kullanılmalı.
     */

    /*
     * ---------------------------------------------------------
     * BLOĞA BASMA
     * ---------------------------------------------------------
     *
     * Oyuncunun bloğun üst yüzeyine temas etmesi.
     */

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

        tryTeleport(level, pos, entity);
    }

    /*
     * ---------------------------------------------------------
     * DIŞ YÜZEYE TEMAS
     * ---------------------------------------------------------
     *
     * Oyuncu bloğun içine girmeden de çalışması için
     * çevredeki yüzey temasını kontrol ediyoruz.
     */

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

        tryTeleport(level, pos, entity);
    }

    /*
     * ---------------------------------------------------------
     * TELEPORT
     * ---------------------------------------------------------
     */

    private static void tryTeleport(
            Level level,
            BlockPos pos,
            Entity entity
    ) {

        /*
         * Client tarafında çalışmasın.
         */

        if (level.isClientSide) {
            return;
        }

        /*
         * Sadece oyuncular.
         */

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Server kontrolü.
         */

        if (player.getServer() == null) {
            return;
        }

        UUID uuid =
                player.getUUID();

        /*
         * Aynı teleport işlemini ikinci kez başlatma.
         */

        if (TELEPORTING_PLAYERS.contains(uuid)) {
            return;
        }

        TELEPORTING_PLAYERS.add(uuid);

        try {

            /*
             * %50 Dimension2
             * %50 Humanoid Dimension
             */

            ResourceKey<Level> targetKey;

            if (RANDOM.nextBoolean()) {

                targetKey =
                        DIMENSION2;

            } else {

                targetKey =
                        HUMANOID_DIMENSION;
            }

            ServerLevel targetLevel =
                    player.getServer()
                            .getLevel(targetKey);

            if (targetLevel == null) {
                return;
            }

            /*
             * Rastgele X / Z
             *
             * -1000 ... +1000
             */

            int randomX =
                    RANDOM.nextInt(2001) - 1000;

            int randomZ =
                    RANDOM.nextInt(2001) - 1000;

            /*
             * Y = 2
             */

            player.teleportTo(
                    targetLevel,
                    randomX + 0.5D,
                    2.0D,
                    randomZ + 0.5D,
                    player.getYRot(),
                    player.getXRot()
            );

        } finally {

            TELEPORTING_PLAYERS.remove(uuid);
        }
    }
}
