package com.humanoid.horror.server;

import com.humanoid.horror.system.HorrorWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class LobbyEventHandler {

    public LobbyEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        Player player = event.getEntity();

        // Dedicated Server tarafında yalnızca ServerPlayer ile çalış.
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();

        /*
         * PCKeyHandler CLIENT-ONLY olduğu için burada kesinlikle
         * doğrudan kullanılmıyor.
         *
         * Kilit durumu artık ortak/server-safe HorrorWorldData
         * üzerinden okunuyor.
         */
        HorrorWorldData data = HorrorWorldData.get(serverLevel);

        // Sistem zaten kilitlenip oyun başladıysa
        // oyuncunun lobby sistemi tekrar kurulmasın.
        if (data.isLocked) {
            return;
        }

        // Oyuncuyu Adventure moduna al.
        serverPlayer.setGameMode(GameType.ADVENTURE);

        // Lobby sınırını 16 blok yap.
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        worldBorder.setSize(16.0D);

        // Server'ın ortak spawn noktasını merkez olarak kullan.
        BlockPos spawnPos = serverLevel.getSharedSpawnPos();

        worldBorder.setCenter(
                spawnPos.getX() + 0.5D,
                spawnPos.getZ() + 0.5D
        );

        // Oyuncuyu lobby spawn noktasına ışınla.
        serverPlayer.teleportTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D
        );
    }
}
