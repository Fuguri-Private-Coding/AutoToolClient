package fuguriprivatecoding.autotoolrecode.guis.multiplayer;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.io.IOException;

public class GuiViaVersion extends GuiScreen {

    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    EasingAnimation alphaAnim = new EasingAnimation();

    public GuiViaVersion() {
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sc = new ScaledResolution(mc);

        scroll -= ClientSettings.getScroll();

        float versionVisibleHeight = sc.getScaledHeight() - 45;
        float maxScroll = Math.max(scrollTotalHeight - versionVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        mc.getFramebuffer().framebufferClear();
        BackgroundUtils.run();
        mc.getFramebuffer().bindFramebuffer(true);

        alphaAnim.update(3, Easing.IN_OUT_QUAD);
        alphaAnim.setEnd(1f);

        AlphaUtils.startWrite();

        ClientFontRenderer font = Client.INST.getFonts().fonts.get("SFProRounded");
        String currentVersion = "Current Version: " + ViaLoadingBase.getInstance().getTargetVersion().getName();

        font.drawString(currentVersion, sc.getScaledWidth() / 2f - font.getStringWidth(currentVersion) / 2f, 5 + 2, Color.WHITE, true);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 200, 20, 400, sc.getScaledHeight() - 40, 5f, new Color(0, 0, 0, 0.7f));

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() / 2f - 200, 20 + 1, 400, sc.getScaledHeight() - 40 - 2);

        float offset = scrolls.y;
        scrollTotalHeight = 0;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS.reversed()) {
            Color hoveredColor = ViaLoadingBase.getInstance().getTargetVersion() == protocol ? new Color(0.2f, 0.2f, 0.2f, 0.7f) : new Color(0f, 0f, 0f, 0.7f);
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 195, 25 + offset, 390, 20, 10, hoveredColor);
            font.drawString(protocol.getName(), sc.getScaledWidth() / 2f - font.getStringWidth(protocol.getName()) / 2f, 20f + 11.5f + 2 + offset, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? Color.green : Color.WHITE);
            offset += 25;
            scrollTotalHeight += 25;
        }
        ScissorUtils.disableScissor();
        AlphaUtils.endWrite();
        AlphaUtils.draw(alphaAnim.getValue());
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        alphaAnim.setEnd(0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        ScaledResolution sc = new ScaledResolution(mc);
        float offset = scrolls.y;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS.reversed()) {
            boolean selectProtocol = mouseX > sc.getScaledWidth() / 2f - 195 && mouseX < sc.getScaledWidth() / 2f - 195 + 390 && mouseY > 25 + offset && mouseY < 25 + offset + 20;
            if (selectProtocol && mouseButton == 0) ViaLoadingBase.getInstance().reload(protocol);
            offset += 25;
            scrollTotalHeight += 25;
        }
    }
}