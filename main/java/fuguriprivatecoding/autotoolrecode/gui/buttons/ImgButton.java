package fuguriprivatecoding.autotoolrecode.gui.buttons;

import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.TextureUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ImgButton extends GuiButton {

    ResourceLocation image;

    EasingAnimation hoverAnim = new EasingAnimation();

    public ImgButton(int id, ResourceLocation image, float x, float y, float width, float height) {
        super(id, (int) x, (int) y, (int) width, (int) height, "");
        this.image = image;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnim.update(2, Easing.OUT_BACK);
        hoverAnim.setEnd(hovered ? 1 : 0);

        Color rectColor = Colors.BLACK.withAlpha(0.3f);

        RoundedUtils.drawRect(x, y, width, height, height / 2f, rectColor);

        StencilUtils.setUpTextures(x, y, width, height, height / 2f);
        StencilUtils.writeTexture();
        float factor = hoverAnim.getValue() * 500;

        RoundedUtils.drawCenteredRect(mouseX, mouseY, factor, factor, factor / 2f, rectColor);
        StencilUtils.endWriteTexture();
        Color imageColor = ColorUtils.interpolateColor(Color.WHITE, Color.RED, hoverAnim.getValue());

        TextureUtils.texture(image, x, y, width, height, 0, 1f, imageColor);
    }
}                                           
                                            