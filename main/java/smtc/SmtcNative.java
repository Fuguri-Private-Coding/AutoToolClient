package smtc;

public final class SmtcNative {
    static {
        System.loadLibrary("smtc_bridge");
    }
    private SmtcNative() {}

    public static native boolean nInit();
    public static native void nShutdown();
    public static native TrackInfo nFetchIfChanged(long lastVersion);

    public static native boolean nNext();
    public static native boolean nPrev();
    public static native boolean nStop();
    public static native boolean nTogglePlayPause();
    public static native boolean nPlay();
    public static native boolean nPause();
}