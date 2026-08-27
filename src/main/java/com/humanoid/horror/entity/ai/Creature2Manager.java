package com.humanoid.horror.entity.ai;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID
)
public class Creature2Manager {

    // =========================================================
    // ZAMANLAR
    // =========================================================

    /*
     * 20 dakika:
     * 20 tick x 60 saniye x 20 dakika
     */
    private static final int COOLDOWN_TICKS = 24000;

    /*
     * Maksimum rastgele spawn zamanı:
     * 40 dakika = 48000 tick
     */
    private static final int MAX_SPAWN_TIMER = 48000;

    /*
     * Jumpscare:
     * 24 saniye = 480 tick
     */
    private static final int JUMPSCARE_TICKS = 480;

    /*
     * Jumpscare yaratığı:
     * Her 4 saniyede bir = 80 tick
     */
    private static final int JUMPSCARE_INTERVAL = 80;

    /*
     * Jumpscare yaratığı 1.5 saniye sonra silinir.
     * 1.5 saniye = 30 tick
     */
    private static final int JUMPSCARE_ENTITY_LIFETIME = 30;

    // =========================================================
    // DURUM
    // =========================================================

    private static int globalCooldown = 0;

    private static int nextSpawnTimer =
            new Random().nextInt(MAX_SPAWN_TIMER);

    private static Creature2 activeCreature = null;

    private static ServerPlayer targetPlayer = null;

    private static boolean hasSentLookAtMe = false;

    private static boolean isJumpscareActive = false;

    private static int jumpscareTimer = 0;

    private static final Random RANDOM =
            new Random();

    // =========================================================
    // SES
    // =========================================================

