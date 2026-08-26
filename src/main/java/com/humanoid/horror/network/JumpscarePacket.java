package com.humanoid.horror.network;

import com.humanoid.horror.ClientHUDOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JumpscarePacket {

    public JumpscarePacket() {
    }

    public JumpscarePacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(
            JumpscarePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {
        ctx.get().enqueueWork(() -> {
            // Sunucunun patlamaması için sadece Client'ta çalışmasını sağlıyoruz
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player != null && mc.level != null) {

                    mc.level.playSound(
                            mc.player,
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            SoundEvent.createVariableRangeEvent(
                                    new ResourceLocation("humanoid", "scare")
                            ),
                            SoundSource.HOSTILE,
                            1.0F,
                            1.0F
                    );

                    mc.level.playSound(
                            mc.player,
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            SoundEvent.createVariableRangeEvent(
                                    new ResourceLocation("humanoid", "wep")
                            ),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );

                    ClientHUDOverlay.jumpscareBaslat();
                }
            });
        });

        ctx.get().setPacketHandled(true);
    }
}
