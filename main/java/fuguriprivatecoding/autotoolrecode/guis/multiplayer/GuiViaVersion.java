package fuguriprivatecoding.autotoolrecode.guis.multiplayer;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiViaVersion extends GuiScreen {

    Shadows shadows;
    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    public GuiViaVersion() {
        scrolls = new Animation2D();
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

        if (shadows.isToggled() && shadows.module.get("MainMenu")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40, 5f, Color.white));
        }

        String currentVersion = "Current Version: " + ViaLoadingBase.getInstance().getTargetVersion().getName();

        fontRendererObj.drawString(currentVersion, sc.getScaledWidth() / 2f - fontRendererObj.getStringWidth(currentVersion) / 2f, 5, -1, true);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40, 5f, new Color(15, 15, 15, 150));

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40);

        float offset = scrolls.y;
        scrollTotalHeight = 0;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS) {
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 195, 25 + offset, 390, 20, 5f, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? new Color(75,75,75,150) : new Color(15,15,15,150));
            fontRendererObj.drawString("Version: " + protocol.getName(), sc.getScaledWidth() / 2f - 190, 20f + 11.5f + offset, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? Color.green.getRGB() : -1);
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