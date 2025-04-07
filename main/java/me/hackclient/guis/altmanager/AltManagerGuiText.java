package me.hackclient.guis.altmanager;

import me.hackclient.Client;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.shader.ShaderRenderType;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class AltManagerGuiText extends GuiTextField {

    Shadows shadows;

    public AltManagerGuiText(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);

        if (shadows.mainMenu.isToggled() && shadows.isToggled()) {
            InstanceAccess.NORMAL_BlOOM_RUNNABLE.add(() -> RoundedUtils.drawRect(xPosition, yPosition, width, height, 6f, Color.BLACK));
        }

        RoundedUtils.drawRect(xPosition, yPosition, width, height, 5f, new Color(15,15,15,150));

        fontRendererInstance.drawString(getText(), xPosition + 4,yPosition + height / 3, Color.WHITE.getRGB());
    }
}