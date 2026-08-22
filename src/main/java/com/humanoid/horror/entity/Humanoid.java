package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.core.StalkingEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class Humanoid extends Monster {

    public boolean isWobbling;
    public int breakTickCounter;
    public BlockPos targetBlockPos;

    public Humanoid(
            EntityType<? extends Monster> entityType,
            Level level
    ) {
        super(entityType, level);

        this.isWobbling = false;
        this.breakTickCounter = 0;
        this.targetBlockPos = null;
    }

    /**
     * Humanoid entity attribute'ları.
     *
     * EntityAttributeRegister.java tarafından çağrılır.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public void tick() {
        super.tick();

        /*
         * /start KONTROLÜ
         *
         * Korku sistemi başlamadıysa Humanoid hareket etmez.
         */
        if (!HumanoidMod.isStartTriggered) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        /*
         * ANİMASYON:
         * Merdivendeyken yalpalama kapalı,
         * normal durumda hafif dönme hareketi.
         */
        if (this.onClimbable()) {
            this.isWobbling = false;
        } else {
            this.isWobbling = true;

            float currentYRot = this.getYRot();
            this.setYRot(currentYRot + 0.5F);
        }

        /*
         * Bundan sonraki mekanikler sadece server tarafında çalışır.
         */
        if (this.level().isClientSide) {
            return;
        }

        /*
         * 2. ve 3. gün sinsice takip sistemi.
         */
        if (HumanoidMod.currentDay > 1) {
            StalkingEngine.tickStalkingLogic(this);
        }

        /*
         * BLOK KIRMA SİSTEMİ
         *
         * Humanoid hedefi yoksa baktığı bloğu kontrol eder.
         */
        LivingEntity target = this.getTarget();

        if (target == null) {

            BlockPos pos =
                    this.blockPosition().relative(this.getDirection());

            /*
             * Farklı bir bloğa bakıyorsa sayaç sıfırlanır.
             */
            if (
                    this.targetBlockPos == null
                    || !this.targetBlockPos.equals(pos)
            ) {
                this.targetBlockPos = pos;
                this.breakTickCounter = 0;
            }

            BlockState blockState =
                    this.level().getBlockState(pos);

            /*
             * Bedrock veya hava ise kırma işlemi yapılmaz.
             */
            if (
                    blockState.getBlock() == Blocks.BEDROCK
                    || blockState.isAir()
            ) {
                this.breakTickCounter = 0;
                return;
            }

            this.breakTickCounter++;

            /*
             * Aynı bloğa 30 tick (~1.5 saniye)
             * bakarsa bloğu kırar.
             */
            if (this.breakTickCounter >= 30) {

                this.breakTickCounter = 0;

                this.level().destroyBlock(
                        pos,
                        true
                );
            }

        } else {

            /*
             * Hedef varsa blok kırma sistemi durur.
             */
            this.breakTickCounter = 0;
            this.targetBlockPos = null;
        }
    }

    /**
     * Humanoid oyuncuya vurduğunda jumpscare tetikler.
     */
    @Override
    public boolean doHurtTarget(Entity target) {

        boolean hurt = super.doHurtTarget(target);

        if (hurt && target instanceof Player) {
            executeJumpscareAttack();
        }

        return hurt;
    }

    /**
     * Server tarafından gönderilen Entity Event sinyallerini
     * client tarafında işler.
     */
    @Override
    public void handleEntityEvent(byte id) {

        if (id == 100) {

            /*
             * Dedicated Server'da client sınıflarının
             * yüklenmesini engeller.
             */
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () ->
                            com.humanoid.horror.core.JumpscareManager
                                    .triggerHorror()
            );

        } else {
            super.handleEntityEvent(id);
        }
    }

    /**
     * Oyuncuya jumpscare gönderir.
     */
    public void executeJumpscareAttack() {

        /*
         * Sadece server tarafında Entity Event gönderilir.
         */
        if (!this.level().isClientSide()) {

            this.level().broadcastEntityEvent(
                    this,
                    (byte) 100
            );
        }
    }
}
