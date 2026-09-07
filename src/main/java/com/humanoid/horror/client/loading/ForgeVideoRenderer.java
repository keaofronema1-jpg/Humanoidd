package com.humanoid.horror.client.loading;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;

/**
 * VLCJ tarafından alınan video frame'lerini
 * Minecraft'ın OpenGL render sistemine aktarır.
 *
 * OpenGL işlemleri yalnızca Minecraft render thread'inde
 * gerçekleştirilir.
 */
public final class ForgeVideoRenderer {

    private static int textureId = -1;

    private static int textureWidth = 0;
    private static int textureHeight = 0;

    /*
     * VLCJ callback thread'inden gelen son frame.
     *
     * Bu buffer OpenGL'a doğrudan verilmez.
     * Önce burada tutulur, sonra render thread'inde
     * texture'a aktarılır.
     */
    private static ByteBuffer pendingFrame;

    private static int pendingWidth = 0;
    private static int pendingHeight = 0;

    private static boolean framePending = false;

    private ForgeVideoRenderer() {
    }

    /**
     * OpenGL texture oluşturur.
     */
    public static synchronized void init() {

        if (textureId != -1) {
            return;
        }

        textureId = GL11.glGenTextures();

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

    /**
     * VLCJ callback'inden gelen frame'i bekleyen frame olarak kaydeder.
     *
     * DİKKAT:
     * Burada OpenGL kullanılmaz.
     *
     * Çünkü VLCJ bu metodu Minecraft render thread'i dışında
     * çağırabilir.
     */
    public static synchronized void uploadFrame(
            ByteBuffer source,
            int width,
            int height
    ) {

        if (source == null) {
            return;
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        int requiredSize = width * height * 4;

        if (source.remaining() < requiredSize) {
            return;
        }

        if (pendingFrame == null ||
                pendingFrame.capacity() < requiredSize) {

            pendingFrame =
                    ByteBuffer.allocateDirect(requiredSize);
        }

        pendingFrame.clear();

        ByteBuffer sourceCopy =
                source.duplicate();

        sourceCopy.rewind();

        /*
         * VLCJ RV32 frame:
         *
         * B G R A
         *
         * Minecraft/OpenGL texture:
         *
         * R G B A
         */
        for (int i = 0; i < width * height; i++) {

            int b = sourceCopy.get() & 0xFF;
            int g = sourceCopy.get() & 0xFF;
            int r = sourceCopy.get() & 0xFF;
            int a = sourceCopy.get() & 0xFF;

            pendingFrame.put((byte) r);
            pendingFrame.put((byte) g);
            pendingFrame.put((byte) b);
            pendingFrame.put((byte) a);
        }

        pendingFrame.flip();

        pendingWidth = width;
        pendingHeight = height;

        framePending = true;
    }

    /**
     * Bekleyen frame'i OpenGL texture'a yükler.
     *
     * Bu metod render thread'inde çağrılmalıdır.
     */
    private static synchronized void uploadPendingFrame() {

        if (!framePending) {
            return;
        }

        if (pendingFrame == null) {
            framePending = false;
            return;
        }

        if (pendingWidth <= 0 ||
                pendingHeight <= 0) {

            framePending = false;
            return;
        }

        if (textureId == -1) {
            init();
        }

        textureWidth = pendingWidth;
        textureHeight = pendingHeight;

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                textureId
        );

        GL11.glPixelStorei(
                GL11.GL_UNPACK_ALIGNMENT,
                1
        );

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                textureWidth,
                textureHeight,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                pendingFrame
        );

        GL11.glBindTexture(
                GL11.GL_TEXTURE_2D,
                0
        );

        framePending = false;
    }

    /**
     * Videoyu ekranın tamamına çizer.
     *
     * Bu metod Minecraft render thread'inden çağrılmalıdır.
     */
    public static synchronized void render(
            GuiGraphics guiGraphics,
            int screenWidth,
            int screenHeight
    ) {

        if (screenWidth <= 0 ||
                screenHeight <= 0) {

            return;
        }

        /*
         * Önce VLCJ'den gelen son frame'i
         * OpenGL texture'a aktar.
         */
        uploadPendingFrame();

        if (textureId == -1) {
            return;
        }

        if (textureWidth <= 0 ||
                textureHeight <= 0) {

            return;
        }

        PoseStack poseStack =
                guiGraphics.pose();

        poseStack.pushPose();

        /*
         * Minecraft'ın position + texture shader'ı.
         */

        RenderSystem.setShader(
                GameRenderer::getPositionTexShader
        );

        /*
         * Oluşturduğumuz OpenGL texture'ını
         * texture unit 0'a bağla.
         */
        RenderSystem.setShaderTexture(
                0,
                textureId
        );

        RenderSystem.enableBlend();

        RenderSystem.defaultBlendFunc();

        /*
         * Ekranın tamamını kaplayan quad.
         *
         * Texture koordinatlarını dikey olarak ters
         * veriyoruz çünkü video buffer ile OpenGL
         * koordinatlarının yönü farklı olabilir.
         */
        BufferBuilder builder =
                Tesselator.getInstance().getBuilder();

        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX
        );

        var matrix =
                poseStack.last().pose();

        builder.vertex(
                matrix,
                0.0F,
                screenHeight,
                0.0F
        ).uv(
                0.0F,
                0.0F
        ).endVertex();

        builder.vertex(
                matrix,
                screenWidth,
                screenHeight,
                0.0F
        ).uv(
                1.0F,
                0.0F
        ).endVertex();

        builder.vertex(
                matrix,
                screenWidth,
                0.0F,
                0.0F
        ).uv(
                1.0F,
                1.0F
        ).endVertex();

        builder.vertex(
                matrix,
                0.0F,
                0.0F,
                0.0F
        ).uv(
                0.0F,
                1.0F
        ).endVertex();

        BufferUploader.drawWithShader(
                builder.end()
        );

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public static synchronized int getTextureId() {
        return textureId;
    }

    public static synchronized int getTextureWidth() {
        return textureWidth;
    }

    public static synchronized int getTextureHeight() {
        return textureHeight;
    }

    public static synchronized boolean hasTexture() {
        return textureId != -1 &&
                textureWidth > 0 &&
                textureHeight > 0;
    }

    /**
     * OpenGL texture ve bekleyen frame'i temizler.
     *
     * Render thread'inde çağrılması gerekir.
     */
    public static synchronized void release() {

        if (textureId != -1) {

            GL11.glDeleteTextures(
                    textureId
            );

            textureId = -1;
        }

        textureWidth = 0;
        textureHeight = 0;

        pendingWidth = 0;
        pendingHeight = 0;

        pendingFrame = null;

        framePending = false;
    }
}
