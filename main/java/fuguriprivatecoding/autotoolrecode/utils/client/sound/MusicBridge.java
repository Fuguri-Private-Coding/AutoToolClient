package fuguriprivatecoding.autotoolrecode.utils.client.sound;

import com.google.gson.*;
import com.sun.jna.platform.win32.User32;
import fuguriprivatecoding.autotoolrecode.Client;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fuguriprivatecoding.autotoolrecode.utils.client.sound.FormatUtils.formatSeconds;

public class MusicBridge {
    private static final int BRIDGE_PORT = 38571;
    private static final String STATE_ENDPOINT = "http://127.0.0.1:" + BRIDGE_PORT + "/state";
    private static final String HEALTH_ENDPOINT = "http://127.0.0.1:" + BRIDGE_PORT + "/health";
    
    private static final Gson json = new GsonBuilder().create();
    private static final File bridgeDir = new File(Client.INST.CLIENT_DIR, "music-bridge");
    private static final File scriptFile = new File(bridgeDir, "windows_media_bridge.ps1");
    private static final Object lock = new Object();
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(400))
            .build();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "DevClient-MusicBridge");
        t.setDaemon(true);
        return t;
    });
    
    private static volatile Process helperProcess = null;
    private static volatile boolean desired = false;
    private static volatile long lastStartAttempt = 0L;
    private static volatile MusicSnapshot currentState = new MusicSnapshot(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Turn on Music Sync", null);
    private static volatile long lastStateSyncAt = 0L;
    private static volatile MusicSnapshot lastRichState = new MusicSnapshot();
    private static volatile boolean resourcesExtracted = false;

    static {
        bridgeDir.mkdirs();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                pollState();
            } catch (Exception ignored) {}
        }, 900L, 850L, TimeUnit.MILLISECONDS);
    }
    
    public static void setEnabled(boolean enabled) {
        desired = enabled;
        if (!enabled) {
            if (helperProcess != null) {
                helperProcess.destroy();
                helperProcess = null;
            }
            lastRichState = new MusicSnapshot();
            currentState = new MusicSnapshot(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Turn on Music Sync", null);
            lastStateSyncAt = 0L;
        }
    }
    
    public static void ensureStarted() {
        desired = true;
        maybeStartHelper(false);
    }
    
    public static MusicSnapshot snapshot() {
        return displayState();
    }
    
    public static String requestLogin() {
        return "Music Sync now uses Windows media sessions. Just enable Music Sync.";
    }
    
    public static boolean sendAction(String action) {
        desired = true;
        maybeStartHelper(false);
        BridgeResponse response = post("action", "{\"action\":\"" + action + "\"}");
        if (response == null) return false;
        applySnapshot(response.snapshot);
        return response.ok;
    }
    
    public static void shutdown() {
        desired = false;
        if (helperProcess != null) {
            helperProcess.destroy();
            helperProcess = null;
        }
        lastRichState = new MusicSnapshot();
        currentState = new MusicSnapshot(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "Turn on Music Sync", null);
        lastStateSyncAt = 0L;
    }
    
    private static void pollState() {
        if (!desired && helperProcess == null) return;
        if (desired) maybeStartHelper(false);
        
        BridgeResponse response = getState();
        if (response != null) {
            applySnapshot(response.snapshot);
            return;
        }
        
        Process process = helperProcess;
        if (desired && (process == null || !process.isAlive())) {
            applySnapshot(currentState.copy(
                false, false, false, false, null, null, null, 0.0, 0.0,
                "Restarting Windows media bridge...", null, null, null, null, null
            ));
            maybeStartHelper(false);
        }
    }
    
    private static void applySnapshot(MusicSnapshot snapshot) {
        MusicSnapshot previousDisplayed = displayState();
        MusicSnapshot merged = mergeWithPrevious(snapshot, previousDisplayed);
        currentState = merged;
        if (!merged.title.isEmpty() || !merged.artist.isEmpty() || merged.durationSeconds > 0.0) {
            lastRichState = merged;
        }
        lastStateSyncAt = System.currentTimeMillis();
    }
    
    private static MusicSnapshot mergeWithPrevious(MusicSnapshot snapshot, MusicSnapshot previousDisplayed) {
        MusicSnapshot remembered = lastRichState;
        boolean rememberedHasTrackInfo = !remembered.title.isEmpty() || !remembered.artist.isEmpty() || remembered.durationSeconds > 0.0;
        boolean incomingHasTrackInfo = !snapshot.title.isEmpty() || !snapshot.artist.isEmpty() || snapshot.durationSeconds > 0.0;
        
        MusicSnapshot merged = snapshot;
        if (!incomingHasTrackInfo && rememberedHasTrackInfo) {
            boolean sameSource = snapshot.sourceApp.isEmpty() || remembered.sourceApp.isEmpty() || snapshot.sourceApp.equals(remembered.sourceApp);
            if (sameSource) {
                merged = snapshot.copy(
                    snapshot.ready, snapshot.connected, snapshot.loggedIn, snapshot.playing,
                    remembered.title,
                    snapshot.artist.isEmpty() ? remembered.artist : snapshot.artist,
                    snapshot.album.isEmpty() ? remembered.album : snapshot.album,
                    snapshot.currentSeconds > 0.0 ? snapshot.currentSeconds : remembered.currentSeconds,
                    snapshot.durationSeconds > 0.0 ? snapshot.durationSeconds : remembered.durationSeconds,
                    snapshot.message,
                    snapshot.detail.isEmpty() ? remembered.detail : snapshot.detail,
                    snapshot.error,
                    snapshot.installCommand,
                    snapshot.artworkUrl,
                    snapshot.sourceApp.isEmpty() ? remembered.sourceApp : snapshot.sourceApp
                );
            }
        }
        
        boolean sameTrack = merged.playing && previousDisplayed.playing &&
            !merged.sourceApp.isEmpty() && previousDisplayed.sourceApp.equals(merged.sourceApp) &&
            !merged.title.isEmpty() && previousDisplayed.title.equals(merged.title) &&
            (merged.artist.isEmpty() || previousDisplayed.artist.isEmpty() || previousDisplayed.artist.equals(merged.artist));
        
        if (sameTrack) {
            double monotonicSeconds = Math.max(merged.currentSeconds, previousDisplayed.currentSeconds);
            merged = merged.copy(
                merged.ready, merged.connected, merged.loggedIn, merged.playing,
                merged.title, merged.artist, merged.album,
                Math.min(monotonicSeconds, merged.durationSeconds > 0.0 ? merged.durationSeconds : monotonicSeconds),
                merged.durationSeconds, merged.message, merged.detail, merged.error,
                merged.installCommand, merged.artworkUrl, merged.sourceApp
            );
        }
        
        return merged;
    }
    
    private static MusicSnapshot displayState() {
        MusicSnapshot snapshot = currentState;
        if (!snapshot.playing || snapshot.durationSeconds <= 0.0) return snapshot;
        
        double elapsedSeconds = Math.max(System.currentTimeMillis() - lastStateSyncAt, 0L) / 1000.0;
        if (elapsedSeconds <= 0.0) return snapshot;
        
        return snapshot.copy(
            snapshot.ready, snapshot.connected, snapshot.loggedIn, snapshot.playing,
            snapshot.title, snapshot.artist, snapshot.album,
            Math.min(snapshot.currentSeconds + elapsedSeconds, snapshot.durationSeconds),
            snapshot.durationSeconds, snapshot.message, snapshot.detail, snapshot.error,
            snapshot.installCommand, snapshot.artworkUrl, snapshot.sourceApp
        );
    }
    
    private static void maybeStartHelper(boolean waitForServer) {
        synchronized(lock) {
            long now = System.currentTimeMillis();
            if (helperProcess != null && helperProcess.isAlive()) {
                if (waitForServer) awaitServer(4000L);
                return;
            }
            if (now - lastStartAttempt < 1200L) return;
            lastStartAttempt = now;
            
            try {
                extractResources();
                Process process = new ProcessBuilder(
                    powerShellExecutable(),
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    scriptFile.getAbsolutePath(),
                    "-Port",
                    String.valueOf(BRIDGE_PORT)
                )
                    .directory(bridgeDir)
                    .redirectErrorStream(true)
                    .start();
                
                helperProcess = process;
                consumeLogs(process, "[media-bridge]");
            } catch (Exception e) {
                applySnapshot(currentState.copy(
                    false, false, false, false, null, null, null, 0.0, 0.0,
                    "", null, "Failed to start Windows media bridge: " + e.getMessage(),
                    null, null, null
                ));
                return;
            }
        }
        
        if (waitForServer) awaitServer(4000L);
    }
    
    private static boolean awaitServer(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (ping()) return true;
            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    private static boolean ping() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(HEALTH_ENDPOINT))
                .timeout(Duration.ofMillis(300))
                .GET()
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static BridgeResponse getState() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(STATE_ENDPOINT))
                .timeout(Duration.ofMillis(450))
                .GET()
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) return null;
            return parseResponse(response.body());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static BridgeResponse post(String path, String payload) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + BRIDGE_PORT + "/" + path))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() > 299) return null;
            return parseResponse(response.body());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static BridgeResponse parseResponse(String body) {
        JsonObject root = json.fromJson(body, JsonObject.class);
        MusicSnapshot snapshot = toSnapshot(root);
        String message = root.get("message").toString();
        if (message.isEmpty()) message = snapshot.chatMessage;
        boolean ok = root.get("ok").getAsBoolean();
        return new BridgeResponse(ok, snapshot, message);
    }
    
    private static MusicSnapshot toSnapshot(JsonObject obj) {
        return new MusicSnapshot(
            obj.get("ready").getAsBoolean(),
            obj.get("connected").getAsBoolean(),
            obj.get("logged_in").getAsBoolean(),
            obj.get("playing").getAsBoolean(),
            obj.get("title").toString(),
            obj.get("artist").toString(),
            obj.get("album").toString(),
            obj.get("current_seconds").getAsDouble(),
            obj.get("duration_seconds").getAsDouble(),
            obj.get("message").getAsString(),
            obj.get("detail").getAsString(),
            obj.get("error").getAsString(),
            obj.get("install_command").getAsString(),
            obj.get("artwork_url").getAsString(),
            obj.get("source_app").getAsString(),
            "",
            null
        );
    }
    
    private static void extractResources() {
        if (resourcesExtracted && scriptFile.exists()) return;
        
        synchronized(lock) {
            if (resourcesExtracted && scriptFile.exists()) return;
            copyResource("music/windows_media_bridge.ps1", scriptFile);
            resourcesExtracted = true;
        }
    }
    
    private static void copyResource(String resourcePath, File target) {
        InputStream stream = MusicBridge.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) throw new RuntimeException("Missing resource " + resourcePath);
        try (InputStream input = stream; OutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy resource", e);
        }
    }
    
    private static void consumeLogs(Process process, String prefix) {
        new Thread(() -> {
            try {
                process.getInputStream().transferTo(System.out);
            } catch (Exception ignored) {}
        }, "DevClient-MusicLogs").start();
    }
    
    private static String powerShellExecutable() {
        String winDir = System.getenv("WINDIR");
        if (winDir == null) return "powershell.exe";
        File candidate = new File(winDir, "System32\\WindowsPowerShell\\v1.0\\powershell.exe");
        return candidate.exists() ? candidate.getAbsolutePath() : "powershell.exe";
    }
    
    private static class BridgeResponse {
        final boolean ok;
        final MusicSnapshot snapshot;
        final String message;
        
        BridgeResponse(boolean ok, MusicSnapshot snapshot, String message) {
            this.ok = ok;
            this.snapshot = snapshot;
            this.message = message;
        }
    }
}

