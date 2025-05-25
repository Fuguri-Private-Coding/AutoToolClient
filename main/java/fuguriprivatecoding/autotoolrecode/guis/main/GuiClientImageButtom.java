package fuguriprivatecoding.autotoolrecode.guis.main;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiClientImageButtom extends GuiButton {

    Shadows shadows;

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
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        if (shadows.isToggled() && shadows.module.get("MainMenuGui")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(xPosition, yPosition, width, height, 2f, Color.BLACK));
        }

        RoundedUtils.drawRect(xPosition, yPosition, width, height, 2f, new Color(15,15,15,150));
        RenderUtils.drawImage(image, xPosition, yPosition, width, height);
    }                                       
}                                           
                                            