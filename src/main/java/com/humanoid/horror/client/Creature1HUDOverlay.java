package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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
    // HUD RENDER
    // =========================================================

    @SubscribeEvent
    public static void onRenderOverlay(
            RenderGuiOverlayEvent.Post event
    ) {

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

        /*
         * /start verilmeden HUD kesinlikle görünmez.
         */
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

        /*
         * Client tarafında Creature1 bulunmuyorsa
         * hiçbir şey çizme.
         */
        if (creature == null) {
            return;
        }

        // =====================================================
        // SAYACI AL
        // =====================================================

        int timer =
                creature.getDisplayTimer();

        /*
         * Güvenlik:
         * Negatif değer gösterme.
         */
        if (timer < 0) {
            timer = 0;
        }

        // =====================================================
        // HEDEF OYUNCU ADI
        // =====================================================

        String targetName =
                creature.getTargetName();

        /*
         * Güvenlik:
         * Null veya boş isim gelirse boş göster.
         */
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

        /*
         * =====================================================
         * SAYAÇ
         * =====================================================
         *
         * Sadece sayı.
         * Arka plan yok.
         * Kutu yok.
         * Bar yok.
         */

        graphics.drawString(
                minecraft.font,
                String.valueOf(timer),
                X,
                Y,
                0xFFFFFFFF,
                true
        );

        /*
         * =====================================================
         * HEDEF OYUNCU ADI
         * =====================================================
         *
         * Sayaçın hemen altında gösterilir.
         */

        if (!targetName.isEmpty()) {

            graphics.drawString(
                    minecraft.font,
                    targetName,
                    X,
                    Y + 12,
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

        /*
         * Oyuncunun 512 blok çevresindeki
         * yüklenmiş Creature1 entitylerini kontrol et.
         */
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
