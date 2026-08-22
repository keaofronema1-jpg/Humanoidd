package com.humanoid.horror.pc;

import com.humanoid.horror.event.HorrorTriggerEvent;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mod.EventBusSubscriber(modid = "humanoid", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WallpaperEventHandler {

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.UNICODE_OPTIONS);

        boolean SystemParametersInfo(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    public WallpaperEventHandler() {
    }

    @SubscribeEvent
    public static void onHorrorTrigger(HorrorTriggerEvent event) {
        if (!FMLEnvironment.dist.isClient() || !Platform.isWindows()) {
            return;
        }

        try {
            // 1. WALLPAPER ENGINE AÇIKSA SÜRECİ KAPAT
            killWallpaperEngine();

            String tempDir = System.getProperty("java.io.tmpdir");
            File imageFile = new File(tempDir, "wallpaper.png");

            // 2. .jar İÇİNDEKİ RESMİ TEMP KLASÖRÜNE wallpaper.png OLARAK ÇIKAR
            ResourceLocation textureLoc = new ResourceLocation("humanoid", "textures/gui/wallpaper.png");
            var resourceOptional = Minecraft.getInstance().getResourceManager().getResource(textureLoc);

            if (resourceOptional.isPresent()) {
                Resource resource = resourceOptional.get();
                try (InputStream inputStream = resource.open()) {
                    Files.copy(inputStream, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // 3. DUVAR KAĞIDINI DEĞİŞTİR
            if (imageFile.exists()) {
                executeWallpaperChange(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Wallpaper Engine süreçlerini (32-bit ve 64-bit) sonlandırır.
     */
    private static void killWallpaperEngine() {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM wallpaper32.exe /T");
            Runtime.getRuntime().exec("taskkill /F /IM wallpaper64.exe /T");
        } catch (Exception ignored) {
            // Wallpaper Engine çalışmıyorsa hatayı görmezden gel
        }
    }

    private static void executeWallpaperChange(String imagePath) {
        try {
            int SPI_SETDESKWALLPAPER = 0x0014;
            int SPIF_UPDATEINIFILE = 0x01;
            int SPIF_SENDCHANGE = 0x02;
            int flags = SPIF_UPDATEINIFILE | SPIF_SENDCHANGE;

            User32.INSTANCE.SystemParametersInfo(SPI_SETDESKWALLPAPER, 0, imagePath, flags);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
