package fuguriprivatecoding.autotoolrecode.utils;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;

@UtilityClass
public class Utils implements Imports {
    public boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public boolean isWorldLoaded() {
        return nullCheck() && mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ));
    }

    ByteBuffer buffer = BufferUtils.createByteBuffer(4);

    public static Color getPixelColor(ScaledResolution sc, float x, float y) {
        buffer.clear();

        float scale = sc.getScaleFactor();

        int realX = (int) (x * scale);
        int realY = (int) (mc.displayHeight - (y * scale));

        GL11.glReadPixels(
            realX,
            realY,
            1,
            1,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            buffer
        );

        return new Color(
            buffer.get(0) & 0xFF,
            buffer.get(1) & 0xFF,
            buffer.get(2) & 0xFF
        );
    }
}
