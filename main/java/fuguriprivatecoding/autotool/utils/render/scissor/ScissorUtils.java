package fuguriprivatecoding.autotool.utils.render.scissor;

import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;

public class ScissorUtils implements Imports {

    public static void enableScissor() {
        glEnable(GL_SCISSOR_TEST);
    }

    public static void disableScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void scissor(ScaledResolution scaledResolution, double x, double y, double width, double height) {
        final int scaleFactor = scaledResolution.getScaleFactor();
        glScissor((int) Math.round(x * scaleFactor),
                (int) Math.round((scaledResolution.getScaledHeight() - (y + height)) * scaleFactor),
                (int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor));
    }
}
