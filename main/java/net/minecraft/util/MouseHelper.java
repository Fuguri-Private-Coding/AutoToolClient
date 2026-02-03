package net.minecraft.util;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.RawMouseInput;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class MouseHelper {
    public int deltaX;
    public int deltaY;

    public void grabMouseCursor() {
        Mouse.setGrabbed(true);
        this.deltaX = 0;
        this.deltaY = 0;
    }

    public void ungrabMouseCursor() {
        Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
        Mouse.setGrabbed(false);
    }

    public void mouseXYChange() {
        RawMouseInput mouseInput = Modules.getModule(RawMouseInput.class);

        if (mouseInput != null && mouseInput.isToggled() && Mouse.isGrabbed() && mouseInput.isAvailable()) {
            this.deltaX = (int) RawMouseInput.deltaX;
            this.deltaY = (int) RawMouseInput.deltaY * -1;
            RawMouseInput.thread.reset();
            return;
        }

        this.deltaX = Mouse.getDX();
        this.deltaY = Mouse.getDY();
    }
}
