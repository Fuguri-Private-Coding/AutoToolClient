package fuguriprivatecoding.autotoolrecode.gui.multiplayer;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BackgroundUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class ViaVersionScreen extends GuiScreen {

    int scroll, scrollTotalHeight;
    Animation2D scrolls;

    private boolean isScrolling = false;
    private float scrollOffsetOnClick = 0;

    EasingAnimation alphaAnim = new EasingAnimation();

    public ViaVersionScreen() {
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sc = new ScaledResolution(mc);
        int currentScroll = ClientSettings.getScroll();

        scroll += currentScroll / 120 * 50;

        float versionVisibleHeight = sc.getScaledHeight() - 70;
        float maxScroll = Math.max(scrollTotalHeight - versionVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        scrolls.endY = scroll;
        scrolls.update(15f);

        BackgroundUtils.run();

        alphaAnim.update(3, Easing.IN_OUT_QUAD);
        alphaAnim.setEnd(1f);

        AlphaUtils.startWrite();

        ClientFontRenderer font = Fonts.fonts.get("SFProRounded");
        String currentVersion = "Current Version: " + ViaLoadingBase.getInstance().getTargetVersion().getName();

        font.drawString(currentVersion, sc.getScaledWidth() / 2f - font.getStringWidth(currentVersion) / 2f, 5 + 2, Color.WHITE, true);

        RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 200, 30, 400, sc.getScaledHeight() - 65, 7.5f, new Color(0, 0, 0, 0.7f));

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), sc.getScaledWidth() / 2f - 200, 30, 400, sc.getScaledHeight() - 65);

        float offset = scrolls.y;
        scrollTotalHeight = 0;
        for (ProtocolVersion protocol : ViaLoadingBase.PROTOCOLS.reversed()) {
            Color hoveredColor = ViaLoadingBase.getInstance().getTargetVersion() == protocol ? new Color(0.2f, 0.2f, 0.2f, 0.7f) : new Color(0f, 0f, 0f, 0.7f);
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 195, 35 + offset, 390, 20, 10, hoveredColor);
            font.drawString(protocol.getName(), sc.getScaledWidth() / 2f - font.getStringWidth(protocol.getName()) / 2f, 30f + 11.5f + 2 + offset, ViaLoadingBase.getInstance().getTargetVersion() == protocol ? Color.green : Color.WHITE);
            offset += 25;
            scrollTotalHeight += 25;
        }
        ScissorUtils.disableScissor();

        float scrollbarWidth = 5;
        float scrollbarX = sc.getScaledWidth() / 2f + 210;
        float scrollbarTrackHeight = versionVisibleHeight - 5;
        float scrollbarY = 35;

        float thumbHeight = Math.max((versionVisibleHeight / scrollTotalHeight) * scrollbarTrackHeight, 10f);

        float scrollProgress = maxScroll > 0 ? (-scrolls.y) / maxScroll : 0;
        float thumbY = scrollbarY + (scrollbarTrackHeight - thumbHeight) * scrollProgress;

        if (Mouse.isButtonDown(0) && scrollTotalHeight >= versionVisibleHeight) {
            boolean clickedOnThumb = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight;

            boolean clickedOnTrack = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarTrackHeight;

            if (clickedOnThumb) {
                if (!isScrolling) {
                    scrollOffsetOnClick = mouseY - thumbY;
                    isScrolling = true;
                }
            }

            if (isScrolling) {
                float newThumbY = mouseY - scrollOffsetOnClick;
                newThumbY = Math.max(scrollbarY, newThumbY);
                newThumbY = Math.min(scrollbarY + scrollbarTrackHeight - thumbHeight, newThumbY);

                float trackScrollableHeight = scrollbarTrackHeight - thumbHeight;
                if (trackScrollableHeight > 0) {
                    float newScrollProgress = (newThumbY - scrollbarY) / trackScrollableHeight;
                    scroll = (int) (-newScrollProgress * maxScroll);
                }
            }

            if (clickedOnTrack && !isScrolling) {
                if (mouseY < thumbY) {
                    scroll += (int) (versionVisibleHeight * 2f);
                } else if (mouseY > thumbY + thumbHeight) {
                    scroll -= (int) (versionVisibleHeight * 2f);
                }
            }
        } else {
            isScrolling = false;
        }

        scroll = Math.max(scroll, (int)-maxScroll);
        scroll = Math.min(scroll, 0);

        if (scrollTotalHeight >= versionVisibleHeight) {
            RoundedUtils.drawRect(sc.getScaledWidth() / 2f + 205, 30, 15, sc.getScaledHeight() - 65, 7.5f, new Color(0, 0, 0, 0.7f));
            RoundedUtils.drawRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarTrackHeight, 2, new Color(0f, 0f, 0f, 0.7f));
            RoundedUtils.drawRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 2, Color.WHITE);
        }

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
            boolean selectProtocol = mouseX > sc.getScaledWidth() / 2f - 195 && mouseX < sc.getScaledWidth() / 2f - 195 + 390 && mouseY > 35 + offset && mouseY < 35 + offset + 20;
            if (selectProtocol && mouseButton == 0) ViaLoadingBase.getInstance().reload(protocol);
            offset += 25;
            scrollTotalHeight += 25;
        }
    }
}