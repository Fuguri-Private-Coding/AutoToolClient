package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.joml.Vector2i;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "InvClicker", category = Category.LEGIT, description = "Автоматически кликает в инвентаре за вас.")
public class InvClicker extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 0, 80, 15, 1);
    CheckBox rightClick = new CheckBox("RightClick", this, true);

    StopWatch timer = new StopWatch();

    long delay;

    boolean click = false;

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (Mouse.isButtonDown(0) && Keyboard.isKeyDown(42)) {
                if (isInGui(mc.currentScreen)) {
                    if (timer.reachedMS(delay)) {
                        delay = Math.round(1000f / CPS.getRandomizedIntValue());
                        click = true;
                        timer.reset();
                    }
                }
            }
        }

        if (event instanceof TickEvent && click) {
            inInvClick(mc.currentScreen);
            click = false;
        }
    }

    private boolean isInGui(GuiScreen screen) {
        return screen instanceof GuiChest || screen instanceof GuiInventory;
    }

    private void inInvClick(GuiScreen guiScreen) {
        Vector2i mouse = GuiUtils.getMousePosition();
        int mouseButton = rightClick.isToggled() ? 1 : 0;

        try {
            guiScreen.mouseClick(mouse.x, mouse.y, mouseButton);
        } catch (Exception ignored) {}
    }
}
