package com.humanoid.horror.replacement;

import java.util.UUID;

public class ReplacementPlayer {

    private static UUID targetUUID;
    private static String targetName;

    private ReplacementPlayer() {
    }

    public static void setTarget(UUID uuid, String name) {
        targetUUID = uuid;
        targetName = name;
    }

    public static UUID getTargetUUID() {
        return targetUUID;
    }

    public static String getTargetName() {
        return targetName;
    }

    public static boolean hasTarget() {
        return targetUUID != null;
    }

    public static void clearTarget() {
        targetUUID = null;
        targetName = null;
    }
}
