package fuguriprivatecoding.autotoolrecode.utils.client.sound;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.experimental.UtilityClass;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class Sounds {

    final File SOUND_DIRECTORY = new File(Client.INST.CLIENT_DIR + "/sounds");

    @Getter static Sound enableSound, disableSound, enableVlSound, disableVlSound;

    public void init() throws IOException {
        if (SOUND_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию для звуков.");

        File enable = new File(SOUND_DIRECTORY, "enable.wav");
        File enableVl = new File(SOUND_DIRECTORY, "enablevl.wav");
        File disable = new File(SOUND_DIRECTORY, "disable.wav");
        File disableVl = new File(SOUND_DIRECTORY, "disablevl.wav");

        FileUtils.unpackIfNeeded(enable, "assets/minecraft/autotool/sounds/enable.wav");
        FileUtils.unpackIfNeeded(enableVl, "assets/minecraft/autotool/sounds/enablevl.wav");
        FileUtils.unpackIfNeeded(disable, "assets/minecraft/autotool/sounds/disable.wav");
        FileUtils.unpackIfNeeded(disableVl, "assets/minecraft/autotool/sounds/disablevl.wav");

        enableSound = new Sound(enable);
        disableSound = new Sound(disable);
        enableVlSound = new Sound(enableVl);
        disableVlSound = new Sound(disableVl);

        ClientUtils.chatLog("Успешно инициализировал звуки.");
    }
}
