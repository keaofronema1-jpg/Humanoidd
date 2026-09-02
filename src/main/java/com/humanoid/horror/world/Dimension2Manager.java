package com.humanoid.horror.world;

import com.humanoid.horror.network.Dimension2Packet;
import com.humanoid.horror.network.HumanoidNetwork;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class Dimension2Manager {

    private static final ResourceLocation DIMENSION2 =
            new ResourceLocation(
                    "humanoid",
                    "dimension2"
            );

    private static final Random RANDOM =
            new Random();

    private static final Map<UUID, Integer> timers =
            new HashMap<>();

    private static final Map<UUID, Boolean> eventRunning =
            new HashMap<>();

    private static final int MIN_EVENT_TIME =
            2400;

    private static final int MAX_EVENT_TIME =
            6000;

    private static final int EVENT_LENGTH =
            220;

    @SubscribeEvent
    public static void serverTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.getServer() == null) {
            return;
        }

        for (ServerPlayer player :
                event.getServer()
                        .getPlayerList()
                        .getPlayers()) {

            /*
             * Dimension2'de zamanı sürekli geceye sabitle.
             */
            if (isInDimension2(player)) {

                ServerLevel level =
                        player.serverLevel();

                level.setDayTime(13000);
            }

            if (!isInDimension2(player)) {

                timers.remove(player.getUUID());
                eventRunning.remove(player.getUUID());

                continue;
            }

            UUID uuid = player.getUUID();

            if (eventRunning.getOrDefault(
                    uuid,
                    false
            )) {

                int timer =
                        timers.getOrDefault(
                                uuid,
                                EVENT_LENGTH
                        );

                timer--;

                timers.put(uuid, timer);

                if (timer <= 0) {

                    finishEvent(player);

                }

                continue;
            }

            int timer =
                    timers.getOrDefault(
                            uuid,
                            randomEventTime()
                    );

            timer--;

            if (timer <= 0) {

                startEvent(player);

            } else {

                timers.put(uuid, timer);
            }
        }
    }

    /*
     * Dimension2'de doğal mob spawnlarını tamamen engeller.
     */
    @SubscribeEvent
    public static void onMobSpawn(
            MobSpawnEvent event
    ) {

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!level.dimension()
                .location()
                .equals(DIMENSION2)) {

            return;
        }

        if (event.getSpawnType()
                == MobSpawnEvent.SpawnReason.NATURAL
                || event.getSpawnType()
                == MobSpawnEvent.SpawnReason.CHUNK_GENERATION
                || event.getSpawnType()
                == MobSpawnEvent.SpawnReason.PATROL
                || event.getSpawnType()
                == MobSpawnEvent.SpawnReason.REINFORCEMENT) {

            event.setCanceled(true);
        }
    }

    private static boolean isInDimension2(
            ServerPlayer player
    ) {

        return player.level()
                .dimension()
                .location()
                .equals(DIMENSION2);
    }

    private static int randomEventTime() {

        return MIN_EVENT_TIME +
                RANDOM.nextInt(
                        MAX_EVENT_TIME -
                        MIN_EVENT_TIME +
                        1
                );
    }

    private static void startEvent(
            ServerPlayer player
    ) {

        UUID uuid = player.getUUID();

        eventRunning.put(uuid, true);

        timers.put(
                uuid,
                EVENT_LENGTH
        );

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        EVENT_LENGTH,
                        0,
                        false,
                        false,
                        false
                )
        );

        HumanoidNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor
                        .PLAYER.with(() -> player),

                new Dimension2Packet(
                        Dimension2Packet.Action.START_EVENT
                )
        );
    }

    private static void finishEvent(
            ServerPlayer player
    ) {

        UUID uuid = player.getUUID();

        player.removeEffect(
                MobEffects.BLINDNESS
        );

        HumanoidNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor
                        .PLAYER.with(() -> player),

                new Dimension2Packet(
                        Dimension2Packet.Action.END_EVENT
                )
        );

        eventRunning.remove(uuid);
        timers.remove(uuid);

        ServerLevel overworld =
                player.getServer()
                        .getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return;
        }

        Vec3 spawn =
                Vec3.atBottomCenterOf(
                        overworld.getSharedSpawnPos()
                );

        player.teleportTo(
                overworld,
                spawn.x,
                spawn.y,
                spawn.z,
                player.getYRot(),
                player.getXRot()
        );
    }
}
