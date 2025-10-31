package fuguriprivatecoding.autotoolrecode.guis.buttons;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class TextButton extends GuiTextField {

    Glow shadows;

    public TextButton(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        if (shadows == null) shadows = Client.INST.getModules().getModule(Glow.class);

        RoundedUtils.drawRect(xPosition, yPosition, width, height, height / 2.5f,new Color(0,0,0,0.7f));

        ClientFontRenderer fontRenderer = Fonts.fonts.get("SFProRounded");

        fontRenderer.drawString(getText(), xPosition + width / 2f - fontRenderer.getStringWidth(getText()) / 2f,yPosition + 1 + height / 3f, Color.WHITE);
    }
}