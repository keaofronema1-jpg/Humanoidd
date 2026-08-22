package com.humanoid.horror.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {

        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("humanoid", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(
                JumpscarePacket.class,
                id(),
                NetworkDirection.PLAY_TO_CLIENT
        )
        .decoder(JumpscarePacket::new)
        .encoder(JumpscarePacket::toBytes)
        .consumerMainThread(JumpscarePacket::handle)
        .add();
    }

    public static <MSG> void sendToPlayer(
            MSG message,
            ServerPlayer player
    ) {
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                message
        );
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                message
        );
    }
}
