package com.humanoid.horror.network;

import com.humanoid.horror.client.ClientHUDOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJumpscare {

    public PacketJumpscare() {}

    public PacketJumpscare(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(PacketJumpscare msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Sadece Client tarafında güvenle çalışır
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                // 1. Korku seslerini yerel olarak çal
                mc.level.playSound(
                    mc.player, 
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvent.createVariableRangeEvent(new ResourceLocation("humanoid", "scare")),
                    SoundSource.HOSTILE, 1.0F, 1.0F
                );

                mc.level.playSound(
                    mc.player, 
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvent.createVariableRangeEvent(new ResourceLocation("humanoid", "wep")),
                    SoundSource.PLAYERS, 1.0F, 1.0F
                );

                // 2. Ekran kaplamasını ve animasyonu tetikle
                ClientHUDOverlay.jumpscareBaslat();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
