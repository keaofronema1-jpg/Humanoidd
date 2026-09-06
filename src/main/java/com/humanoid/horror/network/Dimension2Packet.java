package com.humanoid.horror.network;

import com.humanoid.horror.client.Dimension2Client;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class Dimension2Packet {

    public enum Action {

        START_EVENT,

        END_EVENT,

        MUSIC_FINISHED
    }

    private final Action action;

    public Dimension2Packet(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void handleServer(
            ServerPlayer player
    ) {

        if (player == null) {
            return;
        }

        /*
         * Sadece Dimension2'deki oyuncu
         * MUSIC_FINISHED gönderebilir.
         */
        if (action == Action.MUSIC_FINISHED) {

            if (!player.level()
                    .dimension()
                    .location()
                    .equals(
                            new net.minecraft.resources.ResourceLocation(
                                    "humanoid",
                                    "humanoid_dimension"
                            )
                    )) {

                return;
            }

            Dimension2ManagerServer.finishMusicSequence(
                    player
            );
        }
    }

    public void handleClient() {

        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {

                    if (action ==
                            Action.START_EVENT) {

                        Dimension2Client.startEventSound();

                    } else if (action ==
                            Action.END_EVENT) {

                        Dimension2Client.endEvent();
                    }
                }
        );
    }
}
