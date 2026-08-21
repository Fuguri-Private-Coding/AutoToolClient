package fuguriprivatecoding.autotoolrecode.utils.music;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import smtc.SmtcNative;
import smtc.TrackInfo;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

public final class MediaController {
    @Getter private final ScheduledExecutorService executor;
    @Getter private volatile TrackInfo current = TrackInfo.EMPTY;
    @Getter private volatile BufferedImage artworkImage;

    @Getter @Setter
    private volatile BufferedImage lastArtworkImage;

    @Getter private volatile long lastVersion = -1L;
    @Getter private volatile long artworkVersion = -1L;

    @Getter @Setter
    private volatile ResourceLocation songLocation;

    private volatile DynamicTexture dynamicTexture;

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

            if (!changed.available()) {
                return;
            }

            lastVersion = changed.version();
            current = changed;

            byte[] bytes = changed.artworkBytes();

            if (bytes != null && bytes.length > 0) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

                if (image != null) {
                    mc.addScheduledTask(() -> updateTexture(image));
                }
            } else {
                mc.addScheduledTask(this::deleteTexture);
            }

            artworkVersion = changed.version();

        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteTexture() {
        artworkImage = null;

        if (getSongLocation() != null) {
            mc.getTextureManager().deleteTexture(getSongLocation());
            setSongLocation(null);
        }
    }

    private void updateTexture(BufferedImage image) {
        artworkImage = image;

        BufferedImage lastImage = getLastArtworkImage();

        if (image == lastImage) {
            return;
        }

        if (getSongLocation() != null) {
            mc.getTextureManager().deleteTexture(getSongLocation());
        }

        dynamicTexture = new DynamicTexture(image);

        String name = "song_image_" + current.title();

        ResourceLocation songImage =
            mc.getTextureManager().getDynamicTextureLocation(
                name,
                dynamicTexture
            );

        setSongLocation(songImage);
        setLastArtworkImage(image);
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