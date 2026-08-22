package com.humanoid.horror.pc;

import com.humanoid.horror.system.HorrorWorldData;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import com.humanoid.horror.core.ModManager;
import com.humanoid.horror.core.JumpscareManager;

import java.util.HashMap;
import java.util.Map;

public class PCKeyHandler {

    public static boolean isLocked = false;
    private static long lockStartGameTime = 0L;
    private static long lastFocusRestoreAttempt = 0L;
    private static int focusLossCount = 0;
    private static int altF4PressCount = 0;
    private static long lastAltF4PressTime = 0L;

    // SÜRELER: 7 Gün (84,000 Tick), 15 Gün (180,000 Tick)
    private static final long SEVEN_DAYS_TICKS = 84000L;
    private static final long FIFTEEN_DAYS_TICKS = 180000L;

    private static final Map<Integer, Integer> keyPressCount = new HashMap<>();
    private static final Map<Integer, Long> keyPressTimer = new HashMap<>();
    private static final int JUMPSCARE_THRESHOLD = 2;
    private static final long RESET_TIME_MS = 3000L;

    private static Minecraft getMinecraftSafe() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.getWindow() == null) return null;
            return mc;
        } catch (Exception e) {
            return null;
        }
    }

    private static Window getWindowSafe() {
        Minecraft mc = getMinecraftSafe();
        return (mc == null) ? null : mc.getWindow();
    }

    private static long getWindowHandleSafe() {
        Window window = getWindowSafe();
        if (window == null) return 0L;
        try {
            return window.getWindow();
        } catch (Exception e) {
            return 0L;
        }
    }

    private static boolean isKeyBlocked(int key) {
        if (key == 343 || key == 344 || key == 258 || key == 261) {
            return false;
        }

        int[] blocked = {
            293, 300, 256, 320, 284, 145,
            265, 266, 267, 268, 269, 270, 271, 272,
            273, 274, 275, 276, 280, 281, 282, 283,
            284, 285, 286, 287, 288, 289, 290, 291,
            45, 46, 36, 35, 33, 34, 144, 88, 61, 96
        };

        for (int b : blocked) {
            if (key == b) return true;
        }
        return false;
    }

    private static boolean checkAndTriggerJumpscare(int key) {
        Minecraft mc = getMinecraftSafe();
        if (mc == null || !isLocked) return false;

        long currentTime = System.currentTimeMillis();
        Long previousTime = keyPressTimer.get(key);
        Integer count = keyPressCount.getOrDefault(key, 0);

        if (previousTime == null || (currentTime - previousTime) > RESET_TIME_MS) {
            keyPressCount.put(key, 1);
            keyPressTimer.put(key, currentTime);
            return false;
        }

        if (count >= JUMPSCARE_THRESHOLD - 1) {
            JumpscareManager.triggerJumpscare();
            keyPressCount.put(key, 0);
            keyPressTimer.put(key, currentTime);
            return true;
        }

        keyPressCount.put(key, count + 1);
        keyPressTimer.put(key, currentTime);
        return false;
    }

    public static void triggerWorldLock() {
        Minecraft mc = getMinecraftSafe();
        if (mc == null) return;

        isLocked = true;
        lockStartGameTime = mc.level.getGameTime();
        keyPressCount.clear();
        keyPressTimer.clear();
        focusLossCount = 0;
        altF4PressCount = 0;

        try {
            HorrorWorldData data = HorrorWorldData.get(mc.level);
            data.isLocked = true;
            data.lockStartGameTime = lockStartGameTime;
            data.setDirty();
        } catch (Exception ignored) {}

        enforceLockSafe();
    }

    public static void syncFromStorage(boolean lockedFromDisk, long startTimeFromDisk) {
        isLocked = lockedFromDisk;
        lockStartGameTime = startTimeFromDisk;
        keyPressCount.clear();
        keyPressTimer.clear();
        focusLossCount = 0;
        altF4PressCount = 0;
        
        if (isLocked) {
            panicLock();
        }
    }

    private static void enforceLockSafe() {
        try {
            long handle = getWindowHandleSafe();
            if (handle == 0) return;

            GLFW.glfwSetWindowShouldClose(handle, false);
            GLFW.glfwShowWindow(handle);
            GLFW.glfwFocusWindow(handle);
            GLFW.glfwRestoreWindow(handle);
        } catch (Exception ignored) {}
    }

    public static void onClientTick() {
        if (!isLocked) return;

        Minecraft mc = getMinecraftSafe();
        if (mc == null) return;

        long currentWorldTime = mc.level.getGameTime();
        long elapsedTicks = currentWorldTime - lockStartGameTime;

        // 15 GÜN GEÇTİYSE KİLİTLERİ KALDIR
        if (elapsedTicks >= FIFTEEN_DAYS_TICKS) {
            releaseSystemLocks();
            return;
        }

        try {
            long handle = getWindowHandleSafe();
            if (handle != 0) {
                GLFW.glfwSetWindowShouldClose(handle, false);
            }
        } catch (Exception ignored) {}
    }

    public static boolean onKeyInput(int key, int action) {
        if (!isLocked) return true;
        if (action != GLFW.GLFW_PRESS) return true;

        if (key == 256 || key == 300) { // ESC ve F11
            checkAndTriggerJumpscare(key);
            return false;
        }

        if (key == 293) { // ALT+F4
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAltF4PressTime < RESET_TIME_MS) {
                altF4PressCount++;
            } else {
                altF4PressCount = 1;
            }
            lastAltF4PressTime = currentTime;

            if (altF4PressCount >= 2) {
                JumpscareManager.triggerJumpscare();
                altF4PressCount = 0;
            }

            try {
                long handle = getWindowHandleSafe();
                if (handle != 0) {
                    GLFW.glfwSetWindowShouldClose(handle, false);
                }
            } catch (Exception ignored) {}
            return false;
        }

        if (isKeyBlocked(key)) {
            checkAndTriggerJumpscare(key);
            return false;
        }

        return true;
    }

    public static void onWindowFocusChanged(boolean hasFocus) {
        if (!isLocked) return;
        if (hasFocus) {
            focusLossCount = 0;
            return;
        }

        focusLossCount++;

        if (focusLossCount >= 3) {
            focusLossCount = 0;
            try {
                JumpscareManager.triggerJumpscare();
            } catch (Exception ignored) {}
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFocusRestoreAttempt < 1000) return;
        lastFocusRestoreAttempt = currentTime;

        try {
            long handle = getWindowHandleSafe();
            if (handle == 0) return;

            GLFW.glfwFocusWindow(handle);
            GLFW.glfwShowWindow(handle);
            GLFW.glfwRestoreWindow(handle);
            GLFW.glfwSetWindowShouldClose(handle, false);
        } catch (Exception ignored) {}
    }

    public static void onWindowShouldClose() {
        if (!isLocked) return;
        try {
            long handle = getWindowHandleSafe();
            if (handle != 0) {
                GLFW.glfwSetWindowShouldClose(handle, false);
            }
        } catch (Exception ignored) {}
    }

    public static void executeSolveCommand(String enteredPlayerName) {
        if (!isLocked) return;

        Minecraft mc = getMinecraftSafe();
        if (mc == null || mc.getUser() == null) return;

        long currentWorldTime = mc.level.getGameTime();
        long elapsedTicks = currentWorldTime - lockStartGameTime;
        String localPlayerName = mc.getUser().getName();

        // 7. GÜNDEN ÖNCE CHAT /SOLVE KOMUTUNU KULLANIRSA JUMPSCARE
        if (elapsedTicks < SEVEN_DAYS_TICKS) {
            JumpscareManager.triggerJumpscare();
            return;
        }

        if (localPlayerName.equalsIgnoreCase(enteredPlayerName)) {
            releaseSystemLocks();
        } else {
            JumpscareManager.triggerJumpscare();
        }
    }

    public static void releaseSystemLocks() {
        isLocked = false;
        keyPressCount.clear();
        keyPressTimer.clear();
        focusLossCount = 0;
        altF4PressCount = 0;

        Minecraft mc = getMinecraftSafe();
        if (mc != null) {
            try {
                HorrorWorldData data = HorrorWorldData.get(mc.level);
                data.isLocked = false;
                data.lockStartGameTime = 0L;
                data.setDirty();
            } catch (Exception ignored) {}
        }

        try {
            long handle = getWindowHandleSafe();
            if (handle != 0) {
                GLFW.glfwSetWindowShouldClose(handle, true);
            }
        } catch (Exception ignored) {}
    }

    public static void panicLock() {
        isLocked = true;
        keyPressCount.clear();
        keyPressTimer.clear();
        focusLossCount = 0;
        altF4PressCount = 0;
        enforceLockSafe();
    }
}
