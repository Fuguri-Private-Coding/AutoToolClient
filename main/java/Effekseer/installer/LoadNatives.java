package Effekseer.installer;

import Effekseer.swig.EffekseerBackendCore;
import Effekseer.swig.EffekseerManagerCore;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class LoadNatives implements Imports {
    public final File nativesDir = new File(new File("Fusion"), "natives");
    @Getter
    EffekseerManagerCore effekseerManagerCore;

    public void init() {
        if (!nativesDir.exists()) nativesDir.mkdirs();

        String extension = getNativeExtension();
        String LIBRARY_NAME = "libEffekseerNativeForJava";
        String libraryFileName = LIBRARY_NAME + extension;

        File targetFile = new File(nativesDir, libraryFileName);

        if (!targetFile.exists()) {
            try {
                extractNativeFromAssets(libraryFileName, targetFile);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            System.load(targetFile.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

        EffekseerBackendCore.InitializeWithOpenGL();
        effekseerManagerCore = new EffekseerManagerCore();
        System.out.println("Effekseer initialized!");

        effekseerManagerCore = new EffekseerManagerCore();
        if(!effekseerManagerCore.Initialize(8000)) {
            System.out.print("Failed to initialize.");
        }
    }

    private String getNativeExtension() {
        return switch (Util.getOSType()) {
            case WINDOWS -> ".dll";
            case LINUX -> ".so";
            case OSX -> ".dylib";
            default -> "";
        };
    }

    private void extractNativeFromAssets(String libraryFileName, File targetFile) throws Exception {
        String NAMESPACE = "minecraft";
        String NATIVES_PATH = "fusion/natives";
        ResourceLocation resourceLocation = new ResourceLocation(NAMESPACE, NATIVES_PATH + "/" + libraryFileName);

        IResource resource = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            resource = mc.getResourceManager().getResource(resourceLocation);
            inputStream = resource.getInputStream();
            outputStream = Files.newOutputStream(targetFile.toPath());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Не удалось найти нативную библиотеку в assets: " + resourceLocation, e);
        } finally {
            // Закрываем потоки в правильном порядке
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Игнорируем ошибку закрытия
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Игнорируем ошибку закрытия
                }
            }
            // В 1.8.9 IResource не требует явного закрытия
        }
    }
}