package com.humanoid.horror.network;

import com.humanoid.horror.entity.Creature1HUDState;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Creature1HUDPacket {

    private final int distance;
    private final String targetName;
    private final boolean active;

    // =========================================================
    // SERVER -> CLIENT
    // =========================================================

    public Creature1HUDPacket(
            int distance,
            String targetName,
            boolean active
    ) {
        this.distance = distance;
        this.targetName =
                targetName == null ? "" : targetName;
        this.active = active;
    }

    // =========================================================
    // CLIENT DECODER
    // =========================================================

    public Creature1HUDPacket(FriendlyByteBuf buffer) {

        this.distance = buffer.readInt();
        this.targetName = buffer.readUtf(32767);
        this.active = buffer.readBoolean();
    }

    // =========================================================
    // ENCODER
    // =========================================================

    public void toBytes(FriendlyByteBuf buffer) {

        buffer.writeInt(distance);
        buffer.writeUtf(targetName);
        buffer.writeBoolean(active);
    }

    // =========================================================
    // HANDLE
    // =========================================================

    public void handle(
            Supplier<NetworkEvent.Context> supplier
    ) {

        NetworkEvent.Context context =
                supplier.get();

        context.enqueueWork(() -> {

            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> {

                        Creature1HUDState.setDistance(
                                distance
                        );

                        Creature1HUDState.setTargetName(
                                targetName
                        );

                        Creature1HUDState.setActive(
                                active
                        );

                        Minecraft minecraft =
                                Minecraft.getInstance();

                        if (minecraft.level == null) {
                            return;
                        }
                    }
            );
        });

        context.setPacketHandled(true);
    }
}
