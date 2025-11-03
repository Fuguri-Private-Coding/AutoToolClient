package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class ClickGuiScreenNew extends GuiScreen {

    EasingAnimation openAnim = new EasingAnimation();

    EasingAnimation xAnim = new EasingAnimation(20);
    EasingAnimation yAnim = new EasingAnimation(20);
    EasingAnimation widthAnim = new EasingAnimation();
    EasingAnimation heightAnim = new EasingAnimation();

    float x, y, width, height = 0;

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

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
        RoundedUtils.drawRect(x, y, width, 15, radius, rectColor);

        boolean hoverExit = GuiUtils.isHovered(mouseX, mouseY, x + width - 20, y, 15, 15);

        ColorUtils.glColor(hoverExit ? Color.RED : Color.WHITE);
        RenderUtils.drawImage(exitLogo, x + width - 20, y, 15, 15, true);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        boolean hoverExit = GuiUtils.isHovered(mouseX, mouseY, x + width - 20, y, 15, 15);

        if (hoverExit && !closing) {
            openAnim.setEnd(0);
            closing = true;
        }

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