class MusicSnapshot {
    final boolean ready;
    final boolean connected;
    final boolean loggedIn;
    final boolean playing;
    final String title;
    final String artist;
    final String album;
    final double currentSeconds;
    final double durationSeconds;
    final String message;
    final String detail;
    final String error;
    final String installCommand;
    final String artworkUrl;
    final String sourceApp;
    final String chatMessage;
    
    public MusicSnapshot() {
        this(false, false, false, false, "", "", "", 0.0, 0.0, "", "", "", "", "", "", "", null);
    }
    
    public MusicSnapshot(
            Boolean ready, Boolean connected, Boolean loggedIn, Boolean playing,
            String title, String artist, String album,
            Double currentSeconds, Double durationSeconds,
            String message, String detail, String error,
            String installCommand, String artworkUrl, String sourceApp,
            String chatMessage, String extra) {
        this.ready = ready != null ? ready : false;
        this.connected = connected != null ? connected : false;
        this.loggedIn = loggedIn != null ? loggedIn : false;
        this.playing = playing != null ? playing : false;
        this.title = title != null ? title : "";
        this.artist = artist != null ? artist : "";
        this.album = album != null ? album : "";
        this.currentSeconds = currentSeconds != null ? currentSeconds : 0.0;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 0.0;
        this.message = message != null ? message : "";
        this.detail = detail != null ? detail : "";
        this.error = error != null ? error : "";
        this.installCommand = installCommand != null ? installCommand : "";
        this.artworkUrl = artworkUrl != null ? artworkUrl : "";
        this.sourceApp = sourceApp != null ? sourceApp : "";
        this.chatMessage = chatMessage != null ? chatMessage : "";
    }
    
