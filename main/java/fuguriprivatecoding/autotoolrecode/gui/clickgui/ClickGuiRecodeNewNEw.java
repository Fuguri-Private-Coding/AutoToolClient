package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

public class ClickGuiRecodeNewNEw extends GuiScreen {
    public static ClickGuiRecodeNewNEw INST;

    private final ClickGui clickGuiModule = Modules.getModule(ClickGui.class);
    private final ClientSettings clientSettingsModule = Modules.getModule(ClientSettings.class);

    private float x, y, width, height;
    private Colors backgroundColor = Colors.BLACK.withAlpha(clickGuiModule.backgroundAlpha.getValue());

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
        ClientFont font = Fonts.fonts.get(clickGuiModule.fonts.getMode());

        // update alpha
        float clickGuiAlpha = clickGuiModule.backgroundAlpha.getValue() / 255f;

        if (backgroundColor.getAlpha() != clickGuiAlpha)
            backgroundColor = backgroundColor.withAlpha(clickGuiAlpha);

        // background
        float radius = clientSettingsModule.backgroundRadius.getValue();
        RoundedUtils.drawRect(x, y, width, height, radius, backgroundColor);
        RoundedUtils.drawRect(x, y, width, 20, 0, radius, radius, 0, Colors.BLACK);



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
