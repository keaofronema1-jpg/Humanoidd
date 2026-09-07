package com.humanoid.horror.client.loading;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;

public final class ForgeVideoPlayer {

    public static final int FRAME_WIDTH = 384;
    public static final int FRAME_HEIGHT = 240;

    public static final int COLUMNS = 28;
    public static final int ROWS = 28;

    public static final int TOTAL_FRAMES =
            COLUMNS * ROWS;

    public static final int FPS = 30;

    private static final long FRAME_TIME_NS =
            1_000_000_000L / FPS;

    private static final ResourceLocation FORGE_SOUND =
            new ResourceLocation(
                    "humanoid",
                    "forge_intro"
            );

    private static boolean started = false;
    private static boolean finished = false;

    private static long startTime;

    private ForgeVideoPlayer() {
    }

    public static synchronized void startFromResource() {

        if (started) {
            return;
        }

        started = true;
        finished = false;

        /*
         * Animasyonun başlangıç zamanı.
         */
        startTime = System.nanoTime();

        /*
         * Ses de aynı başlangıçta başlatılıyor.
         */
        playSound();
    }

    public static int getCurrentFrame() {

        if (!started) {
            return 0;
        }

        if (finished) {
            return TOTAL_FRAMES - 1;
        }

        long elapsed =
                System.nanoTime() - startTime;

        if (elapsed < 0) {
            elapsed = 0;
        }

        int frame =
                (int) (elapsed / FRAME_TIME_NS);

        if (frame >= TOTAL_FRAMES) {

            finished = true;

            return TOTAL_FRAMES - 1;
        }

        return frame;
    }

    public static int getFrameColumn() {

        return getCurrentFrame() % COLUMNS;
    }

    public static int getFrameRow() {

        return getCurrentFrame() / COLUMNS;
    }

    public static boolean hasFrame() {

        return started;
    }

    public static boolean isStarted() {

        return started;
    }

    public static boolean isFinished() {

        return finished;
    }

    public static int getVideoWidth() {

        return FRAME_WIDTH;
    }

    public static int getVideoHeight() {

        return FRAME_HEIGHT;
    }

    private static void playSound() {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft == null) {
            return;
        }

        try {

            minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            FORGE_SOUND,
                            1.0F
                    )
            );

        } catch (Exception ignored) {
            /*
             * Loading sırasında ses sistemi hazır değilse
             * oyun crash olmasın.
             */
        }
    }

    public static synchronized void stop() {

        finished = true;
        started = false;
    }
}
