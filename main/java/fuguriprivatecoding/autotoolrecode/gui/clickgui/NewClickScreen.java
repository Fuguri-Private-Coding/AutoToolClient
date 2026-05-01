package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.gui.imgui.ImGuiManager;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class NewClickScreen extends GuiScreen {

    public static NewClickScreen INST;

    public static void init() {
        INST = new NewClickScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            ImGuiManager.removeWindow(ClickGui.window);
        }

        if (ClickGui.window.featureListeningForKey != null) {
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }
}
