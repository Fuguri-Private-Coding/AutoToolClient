package fuguriprivatecoding.autotoolrecode.hottext;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class HotTextGui extends GuiScreen {
    public static final HotTextGui INST = new HotTextGui();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);

        final float sizeX = 300;
        final float sizeY = 200;

        final float halfSizeX = 300;
        final float halfSizeY = 200;

        float centerX = sr.getScaledWidth() / 2f;
        float centerY = sr.getScaledHeight() / 2f;
    }
}
