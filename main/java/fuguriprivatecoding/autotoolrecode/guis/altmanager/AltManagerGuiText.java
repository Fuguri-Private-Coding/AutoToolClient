package fuguriprivatecoding.autotoolrecode.guis.altmanager;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class AltManagerGuiText extends GuiTextField {

    Shadows shadows;

    public AltManagerGuiText(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);

        if (shadows.module.get("MainMenuGui") && shadows.isToggled()) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(xPosition, yPosition, width, height, 6f, Color.BLACK));
        }

        RoundedUtils.drawRect(xPosition, yPosition, width, height, 5f, new Color(15,15,15,150));

        fontRendererInstance.drawString(getText(), xPosition + 4,yPosition + height / 3, Color.WHITE.getRGB());
    }
}