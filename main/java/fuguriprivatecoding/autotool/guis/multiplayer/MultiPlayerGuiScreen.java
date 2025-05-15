package fuguriprivatecoding.autotool.guis.multiplayer;

import fuguriprivatecoding.autotool.utils.render.shader.impl.BackgroundUtils;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class MultiPlayerGuiScreen extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        BackgroundUtils.run();

    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
