package com.humanoid.horror.entity.ai;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class Creature1AI extends Goal {

    // =========================================================
    // ENTITY
    // =========================================================

    private final Creature1 entity;

    // =========================================================
    // SABİT HIZ
    // =========================================================

    /*
     * Creature1 her zaman aynı hızda koşar.
     *
     * Oyuncu yürüyüş hızına göre:
     *
     * 1.0 = normal
     * 1.05 = +0.05
     *
     * Bu değer hiçbir zaman birikmez.
     */
    private static final double CREATURE_SPEED = 1.05D;

    // =========================================================
    // MESAFE TOLERANSI
    // =========================================================

    /*
     * Creature1 hedef mesafeye ulaştığında
     * gereksiz ileri-geri hareket etmesin.
     */
    private static final double DISTANCE_TOLERANCE = 0.75D;

    // =========================================================
    // BLOCK BREAK COOLDOWN
    // =========================================================

    private int breakBlockCooldown = 0;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Creature1AI(Creature1 entity) {

        this.entity = entity;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    // =========================================================
    // CAN USE
    // =========================================================

    @Override
    public boolean canUse() {

        /*
         * /start verilmeden AI çalışmasın.
         */
        if (!HumanoidMod.isStartTriggered) {
            return false;
        }

        return this.entity.getCurrentTargetPlayer() != null;
    }

    // =========================================================
    // CAN CONTINUE
    // =========================================================

    @Override
    public boolean canContinueToUse() {

        /*
         * /start sistemi kapanırsa AI hemen dursun.
         */
        if (!HumanoidMod.isStartTriggered) {
            return false;
        }

        return this.entity.getCurrentTargetPlayer() != null;
    }

    // =========================================================
    // START
    // =========================================================

    @Override
    public void start() {

        breakBlockCooldown = 0;
    }

    // =========================================================
    // STOP
    // =========================================================

    @Override
    public void stop() {

        breakBlockCooldown = 0;

        this.entity
                .getNavigation()
                .stop();
    }

    // =========================================================
    // TICK
    // =========================================================

    @Override
    public void tick() {

        /*
         * /start yoksa hiçbir AI işlemi yapma.
         */
        if (!HumanoidMod.isStartTriggered) {

            this.entity
                    .getNavigation()
                    .stop();

            return;
        }

        // =====================================================
        // HEDEF OYUNCU
        // =====================================================

        ServerPlayer target =
                this.entity.getCurrentTargetPlayer();

        if (target == null) {

            this.entity
                    .getNavigation()
                    .stop();

            return;
        }

        Level level =
                this.entity.level();

        // =====================================================
        // HUD / ORTAK SAYAÇTAN HEDEF MESAFEYİ AL
        // =====================================================

        /*
         * BU DEĞER GERÇEK MESAFE DEĞİL.
         *
         * Creature1'in HUD/server sistemindeki
         * ortak hedef mesafesidir.
         *
         * Örneğin:
         *
         * 500 -> 500 blok
         * 200 -> 200 blok
         * 90  -> 90 blok
         * 89  -> 89 blok
         * ...
         * 0   -> oyuncuya ulaşma
         *
         * Burada artık gerçek mesafeyi bu değerin
         * üzerine YAZMIYORUZ.
         */
        int targetDistance =
                this.entity.getTargetDistance();

        if (targetDistance < 0) {
            targetDistance = 0;
        }

        // =====================================================
        // GERÇEK MESAFE
        // =====================================================

        double realDistance =
                this.entity.distanceTo(target);

        // =====================================================
        // HEDEF MESAFEYE GÖRE HAREKET
        // =====================================================

        /*
         * Creature1'in gerçek mesafesi,
         * sayaçta belirtilen mesafeden büyükse
         * oyuncuya doğru sürekli koş.
         *
         * Örnek:
         *
         * Sayaç = 90
         * Gerçek mesafe = 120
         * -> koş
         *
         * Gerçek mesafe = 90
         * -> dur
         *
         * Sayaç 89 olduğunda:
         *
         * Gerçek mesafe = 90
         * Hedef = 89
         * -> tekrar koş
         */
        if (realDistance >
                targetDistance + DISTANCE_TOLERANCE) {

            this.entity
                    .getNavigation()
                    .moveTo(
                            target,
                            CREATURE_SPEED
                    );

        } else {

            /*
             * Hedef mesafeye ulaştı.
             *
             * Sayaç bir sonraki değere düşene kadar
             * gereksiz şekilde oyuncunun üstüne gitme.
             */
            this.entity
                    .getNavigation()
                    .stop();
        }

        // =====================================================
        // OYUNCUYA BAK
        // =====================================================

        this.entity
                .getLookControl()
                .setLookAt(
                        target,
                        30.0F,
                        30.0F
                );

        // =====================================================
        // ENGEL KIRMA
        // =====================================================

        breakBlockCooldown++;

        /*
         * Her 5 tick'te bir önünü kontrol et.
         */
        if (breakBlockCooldown >= 5) {

            breakBlockCooldown = 0;

            clearObstaclesAhead(level);
        }

        /*
         * Jumpscare burada yapılmıyor.
         *
         * Creature1.tick() içerisinde mevcut
         * distance <= 2.0D kontrolü çalışmaya devam eder.
         */
    }

    // =========================================================
    // ÖNDEKİ ENGELLERİ TEMİZLE
    // =========================================================

    private void clearObstaclesAhead(
            Level level
    ) {

        BlockPos basePos =
                this.entity
                        .blockPosition()
                        .relative(
                                this.entity.getDirection()
                        );

        /*
         * Creature1'in önündeki yaklaşık
         * 4 blok yüksekliğindeki alan.
         *
         * basePos       = ayak
         * basePos.above = gövde
         * above(2)      = kafa
         * above(3)      = kafa üstü
         */
        BlockPos[] tunnelPositions =
                new BlockPos[]{
                        basePos,
                        basePos.above(),
                        basePos.above(2),
                        basePos.above(3)
                };

        for (BlockPos pos : tunnelPositions) {

            /*
             * Zaten boşsa hiçbir şey yapma.
             */
            if (level.isEmptyBlock(pos)) {
                continue;
            }

            /*
             * Önündeki bloğu AIR yap.
             *
             * Mevcut davranış korunuyor:
             * taş, toprak, obsidyen, bedrock vb.
             * bloklar temizlenebilir.
             */
            level.setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }
}
