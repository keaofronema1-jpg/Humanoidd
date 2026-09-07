package com.humanoid.horror.entity;

public final class Creature1HUDState {

    private Creature1HUDState() {
    }

    // =========================================================
    // SAYAÇ
    // =========================================================

    private static int distance = 500;

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

        /*
         * 500 -> 499 -> 498 -> ...
         *
         * 0'a geldiğinde daha aşağı inmez.
         */
        if (distance > 0) {
            distance--;
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
    }
}
