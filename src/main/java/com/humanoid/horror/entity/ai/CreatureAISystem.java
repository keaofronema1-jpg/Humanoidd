package com.humanoid.horror.entity.ai;

import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class CreatureAISystem {

    private static final double TARGET_RANGE = 128.0D;

    /*
     * Her creature'ın takip ettiği oyuncuya göre
     * en son bilinen mesafesini saklar.
     *
     * Örnek:
     *
     * Player:   X=100
     * Creature: X=90
     *
     * offsetX = -10
     *
     * Oyuncu başka dimensionda X=500'e giderse:
     *
     * Creature -> X=490
     *
     * Böylece aradaki blok mesafesi korunur.
     */
    private static final Map<UUID, FollowData> FOLLOW_DATA =
            new HashMap<>();

    private CreatureAISystem() {
    }

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {

        LivingEntity entity = event.getEntity();

        /*
         * Sadece bizim creature'larımız.
         */
        if (!isSupportedCreature(entity)) {
            return;
        }

        /*
         * Sadece server.
         */
        Level level = entity.level();

        if (level.isClientSide()) {
            return;
        }

        /*
         * Ölü entity temizliği.
         */
        if (!entity.isAlive()) {
            FOLLOW_DATA.remove(entity.getUUID());
            return;
        }

        /*
         * Creature'ın kendi NoAI sistemi açıksa
         * mevcut özel davranışını bozma.
         */
        if (entity.isNoAi()) {
            return;
        }

        /*
         * Mevcut hedef.
         */
        LivingEntity currentTarget =
                entity.getTarget();

        /*
         * =====================================================
         * ÖNCE DIMENSION TAKİBİ
         * =====================================================
         *
         * Oyuncu dimension değiştirdiyse,
         * normal target validation'dan önce
         * creature'ı onunla beraber taşıyoruz.
         */
        if (currentTarget instanceof ServerPlayer target) {

            FollowData data =
                    FOLLOW_DATA.get(entity.getUUID());

            /*
             * Önceden bu oyuncuyu takip ediyorsa
             * ve dimension değişmişse:
             */
            if (data != null
                    && data.targetUUID.equals(target.getUUID())
                    && target.level() != entity.level()) {

                teleportCreatureWithPlayerOffset(
                        entity,
                        target,
                        data
                );

                return;
            }

            /*
             * Aynı dimensiondaysa mevcut mesafeyi
             * sürekli güncelle.
             */
            if (target.level() == entity.level()
                    && target.isAlive()
                    && !target.isSpectator()) {

                updateFollowData(
                        entity,
                        target
                );
            }
        }

        /*
         * =====================================================
         * MEVCUT HEDEF GEÇERLİ Mİ?
         * =====================================================
         */
        if (isValidTarget(
                currentTarget,
                entity
        )) {

            moveToTarget(
                    entity,
                    currentTarget
            );

            return;
        }

        /*
         * Hedef artık geçerli değilse
         * yeni oyuncu bul.
         */
        ServerPlayer target =
                findNearestPlayer(entity);

        if (target == null) {

            entity.setTarget(null);
            entity.getNavigation().stop();

            FOLLOW_DATA.remove(
                    entity.getUUID()
            );

            return;
        }

        /*
         * Yeni hedef ata.
         */
        entity.setTarget(target);

        /*
         * İlk mesafeyi kaydet.
         */
        updateFollowData(
                entity,
                target
        );

        /*
         * Hedefe hareket.
         */
        moveToTarget(
                entity,
                target
        );
    }

    /*
     * =========================================================
     * DESTEKLENEN CREATURE'LAR
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
     * TAKİP MESAFESİNİ KAYDET
     * =========================================================
     */

    private static void updateFollowData(
            LivingEntity creature,
            ServerPlayer target
    ) {

        double offsetX =
                creature.getX() - target.getX();

        double offsetY =
                creature.getY() - target.getY();

        double offsetZ =
                creature.getZ() - target.getZ();

        FOLLOW_DATA.put(
                creature.getUUID(),
                new FollowData(
                        target.getUUID(),
                        offsetX,
                        offsetY,
                        offsetZ
                )
        );
    }

    /*
     * =========================================================
     * CREATURE'I OYUNCUYLA BERABER DIMENSION DEĞİŞTİR
     * =========================================================
     */

    private static void teleportCreatureWithPlayerOffset(
            LivingEntity creature,
            ServerPlayer target,
            FollowData data
    ) {

        if (!(creature.level()
                instanceof ServerLevel currentLevel)) {
            return;
        }

        ServerLevel targetLevel =
                target.serverLevel();

        /*
         * Oyuncunun yeni dimensiondaki konumu
         * + Creature'ın eski göreli mesafesi.
         */
        double destinationX =
                target.getX() + data.offsetX;

        double destinationY =
                target.getY() + data.offsetY;

        double destinationZ =
                target.getZ() + data.offsetZ;

        float yaw =
                creature.getYRot();

        /*
         * Dimension değişimi için özel teleporter.
         */
        ITeleporter teleporter =
                new ITeleporter() {

                    @Override
                    public Entity placeEntity(
                            Entity entity,
                            ServerLevel oldWorld,
                            ServerLevel newWorld,
                            float rotation,
                            Function<Boolean, Entity> repositionEntity
                    ) {

                        /*
                         * Vanilla'nın dimension entity
                         * oluşturma/taşıma işlemini yap.
                         *
                         * Portal oluşturmasını istemiyoruz.
                         */
                        Entity placed =
                                repositionEntity.apply(false);

                        /*
                         * Son konumu doğrudan
                         * oyuncunun göreli koordinatına ayarla.
                         */
                        placed.teleportTo(
                                destinationX,
                                destinationY,
                                destinationZ
                        );

                        placed.setYRot(yaw);
                        placed.setYHeadRot(yaw);

                        return placed;
                    }
                };

        /*
         * Creature'ı yeni dimensiona gönder.
         */
        Entity newEntity =
                creature.changeDimension(
                        targetLevel,
                        teleporter
                );

        /*
         * changeDimension yeni bir Entity
         * döndürebilir.
         */
        if (newEntity instanceof LivingEntity newCreature) {

            /*
             * Hedefi yeni entity üzerinde koru.
             */
            newCreature.setTarget(target);

            /*
             * Yeni UUID üzerinden mesafe kaydını
             * devam ettir.
             */
            FOLLOW_DATA.put(
                    newCreature.getUUID(),
                    new FollowData(
                            target.getUUID(),
                            data.offsetX,
                            data.offsetY,
                            data.offsetZ
                    )
            );

            /*
             * Eski entity kaydını temizle.
             */
            FOLLOW_DATA.remove(
                    creature.getUUID()
            );
        }
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
         * Farklı dimensiondaki hedefi normal AI
         * hedefi olarak kullanma.
         *
         * Dimension takip sistemi bunu
         * ayrıca hallediyor.
         */
        if (target.level() != creature.level()) {
            return false;
        }

        /*
         * Normal takip mesafesi.
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
             * Başka dimensiondaki oyuncular
             * normal hedef olarak seçilmez.
             */
            if (player.serverLevel()
                    != entity.level()) {

                continue;
            }

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
         * Creature2'nin mevcut speed'i 0 olduğu
         * için summon edildiğinde hareket
         * edebilmesini sağla.
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
         * Humanoid speed.
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
         * Hedefe git.
         */
        entity.getNavigation().moveTo(
                target,
                getMovementSpeed(entity)
        );

        /*
         * Oyuncuya bak.
         */
        entity.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );
    }

    /*
     * =========================================================
     * HAREKET HIZLARI
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

    /*
     * =========================================================
     * TAKİP VERİSİ
     * =========================================================
     */

    private static final class FollowData {

        private final UUID targetUUID;

        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;

        private FollowData(
                UUID targetUUID,
                double offsetX,
                double offsetY,
                double offsetZ
        ) {

            this.targetUUID = targetUUID;

            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
    }
}
