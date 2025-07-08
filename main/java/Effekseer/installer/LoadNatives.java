package Effekseer.installer;

import Effekseer.swig.EffekseerBackendCore;
import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import Effekseer.swig.EffekseerTextureType;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadNatives implements Imports {
    public final File nativesDir = new File(new File("Fusion"), "natives");
    @Getter
    EffekseerManagerCore effekseerManagerCore;
    @Getter
    public EffekseerEffectCore effekseerEffectCore;

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

        String effectPath = "magmablue.efkefc";
        ResourceLocation effectLocation = new ResourceLocation("minecraft", "fusion/particles/" + effectPath);
        effekseerEffectCore = loadEffect(effectLocation, 1.0f);
        if(effekseerEffectCore == null)
        {
            System.out.print("Failed to load.");
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

    public EffekseerEffectCore loadEffect(ResourceLocation effectLocation, float magnification) {
        EffekseerEffectCore effectCore = new EffekseerEffectCore();

        IResourceManager resourceManager = mc.getResourceManager();

        try {
            byte[] bytes = loadResourceBytes(effectLocation, resourceManager);
            if (!effectCore.Load(bytes, bytes.length, magnification)) {
                System.out.println("Failed to load effect: " + effectLocation);
                return null;
            }
        } catch (IOException e) {
            System.out.println("Error reading effect file: " + e.getMessage());
            return null;
        }

        EffekseerTextureType[] textureTypes = new EffekseerTextureType[] {
                EffekseerTextureType.Color,
                EffekseerTextureType.Normal,
                EffekseerTextureType.Distortion
        };

        for (int t = 0; t < textureTypes.length; t++) {
            for (int i = 0; i < effectCore.GetTextureCount(textureTypes[t]); i++) {
                ResourceLocation textureLocation = buildResourceLocation(effectLocation, effectCore.GetTexturePath(i, textureTypes[t]));
                try {
                    byte[] bytes = loadResourceBytes(textureLocation, resourceManager);
                    effectCore.LoadTexture(bytes, bytes.length, i, textureTypes[t]);
                } catch (IOException e) {
                    System.out.println("Error loading texture " + textureLocation + ": " + e.getMessage());
                }
            }
        }

        for (int i = 0; i < effectCore.GetModelCount(); i++) {
            ResourceLocation modelLocation = buildResourceLocation(effectLocation, effectCore.GetModelPath(i));
            try {
                byte[] bytes = loadResourceBytes(modelLocation, resourceManager);
                effectCore.LoadModel(bytes, bytes.length, i);
            } catch (IOException e) {
                System.out.println("Error loading model " + modelLocation + ": " + e.getMessage());
            }
        }

        for (int i = 0; i < effectCore.GetMaterialCount(); i++) {
            ResourceLocation materialLocation = buildResourceLocation(effectLocation, effectCore.GetMaterialPath(i));
            try {
                byte[] bytes = loadResourceBytes(materialLocation, resourceManager);
                effectCore.LoadMaterial(bytes, bytes.length, i);
            } catch (IOException e) {
                System.out.println("Error loading material " + materialLocation + ": " + e.getMessage());
            }
        }

        for (int i = 0; i < effectCore.GetCurveCount(); i++) {
            ResourceLocation curveLocation = buildResourceLocation(effectLocation, effectCore.GetCurvePath(i));
            try {
                byte[] bytes = loadResourceBytes(curveLocation, resourceManager);
                effectCore.LoadCurve(bytes, bytes.length, i);
            } catch (IOException e) {
                System.out.println("Error loading curve " + curveLocation + ": " + e.getMessage());
            }
        }

        return effectCore;
    }

    private byte[] loadResourceBytes(ResourceLocation location, IResourceManager resourceManager) throws IOException {
        IResource resource = null;
        try {
            resource = resourceManager.getResource(location);
            InputStream inputStream = resource.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } finally {
            if (resource != null) {
                try {
                    resource.getInputStream().close(); // Закрываем InputStream
                } catch (IOException e) {
                    // Игнорируем ошибку закрытия
                }
            }
        }
    }

    private ResourceLocation buildResourceLocation(ResourceLocation baseLocation, String resourcePath) {
        String namespace = baseLocation.getResourceDomain();
        String basePath = Paths.get(baseLocation.getResourcePath()).getParent().toString();
        String fullPath = basePath + "/" + resourcePath;
        return new ResourceLocation(namespace, fullPath);
    }
}