package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;

public class NewClickScreen extends GuiScreen {

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);

    float x, y, width, height;

    EasingAnimation openAnimation = new EasingAnimation();

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(mc);

        x = 50;
        y = 50;
        width = sr.getScaledWidth() - 100;
        height = sr.getScaledHeight() - 100;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Color rectColor = Colors.BLACK.withAlpha(clickGui.backgroundAlpha.getValue());
        float rectRadius = clientSettings.backgroundRadius.getValue();

        RoundedUtils.drawRect(x, y, width, height, rectRadius, rectColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }
}
