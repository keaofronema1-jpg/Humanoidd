package com.humanoid.horror.network;

import com.humanoid.horror.client.Dimension2Client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class Dimension2Packet {

    public enum Action {

        START_EVENT,
        END_EVENT

    }

    private final Action action;

    public Dimension2Packet(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void handle() {

        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {

                    if (action == Action.START_EVENT) {

                        Dimension2Client.startEventSound();

                    } else {

                        Dimension2Client.endEvent();
                    }
                }
        );
    }
}
