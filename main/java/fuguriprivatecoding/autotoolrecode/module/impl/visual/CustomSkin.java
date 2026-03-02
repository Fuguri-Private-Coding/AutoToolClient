package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import lombok.Getter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@ModuleInfo(name = "CustomSkin", category = Category.VISUAL, description = "Изменяет вам скин.")
public class CustomSkin extends Module {

    public Mode skinMode = new Mode("SkinMode", this)
        .addMode("None")
        .setMode("None")
        ;
    
    public Mode skinType = new Mode("SkinType", this)
        .addModes("Classic", "Slim")
        .setMode("Slim")
        ;

    DynamicTexture dynamicTexture;
    BufferedImage skinImage;
    public String selectedSkin = "none";
    File skinFile;

    public CustomSkin() {
        updateSkins();
    }
    
    public static String getType() {
        CustomSkin customSkin = Modules.getModule(CustomSkin.class);
        return customSkin.skinType.is("Slim") ? "slim" : "default";
    }

    @Override
    public void onEnable() {
        selectedSkin = "";
        updateSkins();
    }

    public void updateSkins() {
        skinMode.getModes().clear();
        skinMode.addMode("None");
        for (File skin : Objects.requireNonNull(Client.INST.SKIN_DIRECTORY.listFiles())) {
            skinMode.addMode(skin.getName().replaceAll(".png", ""));
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) selectedSkin = "";
    }

    public ResourceLocation getSkin() {
        if (!selectedSkin.equalsIgnoreCase(skinMode.getMode())) {
            skinFile = new File(Client.INST.SKIN_DIRECTORY, skinMode.getMode() + ".png");
            if (!skinFile.exists()) return null;

            try (InputStream inputStream = new FileInputStream(skinFile)) {
                skinImage = ImageIO.read(inputStream);

                if (skinImage == null) {
                    System.err.println("Failed to load skin: " + skinMode.getMode());
                    return null;
                }

                dynamicTexture = new DynamicTexture(skinImage);
                return mc.getTextureManager().getDynamicTextureLocation(
                    "custom_skin_" + skinMode.getMode().toLowerCase(),
                    dynamicTexture
                );
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            } finally {
                selectedSkin = skinMode.getMode();
            }
        }
        return null;
    }

}
