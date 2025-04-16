package me.hackclient.utils.sound;

import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.utils.file.FileUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;

@Getter
public class SoundsManager {

    SoundPlayer enableSound, disableSound, killedSound;

    File directory = Client.INSTANCE.getSoundsDirectory();

    public SoundsManager() throws IOException {
        File enableSoundFile = new File(directory, "enable.wav");
        File disableSoundFile = new File(directory, "disable.wav");
        File killedSoundFile = new File(directory, "killed.wav");

        unpackIfNeeded(enableSoundFile, "assets/minecraft/hackclient/sounds/enable.wav");
        unpackIfNeeded(disableSoundFile, "assets/minecraft/hackclient/sounds/disable.wav");
        unpackIfNeeded(killedSoundFile, "assets/minecraft/hackclient/sounds/killed.wav");

        enableSound = new SoundPlayer(enableSoundFile);
        disableSound = new SoundPlayer(disableSoundFile);
        killedSound = new SoundPlayer(killedSoundFile);
    }

    private void unpackIfNeeded(File file, String assetPath) throws IOException {
        if (!file.exists()) {
            FileUtils.unpackFile(file, assetPath);
        }
    }
}
