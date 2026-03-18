package fuguriprivatecoding.autotoolrecode.utils.client.sound;

import fuguriprivatecoding.autotoolrecode.Client;
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
        if (SOUND_DIRECTORY.mkdirs()) System.out.println("Successful created Sounds Directory.");

        File enable = new File(SOUND_DIRECTORY, "enable.wav");
        File enableVl = new File(SOUND_DIRECTORY, "enablevl.wav");
        File disable = new File(SOUND_DIRECTORY, "disable.wav");
        File disableVl = new File(SOUND_DIRECTORY, "disablevl.wav");

        unpackIfNeeded(enable, "assets/minecraft/autotool/sounds/enable.wav");
        unpackIfNeeded(enableVl, "assets/minecraft/autotool/sounds/enablevl.wav");
        unpackIfNeeded(disable, "assets/minecraft/autotool/sounds/disable.wav");
        unpackIfNeeded(disableVl, "assets/minecraft/autotool/sounds/disablevl.wav");

        enableSound = new Sound(enable);
        disableSound = new Sound(disable);
        enableVlSound = new Sound(enableVl);
        disableVlSound = new Sound(disableVl);
    }

    private void unpackIfNeeded(File file, String assetsPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetsPath);
        }
    }
}
