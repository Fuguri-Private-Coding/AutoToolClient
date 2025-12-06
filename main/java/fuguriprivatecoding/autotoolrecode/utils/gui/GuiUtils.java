package fuguriprivatecoding.autotoolrecode.utils.gui;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

public class GuiUtils implements Imports {

    public static boolean isHovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static boolean isMouseOver(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static boolean isMouseDownLeft(int mouseX, int mouseY, float x, float y, float width, float height) {
        return Mouse.isButtonDown(0) && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static boolean isMouseDownRight(int mouseX, int mouseY, float x, float y, float width, float height) {
        return Mouse.isButtonDown(1) && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static boolean isMouseDownMid(int mouseX, int mouseY, float x, float y, float width, float height) {
        return Mouse.isButtonDown(2) && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static Vector2f getAbsolutePos(float xPercentage, float yPercentage) {
        ScaledResolution sc = new ScaledResolution(mc);

        return new Vector2f(
            (sc.getScaledWidth() / 100f) * xPercentage,
            (sc.getScaledHeight() / 100f) * yPercentage
        );
    }

    public static boolean isMouseHovered(float x, float y, float width, float height) {
        final ScaledResolution sc = new ScaledResolution(mc);
        int i1 = sc.getScaledWidth();
        int j1 = sc.getScaledHeight();

        final int mouseX = Mouse.getX() * i1 / mc.displayWidth;
        final int mouseY = j1 - Mouse.getY() * j1 / mc.displayHeight - 1;

        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
