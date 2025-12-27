package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import lombok.Getter;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@ModuleInfo(name = "CustomCape", category = Category.VISUAL, description = "Изменяет вам плащ.")
public class CustomCape extends Module {

    public Mode capeMode = new Mode("CapeMode", this)
        .addMode("None")
        .setMode("None")
        ;

    @Getter final File CAPE_DIRECTORY = new File(Client.INST.CLIENT_DIR + "/capes");

    DynamicTexture dynamicTexture;
    BufferedImage capeImage;
    public String selectedCape = "none";
    File capeFile;

    public CustomCape() {
        if (CAPE_DIRECTORY.mkdirs()) System.out.println("Successful created Capes Directory.");
        updateCape();
    }

    @Override
    public void onEnable() {
        selectedCape = "";
        updateCape();
    }

    public void updateCape() {
        capeMode.getModes().clear();
        capeMode.addMode("None");
        for (File cape : CAPE_DIRECTORY.listFiles()) {
            capeMode.addMode(cape.getName().replaceAll(".png", ""));
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) selectedCape = "";
    }

    public ResourceLocation getCape() {
        if (!selectedCape.equalsIgnoreCase(capeMode.getMode())) {
            capeFile = new File(CAPE_DIRECTORY, capeMode.getMode() + ".png");
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