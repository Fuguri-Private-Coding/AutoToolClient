package smtc;

import fuguriprivatecoding.autotoolrecode.utils.music.TrackInfo;

public final class SmtcNative {

    public static void init() {
        System.loadLibrary("smtc_bridge");
    }

    public static native boolean nInit();
    public static native void nShutdown();
    public static native TrackInfo nFetchIfChanged(long lastVersion);

    public static native boolean nNext();
    public static native boolean nPrev();
    public static native boolean nStop();
    public static native boolean nTogglePlayPause();
    public static native boolean nPlay();
    public static native boolean nPause();
    public static native boolean nSeek(long positionMs);
}