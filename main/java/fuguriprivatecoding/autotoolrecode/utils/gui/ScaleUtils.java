package fuguriprivatecoding.autotoolrecode.utils.gui;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.opengl.GL11.*;

public class ScaleUtils implements Imports {

    public static ScaledResolution getScaledResolution() {
        return getScaledResolution(1f);
    }

    public static ScaledResolution getScaledResolution(float scaleFactor) {
        ScaledResolution sc = new ScaledResolution(mc);

        sc.setScaleFactor(sc.scaleFactor *= scaleFactor);
        sc.scaledWidth /= scaleFactor;
        sc.scaledHeight /= scaleFactor;

        return sc;
    }

    public static Vector2f getPosition(ScaledResolution sc, float posX, float posY) {
        return new Vector2f(
            (sc.getScaledWidth() / 100f) * posX,
            (sc.getScaledHeight() / 100f) * posY
        );
    }

    public static void startScaling(float x, float y, float width, float height, float scaleFactor) {
        glPushMatrix();
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        double offsetX = centerX * (1 - scaleFactor);
        double offsetY = centerY * (1 - scaleFactor);

        glTranslated(offsetX, offsetY, 0);
        glScaled(scaleFactor, scaleFactor, 1);
    }

    public static void stopScaling() {
        GL11.glPopMatrix();
    }

}
