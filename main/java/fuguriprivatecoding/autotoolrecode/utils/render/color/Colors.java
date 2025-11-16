package fuguriprivatecoding.autotoolrecode.utils.render.color;

import java.awt.*;
import java.awt.color.ColorSpace;

public class Colors extends Color {

    Color color;

    public static final Colors WHITE = new Colors(1f,1f,1f,1f);
    public static final Colors LIGHT_GRAY = new Colors(0.753f, 0.753f, 0.753f, 1f);
    public static final Colors GRAY = new Colors(0.502f, 0.502f, 0.502f, 1f);
    public static final Colors DARK_GRAY = new Colors(0.251f, 0.251f, 0.251f, 1f);
    public static final Colors BLACK = new Colors(0f, 0f, 0f, 1f);
    public static final Colors RED = new Colors(1f, 0f, 0f, 1f);
    public static final Colors PINK = new Colors(1f, 0.686f, 0.686f, 1f);
    public static final Colors ORANGE = new Colors(1f, 0.784f, 0f, 1f);
    public static final Colors YELLOW = new Colors(1f, 1f, 0f, 1f);
    public static final Colors GREEN = new Colors(0f, 1f, 0f, 1f);
    public static final Colors MAGENTA = new Colors(1f, 0f, 1f, 1f);
    public static final Colors CYAN = new Colors(0f, 1f, 1f, 1f);
    public static final Colors BLUE = new Colors(0f, 0f, 1f, 1f);
    public static final Colors PURPLE = new Colors(0.502f, 0f, 0.502f, 1f);
    public static final Colors VIOLET = new Colors(0.933f, 0.51f, 0.933f, 1f);
    public static final Colors INDIGO = new Colors(0.294f, 0f, 0.51f, 1f);
    public static final Colors TURQUOISE = new Colors(0.251f, 0.878f, 0.816f, 1f);
    public static final Colors EMERALD = new Colors(0.18f, 0.545f, 0.341f, 1f);
    public static final Colors CORAL = new Colors(1f, 0.498f, 0.314f, 1f);
    public static final Colors SALMON = new Colors(0.98f, 0.502f, 0.447f, 1f);
    public static final Colors GOLD = new Colors(1f, 0.843f, 0f, 1f);
    public static final Colors SILVER = new Colors(0.753f, 0.753f, 0.753f, 1f);
    public static final Colors BRONZE = new Colors(0.804f, 0.498f, 0.196f, 1f);
    public static final Colors LAVENDER = new Colors(0.902f, 0.902f, 0.98f, 1f);
    public static final Colors MINT = new Colors(0.596f, 1f, 0.596f, 1f);
    public static final Colors OLIVE = new Colors(0.502f, 0.502f, 0f, 1f);
    public static final Colors MAROON = new Colors(0.502f, 0f, 0f, 1f);
    public static final Colors NAVY = new Colors(0f, 0f, 0.502f, 1f);
    public static final Colors TEAL = new Colors(0f, 0.502f, 0.502f, 1f);
    public static final Colors CRIMSON = new Colors(0.863f, 0.078f, 0.235f, 1f);
    public static final Colors CORNFLOWER_BLUE = new Colors(0.392f, 0.584f, 0.929f, 1f);
    public static final Colors CHOCOLATE = new Colors(0.824f, 0.412f, 0.118f, 1f);
    public static final Colors ORCHID = new Colors(0.855f, 0.439f, 0.839f, 1f);

    public Colors(int r, int g, int b) {
        super(r, g, b);
        this.color = new Color(r, g, b);
    }

    public Colors(int r, int g, int b, int a) {
        super(r, g, b, a);
        this.color = new Color(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public Colors(int rgb) {
        super(rgb);
        this.color = new Color(rgb);
    }

    public Colors(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.color = color;
    }

    public Colors(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
        this.color = new Color(rgba, hasalpha);
    }

    public Colors(float r, float g, float b) {
        super(r, g, b);
        this.color = new Color(r, g, b);
    }

    public Colors(float r, float g, float b, float a) {
        super(r, g, b, a);
        this.color = new Color(r, g, b, a);
    }

    public Colors(ColorSpace cspace, float[] components, float alpha) {
        super(cspace, components, alpha);
        this.color = new Color(cspace, components, alpha);
    }

    public Colors withAlpha(float alpha) {
        return new Colors(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
    }

    public Colors withAlphaClamp(float alpha) {
        return new Colors(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, Math.clamp(alpha, 0, 1));
    }
}
