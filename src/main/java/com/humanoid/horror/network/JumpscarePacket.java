// JumpscarePacket.java  (artık Minecraft/LocalPlayer'a hiç referans yok)
package com.humanoid.horror.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class JumpscarePacket {
    public JumpscarePacket() {}
    public JumpscarePacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public static void handle(JumpscarePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                () -> com.humanoid.horror.client.ClientJumpscareHandler.trigger()
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
