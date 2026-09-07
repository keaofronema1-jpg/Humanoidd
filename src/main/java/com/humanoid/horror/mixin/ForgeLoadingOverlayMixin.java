package com.humanoid.horror.mixin;

import com.humanoid.horror.client.loading.ForgeVideoPlayer;
import com.humanoid.horror.client.loading.ForgeVideoRenderer;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.client.loading.ForgeLoadingOverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        int width =
                guiGraphics.guiWidth();

        int height =
                guiGraphics.guiHeight();

        /*
         * Videoyu yalnızca bir kere başlat.
         */
        if (!humanoid$videoStarted) {

            humanoid$videoStarted = true;

            ForgeVideoPlayer.startFromResource();
        }

        /*
         * Video bittiyse artık Forge'un kendi
         * loading ekranına müdahale etme.
         */
        if (humanoid$videoFinished ||
                ForgeVideoPlayer.isFinished()) {

            humanoid$videoFinished = true;

            ForgeVideoPlayer.stop();

            try {
                ForgeVideoRenderer.release();
            } catch (Exception ignored) {
            }

            return;
        }

        /*
         * İlk frame gelene kadar siyah ekran.
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
         * Son frame'i al.
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
         * Forge'un varsayılan anvil / memory /
         * progress ekranını çizmesini engelle.
         */
        ci.cancel();
    }
}
