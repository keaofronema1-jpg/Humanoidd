package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1HUDState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        value = Dist.CLIENT
)
public class Creature1HUDOverlay {

    // =========================================================
    // HUD KONUMU
    // =========================================================

    private static final int X = 10;
    private static final int Y = 10;

    // =========================================================
    // HUD TEXTURE
    // =========================================================

    /*
     * Dosya:
     *
     * assets/humanoid/gui/creature1_hud.png
     */
    private static final ResourceLocation HUD_BACKGROUND =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "gui/creature1_hud.png"
            );

    // =========================================================
    // HUD BOYUTU
    // =========================================================

    private static final int HUD_WIDTH = 160;
    private static final int HUD_HEIGHT = 32;

    // =========================================================
    // YAZI KONUMU
    // =========================================================

    private static final int TEXT_X =
            X + 8;

    private static final int TIMER_Y =
            Y + 6;

    private static final int NAME_Y =
            Y + 18;

    // =========================================================
    // HUD RENDER
    // =========================================================

    @SubscribeEvent
    public static void onRenderOverlay(
            RenderGuiOverlayEvent.Post event
    ) {

        // =====================================================
        // SADECE CROSSHAIR SONRASI
        // =====================================================

        if (event.getOverlay()
                != VanillaGuiOverlay.CROSSHAIR.type()) {

            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        // =====================================================
        // CLIENT KONTROLÜ
        // =====================================================

        if (minecraft.player == null
                || minecraft.level == null) {

            return;
        }

        // =====================================================
        // /START KONTROLÜ
        // =====================================================

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        // =====================================================
        // STATE KONTROLÜ
        // =====================================================

        if (!Creature1HUDState.isActive()) {
            return;
        }

        // =====================================================
        // STATE'DEN SAYAÇ AL
        // =====================================================

        int timer =
                Creature1HUDState.getDistance();

        /*
         * Güvenlik.
         */
        if (timer < 0) {
            timer = 0;
        }

        if (timer > 500) {
            timer = 500;
        }

        // =====================================================
        // HEDEF OYUNCU ADI
        // =====================================================

        String targetName =
                Creature1HUDState.getTargetName();

        if (targetName == null) {
            targetName = "";
        }

        // =====================================================
        // GRAPHICS
        // =====================================================

        GuiGraphics graphics =
                event.getGuiGraphics();

        graphics.pose().pushPose();

        // =====================================================
        // HUD PNG
        // =====================================================

        graphics.blit(
                HUD_BACKGROUND,
                X,
                Y,
                0,
                0,
                HUD_WIDTH,
                HUD_HEIGHT,
                HUD_WIDTH,
                HUD_HEIGHT
        );

        // =====================================================
        // SAYAÇ
        // =====================================================

        graphics.drawString(
                minecraft.font,
                String.valueOf(timer),
                TEXT_X,
                TIMER_Y,
                0xFFFFFFFF,
                true
        );

        // =====================================================
        // HEDEF OYUNCU
        // =====================================================

        if (!targetName.isEmpty()) {

            graphics.drawString(
                    minecraft.font,
                    targetName,
                    TEXT_X,
                    NAME_Y,
                    0xFFFFFFFF,
                    true
            );
        }

        // =====================================================
        // BITIR
        // =====================================================

        graphics.pose().popPose();
    }
}
