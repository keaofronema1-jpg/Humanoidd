package com.humanoid.horror.entity;

import com.humanoid.horror.entity.ai.Creature3ChaseGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Creature3 extends Monster {

    public Creature3(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 1. Öncelikli Hedef: Özel Kovalama Yapay Zekası
        this.goalSelector.addGoal(1, new Creature3ChaseGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void capturePlayer(LivingEntity target) {
        if (target == null || this.level().isClientSide()) return;
        
        // Oyuncuyu yakaladığında mob saldırı hasarı verir
        target.hurt(this.damageSources().mobAttack(this), 10.0F);
    }
}
