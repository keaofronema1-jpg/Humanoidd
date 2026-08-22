package com.humanoid.horror.core;

import com.humanoid.horror.entity.Humanoid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class StalkingEngine {

    // Çok uzaktaki oyuncular için ağır hesaplamalar yapılmasın (64 blok)
    private static final double MAX_STALKING_DISTANCE_SQR = 64.0 * 64.0;

    public StalkingEngine() {
    }

    /**
     * CANAVARIN HAKİKİ SIZMA VE OYUNCUYU DİKİZLEME MANTIĞI
     */
    public static void tickStalkingLogic(Humanoid entity) {
        if (entity == null || !entity.isAlive()) {
            return;
        }

        Level level = entity.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // PERFORMANS OPTİMİZASYONU:
        // Her 4 tick'te bir (saniyede 5 kez) çalıştırıyoruz.
        if (entity.tickCount % 4 != 0) {
            return;
        }

        List<ServerPlayer> players = serverLevel.players();
        if (players.isEmpty()) {
            return;
        }

        ServerPlayer targetPlayer = null;
        double closestDistance = MAX_STALKING_DISTANCE_SQR;

        for (ServerPlayer player : players) {
            if (player != null && player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                double dist = entity.distanceToSqr(player);
                if (dist < closestDistance) {
                    closestDistance = dist;
                    targetPlayer = player;
                }
            }
        }

        // Yakında geçerli bir oyuncu yoksa işlem yapma
        if (targetPlayer == null) {
            return;
        }

        // Önce bakış açısı (Dot Product), sonra görüş çizgisi kontrolü (Raycast)
        if (isPlayerLookingAt(targetPlayer, entity) && hasLineOfSight(serverLevel, targetPlayer, entity)) {
            scareAndVanish(serverLevel, targetPlayer, entity);
        }
    }

    /**
     * OYUNCU CANAVARI FARK ETTİĞİNDE: MEŞALELERİ SÖNDÜR VE ORTADAN KAYBOL
     */
    private static void scareAndVanish(ServerLevel level, ServerPlayer player, Humanoid entity) {
        BlockPos playerPos = player.blockPosition();
        int radius = 3;

        List<BlockPos> torchesToDestroy = new ArrayList<>();

        // Y ekseni aralığı: -1 ila +3
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -1, -radius),
                playerPos.offset(radius, 3, radius))) {

            BlockState state = level.getBlockState(pos);
            
            // Normal, Ruh, Kızıltaş ve Duvar Meşalelerinin tamamını kapsar
            if (state.getBlock() instanceof TorchBlock || 
                state.getBlock() instanceof WallTorchBlock ||
                state.getBlock() instanceof RedstoneTorchBlock ||
                state.getBlock() instanceof RedstoneWallTorchBlock) {
                torchesToDestroy.add(pos.immutable());
            }
        }

        // Meşaleleri kır, duman çıkar ve ateş sönme sesi çal
        for (BlockPos torchPos : torchesToDestroy) {
            level.destroyBlock(torchPos, false); // false = eşya düşürmeden kır
            level.sendParticles(ParticleTypes.SMOKE,
                    torchPos.getX() + 0.5D, torchPos.getY() + 0.5D, torchPos.getZ() + 0.5D,
                    5, 0.1D, 0.1D, 0.1D, 0.02D);
            
            level.playSound(null, torchPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.0F);
        }

        // Canavarın olduğu yerde sinsi gölge/duman efekti ve kaybolma sesi patlat
        Vec3 entityPos = entity.position();
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                entityPos.x, entityPos.y + 1.0D, entityPos.z,
                20, 0.3D, 0.5D, 0.3D, 0.05D);

        level.playSound(null, BlockPos.containing(entityPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);

        // Canavarı dünyadan güvenle sil
        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    /**
     * OYUNCUNUN BAKIŞ AÇISI (GERÇEK VECTOR DOT-PRODUCT) KONTROLÜ
     */
    private static boolean isPlayerLookingAt(Player player, Humanoid entity) {
        Vec3 playerEyePos = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle().normalize();

        Vec3 toEntityVector = new Vec3(
                entity.getX() - playerEyePos.x,
                entity.getEyeY() - playerEyePos.y,
                entity.getZ() - playerEyePos.z
        ).normalize();

        double dotProduct = lookVector.dot(toEntityVector);

        // 0.707D -> Yaklaşık 45 derecelik odaklanma açısı sağlar
        return dotProduct > 0.707D;
    }

    /**
     * OYUNCU İLE CANAVAR ARASINDA BLOK VAR MI KONTROLÜ (RAYCAST)
     * ClipContext.Block.VISUAL kullanılarak görüşü engelleyen görsel bloklar taranır.
     */
    private static boolean hasLineOfSight(Level level, Player player, Humanoid entity) {
        Vec3 startPos = player.getEyePosition(1.0F);

        // 1. Göz Seviyesi Kontrolü
        Vec3 eyePos = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        HitResult eyeResult = level.clip(new ClipContext(
                startPos, eyePos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player
        ));

        if (eyeResult.getType() == HitResult.Type.MISS) {
            return true;
        }

        // 2. Göğüs Seviyesi Kontrolü
        Vec3 chestPos = new Vec3(entity.getX(), entity.getY() + (entity.getBbHeight() * 0.5F), entity.getZ());
        HitResult chestResult = level.clip(new ClipContext(
                startPos, chestPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player
        ));

        return chestResult.getType() == HitResult.Type.MISS;
    }
}
