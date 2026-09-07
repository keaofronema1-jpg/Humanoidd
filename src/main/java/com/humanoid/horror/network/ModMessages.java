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
                .named(
                        new ResourceLocation(
                                "humanoid",
                                "messages"
                        )
                )
                .networkProtocolVersion(
                        () -> "1.0"
                )
                .clientAcceptedVersions(
                        s -> true
                )
                .serverAcceptedVersions(
                        s -> true
                )
                .simpleChannel();

        INSTANCE = net;

        // =====================================================
        // JUMPSCARE PACKET
        // =====================================================

        net.messageBuilder(
                JumpscarePacket.class,
                id(),
                NetworkDirection.PLAY_TO_CLIENT
        )
        .decoder(JumpscarePacket::new)
        .encoder(JumpscarePacket::toBytes)
        .consumerMainThread(JumpscarePacket::handle)
        .add();

        // =====================================================
        // CREATURE1 HUD PACKET
        // =====================================================

        net.messageBuilder(
                Creature1HUDPacket.class,
                id(),
                NetworkDirection.PLAY_TO_CLIENT
        )
        .decoder(Creature1HUDPacket::new)
        .encoder(Creature1HUDPacket::toBytes)
        .consumerMainThread(Creature1HUDPacket::handle)
        .add();
    }

    // =========================================================
    // SERVER -> TEK OYUNCU
    // =========================================================

    public static <MSG> void sendToPlayer(
            MSG message,
            ServerPlayer player
    ) {

        if (INSTANCE == null || player == null) {
            return;
        }

        INSTANCE.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                message
        );
    }

    // =========================================================
    // SERVER -> TÜM OYUNCULAR
    // =========================================================

    public static <MSG> void sendToAllPlayers(
            MSG message
    ) {

        if (INSTANCE == null) {
            return;
        }

        INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                message
        );
    }
}