    public boolean getHasVisualState() {
        return connected || !title.isEmpty();
    }
    
    public float getProgress() {
        if (durationSeconds > 0.0) {
            return (float) Math.min(Math.max(currentSeconds / durationSeconds, 0.0), 1.0);
        }
        return 0f;
    }
    
    public String getDisplayTitle() {
        if (!title.isEmpty()) return title;
        if (connected) return "Media Player";
        return "Music Sync";
    }
    
    public String getDisplaySubtitle() {
        if (!artist.isEmpty()) return artist;
        if (!album.isEmpty()) return album;
        if (connected && title.isEmpty()) return "Ready to control";
        return "";
    }
    
    public String getTimeLabel() {
        if (durationSeconds > 0.0 || currentSeconds > 0.0) {
            return formatSeconds(currentSeconds) + " / " + formatSeconds(durationSeconds);
        }
        return "--:-- / --:--";
    }
    
    public String getActionChip() {
        if (playing) return "PLAYING";
        if (connected && !title.isEmpty()) return "PAUSED";
        if (connected) return "READY";
        return "IDLE";
    }
    
    public String getAppBadge() {
        String normalized = sourceApp
            .replaceAll(".*[\\\\/]", "")
            .replaceAll("\\.[^.]*$", "")
            .trim()
            .toLowerCase();
        
        if (normalized.contains("spotify")) return "SP";
        if (normalized.contains("chrome") || normalized.contains("edge") || 
            normalized.contains("firefox") || normalized.contains("opera")) return "WEB";
        if (normalized.contains("music") || normalized.contains("media") || 
            normalized.contains("zune") || normalized.contains("groove")) return "WIN";
        if (normalized.length() >= 3) return normalized.substring(0, 3).toUpperCase();
        if (normalized.length() == 2) return normalized.toUpperCase();
        if (normalized.length() == 1) return normalized.toUpperCase();
        return "MM";
    }
    
