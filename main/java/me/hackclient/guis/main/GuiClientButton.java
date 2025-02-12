package me.hackclient.guis.main;

import me.hackclient.Client;
import me.hackclient.utils.font.ClientFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

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

        int i = this.getHoverState(this.hovered);

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        final ClientFontRenderer font = Client.INSTANCE.getFontsRepository().fonts.get("Roboto");

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, new Color(0, 160, 160, 255).getRGB());

        final Color color = Color.WHITE;
        font.drawString(displayString, xPosition + width / 2f - font.getWidth(displayString) / 2f, yPosition + (height - 8) / 2f, hovered ? color.darker() : color);
    }
}
