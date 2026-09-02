// ClientJumpscareHandler.java  (bu dosya yalnızca client-side referanslar içerir)
package com.humanoid.horror.client;

import com.humanoid.horror.ClientHUDOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientJumpscareHandler {
    public static void trigger() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            mc.level.playSound(
                mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                SoundEvent.createVariableRangeEvent(new ResourceLocation("humanoid", "scare")),
                SoundSource.HOSTILE, 1.0F, 1.0F
            );
            mc.level.playSound(
                mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                SoundEvent.createVariableRangeEvent(new ResourceLocation("humanoid", "wep")),
                SoundSource.PLAYERS, 1.0F, 1.0F
            );
            ClientHUDOverlay.jumpscareBaslat();
        }
    }
}
