package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

public class ClickGuiRecodeNewNEw extends GuiScreen {

    private final ClickGui clickGui = Modules.getModule(ClickGui.class);

    private float x, y, width, height;

    @Override
    public void initGui() {
        super.initGui();

        ScaledResolution sc = ScaleUtils.getScaledResolution();

        x = 50;
        y = 50;
        width = sc.getScaledWidth() - 100;
        height = sc.getScaledHeight() - 100;
    }

    @Override
    public void onGuiClosed() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Colors rectColor = Colors.BLACK.withAlpha(clickGui.backgroundAlpha.getValue() / 255f);

        RoundedUtils.drawRect(x, y, width, height, 10, rectColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }




}
