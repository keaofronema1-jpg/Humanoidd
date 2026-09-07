package com.humanoid.horror.check;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid"
)
public class StartTimeWeatherManager {

    /*
     * 20 tick = 1 saniye
     */

    private static final long TEN_SECONDS =
            200L;

    /*
     * Hızlı gün/gece döngüsünün toplam sayısı.
     */

    private static final int TOTAL_CYCLES =
            10;

    /*
     * Her hızlı döngünün süresi.
     *
     * 40 tick = 2 saniye.
     */

    private static final long FAST_CYCLE_TICKS =
            40L;

    private static MinecraftServer server;

    private static boolean running =
            false;

    private static int phase =
            0;

    private static long timer =
            0L;

    private static int cycle =
            0;

    private StartTimeWeatherManager() {
    }

    /*
     * ---------------------------------------------------------
     * BAŞLAT
     * ---------------------------------------------------------
     */

    public static void start(
            MinecraftServer minecraftServer
    ) {

        if (minecraftServer == null) {
            return;
        }

        /*
         * Aynı sistemi ikinci kez başlatma.
         */

        if (running) {
            return;
        }

        server =
                minecraftServer;

        running =
                true;

        phase =
                1;

        timer =
                0L;

        cycle =
                0;

        /*
         * İlk aşamada hava olduğu gibi bırakılıyor.
         *
         * Yani /start anında yağmur varsa
         * yağmur devam eder.
         */

        ServerLevel level =
                server.overworld();

        if (level != null) {

            /*
             * Yağmur devam etsin.
             */

            level.setWeatherParameters(
                    6000,
                    0,
                    true,
                    false
            );
        }
    }

    /*
     * ---------------------------------------------------------
     * TICK
     * ---------------------------------------------------------
     */

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
         * -----------------------------------------------------
         * PHASE 1
         *
         * /start sonrası 10 saniye bekle.
         * -----------------------------------------------------
         */

        if (phase == 1) {

            if (timer >= TEN_SECONDS) {

                timer =
                        0L;

                phase =
                        2;

                /*
                 * Yağmur durur.
                 */

                stopRain();

                /*
                 * Anında sabah.
                 */

                setMorning();
            }

            return;
        }

        /*
         * -----------------------------------------------------
         * PHASE 2
         *
         * Sabah olduktan sonra 10 saniye bekle.
         * -----------------------------------------------------
         */

        if (phase == 2) {

            if (timer >= TEN_SECONDS) {

                timer =
                        0L;

                phase =
                        3;

                cycle =
                        0;

                /*
                 * İlk hızlı döngü sabah ile başlıyor.
                 */

                setMorning();
            }

            return;
        }

        /*
         * -----------------------------------------------------
         * PHASE 3
         *
         * 10 hızlı sabah -> midnight döngüsü.
         * -----------------------------------------------------
         */

        if (phase == 3) {

            if (timer >= FAST_CYCLE_TICKS) {

                timer =
                        0L;

                cycle++;

                /*
                 * Sıralama:
                 *
                 * Sabah
                 *   ↓
                 * 1 saniye
                 *   ↓
                 * Midnight
                 *   ↓
                 * 1 saniye
                 *   ↓
                 * Sabah
                 *   ↓
                 * ...
                 */

                if (cycle % 2 == 0) {

                    setMorning();

                } else {

                    setMidnight();
                }

                /*
                 * 10 döngü tamamlandı.
                 */

                if (cycle >= TOTAL_CYCLES * 2) {

                    phase =
                            4;

                    timer =
                            0L;

                    /*
                     * Sonunda kesin olarak midnight.
                     */

                    setMidnight();
                }
            }
        }

        /*
         * -----------------------------------------------------
         * PHASE 4
         *
         * Bitti.
         * -----------------------------------------------------
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

        ServerLevel level =
                server.overworld();

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

        server.overworld()
                .setDayTime(
                        1000L
                );
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

        server.overworld()
                .setDayTime(
                        12000L
                );
    }

    /*
     * ---------------------------------------------------------
     * GECE / 00:00
     * ---------------------------------------------------------
     */

    private static void setMidnight() {

        if (server == null
                || server.overworld() == null) {
            return;
        }

        server.overworld()
                .setDayTime(
                        18000L
                );
    }

    /*
     * ---------------------------------------------------------
     * DURDUR
     * ---------------------------------------------------------
     */

    private static void stop() {

        running =
                false;

        server =
                null;

        timer =
                0L;

        phase =
                0;

        cycle =
                0;
    }

    /*
     * Dışarıdan kontrol gerekirse.
     */

    public static boolean isRunning() {
        return running;
    }
}
