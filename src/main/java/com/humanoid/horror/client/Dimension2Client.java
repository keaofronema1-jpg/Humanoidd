package com.humanoid.horror.client;

import com.humanoid.horror.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class Dimension2Client {

    /*
     * Dimension1:
     * humanoid:humanoid_dimension
     */
    public static final ResourceLocation DIMENSION1 =
            new ResourceLocation(
                    "humanoid",
                    "humanoid_dimension"
            );

    /*
     * Dimension2MusicSound.java hâlâ DIMENSION2
     * ismini kullandığı için uyumluluk amacıyla
     * bu referansı koruyoruz.
     *
     * Artık DIMENSION2 de Dimension1'i gösteriyor.
     */
    public static final ResourceLocation DIMENSION2 =
            DIMENSION1;

    private static Dimension2MusicSound musicSound;

    private static SoundInstance eventSound;

    private static boolean eventPlaying = false;

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

            stopMainMusic();
            return;
        }

        boolean inDimension1 =
                minecraft.level.dimension()
                        .location()
                        .equals(DIMENSION1);

        /*
         * Dimension1'de değilsek müziği durdur.
         */
        if (!inDimension1) {

            if (!eventPlaying) {
                stopMainMusic();
            }

            return;
        }

        /*
         * Dimension1'e girince müziği başlat.
         */
        if (!eventPlaying &&
            musicSound == null) {

            startMainMusic();
        }
    }

    public static void startMainMusic() {

        if (eventPlaying) {
            return;
        }

        if (musicSound != null) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        musicSound =
                new Dimension2MusicSound();

        minecraft.getSoundManager()
                .play(musicSound);
    }

    public static void stopMainMusic() {

        if (musicSound == null) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.getSoundManager()
                .stop(musicSound);

        musicSound = null;
    }

    /*
     * Mevcut event sistemi korunuyor.
     */
    public static void startEventSound() {

        Minecraft minecraft =
                Minecraft.getInstance();

        eventPlaying = true;

        stopMainMusic();

        eventSound =
                SimpleSoundInstance.forUI(
                        ModSounds.DIMENSION2_MUSIC2.get(),
                        1.0F
                );

        minecraft.getSoundManager()
                .play(eventSound);
    }

    public static void endEvent() {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (eventSound != null) {

            minecraft.getSoundManager()
                    .stop(eventSound);

            eventSound = null;
        }

        eventPlaying = false;
    }

    /*
     * =========================================================
     * DIMENSION1 SİS
     * =========================================================
     *
     * Görüş yaklaşık 24 blok.
     */
    @SubscribeEvent
    public static void onFogRender(
            ViewportEvent.RenderFog event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        boolean inDimension1 =
                minecraft.level.dimension()
                        .location()
                        .equals(DIMENSION1);

        if (!inDimension1) {
            return;
        }

        event.setNearPlaneDistance(2.0F);

        event.setFarPlaneDistance(24.0F);

        event.setCanceled(true);
    }

    /*
     * =========================================================
     * SİS RENGİ
     * =========================================================
     */
    @SubscribeEvent
    public static void onFogColor(
            ViewportEvent.ComputeFogColor event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        boolean inDimension1 =
                minecraft.level.dimension()
                        .location()
                        .equals(DIMENSION1);

        if (!inDimension1) {
            return;
        }

        event.setRed(0.005F);
        event.setGreen(0.005F);
        event.setBlue(0.005F);
    }

    /*
     * =========================================================
     * ÇIKIŞ / LOGOUT
     * =========================================================
     */
    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {

        stopMainMusic();

        Minecraft minecraft =
                Minecraft.getInstance();

        if (eventSound != null) {

            minecraft.getSoundManager()
                    .stop(eventSound);

            eventSound = null;
        }

        eventPlaying = false;
    }
}
