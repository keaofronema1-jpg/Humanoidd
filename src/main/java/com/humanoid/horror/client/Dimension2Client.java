package com.humanoid.horror.client;

import com.humanoid.horror.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
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

    public static final ResourceLocation DIMENSION2 =
            new ResourceLocation(
                    "humanoid",
                    "dimension2"
            );

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

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null ||
            minecraft.level == null) {

            stopMainMusic();
            return;
        }

        boolean inDimension2 =
                minecraft.level.dimension()
                        .location()
                        .equals(DIMENSION2);

        if (!inDimension2) {

            if (!eventPlaying) {
                stopMainMusic();
            }

            return;
        }

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

        Minecraft minecraft = Minecraft.getInstance();

        musicSound = new Dimension2MusicSound();

        minecraft.getSoundManager().play(musicSound);
    }

    public static void stopMainMusic() {

        if (musicSound == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        minecraft.getSoundManager().stop(musicSound);

        musicSound = null;
    }

    public static void startEventSound() {

        Minecraft minecraft = Minecraft.getInstance();

        eventPlaying = true;

        stopMainMusic();

        eventSound = SimpleSoundInstance.forUI(
                ModSounds.DIMENSION2_MUSIC2.get(),
                1.0F
        );

        minecraft.getSoundManager().play(eventSound);
    }

    public static void endEvent() {

        Minecraft minecraft = Minecraft.getInstance();

        if (eventSound != null) {
            minecraft.getSoundManager().stop(eventSound);
            eventSound = null;
        }

        eventPlaying = false;
    }

    @SubscribeEvent
    public static void onLogout(
            ClientPlayerNetworkEvent.LoggingOut event
    ) {

        stopMainMusic();

        Minecraft minecraft = Minecraft.getInstance();

        if (eventSound != null) {
            minecraft.getSoundManager().stop(eventSound);
            eventSound = null;
        }

        eventPlaying = false;
    }
}
