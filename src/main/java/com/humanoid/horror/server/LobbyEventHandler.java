package com.humanoid.horror.server;

import com.humanoid.horror.system.HorrorWorldData;

import net.minecraft.server.level.ServerPlayer;

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
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {

        /*
         * SADECE SERVER PLAYER
         */
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Burada artık:
         *
         * - WorldBorder değiştirilmez.
         * - Oyuncu spawn'a ışınlanmaz.
         * - Adventure yapılmaz.
         * - Oyuncunun konumu değiştirilmez.
         *
         * Böylece logout -> login sonrasında oyuncu
         * kaldığı yerde devam eder.
         */

        if (player.serverLevel() == null) {
            return;
        }

        /*
         * Dünya kilitli durumdaysa da hiçbir şey yapma.
         *
         * HorrorWorldData yalnızca server tarafında kullanılıyor.
         */
        HorrorWorldData data =
                HorrorWorldData.get(
                        player.serverLevel()
                );

        if (data != null && data.isLocked) {
            return;
        }

        /*
         * Oyun başlamışsa zaten hiçbir login işlemi
         * yapılmayacak.
         */
        return;
    }
}
