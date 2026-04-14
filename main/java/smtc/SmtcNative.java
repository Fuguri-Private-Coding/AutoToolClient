package smtc;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;

public final class SmtcNative {

    public static void init() {
        System.loadLibrary("smtc_bridge");
        ClientUtils.chatLog("Успешно инициализировал SMTC Native Bridge.");
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
}