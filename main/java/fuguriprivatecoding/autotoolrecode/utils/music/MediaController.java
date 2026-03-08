package fuguriprivatecoding.autotoolrecode.utils.music;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.experimental.UtilityClass;

// created by dicves_recode on 09.03.2026
@UtilityClass
public class MediaController {
    private final User32 USER_32 = User32.INSTANCE;
    private final WinDef.WPARAM WPARAM0 = new WinDef.WPARAM(0);

    private WinDef.HWND getHWND() {
        return USER_32.FindWindow(null, null);
    }

    private void sendCommand(int command) {
        WinDef.HWND hwnd = getHWND();

        if (hwnd != null) {
            int lParamValue = (command << 16) | 0x2000;

            USER_32.PostMessage(
                hwnd,
                0x0319, // WM_APPCOMMAND
                WPARAM0,
                new WinDef.LPARAM(lParamValue)
            );
        }
    }

    public void next() {
        sendCommand(11);
    }

    public void prev() {
        sendCommand(12);
    }

    public void stop() {
        sendCommand(13);
    }

    public void pause() {
        sendCommand(14);
    }

    public void play() {
        sendCommand(46);
    }

    public void testFastForward() {
        sendCommand(49);
    }

    public void testRewind() {
        sendCommand(50);
    }
}
