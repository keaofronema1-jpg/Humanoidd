package com.humanoid.horror.entity.ai;

import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class CreatureAISystem {

    private static final double TARGET_RANGE = 128.0D;

    private CreatureAISystem() {
    }

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {

        LivingEntity entity =
                event.getEntity();

        /*
         * Sadece bizim creature'larımız.
         */
        if (!isSupportedCreature(entity)) {
            return;
        }

        /*
         * Sadece server tarafı.
         */
        Level level =
                entity.level();

        if (level.isClientSide()) {
            return;
        }

        /*
         * Yaşam kontrolü.
         */
        if (!entity.isAlive()) {
            return;
        }

        /*
         * AI tamamen kapalıysa dokunma.
         *
         * Örneğin Creature1'in kendi
         * başlangıç/timer sisteminde
         * setNoAi(true) kullandığı bölüm
         * aynen çalışmaya devam eder.
         */
        if (entity.isNoAi()) {
            return;
        }

        /*
         * Zaten geçerli bir oyuncu hedefliyorsa
         * tekrar hedef aramaya gerek yok.
         */
        LivingEntity currentTarget =
                entity.getTarget();

        if (isValidTarget(currentTarget, entity)) {

            moveToTarget(
                    entity,
                    currentTarget
            );

            return;
        }

        /*
         * Oyuncu bul.
         */
        ServerPlayer target =
                findNearestPlayer(entity);

        if (target == null) {
            entity.setTarget(null);
            entity.getNavigation().stop();
            return;
        }

        /*
         * Hedefi entity'ye ata.
         *
         * Creature3ChaseGoal bunu direkt
         * kullanacaktır.
         *
         * Humanoid de getTarget() üzerinden
         * hedefi görecektir.
         */
        entity.setTarget(target);

        /*
         * Hareket.
         *
         * Creature1'in kendi AI'sı da
         * navigation kullandığı için
         * çakışmayı minimumda tutuyoruz.
         */
        moveToTarget(
                entity,
                target
        );
    }

    /*
     * =========================================================
     * DESTEKLENEN CREATURE
     * =========================================================
     */

    private static boolean isSupportedCreature(
            LivingEntity entity
    ) {

        return entity instanceof Creature1
                || entity instanceof Creature2
                || entity instanceof Creature3
                || entity instanceof Humanoid;
    }

    /*
     * =========================================================
     * HEDEF GEÇERLİ Mİ?
     * =========================================================
     */

    private static boolean isValidTarget(
            LivingEntity target,
            LivingEntity creature
    ) {

        if (target == null) {
            return false;
        }

        if (!target.isAlive()) {
            return false;
        }

        if (target.isSpectator()) {
            return false;
        }

        if (!(target instanceof ServerPlayer)) {
            return false;
        }

        /*
         * Farklı dimension ise geçerli hedef değildir.
         */
        if (target.level() != creature.level()) {
            return false;
        }

        /*
         * Çok uzaklaştıysa yeni hedef ara.
         */
        if (creature.distanceToSqr(target)
                > TARGET_RANGE * TARGET_RANGE) {

            return false;
        }

        return true;
    }

    /*
     * =========================================================
     * EN YAKIN OYUNCUYU BUL
     * =========================================================
     */

    private static ServerPlayer findNearestPlayer(
            LivingEntity entity
    ) {

        if (entity.level().getServer() == null) {
            return null;
        }

        ServerPlayer nearest = null;

        double nearestDistance =
                TARGET_RANGE * TARGET_RANGE;

        for (ServerPlayer player :
                entity.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayers()) {

            /*
             * Başka dimension'daki oyuncular
             * hedeflenmesin.
             */
            if (player.serverLevel()
                    != entity.level()) {
                continue;
            }

            /*
             * Ölü/spectator oyuncular yok.
             */
            if (!player.isAlive()
                    || player.isSpectator()) {
                continue;
            }

            double distance =
                    entity.distanceToSqr(player);

            if (distance > nearestDistance) {
                continue;
            }

            nearestDistance = distance;
            nearest = player;
        }

        return nearest;
    }

    /*
     * =========================================================
     * HEDEFE HAREKET
     * =========================================================
     */

    private static void moveToTarget(
            LivingEntity entity,
            LivingEntity target
    ) {

        if (!isValidTarget(
                target,
                entity
        )) {
            return;
        }

        /*
         * Creature2'nin mevcut hareket hızı
         * 0 olduğu için summon edildiğinde
         * hareket edebilmesi için burada
         * normal creature hızına çıkarıyoruz.
         */
        if (entity instanceof Creature2) {

            if (entity.getAttribute(
                    Attributes.MOVEMENT_SPEED
            ) != null) {

                entity.getAttribute(
                        Attributes.MOVEMENT_SPEED
                ).setBaseValue(0.28D);
            }
        }

        /*
         * Humanoid'in mevcut hızı korunuyor.
         */
        if (entity instanceof Humanoid) {

            if (entity.getAttribute(
                    Attributes.MOVEMENT_SPEED
            ) != null) {

                entity.getAttribute(
                        Attributes.MOVEMENT_SPEED
                ).setBaseValue(0.30D);
            }
        }

        /*
         * Creature3 kendi
         * Creature3ChaseGoal'unda da
         * navigation.moveTo() yapıyor.
         *
         * Burada da hedefi hazır tutuyoruz.
         */
        entity.getNavigation().moveTo(
                target,
                getMovementSpeed(entity)
        );

        /*
         * Her creature oyuncuya baksın.
         */
        entity.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );
    }

    /*
     * =========================================================
     * HAREKET HIZI
     * =========================================================
     */

    private static double getMovementSpeed(
            LivingEntity entity
    ) {

        if (entity instanceof Creature1) {
            return 1.0D;
        }

        if (entity instanceof Creature2) {
            return 1.0D;
        }

        if (entity instanceof Creature3) {
            return 1.35D;
        }

        if (entity instanceof Humanoid) {
            return 1.0D;
        }

        return 1.0D;
    }
}
