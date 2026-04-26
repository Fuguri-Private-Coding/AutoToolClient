package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RectUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class NewClickScreen extends GuiScreen {

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);

    float x, y, width, height;

    boolean closing = false;

    EasingAnimation openAnimation = new EasingAnimation();

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(mc);

        openAnimation.setEnd(true);

        x = 50;
        y = 50;
        width = sr.getScaledWidth() - 100;
        height = sr.getScaledHeight() - 100;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        openAnimation.update(3f, Easing.OUT_BACK);

        if (closing && openAnimation.getValue() <= 0.2f) {
            mc.displayGuiScreen(null);
            closing = false;
        }

        Color rectColor = Colors.BLACK.withAlpha(clickGui.backgroundAlpha.getValue());
        float rectRadius = 10;

        RectUtils.drawRect(x, y, width, height, rectRadius, rectColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == 1) {
            openAnimation.setEnd(false);
            closing = true;
        }
    }
}
