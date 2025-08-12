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

    public static Color mix(int c1, int c2, double size, double max) {
        int f3 = c1 >> 24 & 255;
        int f4 = c1 >> 24 & 255;
        Color col1 = new Color(c1);
        Color col2 = new Color(c2);
        int diffR = (int)((double)col1.getRed() - (double)(col1.getRed() - col2.getRed()) / max * size);
        int diffG = (int)((double)col1.getGreen() - (double)(col1.getGreen() - col2.getGreen()) / max * size);
        int diffB = (int)((double)col1.getBlue() - (double)(col1.getBlue() - col2.getBlue()) / max * size);
        if (diffR > 255) {
            diffR = 255;
        }

        if (diffR < 0) {
            diffR = 0;
        }

        if (diffG > 255) {
            diffG = 255;
        }

        if (diffG < 0) {
            diffG = 0;
        }

        if (diffB > 255) {
            diffB = 255;
        }

        if (diffB < 0) {
            diffB = 0;
        }

        return (new Color(diffR, diffG, diffB));
    }

    public Color mixColor(final Color color1, final Color color2, final int i, final double offset, final double speed) {
        double time = System.currentTimeMillis() / 1000.0;
        double angle = time * speed;

        double staticOffset = i * offset * (Math.PI / 180);

        double sinWave = Math.sin(angle + staticOffset);
        double cosWave = Math.cos(angle + staticOffset);

        final double percent = (sinWave + cosWave + 2) / 4;

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
