package me.hackclient.guis.altManager;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class AltManagerGuiScreen extends GuiScreen {

    GuiTextField guiTextField;

    public AltManagerGuiScreen() {
        ScaledResolution sc = new ScaledResolution(InstanceAccess.mc);
        guiTextField = new GuiTextField(1, InstanceAccess.mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);
    }

    @Override
    public void initGui() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sc = new ScaledResolution(InstanceAccess.mc);
        guiTextField.drawTextBox();
        InstanceAccess.mc.fontRendererObj.drawCenteredString("Current logged as " + InstanceAccess.mc.getSession().getUsername(), sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f - 20, new Color(255, 255, 255, 150).getRGB());
        InstanceAccess.mc.fontRendererObj.drawCenteredString(guiTextField.getText(), sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f, new Color(255, 255, 255, 150).getRGB());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        guiTextField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN) {
            InstanceAccess.mc.getSession().setUsername(guiTextField.getText());
            guiTextField.setText("");
        }
    }
}
