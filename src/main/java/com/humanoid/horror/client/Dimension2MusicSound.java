package com.humanoid.horror.client;

import com.humanoid.horror.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class Dimension2MusicSound extends AbstractTickableSoundInstance {

    public Dimension2MusicSound() {
        super(
                ModSounds.DIMENSION2_MUSIC.get(),
                SoundSource.MUSIC,
                RandomSource.create()
        );

        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
    }

    @Override
    public void tick() {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null ||
            minecraft.level == null) {

            this.stop();
            return;
        }

        if (!minecraft.level.dimension()
                .location()
                .equals(Dimension2Client.DIMENSION2)) {

            this.stop();
        }
    }
}
