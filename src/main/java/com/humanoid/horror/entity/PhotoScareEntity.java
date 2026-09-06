package com.humanoid.horror.entity;

import com.humanoid.horror.registry.ModEntities;
import com.humanoid.horror.registry.ModSounds;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PhotoScareEntity extends Entity {

    private static final double LOOK_DISTANCE = 64.0D;

    private static final int BLINDNESS_TICKS = 100;

    private boolean triggered = false;

    public PhotoScareEntity(
            EntityType<? extends PhotoScareEntity> type,
            ServerLevel level
    ) {
        super(type, level);

        this.noPhysics = true;
    }

    public PhotoScareEntity(
            EntityType<? extends PhotoScareEntity> type,
            net.minecraft.world.level.Level level
    ) {
        super(type, level);

        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.nbt.CompoundTag tag
    ) {
    }

    @Override
    protected void addAdditionalSaveData(
            net.minecraft.nbt.CompoundTag tag
    ) {
    }

    @Override
    public void tick() {

        super.tick();

        /*
         * Photo scare server tarafından kontrol edilir.
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
         * Dünyadaki oyuncuların tamamını kontrol et.
         *
         * Burada tek bir target yok.
         * Kim bakarsa o tetikler.
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
             * Çok uzaktaki oyuncular kontrol edilmez.
             */
            if (distanceToSqr(serverPlayer)
                    > LOOK_DISTANCE * LOOK_DISTANCE) {
                continue;
            }

            /*
             * Oyuncu gerçekten entity'ye bakıyor mu?
             */
            if (isPlayerLookingAtHitbox(serverPlayer)) {

                triggerScare(serverPlayer);

                /*
                 * İlk bakan oyuncu tetiklediği anda
                 * entity biter.
                 */
                break;
            }
        }
    }

    /*
     * =========================================================
     * OYUNCUNUN HITBOX'A BAKIP BAKMADIĞINI KONTROL ET
     * =========================================================
     */

    private boolean isPlayerLookingAtHitbox(
            ServerPlayer player
    ) {

        Vec3 eyePosition =
                player.getEyePosition(1.0F);

        Vec3 lookVector =
                player.getViewVector(1.0F).normalize();

        /*
         * Oyuncunun bakış ışınını uzat.
         */
        Vec3 rayEnd =
                eyePosition.add(
                        lookVector.scale(LOOK_DISTANCE)
                );

        /*
         * Entity'nin gerçek hitbox'ı.
         */
        AABB hitbox =
                getBoundingBox();

        /*
         * Ray ile hitbox kesişiyor mu?
         *
         * Bu sayede oyuncunun kamerayı gerçekten
         * entity'nin üzerine getirmesi gerekir.
         */
        return hitbox.clip(
                eyePosition,
                rayEnd
        ).isPresent();
    }

    /*
     * =========================================================
     * SCARE
     * =========================================================
     */

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
         * entity.ogg sesi.
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
         * Entity anında kaybolur.
         */
        discard();
    }

    /*
     * =========================================================
     * HITBOX
     * =========================================================
     *
     * Photo scare görüntüsünün hitbox'ını burada belirliyoruz.
     */

    @Override
    public AABB getBoundingBox() {

        return new AABB(
                getX() - 0.5D,
                getY(),
                getZ() - 0.5D,

                getX() + 0.5D,
                getY() + 2.0D,
                getZ() + 0.5D
        );
    }

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
