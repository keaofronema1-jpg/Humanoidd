package com.humanoid.horror.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RandomSoundPacket {

    public RandomSoundPacket() {
    }

    public RandomSoundPacket(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public static void handle(
            RandomSoundPacket message,
            Supplier<NetworkEvent.Context> context
    ) {
        context.get().enqueueWork(() -> {

            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () ->
                            com.humanoid.horror.client.RandomSoundClient.play()
            );

        });

        context.get().setPacketHandled(true);
    }
}
