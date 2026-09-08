package com.humanoid.horror.network;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {

    private static final String PROTOCOL_VERSION = "1.0";

    private static SimpleChannel INSTANCE;

    /*
     * Packet ID'leri sabit tutuluyor.
     *
     * 0 = JumpscarePacket
     * 1 = Creature1HUDPacket
     * 2 = RandomSoundPacket
     */
    private static final int JUMPSCARE_PACKET_ID = 0;
    private static final int CREATURE1_HUD_PACKET_ID = 1;
    private static final int RANDOM_SOUND_PACKET_ID = 2;

    private static boolean registered = false;

    private ModMessages() {
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public static synchronized void register() {

        /*
         * Yanlışlıkla iki kere register edilirse
         * channel tekrar oluşturulmasın.
         */
        if (registered) {
            return;
        }

        SimpleChannel net =
                NetworkRegistry.ChannelBuilder
                        .named(
                                new ResourceLocation(
                                        HumanoidMod.MOD_ID,
                                        "messages"
                                )
                        )
                        .networkProtocolVersion(
                                () -> PROTOCOL_VERSION
                        )
                        .clientAcceptedVersions(
                                version ->
                                        version.equals(PROTOCOL_VERSION)
                        )
                        .serverAcceptedVersions(
                                version ->
                                        version.equals(PROTOCOL_VERSION)
                        )
                        .simpleChannel();

        INSTANCE = net;

        // =====================================================
        // 0 - JUMPSCARE
        // =====================================================

        net.messageBuilder(
                        JumpscarePacket.class,
                        JUMPSCARE_PACKET_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(JumpscarePacket::new)
                .encoder(JumpscarePacket::toBytes)
                .consumerMainThread(
                        JumpscarePacket::handle
                )
                .add();

        // =====================================================
        // 1 - CREATURE1 HUD
        // =====================================================

        net.messageBuilder(
                        Creature1HUDPacket.class,
                        CREATURE1_HUD_PACKET_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(Creature1HUDPacket::new)
                .encoder(Creature1HUDPacket::toBytes)
                .consumerMainThread(
                        Creature1HUDPacket::handle
                )
                .add();

        // =====================================================
        // 2 - RANDOM SOUND
        // =====================================================

        net.messageBuilder(
                        RandomSoundPacket.class,
                        RANDOM_SOUND_PACKET_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .decoder(RandomSoundPacket::new)
                .encoder(RandomSoundPacket::toBytes)
                .consumerMainThread(
                        RandomSoundPacket::handle
                )
                .add();

        registered = true;
    }

    // =========================================================
    // INSTANCE
    // =========================================================

    public static SimpleChannel getInstance() {
        return INSTANCE;
    }

    public static boolean isRegistered() {
        return registered && INSTANCE != null;
    }

    // =========================================================
    // SERVER -> TEK OYUNCU
    // =========================================================

    public static <MSG> void sendToPlayer(
            MSG message,
            ServerPlayer player
    ) {

        if (message == null) {
            return;
        }

        if (player == null) {
            return;
        }

        if (INSTANCE == null) {
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

        if (message == null) {
            return;
        }

        if (INSTANCE == null) {
            return;
        }

        INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                message
        );
    }

    // =========================================================
    // SERVER -> TÜM OYUNCULAR
    // ALIAS
    // =========================================================

    public static <MSG> void sendToAll(
            MSG message
    ) {
        sendToAllPlayers(message);
    }
}
