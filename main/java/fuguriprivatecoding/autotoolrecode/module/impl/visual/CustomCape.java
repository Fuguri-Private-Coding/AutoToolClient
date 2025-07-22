package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@ModuleInfo(name = "CustomCape", category = Category.VISUAL, description = "Изменяет вам плащ.")
public class CustomCape extends Module {

    File capeDirectory = new File(Client.INST.getName() + "/capes");

    DynamicTexture dynamicTexture;
    BufferedImage capeImage;
    public String selectedCape = "none";
    File capeFile;

    public CustomCape() {
        if (!capeDirectory.exists()) {
            capeDirectory.mkdirs();
        }
        updateCape();
    }

    @Override
    public void onEnable() {
        updateCape();
    }

    public Mode capeMode = new Mode("Mode", this);

    private void updateCape() {
        capeMode.getModes().clear();
        for (File cape : capeDirectory.listFiles()) {
            capeMode.addMode(cape.getName().replaceAll(".png", ""));
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) selectedCape = "";
    }

    public ResourceLocation getCape() {
        if (!selectedCape.equalsIgnoreCase(capeMode.getMode())) {
            capeFile = new File(capeDirectory, capeMode.getMode() + ".png");
            if (!capeFile.exists()) return null;

            try (InputStream inputStream = new FileInputStream(capeFile)) {
                capeImage = ImageIO.read(inputStream);

                if (capeImage == null) {
                    System.err.println("Failed to load cape: " + capeMode.getMode());
                    return null;
                }

                dynamicTexture = new DynamicTexture(capeImage);
                return mc.getTextureManager().getDynamicTextureLocation(
                        "custom_cape_" + capeMode.getMode().toLowerCase(),
                        dynamicTexture
                );
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            } finally {
                selectedCape = capeMode.getMode();
            }
        }
        return null;
    }
}