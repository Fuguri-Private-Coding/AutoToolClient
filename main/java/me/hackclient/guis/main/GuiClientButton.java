package me.hackclient.guis.main;

import me.hackclient.Client;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class GuiClientButton extends GuiButton {

    Shadows shadows;

    public GuiClientButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public GuiClientButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        final FontRenderer fonts = mc.fontRendererObj;

        if (shadows.isToggled() && shadows.mainMenu.isToggled()) {
            InstanceAccess.NORMAL_BlOOM_RUNNABLES.add(() -> RoundedUtils.drawRect(xPosition, yPosition, width, height, 3f, Color.BLACK));
            BloomUtils.update();
            BloomUtils.run(InstanceAccess.NORMAL_BlOOM_RUNNABLES);
            InstanceAccess.clearRunnables();
        }

        RoundedUtils.drawRect(xPosition, yPosition, width, height, 2f, new Color(15, 15, 15, 150));

        final Color color = Color.WHITE;
        fonts.drawString(displayString, xPosition + width / 2f - fonts.getStringWidth(displayString) / 2f, yPosition + (height - 8) / 2f, hovered ? color.darker().getRGB() : color.getRGB(), true);
    }
}