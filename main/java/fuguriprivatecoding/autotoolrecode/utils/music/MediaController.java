package fuguriprivatecoding.autotoolrecode.utils.music;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;
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

    @Getter @Setter
    private volatile BufferedImage lastArtworkImage;

    @Getter private volatile long lastVersion = -1L;
    @Getter private volatile long artworkVersion = -1L;

    @Getter @Setter
    private ResourceLocation songLocation;

    public MediaController() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (!SmtcNative.nInit()) return;
        executor.scheduleWithFixedDelay(this::tick, 0L, 200L, TimeUnit.MILLISECONDS);
        ClientUtils.chatLog("Медиаконтроллер запущен.");
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

    public boolean next() {
        if (!current.available()) return false;
        return SmtcNative.nNext();
    }

    public boolean prev() {
        if (!current.available()) return false;
        return SmtcNative.nPrev();
    }

    public boolean stop() {
        if (!current.available()) return false;
        return SmtcNative.nStop();
    }

    public boolean playPause() {
        if (!current.available()) return false;
        return SmtcNative.nTogglePlayPause();
    }

    public boolean play() {
        if (!current.available()) return false;
        return SmtcNative.nPlay();
    }

    public boolean pause() {
        if (!current.available()) return false;
        return SmtcNative.nPause();
    }

    public void close() {
        executor.shutdown();
        SmtcNative.nShutdown();
    }
}