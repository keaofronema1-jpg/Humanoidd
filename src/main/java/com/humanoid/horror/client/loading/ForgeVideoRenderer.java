package com.humanoid.horror.client.loading;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;

public final class ForgeVideoRenderer {

    private static int textureId = -1;

    private static int textureWidth = 0;
    private static int textureHeight = 0;

    private static ByteBuffer pendingFrame;

    private static int pendingWidth = 0;
    private static int pendingHeight = 0;

    private static boolean framePending = false;

    private ForgeVideoRenderer() {
    }

    public static synchronized void init() {

        if (textureId != -1) {
            return;
        }

        textureId =
                GL11.glGenTextures();

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                textureId
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_LINEAR
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_S,
                GL12.GL_CLAMP_TO_EDGE
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_WRAP_T,
                GL12.GL_CLAMP_TO_EDGE
        );

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                0
        );
    }

    public static synchronized void uploadFrame(
            ByteBuffer source,
            int width,
            int height
    ) {

        if (source == null ||
                width <= 0 ||
                height <= 0) {

            return;
        }

        init();

        int requiredSize =
                width * height * 4;

        if (pendingFrame == null ||
                pendingFrame.capacity()
                        < requiredSize) {

            pendingFrame =
                    ByteBuffer.allocateDirect(
                            requiredSize
                    );
        }

        pendingFrame.clear();

        ByteBuffer input =
                source.duplicate();

        input.rewind();

        int pixelCount =
                width * height;

        for (int i = 0;
             i < pixelCount;
             i++) {

            if (input.remaining() < 4) {
                break;
            }

            /*
             * VLCJ RV32:
             *
             * B G R A
             *
             * OpenGL:
             *
             * R G B A
             */

            byte b = input.get();
            byte g = input.get();
            byte r = input.get();
            byte a = input.get();

            pendingFrame.put(r);
            pendingFrame.put(g);
            pendingFrame.put(b);
            pendingFrame.put(a);
        }

        pendingFrame.flip();

        pendingWidth =
                width;

        pendingHeight =
                height;

        framePending = true;
    }

    private static synchronized void uploadPendingFrame() {

        if (!framePending ||
                pendingFrame == null) {

            return;
        }

        if (textureId == -1) {
            init();
        }

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                textureId
        );

        if (textureWidth != pendingWidth ||
                textureHeight != pendingHeight) {

            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL11.GL_RGBA8,
                    pendingWidth,
                    pendingHeight,
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    pendingFrame
            );

            textureWidth =
                    pendingWidth;

            textureHeight =
                    pendingHeight;

        } else {

            GL11.glTexSubImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    pendingWidth,
                    pendingHeight,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    pendingFrame
            );
        }

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                0
        );

        framePending = false;
    }

    public static synchronized void render(
            GuiGraphics guiGraphics,
            int screenWidth,
            int screenHeight
    ) {

        if (textureId == -1) {
            return;
        }

        uploadPendingFrame();

        if (textureWidth <= 0 ||
                textureHeight <= 0) {

            return;
        }

        RenderSystem.setShader(
                GameRenderer::getPositionTexShader
        );

        RenderSystem.setShaderTexture(
                0,
                textureId
        );

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();

        BufferBuilder builder =
                Tesselator
                        .getInstance()
                        .getBuilder();

        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX
        );

        /*
         * Full-screen quad
         */

        builder.vertex(
                0.0D,
                screenHeight,
                0.0D
        ).uv(
                0.0F,
                1.0F
        ).endVertex();

        builder.vertex(
                screenWidth,
                screenHeight,
                0.0D
        ).uv(
                1.0F,
                1.0F
        ).endVertex();

        builder.vertex(
                screenWidth,
                0.0D,
                0.0D
        ).uv(
                1.0F,
                0.0F
        ).endVertex();

        builder.vertex(
                0.0D,
                0.0D,
                0.0D
        ).uv(
                0.0F,
                0.0F
        ).endVertex();

        BufferUploader.drawWithShader(
                builder.end()
        );

        RenderSystem.disableBlend();
    }

    public static synchronized void release() {

        if (textureId != -1) {

            GL11.glDeleteTextures(
                    textureId
            );

            textureId = -1;
        }

        pendingFrame = null;

        textureWidth = 0;
        textureHeight = 0;

        pendingWidth = 0;
        pendingHeight = 0;

        framePending = false;
    }
}
