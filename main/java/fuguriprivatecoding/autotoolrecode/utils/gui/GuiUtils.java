package fuguriprivatecoding.autotoolrecode.utils.gui;

import org.lwjgl.input.Mouse;

public class GuiUtils {

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
}
