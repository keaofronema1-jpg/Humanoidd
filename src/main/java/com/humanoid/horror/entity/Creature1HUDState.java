package com.humanoid.horror.entity;

public final class Creature1HUDState {

    private Creature1HUDState() {
    }

    // =========================================================
    // SAYAÇ
    // =========================================================

    private static int distance = 500;

    /*
     * Gerçek zaman bazlı sayaç.
     *
     * 1 saniye = 1 sayı azalması
     */
    private static long lastTimerUpdateNs = 0L;

    // =========================================================
    // HEDEF OYUNCU
    // =========================================================

    private static String targetName = "";

    // =========================================================
    // AKTİF
    // =========================================================

    private static boolean active = false;

    // =========================================================
    // BAŞLAT
    // =========================================================

    public static void start(String playerName) {

        active = true;

        distance = 500;

        /*
         * Sayaç başlatıldığı anda gerçek zamanı kaydet.
         */
        lastTimerUpdateNs =
                System.nanoTime();

        if (playerName == null) {
            targetName = "";
        } else {
            targetName = playerName;
        }
    }

    // =========================================================
    // TICK
    // =========================================================

    public static void tick() {

        if (!active) {
            return;
        }

        if (distance <= 0) {
            distance = 0;
            return;
        }

        long now =
                System.nanoTime();

        /*
         * İlk tick / server yeniden başlatılması
         * gibi durumlarda zaman referansı oluştur.
         */
        if (lastTimerUpdateNs <= 0L) {
            lastTimerUpdateNs = now;
            return;
        }

        long elapsed =
                now - lastTimerUpdateNs;

        if (elapsed < 0L) {
            lastTimerUpdateNs = now;
            return;
        }

        /*
         * Kaç tam saniye geçti?
         */
        long elapsedSeconds =
                elapsed / 1_000_000_000L;

        if (elapsedSeconds <= 0L) {
            return;
        }

        /*
         * Gerçek geçen süre kadar azalt.
         *
         * Normal durumda:
         *
         * 500
         * ↓ 1 saniye
         * 499
         * ↓ 1 saniye
         * 498
         */
        int amount =
                (int) Math.min(
                        elapsedSeconds,
                        distance
                );

        distance -= amount;

        /*
         * Bir sonraki ölçümü tam olarak
         * son işlenen saniyeden devam ettir.
         *
         * Böylece tick hızındaki küçük
         * dalgalanmalar sayaçta birikmez.
         */
        lastTimerUpdateNs +=
                elapsedSeconds * 1_000_000_000L;

        if (distance < 0) {
            distance = 0;
        }
    }

    // =========================================================
    // MESAFE
    // =========================================================

    public static int getDistance() {
        return distance;
    }

    // =========================================================
    // HEDEF İSMİ
    // =========================================================

    public static String getTargetName() {
        return targetName;
    }

    // =========================================================
    // AKTİF Mİ?
    // =========================================================

    public static boolean isActive() {
        return active;
    }

    // =========================================================
    // DURDUR
    // =========================================================

    public static void stop() {

        active = false;

        distance = 500;

        targetName = "";

        lastTimerUpdateNs = 0L;
    }

    // =========================================================
    // TEST / SENKRONİZASYON
    // =========================================================

    public static void setDistance(int value) {

        if (value < 0) {
            value = 0;
        }

        if (value > 500) {
            value = 500;
        }

        distance = value;

        /*
         * Yeni değer verildiğinde zaman sayacını
         * bulunduğumuz ana yeniden sabitle.
         */
        if (active) {
            lastTimerUpdateNs =
                    System.nanoTime();
        }
    }

    public static void setTargetName(String name) {

        if (name == null) {
            targetName = "";
        } else {
            targetName = name;
        }
    }

    public static void setActive(boolean value) {

        active = value;

        if (value) {
            lastTimerUpdateNs =
                    System.nanoTime();
        } else {
            lastTimerUpdateNs = 0L;
        }
    }
}
