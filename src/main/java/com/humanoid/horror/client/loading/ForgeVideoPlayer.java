package com.humanoid.horror.client.loading;

public final class ForgeVideoPlayer {

    // Tek bir frame
    public static final int FRAME_WIDTH = 384;
    public static final int FRAME_HEIGHT = 240;

    // Sprite sheet: 28 x 28
    public static final int COLUMNS = 28;
    public static final int ROWS = 28;

    // Toplam frame
    public static final int TOTAL_FRAMES = COLUMNS * ROWS;

    // Video FPS
    public static final int FPS = 30;

    private static final long FRAME_TIME_NS =
            1_000_000_000L / FPS;

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

        startTime = System.nanoTime();

        // Ses daha sonra burada aynı anda başlatılacak.
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

        /*
         * forge.ogg burada başlatılacak.
         *
         * Konum:
         *
         * assets/humanoid/video/forge.ogg
         *
         * Animasyonun başladığı aynı anda
         * tetiklenecek.
         */
    }

    public static synchronized void stop() {

        finished = true;
        started = false;
    }
}
