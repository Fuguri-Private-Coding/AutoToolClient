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

    @Getter static Sound enableSound, disableSound, killedSound, neverLoseSound, skeetSound;

    public void init() throws IOException {
        if (SOUND_DIRECTORY.mkdirs()) System.out.println("Successful created Sounds Directory.");

        File directory = SOUND_DIRECTORY;
        File enableSoundFile = new File(directory, "enable.wav");
        File disableSoundFile = new File(directory, "disable.wav");
        File killedSoundFile = new File(directory, "killed.wav");
        File neverLoseSoundFile = new File(directory, "neverlose.wav");
        File skeetSoundFile = new File(directory, "skeet.wav");

        unpackIfNeeded(enableSoundFile, "assets/minecraft/autotool/sounds/enable.wav");
        unpackIfNeeded(disableSoundFile, "assets/minecraft/autotool/sounds/disable.wav");
        unpackIfNeeded(killedSoundFile, "assets/minecraft/autotool/sounds/killed.wav");
        unpackIfNeeded(neverLoseSoundFile, "assets/minecraft/autotool/sounds/neverlose.wav");
        unpackIfNeeded(skeetSoundFile, "assets/minecraft/autotool/sounds/skeet.wav");

        enableSound = new Sound(enableSoundFile);
        disableSound = new Sound(disableSoundFile);
        killedSound = new Sound(killedSoundFile);
        neverLoseSound = new Sound(neverLoseSoundFile);
        skeetSound = new Sound(skeetSoundFile);
    }

    private void unpackIfNeeded(File file, String assetPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetPath);
        }
    }
}
