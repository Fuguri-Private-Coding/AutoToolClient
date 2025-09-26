package fuguriprivatecoding.autotoolrecode.utils.sound;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

@Getter
public class SoundsManager {

    SoundPlayer enableSound, disableSound, killedSound, neverLoseSound, skeetSound;

    public SoundsManager() throws IOException {
        File directory = Client.INST.getSoundsDirectory();
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

        enableSound = new SoundPlayer(enableSoundFile);
        disableSound = new SoundPlayer(disableSoundFile);
        killedSound = new SoundPlayer(killedSoundFile);
        neverLoseSound = new SoundPlayer(neverLoseSoundFile);
        skeetSound = new SoundPlayer(skeetSoundFile);
    }

    private void unpackIfNeeded(File file, String assetPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetPath);
        }
    }
}
