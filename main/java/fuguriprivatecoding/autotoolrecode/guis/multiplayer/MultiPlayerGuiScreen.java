package fuguriprivatecoding.autotoolrecode.guis.multiplayer;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientButton;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class MultiPlayerGuiScreen extends GuiScreen {

    int scroll, scrollTotalHeight;

    Shadows shadows;

    ServerData selectedServer;

    Animation2D scrolls;

    public MultiPlayerGuiScreen() {
        scrolls = new Animation2D();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiClientButton(7, this.width / 2 - 154, this.height - 28, 70, 20, "Edit Server"));
        this.buttonList.add(new GuiClientButton(2, this.width / 2 - 74, this.height - 28, 70, 20, "Delete Server"));
        this.buttonList.add(new GuiClientButton(1, this.width / 2 - 154, this.height - 52, 100, 20, "Join Server"));
        this.buttonList.add(new GuiClientButton(4, this.width / 2 - 50, this.height - 52, 100, 20, "Direct Connect"));
        this.buttonList.add(new GuiClientButton(3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, "Add"));
        this.buttonList.add(new GuiClientButton(8, this.width / 2 + 4, this.height - 28, 70, 20, "Refresh"));
        this.buttonList.add(new GuiClientButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, "Cancel"));
        this.buttonList.add(new GuiClientButton(69, 5, 5, 90, 20, "Via Version"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        ScaledResolution sc = new ScaledResolution(mc);
        int currentScroll = Mouse.getDWheel();

        scroll -= currentScroll / 120 * 20;

        float versionVisibleHeight = sc.getScaledHeight() - 45;
        float maxScroll = Math.max(scrollTotalHeight - versionVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        mc.getFramebuffer().framebufferClear();
        BackgroundUtils.run();
        mc.getFramebuffer().bindFramebuffer(true);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 200, 40, 400, sc.getScaledHeight() - 100, 5f, new Color(15, 15, 15, 150));

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() / 2f - 200, 40, 400, sc.getScaledHeight() - 100);

        float offset = scrolls.y;

        scrollTotalHeight = 0;






    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
//        if (button.enabled) {
//            GuiListExtended.IGuiListEntry guilistextended$iguilistentry = this.serverListSelector.func_148193_k() < 0 ? null : this.serverListSelector.getListEntry(this.serverListSelector.func_148193_k());
//
//            if (button.id == 2 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
//                String s4 = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData().serverName;
//
//                if (s4 != null) {
//                    this.deletingServer = true;
//                    String s = I18n.format("selectServer.deleteQuestion", new Object[0]);
//                    String s1 = "\'" + s4 + "\' " + I18n.format("selectServer.deleteWarning", new Object[0]);
//                    String s2 = I18n.format("selectServer.deleteButton", new Object[0]);
//                    String s3 = I18n.format("gui.cancel", new Object[0]);
//                    GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, this.serverListSelector.func_148193_k());
//                    this.mc.displayGuiScreen(guiyesno);
//                }
//            } else if (button.id == 1) {
//                this.connectToSelected();
//            } else if (button.id == 4) {
//                this.directConnect = true;
//                this.mc.displayGuiScreen(new GuiScreenServerList(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
//            } else if (button.id == 3) {
//                this.addingServer = true;
//                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
//            } else if (button.id == 7 && guilistextended$iguilistentry instanceof ServerListEntryNormal) {
//                this.editingServer = true;
//                ServerData serverdata = ((ServerListEntryNormal) guilistextended$iguilistentry).getServerData();
//                this.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
//                this.selectedServer.copyFrom(serverdata);
//                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer));
//            } else if (button.id == 0) {
//                this.mc.displayGuiScreen(new GuiClientMainMenu());
//            } else if (button.id == 8) {
//                this.refreshServerList();
//            } else if (button.id == 69) {
//                this.mc.displayGuiScreen(new GuiViaVersion());
//            }
//        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
