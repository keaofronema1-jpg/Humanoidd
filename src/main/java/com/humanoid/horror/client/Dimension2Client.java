package com.humanoid.horror.client;

import com.humanoid.horror.network.HumanoidNetwork;
import com.humanoid.horror.network.Dimension2Packet;
import com.humanoid.horror.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class Dimension2Client {

    public static final ResourceLocation DIMENSION1 =
            new ResourceLocation(
                    "humanoid",
                    "humanoid_dimension"
            );

    public static final ResourceLocation DIMENSION2 =
            DIMENSION1;

    private static Dimension2MusicSound musicSound;

    private static SoundInstance secondMusic;

    private static boolean secondMusicPlaying = false;

    private static boolean firstMusicFinished = false;

    @SubscribeEvent
    public static void clientTick(
            TickEvent.ClientTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null ||
            minecraft.level == null) {

            stopAllMusic();
            return;
        }

        boolean inDimension =
                minecraft.level.dimension()
                        .location()
                        .equals(DIMENSION1);

        if (!inDimension) {

            stopAllMusic();
            return;
        }

        /*
         * İLK MÜZİK
         */
        if (!firstMusicFinished) {

            if (musicSound == null) {

                startMainMusic();
                return;
            }

            /*
             * looping = false olduğu için
             * SoundManager artık aktif değilse
             * ilk müzik gerçekten bitmiştir.
             */
            if (!minecraft.getSoundManager()
                    .isActive(musicSound)) {

                musicSound = null;
                firstMusicFinished = true;

                startSecondMusic();
            }

            return;
        }

        /*
         * İKİNCİ MÜZİK
         */
        if (secondMusicPlaying &&
            secondMusic != null) {

            if (!minecraft.getSoundManager()
                    .isActive(secondMusic)) {

                secondMusic = null;
                secondMusicPlaying = false;

                /*
                 * İkinci müzik gerçekten bitti.
                 * Server'a bildir.
                 */
                HumanoidNetwork.CHANNEL.sendToServer(
                        new Dimension2Packet(
                                Dimension2Packet.Action.MUSIC_FINISHED
                        )
                );
            }
        }
    }

    public static void startMainMusic() {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (musicSound != null) {
            return;
        }

        if (secondMusicPlaying) {
            return;
        }

        firstMusicFinished = false;

        musicSound =
                new Dimension2MusicSound();

        minecraft.getSoundManager()
                .play(musicSound);
    }

    private static void startSecondMusic() {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (secondMusicPlaying) {
            return;
        }

        secondMusic =
                SimpleSoundInstance.forUI(
                        ModSounds.DIMENSION2_MUSIC2.get(),
                        1.0F
                );

        secondMusicPlaying = true;

        minecraft.getSoundManager()
                .play(secondMusic);
    }

    public static void stopAllMusic() {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (musicSound != null) {

            minecraft.getSoundManager()
                    .stop(musicSound);

            musicSound = null;
        }

        if (secondMusic != null) {

            minecraft.getSoundManager()
                    .stop(secondMusic);

            secondMusic = null;
        }

        secondMusicPlaying = false;
        firstMusicFinished = false;
    }

    /*
     * Eski event sistemi için korunuyor.
     */
    public static void startEventSound() {

        startSecondMusic();
    }

    public static void endEvent() {

        stopAllMusic();
    }

    @SubscribeEvent
    public static void onFogRender(
            ViewportEvent.RenderFog event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (!minecraft.level.dimension()
                .location()
                .equals(DIMENSION1)) {

            return;
        }

        event.setNearPlaneDistance(2.0F);
        event.setFarPlaneDistance(24.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(
            ViewportEvent.ComputeFogColor event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (!minecraft.level.dimension()
                .location()
                .equals(DIMENSION1)) {

            return;
        }

        event.setRed(0.005F);
        event.setGreen(0.005F);
        event.setBlue(0.005F);
    }

    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {

        stopAllMusic();
    }
}
