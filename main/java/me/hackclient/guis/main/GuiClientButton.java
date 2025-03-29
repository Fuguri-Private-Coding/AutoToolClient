package me.hackclient.guis.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class GuiClientButton extends GuiButton {

    public GuiClientButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public GuiClientButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        final FontRenderer fonts = mc.fontRendererObj;

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, new Color(0, 160, 160, 255).getRGB());

        final Color color = Color.WHITE;
        fonts.drawString(displayString, xPosition + width / 2f - fonts.getStringWidth(displayString) / 2f, yPosition + (height - 8) / 2f, hovered ? color.darker().getRGB() : color.getRGB());
    }
}
