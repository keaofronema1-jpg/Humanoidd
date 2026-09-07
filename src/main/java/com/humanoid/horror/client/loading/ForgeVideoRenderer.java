package com.humanoid.horror.client.loading;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class ForgeVideoRenderer {

    private static final ResourceLocation FORGE_TEXTURE =
            new ResourceLocation(
                    "humanoid",
                    "video/forge.png"
            );

    private ForgeVideoRenderer() {
    }

    public static void init() {
        // Minecraft texture yöneticisi forge.png'yi otomatik yükler.
    }

    public static void render(
            GuiGraphics guiGraphics,
            int screenWidth,
            int screenHeight
    ) {

        if (!ForgeVideoPlayer.isStarted()) {
            return;
        }

        int frame =
                ForgeVideoPlayer.getCurrentFrame();

        if (frame < 0) {
            frame = 0;
        }

        if (frame >= ForgeVideoPlayer.TOTAL_FRAMES) {
            frame =
                    ForgeVideoPlayer.TOTAL_FRAMES - 1;
        }

        /*
         * Frame'in sprite sheet üzerindeki konumu.
         *
         * 28 sütun
         * 28 satır
         */

        int column =
                frame % ForgeVideoPlayer.COLUMNS;

        int row =
                frame / ForgeVideoPlayer.COLUMNS;

        /*
         * Her frame:
         *
         * 384 x 240
         */

        int frameWidth =
                ForgeVideoPlayer.FRAME_WIDTH;

        int frameHeight =
                ForgeVideoPlayer.FRAME_HEIGHT;

        /*
         * Sprite sheet:
         *
         * 384 * 28 = 10752
         * 240 * 28 = 6720
         */

        int textureWidth =
                frameWidth *
                        ForgeVideoPlayer.COLUMNS;

        int textureHeight =
                frameHeight *
                        ForgeVideoPlayer.ROWS;

        /*
         * Ekrana görüntüyü oranını bozmadan
         * mümkün olduğunca büyük çiz.
         */

        float videoAspect =
                (float) frameWidth /
                        (float) frameHeight;

        float screenAspect =
                (float) screenWidth /
                        (float) screenHeight;

        int drawWidth;
        int drawHeight;

        int x;
        int y;

        if (screenAspect > videoAspect) {

            drawHeight =
                    screenHeight;

            drawWidth =
                    Math.round(
                            drawHeight *
                                    videoAspect
                    );

            x =
                    (screenWidth -
                            drawWidth) / 2;

            y = 0;

        } else {

            drawWidth =
                    screenWidth;

            drawHeight =
                    Math.round(
                            drawWidth /
                                    videoAspect
                    );

            x = 0;

            y =
                    (screenHeight -
                            drawHeight) / 2;
        }

        /*
         * Sprite sheet'teki frame'in
         * başlangıç koordinatı.
         */

        int sourceX =
                column *
                        frameWidth;

        int sourceY =
                row *
                        frameHeight;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(
                FORGE_TEXTURE,

                x,
                y,

                drawWidth,
                drawHeight,

                sourceX,
                sourceY,

                frameWidth,
                frameHeight,

                textureWidth,
                textureHeight
        );

        RenderSystem.disableBlend();
    }

    public static void release() {
        /*
         * Texture Minecraft tarafından yönetiliyor.
         * Burada silmiyoruz.
         */
    }
}
