package fuguriprivatecoding.autotoolrecode.utils.music;

import lombok.Getter;
import smtc.SmtcNative;
import smtc.TrackInfo;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class MediaController {
    @Getter private final ScheduledExecutorService executor;
    @Getter private volatile TrackInfo current = TrackInfo.EMPTY;
    @Getter private volatile BufferedImage artworkImage;
    @Getter private volatile long lastVersion = -1L;
    @Getter private volatile long artworkVersion = -1L;

    public MediaController() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (!SmtcNative.nInit()) return;
        executor.scheduleWithFixedDelay(this::tick, 0L, 200L, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            TrackInfo changed = SmtcNative.nFetchIfChanged(lastVersion);
            if (changed == null) return;

            if (changed.available()) {
                lastVersion = changed.version();
                current = changed;

                if (changed.artworkBytes() != null && changed.artworkBytes().length > 0) {
                    artworkImage = ImageIO.read(new ByteArrayInputStream(changed.artworkBytes()));
                } else {
                    artworkImage = null;
                }

                artworkVersion = changed.version();
            }
        } catch (Throwable ignored) {}
    }

    public long getInterpolatedPositionMs() {
        TrackInfo info = current;
        if (!info.available()) {
            return 0L;
        }

        long base = info.positionMs();
        if (!info.isPlaying()) {
            return Math.clamp(base, 0L, info.durationMs());
        }

        long now = System.currentTimeMillis();
        long delta = now - info.lastUpdatedEpochMs();

        long interpolated = base + delta;
        return Math.clamp(interpolated, 0L, info.durationMs());
    }

    public boolean next() {
        return SmtcNative.nNext();
    }

    public boolean prev() {
        return SmtcNative.nPrev();
    }

    public boolean stop() {
        return SmtcNative.nStop();
    }

    public boolean playPause() {
        return SmtcNative.nTogglePlayPause();
    }

    public boolean play() {
        return SmtcNative.nPlay();
    }

    public boolean pause() {
        return SmtcNative.nPause();
    }

    public boolean seek(long positionMs) {
        TrackInfo info = current;
        if (!info.available() || !info.seekSupported()) {
            return false;
        }

        long clamped = Math.clamp(positionMs, 0L, info.durationMs());
        return SmtcNative.nSeek(clamped);
    }

    public void close() {
        executor.shutdown();
        SmtcNative.nShutdown();
    }
}