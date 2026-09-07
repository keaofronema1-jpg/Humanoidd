package com.humanoid.horror.check;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "humanoid")
public class StartTimeWeatherManager {

    private static final long TEN_SECONDS = 200L;

    private static final int TOTAL_CYCLES = 10;

    // 10 tick = 0.5 saniye
    private static final long FAST_CYCLE_TICKS = 10L;

    private static MinecraftServer server;

    private static boolean running = false;

    private static int phase = 0;

    private static long timer = 0L;

    private static int cycle = 0;

    private StartTimeWeatherManager() {
    }

    public static void start(MinecraftServer minecraftServer) {

        if (minecraftServer == null) {
            return;
        }

        if (running) {
            return;
        }

        server = minecraftServer;

        running = true;

        phase = 1;

        timer = 0L;

        cycle = 0;

        ServerLevel level = server.overworld();

        if (level != null) {
            level.setWeatherParameters(
                    6000,
                    0,
                    true,
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!running) {
            return;
        }

        if (server == null) {
            stop();
            return;
        }

        if (server.overworld() == null) {
            stop();
            return;
        }

        timer++;

        /*
         * PHASE 1
         *
         * /start sonrası 10 saniye bekle.
         */

        if (phase == 1) {

            if (timer >= TEN_SECONDS) {

                timer = 0L;

                phase = 2;

                stopRain();

                setMorning();
            }

            return;
        }

        /*
         * PHASE 2
         *
         * Sabah olduktan sonra 10 saniye bekle.
         */

        if (phase == 2) {

            if (timer >= TEN_SECONDS) {

                timer = 0L;

                phase = 3;

                cycle = 0;

                setMorning();
            }

            return;
        }

        /*
         * PHASE 3
         *
         * Her 0.5 saniyede bir değişir.
         *
         * 1  -> Midnight
         * 2  -> Sabah
         * 3  -> Midnight
         * 4  -> Sabah
         * ...
         * 9  -> Midnight
         * 10 -> Sabah
         *
         * 10. değişimden sonra Midnight'a alınır
         * ve sistem biter.
         */

        if (phase == 3) {

            if (timer >= FAST_CYCLE_TICKS) {

                timer = 0L;

                cycle++;

                if (cycle % 2 == 0) {

                    setMorning();

                } else {

                    setMidnight();
                }

                /*
                 * TAM 10 DEĞİŞİM.
                 */

                if (cycle >= TOTAL_CYCLES) {

                    phase = 4;

                    timer = 0L;

                    /*
                     * Son durumda kesin Midnight.
                     */

                    setMidnight();
                }
            }

            return;
        }

        /*
         * PHASE 4
         *
         * Sistem tamamlandı.
         */

        if (phase == 4) {
            stop();
        }
    }

    /*
     * ---------------------------------------------------------
     * YAĞMURU DURDUR
     * ---------------------------------------------------------
     */

    private static void stopRain() {

        if (server == null
                || server.overworld() == null) {
            return;
        }

        ServerLevel level = server.overworld();

        level.setWeatherParameters(
                0,
                6000,
                false,
                false
        );
    }

    /*
     * ---------------------------------------------------------
     * SABAH
     * ---------------------------------------------------------
     */

    private static void setMorning() {

        if (server == null
                || server.overworld() == null) {
            return;
        }

        server.overworld().setDayTime(1000L);
    }

    /*
     * ---------------------------------------------------------
     * AKŞAM
     * ---------------------------------------------------------
     */

    private static void setEvening() {

        if (server == null
                || server.overworld() == null) {
            return;
        }

        server.overworld().setDayTime(12000L);
    }

    /*
     * ---------------------------------------------------------
     * MIDNIGHT
     * ---------------------------------------------------------
     */

    private static void setMidnight() {

        if (server == null
                || server.overworld() == null) {
            return;
        }

        server.overworld().setDayTime(18000L);
    }

    /*
     * ---------------------------------------------------------
     * DURDUR
     * ---------------------------------------------------------
     */

    private static void stop() {

        running = false;

        server = null;

        timer = 0L;

        phase = 0;

        cycle = 0;
    }

    public static boolean isRunning() {
        return running;
    }
}
