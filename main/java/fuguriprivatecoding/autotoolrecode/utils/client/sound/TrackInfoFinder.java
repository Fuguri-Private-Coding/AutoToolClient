package fuguriprivatecoding.autotoolrecode.utils.client.sound;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class TrackInfoFinder {
    private static final User32 user32 = User32.INSTANCE;
    private static String currentTrack = "Неизвестно";

    public static void main(String[] args) {
        // Поиск информации о треке
        findTrackInfo();

        // Управление
//        playPause();
        nextTrack();
    }

    public static void findTrackInfo() {
        // Создаем колбэк для EnumWindows
        WinUser.WNDENUMPROC enumProc = (hwnd, data) -> {
            if (user32.IsWindowVisible(hwnd)) {
                char[] className = new char[256];
                char[] windowText = new char[512];

                user32.GetClassName(hwnd, className, 256);
                user32.GetWindowText(hwnd, windowText, 512);

                String clsName = new String(className).trim();
                String winText = new String(windowText).trim();

                // Проверяем, содержит ли окно информацию о треке
                if (isTrackInfoWindow(clsName, winText)) {
                    currentTrack = winText;
                    System.out.println("Найден трек: " + currentTrack);

                    // Можно также получить дочерние окна для более детальной информации
                    findChildWindows(hwnd);
                }
            }
            return true;
        };

        // Запускаем перебор окон
        user32.EnumWindows(enumProc, null);
    }

    private static boolean isTrackInfoWindow(String className, String windowText) {
        // Проверяем различные паттерны для окон с информацией о треке
        return windowText.matches(".*\\s-\\s.*") && // Формат "Исполнитель - Название"
               !windowText.contains("Settings") &&
               !windowText.contains("Настройки") ||
               className.contains("Media") ||
               className.contains("Music");
    }

    private static void findChildWindows(WinDef.HWND parentHwnd) {
        // Ищем дочерние окна для получения дополнительной информации
        WinUser.WNDENUMPROC childProc = (hwnd, data) -> {
            char[] text = new char[256];
            user32.GetWindowText(hwnd, text, 256);
            String childText = new String(text).trim();

            if (!childText.isEmpty() && !childText.equals(currentTrack)) {
                System.out.println("Доп. информация: " + childText);
            }
            return true;
        };

        user32.EnumChildWindows(parentHwnd, childProc, null);
    }

    public static void playPause() {
        // Используем FindWindow для поиска плеера
        WinDef.HWND hwnd = user32.FindWindow(null, null); // Можно указать конкретное окно

        // Отправляем команду
        sendAppCommand(hwnd, 14); // APPCOMMAND_MEDIA_PLAY_PAUSE = 14
    }

    public static void nextTrack() {
        WinDef.HWND hwnd = user32.FindWindow(null, null);
        sendAppCommand(hwnd, 11); // APPCOMMAND_MEDIA_NEXTTRACK = 11
    }

    private static void sendAppCommand(WinDef.HWND hwnd, int command) {
        if (hwnd != null) {
            // Формируем LPARAM: (command << 16) | 0x2000
            int lParamValue = (command << 16) | 0x2000;

            user32.PostMessage(
                    hwnd,                          // HWND
                    0x0319,                        // WM_APPCOMMAND
                    new WinDef.WPARAM(0),           // WPARAM = 0
                    new WinDef.LPARAM(lParamValue)  // LPARAM с командой
            );
        }
    }
}