package com.humanoid.horror.client;

import com.humanoid.horror.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class RandomSoundClient {

    private RandomSoundClient() {
    }

    public static void play() {

        Minecraft minecraft =
                Minecraft.getInstance();

        LocalPlayer player =
                minecraft.player;

        if (player == null) {
            return;
        }

        player.playSound(
                ModSounds.RANDOM.get(),
                1.0F,
                1.0F
        );
    }
}
