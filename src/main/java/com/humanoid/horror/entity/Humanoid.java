package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.core.StalkingEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

    public Humanoid(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.isWobbling = false;
        this.breakTickCounter = 0;
        this.targetBlockPos = null;
    }

    @Override
    public void tick() {
        super.tick();

        // /start KONTROLÜ: Eğer oyun tetiklenmediyse dondur
        if (!HumanoidMod.isStartTriggered) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        // ---- ANIMASYON: MERDİVEN VE YALPALAMA ----
        if (this.onClimbable()) {
            this.isWobbling = false;
        } else {
            this.isWobbling = true;
            float currentYRot = this.getYRot();
            this.setYRot(currentYRot + 0.5F);
        }

        // Sadece Sunucu Tarafında Çalışacak Mantıklar:
        if (this.level().isClientSide) return;

        // ---- MEKANİK: 2. VE 3. GÜN SİNSİ TAKİP ----
        if (HumanoidMod.currentDay > 1) {
            StalkingEngine.tickStalkingLogic(this);
        }

        // ---- MEKANİK: BLOK KIRMA ----
        LivingEntity target = this.getTarget();
        
        if (target == null) {
            BlockPos pos = this.blockPosition().relative(this.getDirection());

            // Baktığı pozisyon değiştiyse sayacı sıfırla (Farklı blokları anında kırma bug'ını çözer)
            if (this.targetBlockPos == null || !this.targetBlockPos.equals(pos)) {
                this.targetBlockPos = pos;
                this.breakTickCounter = 0;
            }

            BlockState blockState = this.level().getBlockState(pos);
            
            if (blockState.getBlock() == Blocks.BEDROCK || blockState.isAir()) {
                this.breakTickCounter = 0;
                return;
            }

            this.breakTickCounter++;
            if (this.breakTickCounter >= 30) { // 1.5 saniye (30 tick) boyunca aynı bloğa bakarsa kırar
                this.breakTickCounter = 0;
                this.level().destroyBlock(pos, true);
            }
        } else {
            this.breakTickCounter = 0;
            this.targetBlockPos = null;
        }
    }

    /**
     * CANAVAR OYUNCUYA VURDUĞUNDA JUMPSCARE PATLATIR
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof Player) {
            executeJumpscareAttack(); // Oyuncuya hasar verince sinyali çak!
        }
        return hurt;
    }

    /**
     * Sunucudan gelen Entity Event sinyallerini dinleyen metod (Client tarafı çalıştırır)
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 100) {
            // Dedicated Server çökmesini önlemek için istemci tarafında güvenle çalıştırıyoruz
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.humanoid.horror.core.JumpscareManager.triggerHorror());
        } else {
            super.handleEntityEvent(id);
        }
    }

    /**
     * Oyuncuya jumpscare atmak istediğin yerde çağıracağın tetikleyici metod
     */
    public void executeJumpscareAttack() {
        if (!this.level().isClientSide()) {
            // Sunucu tarafında: Yakındaki oyunculara 100 numaralı jumpscare sinyalini gönder
            this.level().broadcastEntityEvent(this, (byte) 100);
        }
    }
}
