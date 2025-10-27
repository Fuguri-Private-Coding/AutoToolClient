package fuguriprivatecoding.autotoolrecode.utils.scaling;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.util.vector.Vector2f;

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

}
