package fuguriprivatecoding.autotoolrecode.utils.scaling;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;

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

}
