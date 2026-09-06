package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class PhotoScareManager {

    /*
     * Her oyuncunun kendi spawn cooldown'u.
     */
    private static final Map<UUID, Integer> COOLDOWNS =
            new HashMap<>();

    /*
     * Aynı anda dünyada bulunabilecek maksimum
     * Photo Scare sayısı.
     */
    private static final int MAX_PHOTO_SCARES = 3;

    /*
     * Spawn denemesi aralığı.
     *
     * 20 tick = 1 saniye.
     */
    private static final int CHECK_INTERVAL = 20;

    /*
     * Minimum spawn mesafesi.
     */
    private static final double MIN_DISTANCE = 8.0D;

    /*
     * Maksimum spawn mesafesi.
     */
    private static final double MAX_DISTANCE = 22.0D;

    /*
     * Oyuncu başına minimum bekleme.
     *
     * 1200 tick = 60 saniye.
     */
    private static final int MIN_COOLDOWN = 1200;

    /*
     * Oyuncu başına maksimum bekleme.
     *
     * 2400 tick = 120 saniye.
     */
    private static final int MAX_COOLDOWN = 2400;

    private static int tickCounter = 0;

    // =========================================================
    // SERVER TICK
    // =========================================================

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        /*
         * Horror sistemi başlamadıysa Photo Scare spawn olmaz.
         */
        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        tickCounter++;

        if (tickCounter < CHECK_INTERVAL) {
            return;
        }

        tickCounter = 0;

        /*
         * Her server level'i kontrol et.
         */
        for (ServerLevel level :
                event.getServer().getAllLevels()) {

            /*
             * Aynı dünyada zaten yeterince Photo Scare varsa
             * yeni spawn yapma.
             */
            if (countPhotoScares(level) >= MAX_PHOTO_SCARES) {
                continue;
            }

            for (ServerPlayer player :
                    level.players()) {

                if (!player.isAlive()) {
                    continue;
                }

                if (player.isSpectator()) {
                    continue;
                }

                UUID uuid = player.getUUID();

                /*
                 * Cooldown yoksa oluştur.
                 */
                if (!COOLDOWNS.containsKey(uuid)) {
                    COOLDOWNS.put(
                            uuid,
                            randomCooldown(level.random)
                    );

                    continue;
                }

                int cooldown =
                        COOLDOWNS.get(uuid);

                if (cooldown > 0) {

                    COOLDOWNS.put(
                            uuid,
                            cooldown - CHECK_INTERVAL
                    );

                    continue;
                }

                /*
                 * Cooldown bitti.
                 *
                 * Spawn denemesi yap.
                 */
                trySpawn(level, player);

                /*
                 * Başarılı olsun veya olmasın
                 * tekrar hemen denemesin.
                 */
                COOLDOWNS.put(
                        uuid,
                        randomCooldown(level.random)
                );

                /*
                 * Dünya limitine ulaştıysak dur.
                 */
                if (countPhotoScares(level)
                        >= MAX_PHOTO_SCARES) {
                    break;
                }
            }
        }
    }

    // =========================================================
    // SPAWN
    // =========================================================

    private static void trySpawn(
            ServerLevel level,
            ServerPlayer player
    ) {

        RandomSource random =
                level.random;

        /*
         * En fazla 12 farklı nokta dene.
         */
        for (int attempt = 0; attempt < 12; attempt++) {

            double angle =
                    random.nextDouble()
                            * Math.PI * 2.0D;

            double distance =
                    Mth.nextDouble(
                            random,
                            MIN_DISTANCE,
                            MAX_DISTANCE
                    );

            int x =
                    Mth.floor(
                            player.getX()
                                    + Math.cos(angle)
                                    * distance
                    );

            int z =
                    Mth.floor(
                            player.getZ()
                                    + Math.sin(angle)
                                    * distance
                    );

            /*
             * En uygun yüksekliği bul.
             */
            int y =
                    level.getHeight(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

            /*
             * Yerden iki blok yüksekliğinde
             * yer gerekiyor.
             */
            BlockPos feet =
                    new BlockPos(
                            x,
                            y,
                            z
                    );

            BlockPos head =
                    feet.above();

            /*
             * Ayak ve kafa boş olmalı.
             */
            if (!isEmptySpace(level, feet)) {
                continue;
            }

            if (!isEmptySpace(level, head)) {
                continue;
            }

            /*
             * Entity'nin altında sağlam blok olmalı.
             */
            BlockPos below =
                    feet.below();

            BlockState belowState =
                    level.getBlockState(below);

            if (!belowState.isSolid()) {
                continue;
            }

            /*
             * Çok açık yerde spawn etmesini istemiyoruz.
             *
             * Karanlık alan tercih edilir.
             */
            int brightness =
                    level.getMaxLocalRawBrightness(feet);

            /*
             * 0-7 karanlık kabul ediyoruz.
             */
            boolean dark =
                    brightness <= 7;

            /*
             * Ağacın / duvarın arkasında olma kontrolü.
             */
            boolean behindSomething =
                    hasSolidBlockBehind(
                            level,
                            feet,
                            player
                    );

            if (!dark && !behindSomething) {
                continue;
            }

            /*
             * Oyuncunun ilk anda doğrudan görmemesi gerekiyor.
             */
            if (isVisibleFromPlayer(
                    level,
                    player,
                    feet
            )) {
                continue;
            }

            /*
             * Oyuncunun çok yakınına spawn etme.
             */
            if (player.distanceToSqr(
                    Vec3.atCenterOf(feet)
            ) < MIN_DISTANCE * MIN_DISTANCE) {
                continue;
            }

            /*
             * Photo Scare oluştur.
             */
            PhotoScareEntity entity =
                    ModEntities.PHOTO_SCARE
                            .get()
                            .create(level);

            if (entity == null) {
                return;
            }

            entity.moveTo(
                    feet.getX() + 0.5D,
                    feet.getY(),
                    feet.getZ() + 0.5D,
                    0.0F,
                    0.0F
            );

            /*
             * Fizik kapalı.
             */
            entity.setNoGravity(true);

            /*
             * Dünyaya ekle.
             */
            level.addFreshEntity(entity);

            return;
        }
    }

    // =========================================================
    // EMPTY SPACE
    // =========================================================

    private static boolean isEmptySpace(
            ServerLevel level,
            BlockPos pos
    ) {

        BlockState state =
                level.getBlockState(pos);

        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.CAVE_AIR)
                || state.is(Blocks.VOID_AIR);
    }

    // =========================================================
    // BEHIND TREE / WALL
    // =========================================================

    private static boolean hasSolidBlockBehind(
            ServerLevel level,
            BlockPos entityPos,
            ServerPlayer player
    ) {

        Vec3 direction =
                Vec3.atCenterOf(entityPos)
                        .subtract(
                                player.position()
                        )
                        .normalize();

        int dx =
                Mth.floor(
                        direction.x * 2.0D
                );

        int dz =
                Mth.floor(
                        direction.z * 2.0D
                );

        /*
         * Entity'nin oyuncudan uzak tarafına bak.
         */
        BlockPos behind =
                entityPos.offset(
                        dx,
                        0,
                        dz
                );

        BlockState state =
                level.getBlockState(behind);

        return state.isSolid();
    }

    // =========================================================
    // PLAYER LINE OF SIGHT
    // =========================================================

    private static boolean isVisibleFromPlayer(
            ServerLevel level,
            ServerPlayer player,
            BlockPos entityPos
    ) {

        Vec3 start =
                player.getEyePosition(1.0F);

        Vec3 end =
                Vec3.atCenterOf(entityPos)
                        .add(0.0D, 0.8D, 0.0D);

        /*
         * Oyuncu ile entity arasında blok varsa
         * entity başlangıçta gizli kabul edilir.
         */
        net.minecraft.world.level.ClipContext context =
                new net.minecraft.world.level.ClipContext(
                        start,
                        end,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                );

        net.minecraft.world.phys.BlockHitResult result =
                level.clip(context);

        double blockDistance =
                result.getLocation()
                        .distanceToSqr(start);

        double entityDistance =
                end.distanceToSqr(start);

        /*
         * Blok entity'den önce geliyorsa
         * oyuncu entity'yi direkt göremez.
         */
        return blockDistance + 0.01D
                < entityDistance;
    }

    // =========================================================
    // COUNT
    // =========================================================

    private static int countPhotoScares(
            ServerLevel level
    ) {

        AABB worldBox =
                new AABB(
                        -30_000_000,
                        -2048,
                        -30_000_000,
                        30_000_000,
                        2048,
                        30_000_000
                );

        return level.getEntitiesOfClass(
                PhotoScareEntity.class,
                worldBox
        ).size();
    }

    // =========================================================
    // RANDOM COOLDOWN
    // =========================================================

    private static int randomCooldown(
            RandomSource random
    ) {

        return Mth.nextInt(
                random,
                MIN_COOLDOWN,
                MAX_COOLDOWN
        );
    }
}
