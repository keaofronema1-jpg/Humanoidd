package com.humanoid.horror.core;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.event.HorrorTriggerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        value = Dist.CLIENT
)
public class JumpscareManager {

    public static boolean isJumpscareActive = false;

    private static int jumpscareTimer = 0;

    private static final int MAX_JUMPSCARE_TICKS = 40;

    // =========================================================
    // SPRITE SHEET
    // =========================================================

    private static final int SHEET_WIDTH = 16128;
    private static final int SHEET_HEIGHT = 10080;

    private static final int COLUMNS = 12;
    private static final int ROWS = 10;

    private static final int TOTAL_FRAMES = COLUMNS * ROWS;

    private static final int FRAME_WIDTH =
            SHEET_WIDTH / COLUMNS;

    private static final int FRAME_HEIGHT =
            SHEET_HEIGHT / ROWS;

    private static int currentFrame = 0;

    // =========================================================
    // TEXTURE
    // assets/humanoid/gui/jumpscare.png
    // =========================================================

    private static final ResourceLocation JUMPSCARE_TEXTURE =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "gui/jumpscare.png"
            );

    // =========================================================
    // SOUND
    // assets/humanoid/sounds/scare.ogg
    // =========================================================

    private static final ResourceLocation SCARE_SOUND =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "scare"
            );

    // =========================================================
    // JUMPSCARE BAŞLAT
    // =========================================================

    public static void triggerHorror() {

        if (isJumpscareActive) {
            return;
        }

        isJumpscareActive = true;
        jumpscareTimer = MAX_JUMPSCARE_TICKS;
        currentFrame = 0;

        Minecraft mc = Minecraft.getInstance();

        // -----------------------------------------------------
        // SES
        // -----------------------------------------------------

        if (mc.player != null && mc.level != null) {

            var soundEvent =
                    ForgeRegistries.SOUND_EVENTS.getValue(
                            SCARE_SOUND
                    );

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

        // -----------------------------------------------------
        // WINDOWS WALLPAPER EVENT
        // -----------------------------------------------------

        /*
         * WallpaperEventHandler bu event'i dinliyor.
         *
         * Windows'ta:
         *
         * JumpscareManager
         *       ↓
         * HorrorTriggerEvent
         *       ↓
         * WallpaperEventHandler
         *       ↓
         * Windows wallpaper değişir
         *
         * Windows olmayan sistemlerde
         * WallpaperEventHandler zaten geri döner.
         */

        MinecraftForge.EVENT_BUS.post(
                new HorrorTriggerEvent()
        );
    }

    // =========================================================
    // PCKeyHandler İLE UYUMLULUK
    // =========================================================

    public static void triggerJumpscare() {
        triggerHorror();
    }

    // =========================================================
    // JUMPSCARE DURDUR
    // =========================================================

    public static void stopHorror() {

        isJumpscareActive = false;

        jumpscareTimer = 0;

        currentFrame = 0;
    }

    // =========================================================
    // CLIENT TICK
    // =========================================================

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!isJumpscareActive) {
            return;
        }

        jumpscareTimer--;

        currentFrame =
                (MAX_JUMPSCARE_TICKS - jumpscareTimer)
                        % TOTAL_FRAMES;

        if (jumpscareTimer <= 0) {
            stopHorror();
        }
    }

    // =========================================================
    // EKRANA JUMPSCARE ÇİZ
    // =========================================================

    @SubscribeEvent
    public static void onRenderOverlay(
            RenderGuiOverlayEvent.Post event
    ) {

        if (!isJumpscareActive) {
            return;
        }

        /*
         * Sadece crosshair overlay aşamasında çiziyoruz.
         */
        if (!event.getOverlay().id().equals(
                VanillaGuiOverlay.CROSSHAIR.id()
        )) {
            return;
        }

        int screenWidth =
                event.getWindow().getGuiScaledWidth();

        int screenHeight =
                event.getWindow().getGuiScaledHeight();

        // Sprite sheet X
        int frameX =
                (currentFrame % COLUMNS)
                        * FRAME_WIDTH;

        // Sprite sheet Y
        int frameY =
                (currentFrame / COLUMNS)
                        * FRAME_HEIGHT;

        GuiGraphics guiGraphics =
                event.getGuiGraphics();

        guiGraphics.blit(
                JUMPSCARE_TEXTURE,

                // Ekrandaki konum
                0,
                0,

                // Ekrandaki boyut
                screenWidth,
                screenHeight,

                // Sprite sheet UV başlangıcı
                (float) frameX,
                (float) frameY,

                // Kaynak karenin boyutu
                FRAME_WIDTH,
                FRAME_HEIGHT,

                // Texture'ın tamamının boyutu
                SHEET_WIDTH,
                SHEET_HEIGHT
        );
    }
}
