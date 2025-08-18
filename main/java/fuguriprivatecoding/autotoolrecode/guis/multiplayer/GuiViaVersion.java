package fuguriprivatecoding.autotoolrecode.guis.multiplayer;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiViaVersion extends GuiScreen {

    Glow shadows;
    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    public GuiViaVersion() {
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
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

        ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");

        String currentVersion = "Current Version: " + ViaLoadingBase.getInstance().getTargetVersion().getName();

        font.drawString(currentVersion, sc.getScaledWidth() / 2f - font.getStringWidth(currentVersion) / 2f, 5 + 2, Color.WHITE, true);

        RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40, 5f, new Color(0, 0, 0, 150).getRGB(), Color.BLACK.getRGB(), Color.BLACK.getRGB());

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40);

        float offset = scrolls.y;
        scrollTotalHeight = 0;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS) {
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 195, 25 + offset, 390, 20, 5f, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? new Color(75,75,75,150) : new Color(0,0,0,150));
            font.drawString("Version: " + protocol.getName(), sc.getScaledWidth() / 2f - 190, 20f + 11.5f + 2 + offset, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? Color.green : Color.WHITE);
            offset += 25;
            scrollTotalHeight += 25;
        }
        ScissorUtils.disableScissor();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        ScaledResolution sc = new ScaledResolution(mc);
        float offset = scrolls.y;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS) {
            boolean selectProtocol = mouseX > sc.getScaledWidth() / 2f - 195 && mouseX < sc.getScaledWidth() / 2f - 195 + 390 && mouseY > 25 + offset && mouseY < 25 + offset + 20;
            if (selectProtocol && mouseButton == 0) ViaLoadingBase.getInstance().reload(protocol);
            offset += 25;
            scrollTotalHeight += 25;
        }
    }
}