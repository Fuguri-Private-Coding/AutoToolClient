package fuguriprivatecoding.autotoolrecode.guis.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiClientMainMenu extends GuiScreen {

    public GuiClientMainMenu() {
        this.mc = Imports.mc;
    }

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "hackclient/mainmenu/exit.png");

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new GuiClientButton(0, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25,"SinglePlayer"));
        buttonList.add(new GuiClientButton(1, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25, "MultiPlayer"));
        buttonList.add(new GuiClientButton(2, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25 + 25, "Minecraft Setting"));
        buttonList.add(new GuiClientButton(3, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25 + 25 + 25, "AltManager"));
        buttonList.add(new GuiClientImageButtom(4, sc.getScaledWidth() - 15 - 5, 5, 15,15, exitLogo));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        BackgroundUtils.run();

        final FontRenderer font = mc.fontRendererObj;

        font.drawString(Client.INST.getChangeLog(), 5, 5, -1, true);
        final String userText = "Hello, " + Client.INST.getProfile().getUsername() + " welcome to AutoTool!";
        font.drawCenteredString(userText, sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        final int id = button.id;

        switch (id) {
            case 0 -> mc.displayGuiScreen(new GuiSelectWorld(this));
            case 1 -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case 2 -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case 3 -> mc.displayGuiScreen(Client.INST.getAltManagerGui());
            case 4 -> mc.shutdownMinecraftApplet();
        }
    }
}
