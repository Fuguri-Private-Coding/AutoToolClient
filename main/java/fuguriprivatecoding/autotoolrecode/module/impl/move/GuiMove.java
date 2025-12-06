package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.gui.config.ConfigScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "GuiMove", category = Category.MOVE, description = "Позовляет вам двигатся в окнах.")
public class GuiMove extends Module {

    final MultiMode guis = new MultiMode("Guis", this)
        .addModes("Client", "Inventory", "Chest")
        .set("Client", true)
        ;

    final CheckBox jumpInGui = new CheckBox("JumpInGui", this, true);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (isKeyHandle(mc.currentScreen)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && jumpInGui.isToggled());
            }
        }
    }

    private boolean isKeyHandle(GuiScreen current) {
        return switch (current) {
            case ClickScreen clickScreen -> !clickScreen.binding && clickScreen.activeKeyBind == null && guis.get("Client");
            case ConfigScreen configScreen -> !configScreen.textField.isFocused() && guis.get("Client");
            case GuiInventory _ -> guis.get("Inventory");
            case GuiChest _ -> guis.get("Chest");
            default -> false;
        };
    }
}
