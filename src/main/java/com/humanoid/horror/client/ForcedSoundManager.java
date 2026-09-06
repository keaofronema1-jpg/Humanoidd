package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ForcedSoundManager {

    // 2 saniye = 40 tick
    private static final int RESTORE_DELAY_TICKS = 40;

    private static int musicRestoreTimer = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {

        if (event.phase != ClientTickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            musicRestoreTimer = 0;
            return;
        }

        // Korku sistemi başlamadıysa hiçbir şeyi zorlamıyoruz.
        if (!HorrorClientManager.isHorrorActive) {
            musicRestoreTimer = 0;
            return;
        }

        Options options = minecraft.options;

        /*
         * MÜZİK
         *
         * Oyuncu müziği kapatırsa hemen açmıyoruz.
         * 2 saniye sonra tekrar %100 yapıyoruz.
         */
        double musicVolume = options.getSoundSourceVolume(
                net.minecraft.sounds.SoundSource.MUSIC
        );

        if (musicVolume <= 0.0D) {

            musicRestoreTimer++;

            if (musicRestoreTimer >= RESTORE_DELAY_TICKS) {
                options.getSoundSourceOptionInstance(
                        net.minecraft.sounds.SoundSource.MUSIC
                ).set(1.0D);

                musicRestoreTimer = 0;
            }

        } else {
            musicRestoreTimer = 0;

            // Müzik açıksa sürekli %100'de tut.
            options.getSoundSourceOptionInstance(
                    net.minecraft.sounds.SoundSource.MUSIC
            ).set(1.0D);
        }

        /*
         * DİĞER TÜM SESLER
         *
         * Hostile, player, ambient, block vb.
         * ses kategorilerini %100 tutuyoruz.
         *
         * MASTER hariç.
         */
        for (net.minecraft.sounds.SoundSource source :
                net.minecraft.sounds.SoundSource.values()) {

            if (source == net.minecraft.sounds.SoundSource.MASTER) {
                continue;
            }

            if (source == net.minecraft.sounds.SoundSource.MUSIC) {
                continue;
            }

            options.getSoundSourceOptionInstance(source).set(1.0D);
        }
    }
}
