package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.ai.Creature2Manager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        value = Dist.CLIENT
)
public class Creature2HUDOverlay {

    public static boolean persistent = false;
    private static int displayTicks = 0;
    private static int animTick = 0;

    /**
     * Belirli bir tick süresi boyunca ekranda RUN efektini oynatır.
     */
    public static void show(int ticks) {
        displayTicks = ticks;
    }

    /**
     * Efektin sürekli ekranda kalmasını veya kapatılmasını sağlar.
     */
    public static void setPersistent(boolean state) {
        persistent = state;

        if (!state) {
            displayTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (displayTicks > 0) {
            displayTicks--;
        }

        if (persistent
                || displayTicks > 0
                || Creature2Manager.isJumpscareActiveForHUD()) {

            animTick++;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {

        boolean isActive =
                persistent
                || displayTicks > 0
                || Creature2Manager.isJumpscareActiveForHUD();

        if (!isActive) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = mc.font;

        int screenWidth =
                event.getWindow().getGuiScaledWidth();

        int screenHeight =
                event.getWindow().getGuiScaledHeight();

        // Titreme
        int shakeXAmount = persistent ? 12 : 8;

        int offsetX =
                (int) ((Math.random() - 0.5D) * shakeXAmount);

        int shakeYAmount = persistent ? 8 : 6;

        int offsetY =
                (int) ((Math.random() - 0.5D) * shakeYAmount);

        // Şeffaflık dalgalanması
        float sinValue =
                (float) Math.abs(
                        Math.sin(animTick * 0.15D)
                );

        int alpha =
                (int) (
                        (sinValue * 0.5F + 0.5F)
                        * 255.0F
                );

        int darkRedGlow =
                (alpha << 24) | 0x550000;

        int brightRedMain =
                (alpha << 24) | 0xFF0000;

        String text = "RUN";

        float scale = 4.0F;

        float textWidth =
                font.width(text) * scale;

        float x =
                (screenWidth - textWidth) / 2.0F
                + offsetX;

        float y =
                (screenHeight / 3.0F)
                + offsetY;

        PoseStack poseStack =
                guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(
                x,
                y,
                0.0F
        );

        poseStack.scale(
                scale,
                scale,
                1.0F
        );

        // Koyu kırmızı gölge
        guiGraphics.drawString(
                font,
                text,
                1,
                1,
                darkRedGlow,
                false
        );

        // Parlak kırmızı yazı
        guiGraphics.drawString(
                font,
                text,
                0,
                0,
                brightRedMain,
                false
        );

        poseStack.popPose();
    }
}
