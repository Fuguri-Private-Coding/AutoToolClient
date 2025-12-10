package fuguriprivatecoding.autotoolrecode.gui.buttons;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ImgButton extends GuiButton {

    ResourceLocation image;

    float x, y, width, height;
    float prevX, prevY, prevWidth, prevHeight;

    EasingAnimation hoverAnim = new EasingAnimation();

    public ImgButton(int id, ResourceLocation image, float x, float y, float width, float height) {
        super(id, (int) x, (int) y, (int) width, (int) height, "");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.prevX = x;
        this.prevY = y;
        this.prevWidth = width;
        this.prevHeight = height;
        this.image = image;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnim.update(2, Easing.OUT_BACK);
        hoverAnim.setEnd(hovered ? 1 : 0);

        float x = prevX - hoverAnim.getValue() * 1;
        float y = prevY - hoverAnim.getValue() * 1;
        float width = prevWidth + hoverAnim.getValue() * 2;
        float height = prevHeight + hoverAnim.getValue() * 2;

        Color rectColor = ColorUtils.interpolateColor(new Color(0,0,0,0.7f), new Color(0,0,0,0.8f), hoverAnim.getValue());

        RoundedUtils.drawRect(x, y, width, height, height / 2f, rectColor);

        Color imageColor = ColorUtils.interpolateColor(Color.WHITE, Color.RED, hoverAnim.getValue());

        ColorUtils.glColor(imageColor);
        RenderUtils.drawImage(image, x, y, width, height, true);
    }
}                                           
                                            