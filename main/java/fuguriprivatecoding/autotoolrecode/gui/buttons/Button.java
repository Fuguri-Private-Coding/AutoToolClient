package fuguriprivatecoding.autotoolrecode.gui.buttons;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class Button extends GuiButton {
    String name;

    float x, y, width, height;
    float prevX, prevY, prevWidth, prevHeight;

    EasingAnimation hoverAnim = new EasingAnimation();

    public Button(int id, String name, float x, float y, float width, float height) {
        super(id, (int) x, (int) y, (int) width, (int) height, name);
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.prevX = x;
        this.prevY = y;
        this.prevWidth = width;
        this.prevHeight = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x, y, width, height);

        ClientFont fontRenderer = Fonts.fonts.get("SFProRounded");

        hoverAnim.update(2, Easing.OUT_BACK);
        hoverAnim.setEnd(hovered ? 1 : 0);

        float x = prevX - hoverAnim.getValue() * 1;
        float y = prevY - hoverAnim.getValue() * 1;
        float width = prevWidth + hoverAnim.getValue() * 2;
        float height = prevHeight + hoverAnim.getValue() * 2;

        Color rectColor = ColorUtils.interpolateColor(Colors.BLACK.withAlpha(0.7f), Colors.BLACK.withAlpha(0.8f), hoverAnim.getValue());

        RoundedUtils.drawRect(x, y, width, height, height / 2f, rectColor);

        float textX = x + width / 2f;
        float textY = y + 2 + (height - 8) / 2f;

        fontRenderer.drawCenteredString(name, textX, textY, Colors.WHITE);
    }
}