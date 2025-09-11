package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.guis.imgui.ClickGuiWindow;
import fuguriprivatecoding.autotoolrecode.guis.imgui.ImGuiManager;
import net.minecraft.client.gui.GuiScreen;

public class ImGuiScreen extends GuiScreen {

    public final static ClickGuiWindow clickGuiWindow = new ClickGuiWindow();

    @Override
    public void onGuiClosed() {
        ImGuiManager.removeWindow(clickGuiWindow);
    }
}
