package com.humanoid.horror.entity;

import com.humanoid.horror.registry.ModSounds;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PhotoScareEntity extends Entity {

    private static final double LOOK_DISTANCE = 64.0D;

    private static final int BLINDNESS_TICKS = 100;

    /*
     * Entity boyutu.
     *
     * 1.0 = normal
     * 1.25 = %25 daha büyük
     */
    private static final float ENTITY_SCALE = 1.25F;

    private boolean triggered = false;

    public PhotoScareEntity(
            EntityType<? extends PhotoScareEntity> type,
            Level level
    ) {
        super(type, level);

        this.noPhysics = true;
    }

    // =========================================================
    // ENTITY SIZE
    // =========================================================

    @Override
    public EntityDimensions getDimensions(
            Pose pose
    ) {
        return super.getDimensions(pose)
                .scale(ENTITY_SCALE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(
            CompoundTag tag
    ) {
    }

    @Override
    protected void addAdditionalSaveData(
            CompoundTag tag
    ) {
    }

    @Override
    public void tick() {

        super.tick();

        /*
         * Photo scare server tarafında kontrol edilir.
         */
        if (level().isClientSide()) {
            return;
        }

        if (triggered) {
            return;
        }

        if (!isAlive()) {
            return;
        }

        /*
         * Dünyadaki bütün oyuncuları kontrol et.
         */
        for (Player player : level().players()) {

            if (!(player instanceof ServerPlayer serverPlayer)) {
                continue;
            }

            if (!serverPlayer.isAlive()) {
                continue;
            }

            if (serverPlayer.isSpectator()) {
                continue;
            }

            /*
             * 64 bloktan uzaktaki oyuncular kontrol edilmez.
             */
            if (distanceToSqr(serverPlayer)
                    > LOOK_DISTANCE * LOOK_DISTANCE) {
                continue;
            }

            /*
             * Oyuncunun bakış ışını hitbox'a ulaşıyor mu?
             */
            if (isPlayerLookingAtHitbox(serverPlayer)) {

                triggerScare(serverPlayer);

                break;
            }
        }
    }

    // =========================================================
    // HITBOX BAKIŞ KONTROLÜ
    // =========================================================

    private boolean isPlayerLookingAtHitbox(
            ServerPlayer player
    ) {

        Vec3 eyePosition =
                player.getEyePosition(1.0F);

        Vec3 lookVector =
                player.getViewVector(1.0F).normalize();

        Vec3 rayEnd =
                eyePosition.add(
                        lookVector.scale(LOOK_DISTANCE)
                );

        /*
         * Artık %25 daha büyük entity boyutuna göre
         * oluşturulan hitbox kullanılır.
         */
        AABB hitbox = getBoundingBox();

        return hitbox.clip(
                eyePosition,
                rayEnd
        ).isPresent();
    }

    // =========================================================
    // SCARE
    // =========================================================

    private void triggerScare(
            ServerPlayer player
    ) {

        if (triggered) {
            return;
        }

        triggered = true;

        /*
         * 5 saniye Blindness.
         */
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        BLINDNESS_TICKS,
                        0,
                        false,
                        false,
                        true
                )
        );

        /*
         * entity.ogg
         */
        if (ModSounds.PHOTO_ENTITY.isPresent()) {

            level().playSound(
                    null,
                    blockPosition(),
                    ModSounds.PHOTO_ENTITY.get(),
                    SoundSource.HOSTILE,
                    1.0F,
                    1.0F
            );
        }

        /*
         * Entity anında yok olur.
         */
        discard();
    }

    // =========================================================
    // ENTITY DAVRANIŞI
    // =========================================================

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(
            net.minecraft.world.damagesource.DamageSource source
    ) {
        return true;
    }
}
