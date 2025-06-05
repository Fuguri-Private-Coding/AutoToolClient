package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class NewClickGuiScreen extends GuiScreen {


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void initGui() {
        super.initGui();
    }




    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {

        }
    }
}
