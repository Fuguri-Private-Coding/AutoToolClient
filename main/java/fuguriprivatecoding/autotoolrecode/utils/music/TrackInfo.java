package fuguriprivatecoding.autotoolrecode.utils.music;

public record TrackInfo(long version, String title, String artist, Status playbackStatus, boolean available,
                        byte[] artworkBytes, long durationMs, long positionMs, long lastUpdatedEpochMs,
                        boolean seekSupported) {
    enum Status {
        CLOSED, OPENED, CHANGING, STOPPED, PLAYING, PAUSED
    }

    public static final TrackInfo EMPTY =
            new TrackInfo(0L, "", "", Status.CLOSED, false, null, 0L, 0L, 0L, false);

    public boolean isPlaying() {
        return playbackStatus == Status.PLAYING;
    }

    public boolean isPaused() {
        return playbackStatus == Status.PAUSED;
    }
}