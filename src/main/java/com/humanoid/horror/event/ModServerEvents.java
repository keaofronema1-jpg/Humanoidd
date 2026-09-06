package com.humanoid.horror.event;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.check.Check;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ModServerEvents {

    /*
     * Sunucu ilk açıldığında başlangıç prison sistemi
     * yalnızca burada kurulacak.
     *
     * Oyuncu logout/login yaptığında tekrar kurulmayacak.
     */
    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {

        Check.setupInitialPrison(
                event.getServer()
        );
    }
}
