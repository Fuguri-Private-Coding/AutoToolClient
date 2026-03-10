package smtc;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class WindowsNowPlayingService implements Closeable {
    @Getter private final ScheduledExecutorService executor;
    @Getter private volatile TrackInfo current = TrackInfo.EMPTY;
    @Getter private volatile BufferedImage artworkImage;
    private volatile long lastVersion = -1L;
    @Getter private volatile long artworkVersion = -1L;

    public WindowsNowPlayingService() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SMTC-Poller");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (!SmtcNative.nInit()) return;
        executor.scheduleWithFixedDelay(this::tick, 0L, 200L, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        try {
            TrackInfo changed = SmtcNative.nFetchIfChanged(lastVersion);

            if (changed == null) {
                return;
            }

            lastVersion = changed.version();
            current = changed;

            if (changed.artworkBytes() != null && changed.artworkBytes().length > 0) {
                artworkImage = ImageIO.read(new ByteArrayInputStream(changed.artworkBytes()));
            } else {
                artworkImage = null;
            }
            artworkVersion = changed.version();
        } catch (Throwable ignored) {}
    }

    @Override
    public void close() {
        executor.shutdown();
//        SmtcNative.nShutdown();
    }
}