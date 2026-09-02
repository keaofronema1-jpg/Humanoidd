package com.humanoid.horror.replacement;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

@Mod.EventBusSubscriber
public class ReplacementAI {

    private static final double DETECTION_RANGE = 16.0D;
    private static final double ATTACK_RANGE = 5.0D;
    private static final double MOVE_SPEED = 0.40D;

    private static final int ATTACK_COOLDOWN = 1;

    private static int attackTimer = 0;

    private static Player currentTarget;

    private ReplacementAI() {
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        FakePlayer replacement =
                ReplacementEntity.get();

        if (replacement == null
                || !ReplacementEntity.isActive()) {

            currentTarget = null;
            attackTimer = 0;
            return;
        }

        if (attackTimer > 0) {
            attackTimer--;
        }

        if (!isValidTarget(
                replacement,
                currentTarget
        )) {

            currentTarget =
                    findNearestPlayer(replacement);
        }

        if (currentTarget == null) {
            stop(replacement);
            return;
        }

        lookAt(
                replacement,
                currentTarget
        );

        double distance =
                replacement.distanceTo(
                        currentTarget
                );

        /*
         * 4 blok içine girdiyse saldır.
         */
        if (distance <= ATTACK_RANGE) {

            stop(replacement);

            if (!replacement.hasLineOfSight(
                    currentTarget
            )) {
                return;
            }

            attack(
                    replacement,
                    currentTarget
            );

            return;
        }

        /*
         * Hedef uzaktaysa takip et.
         */
        moveTo(
                replacement,
                currentTarget
        );
    }

    private static Player findNearestPlayer(
            FakePlayer replacement
    ) {

        return replacement.serverLevel()
                .players()
                .stream()

                .filter(player ->
                        player != replacement
                )

                .filter(player ->
                        !(player instanceof FakePlayer)
                )

                .filter(Player::isAlive)

                .filter(player ->
                        !player.isSpectator()
                )

                .filter(player ->
                        replacement.distanceToSqr(player)
                                <= DETECTION_RANGE
                                * DETECTION_RANGE
                )

                .min(
                        Comparator.comparingDouble(
                                replacement::distanceToSqr
                        )
                )

                .orElse(null);
    }

    private static boolean isValidTarget(
            FakePlayer replacement,
            Player target
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

        if (target instanceof FakePlayer) {
            return false;
        }

        if (target.level()
                != replacement.level()) {

            return false;
        }

        return replacement.distanceToSqr(target)
                <= DETECTION_RANGE
                * DETECTION_RANGE;
    }

    private static void lookAt(
            FakePlayer replacement,
            LivingEntity target
    ) {

        double dx =
                target.getX()
                        - replacement.getX();

        double dz =
                target.getZ()
                        - replacement.getZ();

        double horizontal =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        if (horizontal < 0.001D) {
            return;
        }

        double dy =
                target.getY()
                        + target.getEyeHeight()
                        - replacement.getY()
                        - replacement.getEyeHeight();

        float yaw =
                (float) (
                        Math.toDegrees(
                                Math.atan2(
                                        dz,
                                        dx
                                )
                        )
                                - 90.0D
                );

        float pitch =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(
                                        dy,
                                        horizontal
                                )
                        )
                );

        replacement.setYRot(yaw);

        replacement.setYHeadRot(yaw);

        replacement.setXRot(pitch);
    }

    private static void moveTo(
            FakePlayer replacement,
            LivingEntity target
    ) {

        double dx =
                target.getX()
                        - replacement.getX();

        double dz =
                target.getZ()
                        - replacement.getZ();

        double distance =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        if (distance < 0.001D) {
            return;
        }

        double x =
                dx / distance
                        * MOVE_SPEED;

        double z =
                dz / distance
                        * MOVE_SPEED;

        replacement.setDeltaMovement(
                x,
                replacement.getDeltaMovement().y,
                z
        );

        replacement.hasImpulse = true;
    }

    private static void stop(
            FakePlayer replacement
    ) {

        replacement.setDeltaMovement(
                0.0D,
                replacement.getDeltaMovement().y,
                0.0D
        );

        replacement.hasImpulse = true;
    }

    private static void attack(
            FakePlayer replacement,
            LivingEntity target
    ) {

        if (attackTimer > 0) {
            return;
        }

        if (!target.isAlive()) {
            currentTarget = null;
            return;
        }

        if (replacement.distanceTo(target)
                > ATTACK_RANGE) {

            return;
        }

        if (!replacement.hasLineOfSight(target)) {
            return;
        }

        replacement.attack(target);

        replacement.resetAttackStrengthTicker();

        attackTimer = ATTACK_COOLDOWN;
    }

    public static void reset() {

        currentTarget = null;

        attackTimer = 0;
    }
}
