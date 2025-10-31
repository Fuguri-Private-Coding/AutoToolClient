package fuguriprivatecoding.autotoolrecode.guis.buttons;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class Button extends GuiButton {

    Glow shadows;

    public Button(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (shadows == null) shadows = Client.INST.getModules().getModule(Glow.class);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        final ClientFontRenderer fonts = Fonts.fonts.get("SFProRounded");

        RoundedUtils.drawRect(xPosition, yPosition, width, height, height / 2.5f, hovered ? new Color(0,0,0,0.5f) : new Color(0,0,0,0.7f));

        final Color color = Color.WHITE;
        fonts.drawString(displayString, xPosition + width / 2f - fonts.getStringWidth(displayString) / 2f, yPosition + 2 + (height - 8) / 2f, hovered ? color.darker() : color, true);
    }
}