package fuguriprivatecoding.autotoolrecode.gui.buttons;

import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class Button extends GuiButton {
    String name;

    EasingAnimation hoverAnim = new EasingAnimation();

    public Button(int id, String name, float x, float y, float width, float height) {
        super(id, (int) x, (int) y, (int) width, (int) height, name);
        this.name = name;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x, y, width, height);

        MsdfFont fontRenderer = Fonts.get("Bold");

        hoverAnim.update(2f, Easing.OUT_BACK);
        hoverAnim.setEnd(hovered ? 1 : 0);

        Color rectColor = Colors.BLACK.withAlpha(0.3f);

        RoundedUtils.drawRect(x, y, width, height, height / 2f, rectColor);

        StencilUtils.setUpTextures(x, y, width, height, height / 2f);
        StencilUtils.writeTexture();
        float factor = hoverAnim.getValue() * 500;

        RoundedUtils.drawCenteredRect(mouseX, mouseY, factor, factor, factor / 2f, rectColor);
        StencilUtils.endWriteTexture();

        float textX = x + width / 2f;
        float textY = y + 2 + (height - 8) / 2f;

        fontRenderer.drawCenter(name, textX, textY, 8 + (hoverAnim.getValue() * 0.2f), Colors.WHITE);
    }
}