    public MusicSnapshot copy(
            Boolean ready, Boolean connected, Boolean loggedIn, Boolean playing,
            String title, String artist, String album,
            Double currentSeconds, Double durationSeconds,
            String message, String detail, String error,
            String installCommand, String artworkUrl, String sourceApp) {
        return new MusicSnapshot(
            ready != null ? ready : this.ready,
            connected != null ? connected : this.connected,
            loggedIn != null ? loggedIn : this.loggedIn,
            playing != null ? playing : this.playing,
            title != null ? title : this.title,
            artist != null ? artist : this.artist,
            album != null ? album : this.album,
            currentSeconds != null ? currentSeconds : this.currentSeconds,
            durationSeconds != null ? durationSeconds : this.durationSeconds,
            message != null ? message : this.message,
            detail != null ? detail : this.detail,
            error != null ? error : this.error,
            installCommand != null ? installCommand : this.installCommand,
            artworkUrl != null ? artworkUrl : this.artworkUrl,
            sourceApp != null ? sourceApp : this.sourceApp,
            this.chatMessage,
            null
        );
    }
}

class JsonObjectExtensions {
    public static String stringValue(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && element instanceof JsonPrimitive) {
            String value = ((JsonPrimitive) element).getAsString();
            return value != null ? value : "";
        }
        return "";
    }
    
    public static boolean booleanValue(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && element instanceof JsonPrimitive) {
            Boolean value = ((JsonPrimitive) element).getAsBoolean();
            return value != null ? value : false;
        }
        return false;
    }
    
    public static double doubleValue(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && element instanceof JsonPrimitive) {
            Double value = ((JsonPrimitive) element).getAsDouble();
            return value != null ? value : 0.0;
        }
        return 0.0;
    }
}

class FormatUtils {
    public static String formatSeconds(double seconds) {
        if (!Double.isFinite(seconds) || seconds <= 0.0) return "0:00";
        int total = Math.max((int) seconds, 0);
        int mins = total / 60;
        int secs = total % 60;
        return String.format("%d:%02d", mins, secs);
    }
}