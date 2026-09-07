package com.humanoid.horror.client.loading;

import net.minecraft.client.Minecraft;

import org.lwjgl.system.MemoryUtil;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ForgeVideoPlayer {

    private static final String VIDEO_RESOURCE =
            "/assets/humanoid/video/forge.mp4";

    private static MediaPlayerFactory factory;
    private static EmbeddedMediaPlayer player;

    private static ByteBuffer writeBuffer;
    private static ByteBuffer readBuffer;

    private static volatile int videoWidth;
    private static volatile int videoHeight;

    private static volatile boolean frameReady;
    private static volatile boolean finished;
    private static volatile boolean started;

    private ForgeVideoPlayer() {
    }

    /**
     * Mod jar içerisindeki forge.mp4 dosyasını çıkarıp oynatır.
     */
    public static synchronized void startFromResource() {

        if (started) {
            return;
        }

        try {

            Minecraft minecraft =
                    Minecraft.getInstance();

            Path videoPath =
                    minecraft.gameDirectory
                            .toPath()
                            .resolve("humanoid_forge_loading.mp4");

            try (InputStream input =
                         ForgeVideoPlayer.class
                                 .getResourceAsStream(
                                         VIDEO_RESOURCE
                                 )) {

                if (input == null) {

                    finished = true;
                    return;
                }

                Files.copy(
                        input,
                        videoPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            start(videoPath);

        } catch (Exception e) {

            finished = true;
        }
    }

    public static synchronized void start(Path video) {

        if (started) {
            return;
        }

        if (video == null ||
                !Files.exists(video)) {

            finished = true;
            return;
        }

        started = true;
        finished = false;
        frameReady = false;

        try {

            factory =
                    new MediaPlayerFactory();

            player =
                    factory
                            .mediaPlayers()
                            .newEmbeddedMediaPlayer();

            player.events()
                    .addMediaPlayerEventListener(
                            new MediaPlayerEventAdapter() {

                                @Override
                                public void finished(
                                        MediaPlayer mediaPlayer
                                ) {

                                    finished = true;
                                }

                                @Override
                                public void error(
                                        MediaPlayer mediaPlayer
                                ) {

                                    finished = true;
                                }
                            }
                    );

            BufferFormatCallback bufferFormatCallback =
                    new BufferFormatCallback() {

                        @Override
                        public BufferFormat getBufferFormat(
                                int sourceWidth,
                                int sourceHeight
                        ) {

                            videoWidth =
                                    sourceWidth;

                            videoHeight =
                                    sourceHeight;

                            return new RV32BufferFormat(
                                    sourceWidth,
                                    sourceHeight
                            );
                        }
                    };

            RenderCallback renderCallback =
                    new RenderCallback() {

                        @Override
                        public void onDisplay(
                                MediaPlayer mediaPlayer,
                                ByteBuffer buffer
                        ) {

                            if (buffer == null) {
                                return;
                            }

                            int size =
                                    buffer.remaining();

                            if (size <= 0) {
                                return;
                            }

                            synchronized (
                                    ForgeVideoPlayer.class
                            ) {

                                if (writeBuffer == null ||
                                        writeBuffer.capacity() < size) {

                                    if (writeBuffer != null) {
                                        MemoryUtil.memFree(
                                                writeBuffer
                                        );
                                    }

                                    writeBuffer =
                                            MemoryUtil.memAlloc(size);
                                }

                                writeBuffer.clear();

                                ByteBuffer source =
                                        buffer.duplicate();

                                source.rewind();

                                writeBuffer.put(source);

                                writeBuffer.flip();

                                ByteBuffer oldRead =
                                        readBuffer;

                                readBuffer =
                                        writeBuffer;

                                writeBuffer =
                                        oldRead;

                                frameReady = true;
                            }
                        }
                    };

            player.videoSurface().set(
                    factory.videoSurfaces()
                            .newVideoSurface(
                                    bufferFormatCallback,
                                    renderCallback,
                                    true
                            )
            );

            player.media().play(
                    video.toAbsolutePath().toString()
            );

        } catch (Exception e) {

            finished = true;

            cleanupInternal();
        }
    }

    public static synchronized ByteBuffer getFrameBuffer() {

        if (!frameReady ||
                readBuffer == null) {

            return null;
        }

        ByteBuffer result =
                readBuffer.duplicate();

        result.rewind();

        return result;
    }

    public static boolean hasFrame() {
        return frameReady;
    }

    public static int getVideoWidth() {
        return videoWidth;
    }

    public static int getVideoHeight() {
        return videoHeight;
    }

    public static boolean isFinished() {
        return finished;
    }

    public static boolean isStarted() {
        return started;
    }

    public static synchronized void stop() {

        finished = true;

        cleanupInternal();
    }

    private static void cleanupInternal() {

        if (player != null) {

            try {
                player.controls().stop();
            } catch (Exception ignored) {
            }

            try {
                player.release();
            } catch (Exception ignored) {
            }

            player = null;
        }

        if (factory != null) {

            try {
                factory.release();
            } catch (Exception ignored) {
            }

            factory = null;
        }

        if (writeBuffer != null) {

            try {
                MemoryUtil.memFree(
                        writeBuffer
                );
            } catch (Exception ignored) {
            }

            writeBuffer = null;
        }

        if (readBuffer != null) {

            try {
                MemoryUtil.memFree(
                        readBuffer
                );
            } catch (Exception ignored) {
            }

            readBuffer = null;
        }

        videoWidth = 0;
        videoHeight = 0;

        frameReady = false;
        started = false;
    }
}
