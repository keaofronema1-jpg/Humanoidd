package com.humanoid.horror.entity.ai;

import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class Creature2Manager {

    private static final int COOLDOWN_TICKS = 24000; // 20 Dakika Cooldown
    private static int globalCooldown = 0;
    private static int nextSpawnTimer = new Random().nextInt(48000); // 40 dk içinde rastgele
    
    private static Creature2 activeCreature = null;
    private static ServerPlayer targetPlayer = null;
    
    private static boolean hasSentLookAtMe = false;
    private static boolean isJumpscareActive = false;
    private static int jumpscareTimer = 0;
    
    public static final SoundEvent CONT_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation("humanoid", "cont"));

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Global Cooldown Azaltma
        if (globalCooldown > 0) {
            globalCooldown--;
            return;
        }

        ServerLevel level = event.getServer().overworld();
        long dayCount = level.getDayTime() / 24000;

        // 1. ŞART: En az 3. Gün Olmalı
        if (dayCount < 3) return;

        // Tetiklenme zamanı takibi
        if (activeCreature == null && !isJumpscareActive) {
            if (--nextSpawnTimer <= 0) {
                spawnCreatureForRandomPlayer(level);
            }
        }

        // Aktif Yaratık Takibi ve Görüş Açısı Kontrolleri
        if (activeCreature != null && targetPlayer != null && !isJumpscareActive) {
            handleStalkingLogic();
        }

        // 24 Saniyelik Jumpscare Döngüsü
        if (isJumpscareActive && targetPlayer != null) {
            handleJumpscareSequence(level);
        }
    }

    private static void spawnCreatureForRandomPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(p -> p.isAlive() && !p.isSpectator());
        if (players.isEmpty()) return;

        targetPlayer = players.get(new Random().nextInt(players.size()));
        
        // Oyuncunun bakış yönünde uzak bir nokta bul (15-25 blok ötede)
        Vec3 lookAngle = targetPlayer.getLookAngle();
        Vec3 spawnVec = targetPlayer.position().add(lookAngle.scale(20.0D));
        BlockPos spawnPos = BlockPos.containing(spawnVec);

        activeCreature = new Creature2(ModEntities.CREATURE2.get(), level);
        activeCreature.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        
        // DOĞDUĞU AN KAFASI AŞIRI HIZLI 360 DERECE DÖNMEYE BAŞLAR
        activeCreature.setHeadSpinning(true);
        
        level.addFreshEntity(activeCreature);

        // Önüne Redstone Torch Koyma
        BlockPos torchPos = spawnPos.relative(activeCreature.getDirection());
        level.setBlock(torchPos, Blocks.REDSTONE_TORCH.defaultBlockState(), 3);

        hasSentLookAtMe = false;
    }

    private static void handleStalkingLogic() {
        Vec3 playerToCreature = activeCreature.position().subtract(targetPlayer.position()).normalize();
        Vec3 playerLook = targetPlayer.getLookAngle();

        double dot = playerLook.dot(playerToCreature); // Görüş Açısı Vektör Çarpımı

        // A. EKRANA GİRDİ Mİ? (Dot > 0.5 = Ekranın Görüş Alanı İçinde)
        if (dot > 0.5D && !hasSentLookAtMe) {
            targetPlayer.sendSystemMessage(Component.literal("<JavaObjectEntityNotFound>LookAtMe"));
            hasSentLookAtMe = true; // Tekrar spam yapmasın
        }

        // B. HITBOX'INA DİREKT BAKTI MI? (Baktığı an Jumpscare Döngüsü Başlar)
        if (dot > 0.98D) {
            startJumpscareSequence();
        }
    }

    private static void startJumpscareSequence() {
        isJumpscareActive = true;
        jumpscareTimer = 480; // 24 Saniye (24 * 20 tick = 480)

        // Ses Oynat (assets/humanoid/sounds/cont.ogg)
        targetPlayer.level().playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                CONT_SOUND, SoundSource.MASTER, 1.0F, 1.0F);

        // 24 Saniyelik Körlük Efekti
        targetPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 480, 0, false, false));

        if (activeCreature != null) {
            activeCreature.discard(); // Sahnedeki ilk uzaktaki yaratığı sil
        }
    }

    private static void handleJumpscareSequence(ServerLevel level) {
        jumpscareTimer--;

        // Her 4 Saniyede Bir (80 Tick) Oyuncunun 1 Blok Önünde Belir
        if (jumpscareTimer % 80 == 0 && jumpscareTimer > 0) {
            Vec3 frontPos = targetPlayer.position().add(targetPlayer.getLookAngle().scale(1.0D));
            
            Creature2 jumpscareEntity = new Creature2(ModEntities.CREATURE2.get(), level);
            jumpscareEntity.setPos(frontPos.x, frontPos.y, frontPos.z);
            jumpscareEntity.setHeadSpinning(true); // Kafası 360 derece dönmeye devam eder
            level.addFreshEntity(jumpscareEntity);

            // Çevredeki Meşaleleri ve Gamma/Işık Saçan Blokları Sil
            extinguishLightSources(level, targetPlayer.blockPosition(), 12);

            // 1.5 Saniye (30 Tick) Sonra Kaybolma
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 30, () -> {
                jumpscareEntity.discard();
            }));
        }

        // 24 Saniye Bittiğinde Temizlik ve Cooldown Reset
        if (jumpscareTimer <= 0) {
            isJumpscareActive = false;
            activeCreature = null;
            targetPlayer = null;
            globalCooldown = COOLDOWN_TICKS; // 20 dakika Cooldown başlat
            nextSpawnTimer = new Random().nextInt(48000); // Yeni 40 dk zamanlayıcı
        }
    }

    // YANINDAKİ TÜM MEŞALELERİ VE IŞIK BLOKLARINI AIR İLE DEĞİŞTİRİR
    private static void extinguishLightSources(ServerLevel level, BlockPos center, int radius) {
        BlockPos.betweenClosedStream(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))
                .forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    // Işık yayan her bloğu (Meşale, Fener, Işık Taşı vb.) AIR yap
                    if (state.getLightEmission(level, pos) > 0 || state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.REDSTONE_TORCH)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                });
    }

    public static boolean isJumpscareActiveForHUD() {
        return isJumpscareActive;
    }
}
