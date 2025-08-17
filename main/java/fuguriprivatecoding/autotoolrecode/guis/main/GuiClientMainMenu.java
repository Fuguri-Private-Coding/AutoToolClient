package fuguriprivatecoding.autotoolrecode.guis.main;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.resource.ResourceUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class GuiClientMainMenu extends GuiScreen {

    public GuiClientMainMenu() {
        this.mc = Imports.mc;
    }

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "hackclient/mainmenu/exit.png");

    private InputStream avatarStream;
    private ResourceLocation avatarTexture;

    private boolean initialized = false;

    @Override
    public void initGui() {
        final ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new GuiClientButton(0, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25,"SinglePlayer"));
        buttonList.add(new GuiClientButton(1, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25, "MultiPlayer"));
        buttonList.add(new GuiClientButton(2, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25 + 25, "MinecraftSetting"));
        buttonList.add(new GuiClientButton(3, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25 + 25 + 25, "AltManager"));
        buttonList.add(new GuiClientButton(4, sc.getScaledWidth() / 2 - 100, sc.getScaledHeight() / 2 + 25 + 25 + 25 + 25 + 25, "UpdateProfile"));
        buttonList.add(new GuiClientImageButtom(5, sc.getScaledWidth() - 15 - 5, 5, 15,15, exitLogo));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sc = new ScaledResolution(mc);
        final FontRenderer font = mc.fontRendererObj;
        final String userText = "Hello, " + Client.INST.getProfile().getUsername() + " welcome to AutoTool!";
        BackgroundUtils.run();

        if (initialized) {
            font.drawCenteredString(userText, sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f - 45, Color.WHITE.getRGB());
            renderDiscordProfile(sc, font);
        } else if (ClientIRC.profile != null) {
            updateImages();
            font.drawCenteredString(userText, sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f, Color.WHITE.getRGB());
        }

        font.drawString(Client.INST.getChangeLog(), 5, 5, -1, true);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderDiscordProfile(ScaledResolution sc, FontRenderer font) {
        Color transpent = new Color(0,0,0,0);
        Color profileColor = ClientIRC.profile.getProfileColor() != null ? ClientIRC.profile.getProfileColor() : Color.black;
        Color serverRoleColor = ClientIRC.profile.getServerRoleColor() != null ? ClientIRC.profile.getServerRoleColor() : Color.black;

        RenderUtils.drawMixedRoundedRect(sc.getScaledWidth() / 2f - 101, sc.getScaledHeight() / 2f - 31, 202, 52, 1f, transpent, serverRoleColor, 2f);
        RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 101, sc.getScaledHeight() / 2f - 31, 202, 52, 5f, transpent.getRGB(), serverRoleColor.getRGB(), profileColor.getRGB());

        if (avatarTexture != null) {
            StencilUtils.renderStencil(
                    () -> RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 95, sc.getScaledHeight() / 2f - 25, 40, 40, 22f, new Color(0,0,0,255).getRGB(), serverRoleColor.getRGB(), profileColor.getRGB()),
                    () -> ResourceUtils.drawDiscord(avatarTexture, sc.getScaledWidth() / 2 - 95, sc.getScaledHeight() / 2 - 25, 40, 40)
            );
        }
        RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 95, sc.getScaledHeight() / 2f - 25, 40, 40, 20f, new Color(0,0,0,0).getRGB(), profileColor.getRGB(), serverRoleColor.getRGB());
        if (ClientIRC.profile.getUserName() != null) font.drawString(ClientIRC.profile.getUserName(), sc.getScaledWidth() / 2f - 45, sc.getScaledHeight() / 2f - 10f, Color.WHITE.getRGB(), true);
    }

    private void updateImages() {
        if (ClientIRC.profile == null || initialized) return;

        if (avatarStream == null && ClientIRC.profile.getAvatar() != null) {
            avatarStream = ClientIRC.profile.getAvatar();
        }

        if (avatarStream != null && avatarTexture == null) {
            avatarTexture = ResourceUtils.loadTextureFromStream(avatarStream, "discord_avatar");
        }

        initialized = avatarTexture != null;
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
            case 4 -> ClientIRC.setDiscordProfile(Client.INST.getDiscord().getId());
            case 5 -> mc.shutdownMinecraftApplet();
        }
    }
}
