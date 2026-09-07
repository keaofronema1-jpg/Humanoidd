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
         * Animasyonu yalnızca bir kere başlat.
         */
        if (!humanoid$videoStarted) {

            humanoid$videoStarted = true;

            ForgeVideoPlayer.startFromResource();
            ForgeVideoRenderer.init();
        }

        /*
         * Animasyon bittiyse Forge'un normal
         * loading ekranına geri dön.
         */
        if (humanoid$videoFinished ||
                ForgeVideoPlayer.isFinished()) {

            humanoid$videoFinished = true;

            ForgeVideoPlayer.stop();
            ForgeVideoRenderer.release();

            return;
        }

        /*
         * Önce tamamen siyah arka plan.
         */
        guiGraphics.fill(
                0,
                0,
                width,
                height,
                0xFF000000
        );

        /*
         * Sprite sheet'teki mevcut frame'i çiz.
         */
        ForgeVideoRenderer.render(
                guiGraphics,
                width,
                height
        );

        /*
         * Forge'un kendi loading ekranını
         * çizmesini engelle.
         */
        ci.cancel();
    }
}
