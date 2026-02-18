package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.gui.config.ConfigScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;

@ModuleInfo(name = "GuiMove", category = Category.MOVE, description = "Позовляет вам двигатся в окнах.")
public class GuiMove extends Module {

    final MultiMode guis = new MultiMode("Guis", this)
        .addModes("Client", "Inventory", "Chest")
        .set("Client", true)
        ;

    MultiMode availableMoveInGui = new MultiMode("AvailableMoveInGui", this)
        .add("Jump", true)
        .add("Sneak")
        ;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (isKeyHandle(mc.currentScreen)) {
                MoveUtils.handleKeyBinding(
                    true, true, true,
                    false,
                    true,
                    true
                );
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