    public static final SoundEvent CONT_SOUND =
            SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(
                            HumanoidMod.MOD_ID,
                            "cont"
                    )
            );

    // =========================================================
    // SERVER TICK
    // =========================================================

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (
                event.phase
                        != TickEvent.Phase.END
        ) {
            return;
        }

        // =====================================================
        // COOLDOWN
        // =====================================================

        if (globalCooldown > 0) {

            globalCooldown--;

            return;
        }

        ServerLevel level =
                event.getServer().overworld();

        if (level == null) {
            return;
        }

        // =====================================================
        // GÜN KONTROLÜ
        // =====================================================

        long dayCount =
                level.getDayTime() / 24000L;

        /*
         * En az 3. gün.
         */
        if (dayCount < 3L) {
            return;
        }

        // =====================================================
        // AKTİF CREATURE KONTROLÜ
        // =====================================================

        if (
                activeCreature != null
                        && !activeCreature.isAlive()
                        && !isJumpscareActive
        ) {

            activeCreature = null;
            targetPlayer = null;
            hasSentLookAtMe = false;
        }

        // =====================================================
        // YENİ SPAWN
        // =====================================================

        if (
                activeCreature == null
                        && !isJumpscareActive
        ) {

            nextSpawnTimer--;

            if (nextSpawnTimer <= 0) {

                spawnCreatureForRandomPlayer(
                        level
                );
            }
        }

        // =====================================================
        // STALKING
        // =====================================================

        if (
                activeCreature != null
                        && targetPlayer != null
                        && !isJumpscareActive
        ) {

            /*
             * Oyuncu artık geçerli değilse
             * sistemi sıfırla.
             */
            if (
                    !targetPlayer.isAlive()
                            || targetPlayer.isSpectator()
            ) {

                activeCreature.discard();

                activeCreature = null;
                targetPlayer = null;

                hasSentLookAtMe = false;

                nextSpawnTimer =
                        RANDOM.nextInt(
                                MAX_SPAWN_TIMER
                        );

                return;
            }

            handleStalkingLogic();
        }

        // =====================================================
        // JUMPSCARE
        // =====================================================

        if (
                isJumpscareActive
                        && targetPlayer != null
        ) {

            if (
                    !targetPlayer.isAlive()
                            || targetPlayer.isSpectator()
            ) {

                stopJumpscare(level);

                return;
            }

            handleJumpscareSequence(
                    level
            );
        }
    }

    // =========================================================
    // CREATURE2 SPAWN
    // =========================================================

    private static void spawnCreatureForRandomPlayer(
            ServerLevel level
    ) {

        List<ServerPlayer> players =
                level.getPlayers(
                        player ->
                                player.isAlive()
                                        && !player.isSpectator()
                );

        if (players.isEmpty()) {

            nextSpawnTimer =
                    RANDOM.nextInt(
                            MAX_SPAWN_TIMER
                    );

            return;
        }

        // =====================================================
        // RASTGELE OYUNCU
        // =====================================================

        targetPlayer =
                players.get(
                        RANDOM.nextInt(
                                players.size()
                        )
                );

        // =====================================================
        // OYUNCUNUN BAKTIĞI YÖN
        // =====================================================

        Vec3 lookAngle =
                targetPlayer.getLookAngle();

        /*
         * Yaklaşık 20 blok ileride.
         */
        Vec3 spawnVec =
                targetPlayer.position()
                        .add(
                                lookAngle.scale(
                                        20.0D
                                )
                        );

        BlockPos rawPos =
                BlockPos.containing(
                        spawnVec
                );

        // =====================================================
        // GÜVENLİ YÜKSEKLİK
        // =====================================================

        BlockPos surfacePos =
                level.getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        rawPos
                );

        double spawnX =
                rawPos.getX() + 0.5D;

        double spawnY =
                surfacePos.getY();

        double spawnZ =
                rawPos.getZ() + 0.5D;

        // =====================================================
        // CREATURE2 OLUŞTUR
        // =====================================================

        Creature2 creature =
                new Creature2(
                        ModEntities.CREATURE2.get(),
                        level
                );

        creature.setPos(
                spawnX,
                spawnY,
                spawnZ
        );

        /*
         * Doğduğu anda kafa dönmeye başlar.
         */
        creature.setHeadSpinning(
                true
        );

        level.addFreshEntity(
                creature
        );

        activeCreature =
                creature;

        hasSentLookAtMe =
                false;

        // =====================================================
        // REDSTONE TORCH
        // =====================================================

        BlockPos torchPos =
                BlockPos.containing(
                        spawnX,
                        spawnY,
                        spawnZ
                ).relative(
                        creature.getDirection()
                );

        /*
         * Sadece hava ise torch koy.
         */
        if (
                level.isEmptyBlock(
                        torchPos
                )
        ) {

            level.setBlock(
                    torchPos,
                    Blocks.REDSTONE_TORCH
                            .defaultBlockState(),
                    3
            );
        }

        // =====================================================
        // TIMER RESET
        // =====================================================

        nextSpawnTimer =
                RANDOM.nextInt(
                        MAX_SPAWN_TIMER
                );
    }

    // =========================================================
    // STALKING
    // =========================================================

    private static void handleStalkingLogic() {

        if (
                activeCreature == null
                        || targetPlayer == null
        ) {
            return;
        }

        Vec3 difference =
                activeCreature.position()
                        .subtract(
                                targetPlayer.position()
                        );

        /*
         * Sıfıra bölünme / NaN riskini önle.
         */
        if (difference.lengthSqr() < 0.0001D) {
            return;
        }

        Vec3 playerToCreature =
                difference.normalize();

        Vec3 playerLook =
                targetPlayer.getLookAngle();

        double dot =
                playerLook.dot(
                        playerToCreature
                );

        // =====================================================
        // GÖRÜŞ ALANI
        // =====================================================

        if (
                dot > 0.5D
                        && !hasSentLookAtMe
        ) {

            targetPlayer.sendSystemMessage(
                    Component.literal(
                            "<JavaObjectEntityNotFound>LookAtMe"
                    )
            );

            hasSentLookAtMe =
                    true;
        }

        // =====================================================
        // DİREKT BAKIŞ
        // =====================================================

        if (dot > 0.98D) {

            startJumpscareSequence();
        }
    }

    // =========================================================
    // JUMPSCARE BAŞLAT
    // =========================================================

    private static void startJumpscareSequence() {

        if (
                isJumpscareActive
                        || targetPlayer == null
        ) {
            return;
        }

        isJumpscareActive =
                true;

        jumpscareTimer =
                JUMPSCARE_TICKS;

        // =====================================================
        // SES
        // =====================================================

        targetPlayer.level().playSound(
                null,
                targetPlayer.getX(),
                targetPlayer.getY(),
                targetPlayer.getZ(),
                CONT_SOUND,
                SoundSource.MASTER,
                1.0F,
                1.0F
        );

        // =====================================================
        // KÖRLÜK
        // =====================================================

        targetPlayer.addEffect(
                new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        JUMPSCARE_TICKS,
                        0,
                        false,
                        false
                )
        );

        // =====================================================
        // UZAK CREATURE2'Yİ SİL
        // =====================================================

        if (
                activeCreature != null
                        && activeCreature.isAlive()
        ) {

            activeCreature.discard();
        }

        activeCreature =
                null;
    }

    // =========================================================
    // JUMPSCARE SEQUENCE
    // =========================================================

    private static void handleJumpscareSequence(
            ServerLevel level
    ) {

        jumpscareTimer--;

        // =====================================================
        // HER 4 SANİYEDE BİR
        // =====================================================

        if (
                jumpscareTimer > 0
                        && jumpscareTimer
                                % JUMPSCARE_INTERVAL
                                == 0
        ) {

            Vec3 frontPos =
                    targetPlayer.position()
                            .add(
                                    targetPlayer
                                            .getLookAngle()
                                            .scale(1.0D)
                            );

            Creature2 jumpscareEntity =
                    new Creature2(
                            ModEntities.CREATURE2.get(),
                            level
                    );

            jumpscareEntity.setPos(
                    frontPos.x,
                    frontPos.y,
                    frontPos.z
            );

            jumpscareEntity.setHeadSpinning(
                    true
            );

            level.addFreshEntity(
                    jumpscareEntity
            );

            // =================================================
            // IŞIKLARI SİL
            // =================================================

            extinguishLightSources(
                    level,
                    targetPlayer.blockPosition(),
                    12
            );

            // =================================================
            // 30 TICK SONRA SİL
            // =================================================

            int removeTick =
                    level.getServer()
                            .getTickCount()
                            + JUMPSCARE_ENTITY_LIFETIME;

            level.getServer().tell(
                    new net.minecraft.server.TickTask(
                            removeTick,
                            () -> {

                                if (
                                        jumpscareEntity
                                                .isAlive()
                                ) {

                                    jumpscareEntity
                                            .discard();
                                }
                            }
                    )
            );
        }

        // =====================================================
        // JUMPSCARE BİTTİ
        // =====================================================

        if (jumpscareTimer <= 0) {

            stopJumpscare(level);
        }
    }

    // =========================================================
    // JUMPSCARE TEMİZLE
    // =========================================================

    private static void stopJumpscare(
            ServerLevel level
    ) {

        isJumpscareActive =
                false;

        jumpscareTimer =
                0;

        activeCreature =
                null;

        targetPlayer =
                null;

        hasSentLookAtMe =
                false;

        /*
         * 20 dakika cooldown.
         */
        globalCooldown =
                COOLDOWN_TICKS;

        /*
         * Sonraki spawn için yeniden
         * 0-40 dakika arasında rastgele süre.
         */
        nextSpawnTimer =
                RANDOM.nextInt(
                        MAX_SPAWN_TIMER
                );
    }

    // =========================================================
    // IŞIKLARI SÖNDÜR
    // =========================================================

    private static void extinguishLightSources(
            ServerLevel level,
            BlockPos center,
            int radius
    ) {

        BlockPos.betweenClosedStream(
                center.offset(
                        -radius,
                        -radius,
                        -radius
                ),
                center.offset(
                        radius,
                        radius,
                        radius
                )
        ).forEach(
                pos -> {

                    BlockState state =
                            level.getBlockState(
                                    pos
                            );

                    /*
                     * Işık yayan bloklar.
                     */
                    if (
                            state.getLightEmission(
                                    level,
                                    pos
                            ) > 0

                                    || state.is(
                                            Blocks.TORCH
                                    )

                                    || state.is(
                                            Blocks.WALL_TORCH
                                    )

                                    || state.is(
                                            Blocks.REDSTONE_TORCH
                                    )
                    ) {

                        level.setBlock(
                                pos,
                                Blocks.AIR
                                        .defaultBlockState(),
                                3
                        );
                    }
                }
        );
    }

    // =========================================================
    // HUD KONTROLÜ
    // =========================================================

    public static boolean isJumpscareActiveForHUD() {

        return isJumpscareActive;
    }
}
