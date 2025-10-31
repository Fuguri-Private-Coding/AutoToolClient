package fuguriprivatecoding.autotoolrecode.guis.buttons;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ImgButton extends GuiButton {

    @Getter
    final ResourceLocation image;

    public ImgButton(int buttonId, int x, int y, ResourceLocation image) {
        super(buttonId, x, y, "");
        this.image = image;
    }

    public ImgButton(int buttonId, int x, int y, int widthIn, int heightIn, ResourceLocation image) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.image = image;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        RoundedUtils.drawRect(xPosition, yPosition, width, height, height / 2f, hovered ? new Color(0,0,0,0.5f) : new Color(0,0,0,0.7f));
        RenderUtils.drawImage(image, xPosition, yPosition, width, height);
    }                                       
}                                           
                                            