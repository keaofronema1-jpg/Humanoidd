package com.humanoid.horror.client.loading;

import com.humanoid.horror.HumanoidMod;
import org.lwjgl.system.MemoryUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ForgeVideoPlayer {

    private static MediaPlayerFactory factory;
    private static EmbeddedMediaPlayer player;

    private static ByteBuffer frameBuffer;
    private static ByteBuffer snapshotBuffer;

    private static volatile int videoWidth;
    private static volatile int videoHeight;

    private static volatile boolean frameReady;
    private static volatile boolean finished;
    private static volatile boolean started;

    private ForgeVideoPlayer() {
    }

    public static synchronized void startFromResource() {

        if (started) {
            return;
        }

        try {
            Path extractedVideo = extractVideo();

            if (extractedVideo == null) {
                finished = true;
                return;
            }

            start(extractedVideo);

        } catch (Exception e) {
            e.printStackTrace();
            finished = true;
        }
    }

    private static Path extractVideo() throws IOException {

        Path videoPath = HumanoidMod.getVideoPath();

        if (videoPath == null) {
            return null;
        }

        if (Files.exists(videoPath)) {
            return videoPath;
        }

        Files.createDirectories(videoPath.getParent());

        try (InputStream input =
                     ForgeVideoPlayer.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     "assets/humanoid/video/forge.mp4"
                             )) {

            if (input == null) {
                return null;
            }

            Files.copy(
                    input,
                    videoPath
            );
        }

        return videoPath;
    }

    public static synchronized void start(Path video) {

        if (started) {
            return;
        }

        if (video == null) {
            finished = true;
            return;
        }

        if (!Files.exists(video)) {
            finished = true;
            return;
        }

        started = true;
        finished = false;
        frameReady = false;

        videoWidth = 0;
        videoHeight = 0;

        factory = new MediaPlayerFactory();

        player = factory
                .mediaPlayers()
                .newEmbeddedMediaPlayer();

        player.events().addMediaPlayerEventListener(
                new MediaPlayerEventAdapter() {

                    @Override
                    public void finished(MediaPlayer mediaPlayer) {
                        finished = true;
                    }

                    @Override
                    public void error(MediaPlayer mediaPlayer) {
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

                        videoWidth = sourceWidth;
                        videoHeight = sourceHeight;

                        return new RV32BufferFormat(
                                sourceWidth,
                                sourceHeight
                        );
                    }

                    @Override
                    public void allocatedBuffers(
                            ByteBuffer[] buffers
                    ) {
                        // VLCJ tarafından native buffer'lar
                        // burada hazırlanıyor.
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

                        synchronized (ForgeVideoPlayer.class) {

                            int size = buffer.remaining();

                            if (frameBuffer == null ||
                                    frameBuffer.capacity() < size) {

                                if (frameBuffer != null) {
                                    MemoryUtil.memFree(frameBuffer);
                                }

                                frameBuffer =
                                        MemoryUtil.memAlloc(size);
                            }

                            frameBuffer.clear();

                            ByteBuffer source =
                                    buffer.duplicate();

                            frameBuffer.put(source);

                            frameBuffer.flip();

                            frameReady = true;
                        }
                    }

                    @Override
                    public void unlock(
                            MediaPlayer mediaPlayer
                    ) {
                        // Buffer kilidi burada bırakılır.
                        // Frame zaten kendi buffer'ımıza kopyalandı.
                    }
                };

        player.videoSurface().set(
                factory.videoSurfaces().newVideoSurface(
                        bufferFormatCallback,
                        renderCallback,
                        true
                )
        );

        boolean playing =
                player.media().play(
                        video.toAbsolutePath().toString()
                );

        if (!playing) {
            finished = true;
        }
    }

    public static synchronized ByteBuffer getFrameBuffer() {

        if (!frameReady || frameBuffer == null) {
            return null;
        }

        int size = frameBuffer.remaining();

        if (snapshotBuffer == null ||
                snapshotBuffer.capacity() < size) {

            if (snapshotBuffer != null) {
                MemoryUtil.memFree(snapshotBuffer);
            }

            snapshotBuffer =
                    MemoryUtil.memAlloc(size);
        }

        snapshotBuffer.clear();

        ByteBuffer source =
                frameBuffer.duplicate();

        snapshotBuffer.put(source);

        snapshotBuffer.flip();

        return snapshotBuffer.duplicate();
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

        if (frameBuffer != null) {

            try {
                MemoryUtil.memFree(frameBuffer);
            } catch (Exception ignored) {
            }

            frameBuffer = null;
        }

        if (snapshotBuffer != null) {

            try {
                MemoryUtil.memFree(snapshotBuffer);
            } catch (Exception ignored) {
            }

            snapshotBuffer = null;
        }

        videoWidth = 0;
        videoHeight = 0;

        frameReady = false;
        started = false;
    }
}
