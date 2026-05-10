package fuguriprivatecoding.autotoolrecode.utils.render.color;

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
        progress = Math.clamp(progress, 0, 1);

        int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        int alpha = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * progress);

        return new Color(red, green, blue, alpha);
    }

    public static Color mix(int c1, int c2, double size, double max) {
        Color start = new Color(c1);
        Color end = new Color(c2);

        float progress = (float) (size / max);

        int r = (int) Math.clamp(start.getRed() + (end.getRed() - start.getRed()) * progress, 0, 255);
        int g = (int) Math.clamp(start.getGreen() + (end.getGreen() - start.getGreen()) * progress, 0, 255);
        int b = (int) Math.clamp(start.getBlue() + (end.getBlue() - start.getBlue()) * progress, 0, 255);
        int a = (int) Math.clamp(start.getAlpha() + (end.getAlpha() - start.getAlpha()) * progress, 0, 255);

        return new Color(r, g, b, a);
    }

    public Color mixColor(final Color color1, final Color color2, final int i, final double offset, final double speed) {
        double time = System.currentTimeMillis() / 1000.0;
        double angle = time * speed;

        double staticOffset = i * offset * (Math.PI / 180);

        double wave = Math.sin(angle + staticOffset);

        final double percent = (wave + 1) / 2;

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
