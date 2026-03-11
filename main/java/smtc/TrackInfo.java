package smtc;

public record TrackInfo(long version, String title, String artist, int playbackStatus, boolean available,
                        byte[] artworkBytes, long durationMs, long positionMs, long lastUpdatedEpochMs,
                        boolean seekSupported) {
    public static final int STATUS_CLOSED = 0;
    public static final int STATUS_OPENED = 1;
    public static final int STATUS_CHANGING = 2;
    public static final int STATUS_STOPPED = 3;
    public static final int STATUS_PLAYING = 4;
    public static final int STATUS_PAUSED = 5;

    public static final TrackInfo EMPTY =
            new TrackInfo(0L, "", "", STATUS_CLOSED, false, null, 0L, 0L, 0L, false);

    public boolean isPlaying() {
        return playbackStatus == STATUS_PLAYING;
    }

    public boolean isPaused() {
        return playbackStatus == STATUS_PAUSED;
    }
}