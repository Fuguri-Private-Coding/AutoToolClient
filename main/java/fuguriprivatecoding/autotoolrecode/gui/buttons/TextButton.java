package fuguriprivatecoding.autotoolrecode.gui.buttons;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class TextButton extends GuiTextField {

    float x, y, width, height;
    float prevX, prevY, prevWidth, prevHeight;

    boolean forward;

    EasingAnimation hoverAnim = new EasingAnimation();
    EasingAnimation keyTypedAnim = new EasingAnimation();

    public TextButton(int id, float x, float y, float width, float height) {
        super(id, null, (int) x, (int) y, (int) width, (int) height);
        this.setMaxStringLength(16);
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
    public void drawTextBox() {
        boolean hovered = GuiUtils.isMouseHovered(x, y, width, height) || !"".equals(getText());

        ClientFontRenderer fontRenderer = Fonts.fonts.get("SFProRounded");

        hoverAnim.update(0.7f, Easing.OUT_ELASTIC);
        hoverAnim.setEnd(hovered ? 1 : 0);

        keyTypedAnim.update(6, Easing.OUT_BACK);

        if (keyTypedAnim.getValue() == 1 && forward) {
            keyTypedAnim.setEnd(0);
            forward = false;
        }

        float x = prevX - hoverAnim.getValue() * 0.5f;
        float y = prevY - hoverAnim.getValue() * 0.5f;
        float width = prevWidth + hoverAnim.getValue() * 1;
        float height = prevHeight + hoverAnim.getValue() * 1;

        float textX = x + width / 2f;
        float textY = y + 2 + (height - 8) / 2f;

        float cursorX = (float) (x + width / 2f + fontRenderer.getStringWidth(getText()) / 2f + 1);
        float cursorY = textY - 2 - 4.5f * hoverAnim.getValue() + 4.5f - keyTypedAnim.getValue() * 2 / 2;
        float cursorWidth = 0.5f;
        float cursorHeight = 9 * hoverAnim.getValue() + keyTypedAnim.getValue() * 2;

        Color rectColor = ColorUtils.interpolateColor(Colors.BLACK.withAlpha(0.7f), Colors.BLACK.withAlpha(0.8f), hoverAnim.getValue());
        Color cursorColor = ColorUtils.interpolateColor(Colors.BLACK, Colors.WHITE, hoverAnim.getValue());

        RoundedUtils.drawRect(x, y, width, height, height / 2f, rectColor);
        RoundedUtils.drawRect(cursorX, cursorY, cursorWidth, cursorHeight, 0.5f * hoverAnim.getValue(), cursorColor);
        fontRenderer.drawCenteredString(getText(), textX, textY, Colors.WHITE);
    }

    @Override
    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        keyTypedAnim.setEnd(1);
        forward = true;
        return super.textboxKeyTyped(p_146201_1_, p_146201_2_);
    }
}