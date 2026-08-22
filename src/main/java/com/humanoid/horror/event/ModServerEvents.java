package com.humanoid.horror.event;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.check.Check;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HumanoidMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModServerEvents {

    // Sunucu tamamen açıldığında WorldBorder ayarını yapar
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Check.setupInitialPrison(event.getServer());
    }

    // Oyuncu sunucuya katıldığında doğru konumda başlatılmasını garanti eder
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!HumanoidMod.isStartTriggered && event.getEntity().getServer() != null) {
            Check.setupInitialPrison(event.getEntity().getServer());
        }
    }
}
