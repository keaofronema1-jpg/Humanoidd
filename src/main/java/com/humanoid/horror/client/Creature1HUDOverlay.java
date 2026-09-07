package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        value = Dist.CLIENT
)
public class Creature1HUDOverlay {

    // =========================================================
    // HUD KONUMU
    // =========================================================

    private static final int X = 10;
    private static final int Y = 10;

    // =========================================================
    // HUD ARKA PLANI
    // =========================================================

    private static final ResourceLocation HUD_BACKGROUND =
            new ResourceLocation(
                    "humanoid",
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

    private static final int TEXT_X = X + 8;
    private static final int TIMER_Y = Y + 6;
    private static final int NAME_Y = Y + 18;

    // =========================================================
    // HUD RENDER
    // =========================================================

    @SubscribeEvent
    public static void onRenderOverlay(
            RenderGuiOverlayEvent.Post event
    ) {

        /*
         * HUD'ı yalnızca crosshair çizildikten sonra çiz.
         *
         * Böylece her frame'de tek kez ve güvenilir
         * bir render noktası elde ediyoruz.
         */
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        // =====================================================
        // OYUNCU / DÜNYA KONTROLÜ
        // =====================================================

        if (
                minecraft.player == null
                        || minecraft.level == null
        ) {
            return;
        }

        // =====================================================
        // /START KONTROLÜ
        // =====================================================

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        Level level =
                minecraft.level;

        // =====================================================
        // CREATURE1 BUL
        // =====================================================

        Creature1 creature =
                findCreature1(
                        level,
                        minecraft.player
                );

        if (creature == null) {
            return;
        }

        // =====================================================
        // SAYACI AL
        // =====================================================

        int timer =
                creature.getDisplayTimer();

        if (timer < 0) {
            timer = 0;
        }

        // =====================================================
        // HEDEF OYUNCU ADI
        // =====================================================

        String targetName =
                creature.getTargetName();

        if (
                targetName == null
                        || targetName.isEmpty()
        ) {
            targetName = "";
        }

        // =====================================================
        // ÇİZİM
        // =====================================================

        GuiGraphics graphics =
                event.getGuiGraphics();

        graphics.pose().pushPose();

        // =====================================================
        // PNG
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
        // HEDEF OYUNCU ADI
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

        graphics.pose().popPose();
    }

    // =========================================================
    // EN YAKIN CREATURE1'I BUL
    // =========================================================

    private static Creature1 findCreature1(
            Level level,
            Entity player
    ) {

        Creature1 closest =
                null;

        double closestDistance =
                Double.MAX_VALUE;

        // =====================================================
        // 512 BLOK İÇİNDEKİ CREATURE1'LER
        // =====================================================

        for (
                Creature1 creature :
                level.getEntitiesOfClass(
                        Creature1.class,
                        player.getBoundingBox()
                                .inflate(512.0D)
                )
        ) {

            double distance =
                    player.distanceToSqr(
                            creature
                    );

            if (
                    closest == null
                            || distance < closestDistance
            ) {

                closest =
                        creature;

                closestDistance =
                        distance;
            }
        }

        return closest;
    }
}
