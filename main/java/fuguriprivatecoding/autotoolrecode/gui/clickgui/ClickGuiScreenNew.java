package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.Scroll;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedGradUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class ClickGuiScreenNew extends GuiScreen {

    public static ClickGuiScreenNew INST;

    private final EasingAnimation openAnim = new EasingAnimation();

    private final ClickGui clickGui = Modules.getModule(ClickGui.class);

    private float x, y, width, height;
    private boolean binding, closing;

    private final Scroll moduleScroll = new Scroll(25);
    private final Scroll settingsScroll = new Scroll(15);

    public static void init() {
        INST = new ClickGuiScreenNew();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);
        x = 50;
        y = 50;
        width = sc.getScaledWidth() - 100;
        height = sc.getScaledHeight() - 100;
        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        openAnim.update(4f, Easing.OUT_CUBIC);

        float alpha = openAnim.getClampValue();

        Color rectColor = Colors.BLACK.withAlphaClamp(0.6f).withMultiplyAlpha(alpha);
        Color rectDarkColor = Colors.DARK_GRAY.withAlphaClamp(1f).withMultiplyAlpha(alpha);

        if (closing && openAnim.getValue() == 0.2) {
            mc.currentScreen.onGuiClosed();
            mc.thePlayer.closeScreen();
        }

        RoundedUtils.drawRect(x, y, width, height, 10f, rectColor);
        RenderUtils.drawRoundedOutLineRectangle(x + 5, y + 5, 80, height - 10, 7.5f, rectColor, rectDarkColor, rectDarkColor);



    }
}
