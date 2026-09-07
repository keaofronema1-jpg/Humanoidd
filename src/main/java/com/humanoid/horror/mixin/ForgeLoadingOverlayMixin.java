package com.humanoid.horror.mixin;

import com.humanoid.horror.client.loading.ForgeVideoPlayer;
import com.humanoid.horror.client.loading.ForgeVideoRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(ForgeLoadingOverlay.class)
public class ForgeLoadingOverlayMixin {

    private static boolean humanoid$videoStarted = false;
    private static boolean humanoid$videoFinished = false;

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void humanoid$replaceForgeLoadingScreen(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        /*
         * Video daha önce başlatılmadıysa başlat.
         */
        if (!humanoid$videoStarted) {

            humanoid$videoStarted = true;

            try {

                Path videoPath =
                        minecraft.gameDirectory
                                .toPath()
                                .resolve("assets")
                                .resolve("humanoid")
                                .resolve("video")
                                .resolve("forge.mp4");

                /*
                 * Öncelikle gerçek dosya yolunu kontrol ediyoruz.
                 */
                if (Files.exists(videoPath)) {

                    ForgeVideoPlayer.start(videoPath);

                } else {

                    /*
                     * Resource içerisindeki forge.mp4'ü
                     * geçici dosyaya çıkar.
                     */
                    Path tempVideo =
                            minecraft.gameDirectory
                                    .toPath()
                                    .resolve("humanoid_forge_loading.mp4");

                    try (var input =
                                 ForgeLoadingOverlayMixin.class
                                         .getResourceAsStream(
                                                 "/assets/humanoid/video/forge.mp4"
                                         )) {

                        if (input != null) {

                            Files.copy(
                                    input,
                                    tempVideo,
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );

                            ForgeVideoPlayer.start(tempVideo);

                        } else {

                            humanoid$videoFinished = true;
                        }
                    }
                }

            } catch (Exception e) {

                /*
                 * Video açılamazsa Minecraft'ın
                 * loading sürecini kilitleme.
                 */
                humanoid$videoFinished = true;
            }
        }

        /*
         * Video tamamlandıysa artık Forge'un kendi
         * render metodunun çalışmasına izin veriyoruz.
         */
        if (humanoid$videoFinished ||
                ForgeVideoPlayer.isFinished()) {

            humanoid$videoFinished = true;

            try {
                ForgeVideoRenderer.release();
            } catch (Exception ignored) {
            }

            /*
             * Forge normal loading ekranına geri dönsün.
             */
            return;
        }

        /*
         * Videonun ilk frame'i henüz gelmediyse
         * siyah ekran göster.
         */
        if (!ForgeVideoPlayer.hasFrame()) {

            guiGraphics.fill(
                    0,
                    0,
                    width,
                    height,
                    0xFF000000
            );

            ci.cancel();
            return;
        }

        /*
         * VLCJ'den gelen frame'i al.
         */
        var frame =
                ForgeVideoPlayer.getFrameBuffer();

        if (frame != null) {

            int videoWidth =
                    ForgeVideoPlayer.getVideoWidth();

            int videoHeight =
                    ForgeVideoPlayer.getVideoHeight();

            if (videoWidth > 0 &&
                    videoHeight > 0) {

                ForgeVideoRenderer.uploadFrame(
                        frame,
                        videoWidth,
                        videoHeight
                );
            }
        }

        /*
         * Videoyu tam ekran çiz.
         */
        ForgeVideoRenderer.render(
                guiGraphics,
                width,
                height
        );

        /*
         * Forge'un anvil / memory / progress bar
         * ekranını göstermesini engelle.
         */
        ci.cancel();
    }
}
