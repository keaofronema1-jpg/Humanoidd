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

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class WallpaperEventHandler {

    /**
     * Windows User32 API.
     */
    public interface User32 extends StdCallLibrary {

        User32 INSTANCE = Native.load(
                "user32",
                User32.class,
                W32APIOptions.UNICODE_OPTIONS
        );

        boolean SystemParametersInfo(
                int uiAction,
                int uiParam,
                String pvParam,
                int fWinIni
        );
    }

    public WallpaperEventHandler() {
    }

    /**
     * HorrorTriggerEvent geldiğinde Windows duvar kağıdını değiştirir.
     */
    @SubscribeEvent
    public static void onHorrorTrigger(HorrorTriggerEvent event) {

        /*
         * Sadece Windows + Client tarafında çalış.
         */
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }

        if (!Platform.isWindows()) {
            return;
        }

        try {

            /*
             * 1. Wallpaper Engine işlemlerini kapat.
             */
            killWallpaperEngine();

            /*
             * 2. Geçici dosyanın konumunu oluştur.
             */
            String tempDir = System.getProperty("java.io.tmpdir");

            File imageFile = new File(
                    tempDir,
                    "wallpaper.png"
            );

            /*
             * 3. Mod içerisindeki wallpaper.png dosyasını bul.
             *
             * assets/humanoid/textures/gui/wallpaper.png
             */
            ResourceLocation textureLoc =
                    new ResourceLocation(
                            "humanoid",
                            "textures/gui/wallpaper.png"
                    );

            Minecraft mc = Minecraft.getInstance();

            if (mc == null || mc.getResourceManager() == null) {
                return;
            }

            var resourceOptional =
                    mc.getResourceManager()
                            .getResource(textureLoc);

            /*
             * 4. Texture bulunduysa geçici klasöre kopyala.
             */
            if (resourceOptional.isPresent()) {

                Resource resource =
                        resourceOptional.get();

                try (InputStream inputStream =
                             resource.open()) {

                    Files.copy(
                            inputStream,
                            imageFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            /*
             * 5. Dosya başarıyla oluşturulduysa
             * Windows wallpaper olarak ayarla.
             */
            if (imageFile.exists()) {

                executeWallpaperChange(
                        imageFile.getAbsolutePath()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Wallpaper Engine işlemlerini kapatır.
     *
     * Wallpaper Engine çalışmıyorsa hata önemsenmez.
     */
    private static void killWallpaperEngine() {

        try {

            Runtime.getRuntime().exec(
                    "taskkill /F /IM wallpaper32.exe /T"
            );

        } catch (Exception ignored) {
        }

        try {

            Runtime.getRuntime().exec(
                    "taskkill /F /IM wallpaper64.exe /T"
            );

        } catch (Exception ignored) {
        }
    }

    /**
     * Windows User32 API kullanarak
     * masaüstü duvar kağıdını değiştirir.
     */
    private static void executeWallpaperChange(
            String imagePath
    ) {

        try {

            final int SPI_SETDESKWALLPAPER =
                    0x0014;

            final int SPIF_UPDATEINIFILE =
                    0x01;

            final int SPIF_SENDCHANGE =
                    0x02;

            final int flags =
                    SPIF_UPDATEINIFILE
                    | SPIF_SENDCHANGE;

            User32.INSTANCE.SystemParametersInfo(
                    SPI_SETDESKWALLPAPER,
                    0,
                    imagePath,
                    flags
            );

        } catch (Throwable t) {

            t.printStackTrace();
        }
    }
}
