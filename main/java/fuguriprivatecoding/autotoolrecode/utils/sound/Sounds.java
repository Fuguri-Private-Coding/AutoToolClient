package fuguriprivatecoding.autotoolrecode.utils.sound;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class Sounds {

    final File SOUND_DIRECTORY = new File(Client.getInstance().CLIENT_DIR + "/sounds");

    @Getter static Sound enableSound, disableSound;

    public void init() throws IOException {
        if (SOUND_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию для звуков.");

        File enable = new File(SOUND_DIRECTORY, "enableSound.wav");
        File disable = new File(SOUND_DIRECTORY, "disableSound.wav");

        FileUtils.unpackIfNeeded(enable, "assets/minecraft/autotool/sounds/enableSound.wav");
        FileUtils.unpackIfNeeded(disable, "assets/minecraft/autotool/sounds/disableSound.wav");

        enableSound = new Sound(enable);
        disableSound = new Sound(disable);

        ClientUtils.chatLog("Успешно инициализировал звуки.");
    }
}
