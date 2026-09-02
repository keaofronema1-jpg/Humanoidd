package com.humanoid.horror.network;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class HumanoidNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(
                            HumanoidMod.MOD_ID,
                            "main"
                    ),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    public static void register() {

        CHANNEL.registerMessage(
                0,
                Dimension2Packet.class,

                (packet, buffer) -> {
                    buffer.writeEnum(packet.getAction());
                },

                buffer -> new Dimension2Packet(
                        buffer.readEnum(
                                Dimension2Packet.Action.class
                        )
                ),

                (packet, context) -> {

                    context.get().enqueueWork(
                            packet::handle
                    );

                    context.get().setPacketHandled(true);
                }
        );
    }
}
