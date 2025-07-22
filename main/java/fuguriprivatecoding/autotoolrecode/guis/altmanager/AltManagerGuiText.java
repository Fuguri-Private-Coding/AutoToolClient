package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class AltManagerGuiText extends GuiTextField {

    Glow shadows;

    public AltManagerGuiText(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);

        RenderUtils.drawRoundedOutLineRectangle(xPosition, yPosition, width, height, 8.5f,new Color(0,0,0,150).getRGB(), Color.black.getRGB(), Color.black.getRGB());

        fontRendererInstance.drawString(getText(), xPosition + 4,yPosition + height / 3, Color.WHITE.getRGB());
    }
}