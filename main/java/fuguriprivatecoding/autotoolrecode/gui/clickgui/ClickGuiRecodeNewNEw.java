package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

public class ClickGuiRecodeNewNEw extends GuiScreen {
    public static ClickGuiRecodeNewNEw INST;

    private final ClickGui clickGuiModule = Modules.getModule(ClickGui.class);

    private float x, y, width, height;
    private Colors backgroundColor = Colors.BLACK.withAlpha(clickGuiModule.backgroundAlpha.getValue());

    private MsdfFont font = Fonts.get("Regular");

    public static void init() {
        INST = new ClickGuiRecodeNewNEw();
    }

    @Override
    public void initGui() {
        super.initGui();
        // TODO: сделать чтобы позиции и размер сохранялись в json'ку
        setByPadding(50);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        font = Fonts.get("Regular");
        font.setSize(13);

        // update alpha
        int clickGuiAlpha = clickGuiModule.backgroundAlpha.getValue();

        if (backgroundColor.getAlpha() != clickGuiAlpha)
            backgroundColor = backgroundColor.withAlpha(clickGuiAlpha);

        // background
        float radius = clickGuiModule.radius.getValue();
        RoundedUtils.drawRect(x, y, width, height, radius, backgroundColor);

        // black header
        String autoToolText = Client.INST.getFullName();
        float autoToolTextHeight = font.height(autoToolText);

        RoundedUtils.drawRect(x, y, width, autoToolTextHeight + 4, 0, radius, radius, 0, Colors.BLACK);
        font.draw(autoToolText, x + 5, y + autoToolTextHeight + 2, Colors.WHITE);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    private void setByPadding(float padding) {
        ScaledResolution sc = ScaleUtils.getScaledResolution();

        x = padding;
        y = padding;
        width = sc.getScaledWidth() - padding * 2;
        height = sc.getScaledHeight() - padding * 2;
    }
}
