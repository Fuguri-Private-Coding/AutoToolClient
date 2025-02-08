package me.hackclient.utils.sound;

import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;

@Getter
public class SoundsManager {

    SoundPlayer enableSound, disableSound, pukpukSound, LegitFast;

    public SoundsManager() throws IOException {
        File directory = Client.INSTANCE.getSoundsDirectory();
        File enableSoundFile = new File(directory, "enable.wav");
        File disableSoundFile = new File(directory, "disable.wav");
        File pukpukSoundFile = new File(directory, "pukpuk.wav");
        File legitFastSoundFile = new File(directory, "LegitFast.mp3");

        unpackIfNeeded(legitFastSoundFile, "assets/minecraft/hackclient/sounds/LegitFast.mp3");
        unpackIfNeeded(enableSoundFile, "assets/minecraft/hackclient/sounds/enable.wav");
        unpackIfNeeded(disableSoundFile, "assets/minecraft/hackclient/sounds/disable.wav");
        unpackIfNeeded(pukpukSoundFile, "assets/minecraft/hackclient/sounds/pukpuk.wav");

        LegitFast = new SoundPlayer(legitFastSoundFile);
        enableSound = new SoundPlayer(enableSoundFile);
        disableSound = new SoundPlayer(disableSoundFile);
        pukpukSound = new SoundPlayer(pukpukSoundFile);
    }

    private void unpackIfNeeded(File file, String assetPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetPath);
        }
    }
}
