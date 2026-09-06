package com.humanoid.horror;

import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.network.RandomSoundPacket;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class RandomSoundManager {

    private static final long TICKS_PER_DAY = 24000L;

    private static final Map<UUID, Long> LAST_DAY = new HashMap<>();
    private static final Map<UUID, Integer> RANDOM_MINUTE = new HashMap<>();
    private static final Map<UUID, Long> LAST_PLAYED_DAY = new HashMap<>();

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        tickCounter++;

        if (tickCounter < 20) {
            return;
        }

        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {

            long dayTime = level.getDayTime();

            long currentDay = dayTime / TICKS_PER_DAY;

            int tickOfDay = (int) (dayTime % TICKS_PER_DAY);

            for (ServerPlayer player : level.players()) {

                if (!player.isAlive() || player.isSpectator()) {
                    continue;
                }

                UUID uuid = player.getUUID();

                Long oldDay = LAST_DAY.get(uuid);

                /*
                 * Oyuncu sisteme ilk defa giriyorsa
                 * bugünün rastgele dakikasını oluştur.
                 */
                if (oldDay == null) {

                    LAST_DAY.put(uuid, currentDay);

                    RANDOM_MINUTE.put(
                            uuid,
                            randomMinute()
                    );

                    continue;
                }

                /*
                 * Yeni Minecraft günü.
                 */
                if (currentDay != oldDay) {

                    LAST_DAY.put(uuid, currentDay);

                    RANDOM_MINUTE.put(
                            uuid,
                            randomMinute()
                    );

                    continue;
                }

                Integer selectedMinute =
                        RANDOM_MINUTE.get(uuid);

                if (selectedMinute == null) {

                    selectedMinute = randomMinute();

                    RANDOM_MINUTE.put(
                            uuid,
                            selectedMinute
                    );
                }

                /*
                 * Minecraft:
                 *
                 * 1 dakika = 1200 tick
                 */
                int targetTick =
                        selectedMinute * 1200;

                /*
                 * Seçilen dakikaya ulaşıldı.
                 */
                if (tickOfDay >= targetTick
                        && tickOfDay < targetTick + 20) {

                    Long lastPlayed =
                            LAST_PLAYED_DAY.get(uuid);

                    /*
                     * Aynı gün içerisinde sadece
                     * bir kere çalmasına izin ver.
                     */
                    if (lastPlayed == null
                            || lastPlayed != currentDay) {

                        LAST_PLAYED_DAY.put(
                                uuid,
                                currentDay
                        );

                        ModMessages.sendToPlayer(
                                new RandomSoundPacket(),
                                player
                        );
                    }
                }
            }
        }
    }

    private static int randomMinute() {

        /*
         * 0-19 arası Minecraft dakikası.
         */
        return ThreadLocalRandom.current()
                .nextInt(0, 20);
    }
}
