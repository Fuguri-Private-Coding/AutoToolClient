package fuguriprivatecoding.autotoolrecode.guis.main;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiClientImageButtom extends GuiButton {

    @Getter
    final ResourceLocation image;

    public GuiClientImageButtom(int buttonId, int x, int y, ResourceLocation image) {
        super(buttonId, x, y, "");
        this.image = image;
    }

    public GuiClientImageButtom(int buttonId, int x, int y, int widthIn, int heightIn, ResourceLocation image) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.image = image;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        RenderUtils.drawRoundedOutLineRectangle(xPosition, yPosition, width, height, 3.4f,new Color(0,0,0,150).getRGB(), Color.black.getRGB(), Color.black.getRGB());
        RenderUtils.drawImage(image, xPosition, yPosition, width, height);
    }                                       
}                                           
                                            