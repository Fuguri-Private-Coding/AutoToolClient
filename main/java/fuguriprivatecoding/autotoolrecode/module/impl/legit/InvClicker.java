package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "InvClicker", category = Category.LEGIT, description = "Автоматически кликает в инвентаре за вас.")
public class InvClicker extends Module {

    IntegerSetting delay = new IntegerSetting("Delay", this, 0, 10,0);

    int mouseDown;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent && (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiInventory)) {
            if (!Mouse.isButtonDown(0) || !Keyboard.isKeyDown(54) && !Keyboard.isKeyDown(42)) {
                mouseDown = 0;
                return;
            }
            mouseDown++;
            inInvClick(mc.currentScreen);
        }
    }

    private void inInvClick(GuiScreen guiScreen) {
        int mouseInGUIPosX = Mouse.getX() * guiScreen.width / mc.displayWidth;
        int mouseInGUIPosY = guiScreen.height - Mouse.getY() * guiScreen.height / mc.displayHeight - 1;
        try {
            if (mouseDown >= delay.getValue()) {
                try {
                    guiScreen.mouseClick(mouseInGUIPosX, mouseInGUIPosY, 1);
                } catch (Exception ignored) {}
                mouseDown = 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
