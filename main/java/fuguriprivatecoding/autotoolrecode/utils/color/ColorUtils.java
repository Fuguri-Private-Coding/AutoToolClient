package fuguriprivatecoding.autotoolrecode.utils.color;

import lombok.experimental.UtilityClass;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@UtilityClass
public class ColorUtils {

    public Color fadeColor(final Color color1, final Color color2, final double speed) {
        final double percent = (Math.sin(System.currentTimeMillis() / 1000D * speed) + 1) / 2;
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        final int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inverse_percent);
        return new Color(redPart, greenPart, bluePart, alphaPart);
    }

    public Color mixColor(final Color color1, final Color color2, final int i, final double offset) {
        final double percent = (Math.cos(i * Math.PI / 180 * offset) + 1) / 2;
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        final int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inverse_percent);
        return new Color(redPart, greenPart, bluePart, alphaPart);
    }

    public void glColor(Color color) {
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public void glColor(Color color, float alpha) {
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
    }

    public void resetColor() {
        glColor(Color.white);
    }
}
