package fuguriprivatecoding.autotoolrecode.utils.file;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class WindowIconHelper {

    public static void setWindowIcon(ResourceLocation icon16x16, ResourceLocation icon32x32) {
        try {
            ByteBuffer[] icons = new ByteBuffer[]{
                    loadIcon(icon16x16),
                    loadIcon(icon32x32)
            };

            Display.setIcon(icons);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ByteBuffer loadIcon(ResourceLocation resource) throws IOException {
        InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
        BufferedImage image = ImageIO.read(inputStream);

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }

        buffer.flip();
        return buffer;
    }
}