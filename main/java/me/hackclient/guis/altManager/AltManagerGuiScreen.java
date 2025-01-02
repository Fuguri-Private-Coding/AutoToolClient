package me.hackclient.guis.altManager;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class AltManagerGuiScreen extends GuiScreen {

    GuiTextField guiTextField;

    public AltManagerGuiScreen() {
        mc = Minecraft.getMinecraft();
        ScaledResolution sc = new ScaledResolution(mc);
        guiTextField = new GuiTextField(1, mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);
    }

    @Override
    public void initGui() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sc = new ScaledResolution(mc);
        mc.getFramebuffer().framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);
        guiTextField.drawTextBox();
        mc.fontRendererObj.drawCenteredString("Current logged as " + mc.getSession().getUsername(), sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f - 20, new Color(255, 255, 255, 150).getRGB());
        mc.fontRendererObj.drawCenteredString(guiTextField.getText(), sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f, new Color(255, 255, 255, 150).getRGB());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        guiTextField.setFocused(true);
        guiTextField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN) {
            mc.getSession().setUsername(guiTextField.getText());
            guiTextField.setText("");
        }
    }
}
