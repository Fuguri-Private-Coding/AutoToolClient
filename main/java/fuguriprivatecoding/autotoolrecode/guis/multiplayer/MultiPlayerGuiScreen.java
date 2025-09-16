package fuguriprivatecoding.autotoolrecode.guis.multiplayer;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.events.ServerJoinEvent;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiPlayerGuiScreen extends GuiMultiplayer {

    public MultiPlayerGuiScreen(GuiScreen parentScreen) {
        super(parentScreen);
    }

    ServerData lastClickedServerListEntryNormal;
    ServerData selectedServer;
    long lastClickTime;
    int scroll, scrollTotalHeight;

    EasingAnimation alphaAnim = new EasingAnimation();

    Animation scrollAnim = new Animation();

    List<GuiButton> guiButtonList = new ArrayList<>();

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = new ScaledResolution(mc);
        buttonList.add(new GuiClientButton(50, (int) (sc.getScaledWidth() / 2f - 150), sc.getScaledHeight() - 35 + 2, 97, 25, "Add Server"));
        buttonList.add(new GuiClientButton(51, (int) (sc.getScaledWidth() / 2f - 150 + 97 + 4.5), sc.getScaledHeight() - 35 + 2, 97, 25, "Direct Connect"));
        buttonList.add(new GuiClientButton(52, (int) (sc.getScaledWidth() / 2f - 150 + 97 + 97 + 4.5 + 4.5), sc.getScaledHeight() - 35 + 2, 97, 25, "Delete"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sc = new ScaledResolution(mc);
        alphaAnim.update(3f, Easing.IN_OUT_QUAD);
        alphaAnim.setEnd(1f);
        int currentScroll = Mouse.getDWheel();

        scroll -= currentScroll / 120 * 25;

        float altVisibleHeight = sc.getScaledHeight() - 50;
        float maxScroll = Math.max(scrollTotalHeight - altVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrollAnim.update(15f);
        scrollAnim.setEndValue(scroll);

        ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("SFProRounded");
        BackgroundUtils.run();

        AlphaUtils.startWrite();

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 150, 10, 300, sc.getScaledHeight() - 50, 10, new Color(0f, 0f, 0f, 0.7f));

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, sc.getScaledWidth() / 2f - 150, 10 + 1, 300, sc.getScaledHeight() - 50 - 2);
        float offset = scrollAnim.value;
        scrollTotalHeight = 0;
        for (ServerListEntryNormal serverListEntryNormal : serverListSelector.serverListInternet) {
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 150 + 5, 15 + offset, 290, 50, 7, selectedServer != null && selectedServer.equals(serverListEntryNormal.getServerData()) ? new Color(0.2f, 0.2f, 0.2f, 0.7f) : new Color(0f , 0f, 0f, 0.7f));
            float finalOffset = offset;
            fontRenderer.drawString(serverListEntryNormal.getServerData().serverName + " (" + serverListEntryNormal.getServerData().serverIP + ")", sc.getScaledWidth() / 2f - 100 + 5 + 5, 15 + 5 + 2 + offset, Color.WHITE);

            if (!serverListEntryNormal.getServerData().field_78841_f) {
                ServerListEntryNormal.field_148302_b.submit(() -> {
                    try {
                        serverListEntryNormal.owner.getOldServerPinger().ping(serverListEntryNormal.server);
                    } catch (UnknownHostException var2) {
                        serverListEntryNormal.server.pingToServer = -1L;
                        serverListEntryNormal.server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                    } catch (Exception var3) {
                        serverListEntryNormal.server.pingToServer = -1L;
                        serverListEntryNormal.server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                    }
                });
            }

            fontRenderer.drawString(Objects.requireNonNullElse(serverListEntryNormal.getServerData().serverMOTD, "Can't resolve youre izernet dalbaeb!"), sc.getScaledWidth() / 2f - 100 + 5 + 5, 15 + 5 + 16 + 5 + offset, Color.WHITE);

            if (serverListEntryNormal.chotoservericonkaprepare != null) RenderUtils.drawImage(serverListEntryNormal.serverIcon,sc.getScaledWidth() / 2f - 150 + 5 + 5, 20 + finalOffset, 40, 40, true);

            offset += 55;
            scrollTotalHeight += 55;
        }
        ScissorUtils.disableScissor();

        for (GuiButton guiButton : buttonList) {
            guiButton.drawButton(mc, mouseX,mouseY);
        }

        AlphaUtils.endWrite();
        AlphaUtils.draw(alphaAnim.getValue());

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 50 -> {//add server
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
            }

            case 51 -> {//direct

            }

            case 52 -> {// delete

            }
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

        float offset = scrollAnim.value;
        for (ServerListEntryNormal serverListEntryNormal : serverListSelector.serverListInternet) {
            boolean hoverServerList = mouseX > sc.getScaledWidth() / 2f - 150 && mouseX < sc.getScaledWidth() / 2f - 150 + 300 && mouseY > 10 && mouseY < 10 + sc.getScaledHeight() - 50;
            boolean hoverServer = mouseX > sc.getScaledWidth() / 2f - 150 + 5 && mouseX < sc.getScaledWidth() / 2f - 150 + 5 + 290 && mouseY > 15 + offset && mouseY < 15 + offset + 50;
            if (hoverServer && hoverServerList) {
                long currentTime = System.currentTimeMillis();

                ServerData serverData = serverListEntryNormal.getServerData();

                if (lastClickedServerListEntryNormal == serverData && (currentTime - lastClickTime) < 250) {
                    connectToServer(serverData);
                } else {
                    toggleServer(serverData);
                }

                lastClickTime = currentTime;
                lastClickedServerListEntryNormal = serverData;
            }
            offset += 55;
        }

    }

    private void connectToServer(ServerData server) {
        mc.displayGuiScreen(new GuiConnecting(this, this.mc, server));
        new ServerJoinEvent(server).callNoWorldNoPlayer();
    }

    void toggleServer(ServerData data) {
        if (selectedServer == data) selectedServer = null; else selectedServer = data;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        alphaAnim.setEnd(0f);
    }
}
