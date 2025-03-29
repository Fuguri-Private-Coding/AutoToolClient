package me.hackclient.module.impl.legit;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.lang.reflect.Method;

@ModuleInfo(
        name = "InvClicker",
        category = Category.LEGIT
)
public class InvClicker extends Module {

    IntegerSetting delay = new IntegerSetting("Delay", this, 0, 10,0);

    int mouseDown;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
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
                    Method mouseClickedMethod = GuiScreen.class.getDeclaredMethod(
                            "mouseClicked",
                            int.class,
                            int.class,
                            int.class
                    );

                    mouseClickedMethod.setAccessible(true);

                    mouseClickedMethod.invoke(guiScreen, mouseInGUIPosX, mouseInGUIPosY, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mouseDown = 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
