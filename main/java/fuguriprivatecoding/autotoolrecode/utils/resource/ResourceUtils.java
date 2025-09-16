package fuguriprivatecoding.autotoolrecode.utils.resource;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ResourceUtils extends JFrame implements Imports {

    public static ResourceLocation loadTextureFromStream(InputStream stream, String name) {
        try {
            BufferedImage image = ImageIO.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            return mc.getTextureManager().getDynamicTextureLocation(name, dynamicTexture);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
