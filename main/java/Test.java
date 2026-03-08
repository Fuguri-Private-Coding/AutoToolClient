import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

    public static class MediaSessionInfo {
        private String title;
        private String artist;
        private String albumTitle;
        private String albumArtist;
        private Integer trackNumber;
        private String subtitle;
        private String playbackStatus;
        private boolean isPlaying;
        private boolean hasThumbnail;
        private String errorMessage;

        // Геттеры и сеттеры
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }

        public String getAlbumTitle() { return albumTitle; }
        public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }

        public String getAlbumArtist() { return albumArtist; }
        public void setAlbumArtist(String albumArtist) { this.albumArtist = albumArtist; }

        public Integer getTrackNumber() { return trackNumber; }
        public void setTrackNumber(Integer trackNumber) { this.trackNumber = trackNumber; }

        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

        public String getPlaybackStatus() { return playbackStatus; }
        public void setPlaybackStatus(String playbackStatus) { this.playbackStatus = playbackStatus; }

        public boolean isPlaying() { return isPlaying; }
        public void setPlaying(boolean playing) { isPlaying = playing; }

        public boolean isHasThumbnail() { return hasThumbnail; }
        public void setHasThumbnail(boolean hasThumbnail) { this.hasThumbnail = hasThumbnail; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        @Override
        public String toString() {
            if (errorMessage != null) {
                return "MediaSessionInfo{error='" + errorMessage + "'}";
            }
            return String.format(
                "MediaSessionInfo{title='%s', artist='%s', album='%s', status='%s', playing=%s}",
                title, artist, albumTitle, playbackStatus, isPlaying
            );
        }
    }

    public static MediaSessionInfo getCurrentMediaInfo() {
        // Читаем скрипт из файла или используем как строку
        String script = loadPowerShellScript();

        ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", script);
        processBuilder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("PowerShell exited with error code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        // Парсим JSON ответ
        return parseMediaInfo(output.toString());
    }

    private static String loadPowerShellScript() {
        // Здесь полный PowerShell скрипт из предыдущего ответа
        return "Add-Type -AssemblyName System.Runtime.WindowsRuntime\r\n" +
            "$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | ? { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\r\n" +
            "\r\n" +
            "Function Await($WinRtTask, $ResultType) {\r\n" +
            "    $asTask = $asTaskGeneric.MakeGenericMethod($ResultType)\r\n" +
            "    $netTask = $asTask.Invoke($null, @($WinRtTask))\r\n" +
            "    $netTask.Wait(-1) | Out-Null\r\n" +
            "    $netTask.Result\r\n" +
            "}\r\n" +
            "\r\n" +
            "[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.System, ContentType = WindowsRuntime] | Out-Null\r\n" +
            "\r\n" +
            "$manager = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\r\n" +
            "\r\n" +
            "if ($manager -ne $null) {\r\n" +
            "    $session = $manager.GetCurrentSession()\r\n" +
            "    \r\n" +
            "    if ($session -ne $null) {\r\n" +
            "        try {\r\n" +
            "            $mediaProperties = Await ($session.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\r\n" +
            "            $playbackInfo = $session.GetPlaybackInfo()\r\n" +
            "            $playbackStatus = $playbackInfo.PlaybackStatus\r\n" +
            "            \r\n" +
            "            $result = @{\r\n" +
            "                Title = if ($mediaProperties.Title) { $mediaProperties.Title } else { $null }\r\n" +
            "                Artist = if ($mediaProperties.Artist) { $mediaProperties.Artist } else { $null }\r\n" +
            "                AlbumTitle = if ($mediaProperties.AlbumTitle) { $mediaProperties.AlbumTitle } else { $null }\r\n" +
            "                AlbumArtist = if ($mediaProperties.AlbumArtist) { $mediaProperties.AlbumArtist } else { $null }\r\n" +
            "                TrackNumber = if ($mediaProperties.TrackNumber -ne 0) { $mediaProperties.TrackNumber } else { $null }\r\n" +
            "                Subtitle = if ($mediaProperties.Subtitle) { $mediaProperties.Subtitle } else { $null }\r\n" +
            "                PlaybackStatus = $playbackStatus.ToString()\r\n" +
            "                IsPlaying = ($playbackStatus -eq [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus]::Playing)\r\n" +
            "                HasThumbnail = ($mediaProperties.Thumbnail -ne $null)\r\n" +
            "                ErrorMessage = $null\r\n" +
            "            }\r\n" +
            "            \r\n" +
            "            $result | ConvertTo-Json -Compress\r\n" +
            "        }\r\n" +
            "        catch {\r\n" +
            "            $errorResult = @{\r\n" +
            "                Title = $null\r\n" +
            "                Artist = $null\r\n" +
            "                AlbumTitle = $null\r\n" +
            "                AlbumArtist = $null\r\n" +
            "                TrackNumber = $null\r\n" +
            "                Subtitle = $null\r\n" +
            "                PlaybackStatus = \"Error\"\r\n" +
            "                IsPlaying = $false\r\n" +
            "                HasThumbnail = $false\r\n" +
            "                ErrorMessage = $_.Exception.Message\r\n" +
            "            }\r\n" +
            "            $errorResult | ConvertTo-Json -Compress\r\n" +
            "        }\r\n" +
            "    }\r\n" +
            "    else {\r\n" +
            "        $noSessionResult = @{\r\n" +
            "            Title = $null\r\n" +
            "            Artist = $null\r\n" +
            "            AlbumTitle = $null\r\n" +
            "            AlbumArtist = $null\r\n" +
            "            TrackNumber = $null\r\n" +
            "            Subtitle = $null\r\n" +
            "            PlaybackStatus = \"NoSession\"\r\n" +
            "            IsPlaying = $false\r\n" +
            "            HasThumbnail = $false\r\n" +
            "            ErrorMessage = \"No active media session\"\r\n" +
            "        }\r\n" +
            "        $noSessionResult | ConvertTo-Json -Compress\r\n" +
            "    }\r\n" +
            "}\r\n" +
            "else {\r\n" +
            "    $errorResult = @{\r\n" +
            "        Title = $null\r\n" +
            "        Artist = $null\r\n" +
            "        AlbumTitle = $null\r\n" +
            "        AlbumArtist = $null\r\n" +
            "        TrackNumber = $null\r\n" +
            "        Subtitle = $null\r\n" +
            "        PlaybackStatus = \"Error\"\r\n" +
            "        IsPlaying = $false\r\n" +
            "        HasThumbnail = $false\r\n" +
            "        ErrorMessage = \"Failed to get media manager\"\r\n" +
            "    }\r\n" +
            "    $errorResult | ConvertTo-Json -Compress\r\n" +
            "}";
    }

    private static MediaSessionInfo parseMediaInfo(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            JsonNode root = mapper.readTree(json);
            MediaSessionInfo info = new MediaSessionInfo();

            info.setTitle(getTextValue(root, "Title"));
            info.setArtist(getTextValue(root, "Artist"));
            info.setAlbumTitle(getTextValue(root, "AlbumTitle"));
            info.setAlbumArtist(getTextValue(root, "AlbumArtist"));
            info.setTrackNumber(getIntValue(root, "TrackNumber"));
            info.setSubtitle(getTextValue(root, "Subtitle"));
            info.setPlaybackStatus(getTextValue(root, "PlaybackStatus"));
            info.setPlaying(root.has("IsPlaying") && root.get("IsPlaying").asBoolean());
            info.setHasThumbnail(root.has("HasThumbnail") && root.get("HasThumbnail").asBoolean());
            info.setErrorMessage(getTextValue(root, "ErrorMessage"));

            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getTextValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private static Integer getIntValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asInt() : null;
    }

    // Пример использования
    public static void main(String[] args) {
        MediaSessionInfo mediaInfo = getCurrentMediaInfo();

        if (mediaInfo != null) {
            if (mediaInfo.getErrorMessage() != null) {
                System.out.println("Error: " + mediaInfo.getErrorMessage());
            } else {
                System.out.println("Currently playing:");
                System.out.println("Title: " + mediaInfo.getTitle());
                System.out.println("Artist: " + mediaInfo.getArtist());
                System.out.println("Album: " + mediaInfo.getAlbumTitle());
                System.out.println("Status: " + mediaInfo.getPlaybackStatus());
                System.out.println("Is Playing: " + mediaInfo.isPlaying());
                System.out.println("Has Thumbnail: " + mediaInfo.isHasThumbnail());
            }
        } else {
            System.out.println("Failed to get media information");
        }
    }
}