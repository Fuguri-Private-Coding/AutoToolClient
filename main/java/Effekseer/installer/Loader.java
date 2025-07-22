package Effekseer.installer;

import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerTextureType;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class Loader implements Imports {
    public static EffekseerEffectCore loadEffect(String effectName, float magnification) {
        ResourceLocation effectLocation = new ResourceLocation("minecraft", "fusion/particles/" + effectName);
        EffekseerEffectCore effectCore = new EffekseerEffectCore();

        IResourceManager resourceManager = mc.getResourceManager();

        try {
            byte[] bytes = loadResourceBytes(effectLocation, resourceManager);
            if (!effectCore.Load(bytes, bytes.length, magnification)) {
                System.out.println("Failed to load effect: " + effectLocation);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error reading effect file: " + e.getMessage());
            return null;
        }

        EffekseerTextureType[] textureTypes = new EffekseerTextureType[] {
                EffekseerTextureType.Color,
                EffekseerTextureType.Normal,
                EffekseerTextureType.Distortion
        };

        for (EffekseerTextureType textureType : textureTypes) {
            for (int i = 0; i < effectCore.GetTextureCount(textureType); i++) {
                ResourceLocation textureLocation = buildResourceLocation(effectLocation, effectCore.GetTexturePath(i, textureType));
                try {
                    byte[] bytes = loadResourceBytes(textureLocation, resourceManager);
                    effectCore.LoadTexture(bytes, bytes.length, i, textureType);
                } catch (Exception e) {
                    System.out.println("Error loading texture " + textureLocation + ": " + e.getMessage());
                }
            }
        }

        for (int i = 0; i < effectCore.GetModelCount(); i++) {
            ResourceLocation modelLocation = buildResourceLocation(effectLocation, effectCore.GetModelPath(i));
            try {
                byte[] bytes = loadResourceBytes(modelLocation, resourceManager);
                effectCore.LoadModel(bytes, bytes.length, i);
            } catch (Exception e) {
                System.out.println("Error loading model " + modelLocation + ": " + e.getMessage());
            }
        }

        for (int i = 0; i < effectCore.GetMaterialCount(); i++) {
            ResourceLocation materialLocation = buildResourceLocation(effectLocation, effectCore.GetMaterialPath(i));
            try {
                byte[] bytes = loadResourceBytes(materialLocation, resourceManager);
                effectCore.LoadMaterial(bytes, bytes.length, i);
            } catch (Exception e) {
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return effectCore;
    }

    public static byte[] loadResourceBytes(ResourceLocation location, IResourceManager resourceManager) throws Exception {
        IResource resource = resourceManager.getResource(location);
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        } finally {
            // Manually close the resource if it needs to be closed
            if (resource instanceof AutoCloseable) {
                ((AutoCloseable) resource).close();
            }
        }
    }

    private static ResourceLocation buildResourceLocation(ResourceLocation baseLocation, String resourcePath) {
        String namespace = baseLocation.getResourceDomain();
        String basePath = Paths.get(baseLocation.getResourcePath()).getParent().toString();
        String fullPath = Paths.get(basePath, resourcePath).toString().replace('\\', '/');
        return new ResourceLocation(namespace, fullPath);
    }
}
