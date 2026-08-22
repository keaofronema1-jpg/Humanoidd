package com.humanoid.horror.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Creature2 extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> HEAD_SPINNING = SynchedEntityData.defineId(Creature2.class, EntityDataSerializers.BOOLEAN);
    public float spinningHeadYaw = 0.0F;

    public Creature2(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true); // Oyuncu vuramaz ve hasar veremez
    }

    @Override
    protected void defineSynchedData() {
        // 1.20.1 Forge uyumu için super çağrısı kaldırıldı
        this.entityData.define(HEAD_SPINNING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D); // Tamamen sabittir, yürümez
    }

    @Override
    public void tick() {
        super.tick();

        // 360 Derece Kafa Dönme Animasyon Mantığı
        if (this.isHeadSpinning()) {
            this.spinningHeadYaw += 25.0F;
            if (this.spinningHeadYaw >= 360.0F) {
                this.spinningHeadYaw -= 360.0F;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // Hiçbir şekilde hasar almaz
    }

    public void setHeadSpinning(boolean spinning) {
        this.entityData.set(HEAD_SPINNING, spinning);
    }

    public boolean isHeadSpinning() {
        return this.entityData.get(HEAD_SPINNING);
    }
}
