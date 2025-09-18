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

    public static Color interpolateColor(Color start, Color end, float progress) {
        progress = Math.max(0, Math.min(1, progress));

        int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        int alpha = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * progress);

        return new Color(red, green, blue, alpha);
    }

    public static Color mix(int c1, int c2, double size, double max) {
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

    public static Color rainbow(double speed, double offset, int i, float alpha) {
        double time = System.currentTimeMillis() / 1000.0;
        double angle = time * speed;
        double staticOffset = i * offset * (Math.PI / 180);

        float hue = (float) ((angle + staticOffset) % (2 * Math.PI) / (2 * Math.PI));
        int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        Color currentColor = new Color(rgb);
        return new Color(
            currentColor.getRed() / 255F,
            currentColor.getGreen() / 255F,
            currentColor.getBlue() / 255F,
            alpha
        );
    }

    public static Color rainbow(float offset, float alpha) {
        float hue = ((System.nanoTime() + offset * 1000) / 10000000000F) % 1;
        int rgb = Color.HSBtoRGB(hue, 1F, 1F);
        Color currentColor = new Color(rgb);
        return new Color(
            currentColor.getRed() / 255F,
            currentColor.getGreen() / 255F,
            currentColor.getBlue() / 255F,
            alpha
        );
    }

    public static Color rainbow() {
        return rainbow(40, 1f);
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
