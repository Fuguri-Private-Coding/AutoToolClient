package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.scaling.ScaleUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;

public class ClickGuiScreenNew extends GuiScreen {

    EasingAnimation openAnim = new EasingAnimation();

    EasingAnimation xAnim = new EasingAnimation(20);
    EasingAnimation yAnim = new EasingAnimation(20);
    EasingAnimation widthAnim = new EasingAnimation();
    EasingAnimation heightAnim = new EasingAnimation();

    float x, y, width, height = 0;

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);
    ClientSettings clientSettings = Client.INST.getModuleManager().getModule(ClientSettings.class);

    boolean closing;

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = ScaleUtils.getScaledResolution();

        widthAnim.setValue(sc.getScaledWidth() - 40);
        heightAnim.setValue(sc.getScaledHeight() - 40);

        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        updateGuiAnimations();

        Color rectColor = new Color(0,0,0,(clickGui.backgroundAlpha.getValue() / 255f) * openAnim.getValue());

        float radius = clientSettings.backgroundRadius.getValue();

        RoundedUtils.drawRect(x, y, width, height, radius, rectColor);


    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !closing) {
            openAnim.setEnd(0);
            closing = true;
        }
    }

    private void updateGuiAnimations() {
        xAnim.update(7f, Easing.OUT_CUBIC);
        yAnim.update(7f, Easing.OUT_CUBIC);
        widthAnim.update(7f, Easing.OUT_CUBIC);
        heightAnim.update(7f, Easing.OUT_CUBIC);

        x = xAnim.getValue();
        y = yAnim.getValue();
        width = widthAnim.getValue();
        height = heightAnim.getValue();
    }
}
