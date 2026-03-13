package smtc;

public record TrackInfo(long version, String title, String artist, int playbackStatus, boolean available,
                        byte[] artworkBytes, long durationMs, long positionMs, long lastUpdatedEpochMs,
                        boolean seekSupported) {
    enum Status {
        CLOSED, OPENED, CHANGING, STOPPED, PLAYING, PAUSED
    }

    public static final TrackInfo EMPTY =
            new TrackInfo(0L, "", "", Status.CLOSED.ordinal(), false, null, 0L, 0L, 0L, false);

    public boolean isPlaying() {
        return playbackStatus == Status.PLAYING.ordinal();
    }

    public boolean isPaused() {
        return playbackStatus == Status.PAUSED.ordinal();
    }
}