package fuguriprivatecoding.autotoolrecode.utils.music;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.experimental.UtilityClass;
import smtc.SmtcNative;

// created by dicves_recode on 09.03.2026
@UtilityClass
public class MediaController {

    public boolean next() { return SmtcNative.nNext(); }
    public boolean prev() { return SmtcNative.nPrev(); }
    public boolean stop() { return SmtcNative.nStop(); }
    public boolean playPause() { return SmtcNative.nTogglePlayPause(); }
    public boolean play() { return SmtcNative.nPlay(); }
    public boolean pause() { return SmtcNative.nPause(); }
}
