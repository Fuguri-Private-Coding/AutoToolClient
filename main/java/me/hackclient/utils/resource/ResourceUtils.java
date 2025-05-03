package me.hackclient.utils.resource;

import me.hackclient.utils.interfaces.Imports;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ResourceUtils implements Imports {

    public static ByteBuffer[] getClientLogo() {
        try {
            return new ByteBuffer[] {
                    readImageToBuffer(ClassLoader.getSystemResourceAsStream("assets/minecraft/hackclient/logo/logo.png"))
            };
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        if (imageStream == null) {
            return null;
        }

        BufferedImage bufferedImage = ImageIO.read(imageStream);
        if (bufferedImage == null) {
            return null;
        }

        int[] rgb = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * rgb.length);

        for (int i : rgb) {
            byteBuffer.putInt((i << 8) | ((i >> 24) & 255));
        }

        byteBuffer.flip();
        return byteBuffer;
    }
}
