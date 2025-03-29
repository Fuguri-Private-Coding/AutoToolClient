package me.hackclient.guis.main;

import me.hackclient.Client;
import me.hackclient.guis.altManager.AltManagerGuiScreen;
import me.hackclient.utils.font.ClientFontRenderer;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiClientMainMenu extends GuiScreen {

    static final ResourceLocation backround = new ResourceLocation("minecraft", "hackclient/image/backround.png");

    public GuiClientMainMenu() {
        this.mc = InstanceAccess.mc;
    }

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new GuiClientButton(0, 10, sc.getScaledHeight() - 30 - 25 - 25 - 25, "SinglePlayer"));
        buttonList.add(new GuiClientButton(1, 10, sc.getScaledHeight() - 30 - 25 - 25, "MultiPlayer"));
        buttonList.add(new GuiClientButton(2, 10, sc.getScaledHeight() - 30 - 25, "Minecraft Setting"));
        buttonList.add(new GuiClientButton(3, 10, sc.getScaledHeight() - 30, "AltManager"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        mc.getRenderEngine().bindTexture(backround);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, sc.getScaledWidth(), sc.getScaledHeight(), sc.getScaledWidth(), sc.getScaledHeight());
        GlStateManager.bindTexture(0);

        final FontRenderer font = mc.fontRendererObj;
        final String name = Client.INSTANCE.getName();

        font.drawString(name, sc.getScaledWidth() / 2f - font.getStringWidth(name) / 2f, sc.getScaledHeight() / 2f - 8, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        final int id = button.id;

        switch (id) {
            case 0 -> mc.displayGuiScreen(new GuiSelectWorld(this));
            case 1 -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case 2 -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case 3 -> mc.displayGuiScreen(new AltManagerGuiScreen());
            case 4 -> mc.shutdownMinecraftApplet();
        }
    }
}
