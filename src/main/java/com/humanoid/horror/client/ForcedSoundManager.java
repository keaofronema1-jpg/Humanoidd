package com.humanoid.horror.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ForcedSoundManager {

    private static int tickCounter = 0;

    private ForcedSoundManager() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {

        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        tickCounter++;

        /*
         * Her 2 saniyede bir ses seviyelerini kontrol et.
         */
        if (tickCounter < 40) {
            return;
        }

        tickCounter = 0;

        Options options = minecraft.options;

        /*
         * Müzik %100
         */
        options.getSoundSourceOptionInstance(
                SoundSource.MUSIC
        ).set(1.0D);

        /*
         * Diğer sesler %100
         */
        options.getSoundSourceOptionInstance(
                SoundSource.AMBIENT
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.BLOCKS
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.HOSTILE
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.NEUTRAL
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.PLAYERS
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.RECORDS
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.VOICE
        ).set(1.0D);

        options.getSoundSourceOptionInstance(
                SoundSource.WEATHER
        ).set(1.0D);
    }
}
