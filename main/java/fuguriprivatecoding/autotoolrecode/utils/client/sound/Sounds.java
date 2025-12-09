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

    @Getter static Sound enableSound, disableSound;

    public void init() throws IOException {
        if (SOUND_DIRECTORY.mkdirs()) System.out.println("Successful created Sounds Directory.");

        File enableSoundFile = new File(SOUND_DIRECTORY, "enable.wav");
        File disableSoundFile = new File(SOUND_DIRECTORY, "disable.wav");

        unpackIfNeeded(enableSoundFile, "assets/minecraft/autotool/sounds/enable.wav");
        unpackIfNeeded(disableSoundFile, "assets/minecraft/autotool/sounds/disable.wav");

        enableSound = new Sound(enableSoundFile);
        disableSound = new Sound(disableSoundFile);
    }

    private void unpackIfNeeded(File file, String assetPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetPath);
        }
    }
}
