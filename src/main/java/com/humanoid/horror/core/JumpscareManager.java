package com.humanoid.horror.core;

import com.humanoid.horror.HumanoidMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = HumanoidMod.MOD_ID, value = Dist.CLIENT)
public class JumpscareManager {

    public static boolean isJumpscareActive = false;
    private static int jumpscareTimer = 0;
    private static final int MAX_JUMPSCARE_TICKS = 40; // ~2 saniye animasyon

    // Sprite Sheet Tablo Özellikleri (16128x10080 sprite sheet için)
    private static final int SHEET_WIDTH = 16128;
    private static final int SHEET_HEIGHT = 10080;
    private static final int COLUMNS = 12; // Sütun sayısı
    private static final int ROWS = 10;    // Satır sayısı
    private static final int TOTAL_FRAMES = COLUMNS * ROWS;

    private static final int FRAME_WIDTH = SHEET_WIDTH / COLUMNS;
    private static final int FRAME_HEIGHT = SHEET_HEIGHT / ROWS;

    private static int currentFrame = 0;

    // Tam dosya yolu: assets/humanoid/gui/jumpscare.png
    private static final ResourceLocation JUMPSCARE_TEXTURE = 
            new ResourceLocation(HumanoidMod.MOD_ID, "gui/jumpscare.png");
    
    // Ses yolu: assets/humanoid/sounds/scare.ogg
    private static final ResourceLocation SCARE_SOUND = 
            new ResourceLocation(HumanoidMod.MOD_ID, "scare");

    public static void triggerHorror() {
        if (isJumpscareActive) return;

        isJumpscareActive = true;
        jumpscareTimer = MAX_JUMPSCARE_TICKS;
        currentFrame = 0;

        // scare.ogg sesini oynat
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            var soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(SCARE_SOUND);
            if (soundEvent != null) {
                mc.level.playSound(
                        mc.player, 
                        mc.player.blockPosition(), 
                        soundEvent, 
                        SoundSource.MASTER, 
                        1.0F, 
                        1.0F
                );
            }
        }
    }

    public static void stopHorror() {
        isJumpscareActive = false;
        jumpscareTimer = 0;
        currentFrame = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isJumpscareActive) {
            jumpscareTimer--;
            
            // Animasyon karesini hesapla
            currentFrame = (MAX_JUMPSCARE_TICKS - jumpscareTimer) % TOTAL_FRAMES;

            if (jumpscareTimer <= 0) {
                stopHorror();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!isJumpscareActive) return;

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();

            // Aktif kulvardaki UV koordinatı
            int frameX = (currentFrame % COLUMNS) * FRAME_WIDTH;
            int frameY = (currentFrame / COLUMNS) * FRAME_HEIGHT;

            GuiGraphics guiGraphics = event.getGuiGraphics();
            
            guiGraphics.blit(
                    JUMPSCARE_TEXTURE, 
                    0, 0, 
                    screenWidth, screenHeight, 
                    (float) frameX, (float) frameY, 
                    FRAME_WIDTH, FRAME_HEIGHT, 
                    SHEET_WIDTH, SHEET_HEIGHT
            );
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (isJumpscareActive) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                mc.setScreen(null);
            }
        }
    }
}
