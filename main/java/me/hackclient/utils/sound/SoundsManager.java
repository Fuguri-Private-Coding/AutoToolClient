package me.hackclient.utils.sound;

import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

@Getter
public class SoundsManager {

    SoundPlayer enableSound, disableSound, killedSound, neverLoseSound, skeetSound;

    public SoundsManager() throws IOException {
        File directory = Client.INSTANCE.getSoundsDirectory();
        File enableSoundFile = new File(directory, "enable.wav");
        File disableSoundFile = new File(directory, "disable.wav");
        File killedSoundFile = new File(directory, "killed.wav");
        File neverLoseSoundFile = new File(directory, "neverlose.wav");
        File skeetSoundFile = new File(directory, "skeet.wav");

        unpackIfNeeded(enableSoundFile, "assets/minecraft/hackclient/sounds/enable.wav");
        unpackIfNeeded(disableSoundFile, "assets/minecraft/hackclient/sounds/disable.wav");
        unpackIfNeeded(killedSoundFile, "assets/minecraft/hackclient/sounds/killed.wav");
        unpackIfNeeded(neverLoseSoundFile, "assets/minecraft/hackclient/sounds/neverlose.wav");
        unpackIfNeeded(skeetSoundFile, "assets/minecraft/hackclient/sounds/skeet.wav");

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
