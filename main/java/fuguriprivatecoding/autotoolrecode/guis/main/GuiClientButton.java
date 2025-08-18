package fuguriprivatecoding.autotoolrecode.guis.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class GuiClientButton extends GuiButton {

    Glow shadows;

    public GuiClientButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public GuiClientButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        final ClientFontRenderer fonts = Client.INST.getFonts().fonts.get("MuseoSans");

        RenderUtils.drawRoundedOutLineRectangle(xPosition, yPosition, width, height, 0, new Color(0,0,0,150).getRGB(), Color.black.getRGB(), Color.black.getRGB());

        final Color color = Color.WHITE;
        fonts.drawString(displayString, xPosition + width / 2f - fonts.getStringWidth(displayString) / 2f, yPosition + 2 + (height - 8) / 2f, hovered ? color.darker() : color, true);
    }
}