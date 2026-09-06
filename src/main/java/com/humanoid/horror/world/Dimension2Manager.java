package com.humanoid.horror.world;

import com.humanoid.horror.network.Dimension2Packet;
import com.humanoid.horror.network.HumanoidNetwork;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
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

    private static final ResourceLocation DIMENSION1 =
            new ResourceLocation(
                    "humanoid",
                    "humanoid_dimension"
            );

    private static final Set<UUID> playersInSequence =
            new HashSet<>();

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
             * Dimension2'de zamanı geceye sabitle.
             */
            if (isInDimension2(player)) {

                ServerLevel level =
                        player.serverLevel();

                level.setDayTime(13000);
            }

            /*
             * Dimension2'den çıktıysa
             * sequence bilgisini temizle.
             */
            if (!isInDimension2(player)) {

                playersInSequence.remove(
                        player.getUUID()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(
            LivingEvent.LivingTickEvent event
    ) {

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (!isInDimension2(mob)) {
            return;
        }

        ResourceLocation entityId =
                net.minecraft.core.registries
                        .BuiltInRegistries
                        .ENTITY_TYPE
                        .getKey(
                                mob.getType()
                        );

        if (entityId == null) {
            return;
        }

        /*
         * Vanilla moblarını Dimension2'de tutma.
         */
        if ("minecraft".equals(
                entityId.getNamespace()
        )) {

            mob.discard();
        }
    }

    public static void beginMusicSequence(
            ServerPlayer player
    ) {

        if (player == null) {
            return;
        }

        if (!isInDimension2(player)) {
            return;
        }

        playersInSequence.add(
                player.getUUID()
        );
    }

    public static void finishMusicSequence(
            ServerPlayer player
    ) {

        if (player == null) {
            return;
        }

        if (!isInDimension2(player)) {
            return;
        }

        UUID uuid =
                player.getUUID();

        /*
         * Aynı bitiş paketinin iki kere
         * teleport ettirmesini engelle.
         */
        if (!playersInSequence.add(uuid)) {
            return;
        }

        playersInSequence.remove(uuid);

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

    private static boolean isInDimension2(
            ServerPlayer player
    ) {

        return player.level()
                .dimension()
                .location()
                .equals(DIMENSION2);
    }

    private static boolean isInDimension2(
            Mob mob
    ) {

        return mob.level()
                .dimension()
                .location()
                .equals(DIMENSION2);
    }
}
