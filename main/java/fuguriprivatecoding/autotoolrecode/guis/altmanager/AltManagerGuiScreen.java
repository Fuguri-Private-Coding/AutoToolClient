package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.irc.packet.impl.MyNickNamePacket;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class AltManagerGuiScreen extends GuiScreen {

    AltManagerGuiText altManagerGuiText;

    public AltManagerGuiScreen() {
        mc = Minecraft.getMinecraft();
        ScaledResolution sc = new ScaledResolution(mc);
        altManagerGuiText = new AltManagerGuiText(1, mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sc = new ScaledResolution(mc);
        mc.getFramebuffer().framebufferClear();
        BackgroundUtils.run();
        mc.getFramebuffer().bindFramebuffer(true);
        altManagerGuiText.drawTextBox();
        altManagerGuiText.setMaxStringLength(16);
        mc.fontRendererObj.drawCenteredString("Current logged as " + mc.getSession().getUsername(), sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f - 20, new Color(255, 255, 255, 150).getRGB());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        altManagerGuiText.setFocused(true);
        altManagerGuiText.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN && !altManagerGuiText.getText().isEmpty()) {
            mc.getSession().setUsername(altManagerGuiText.getText());
            //Client.INST.getClientSocket().sendPacketToServer(new MyNickNamePacket(altManagerGuiText.getText()));
            altManagerGuiText.setText("");
        }
    }

    public String getClipBoard() {
        try {
            return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
