package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.gui.config.ConfigScreen;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(name = "NoScreenClose", category = Category.MISC, description = "ЗАПРЕТ НА ЗАКРЫТИЕ ОКОН.")
public class NoScreenClose extends Module {

    MultiMode modes = new MultiMode("Modes", this)
        .addModes("ClickScreen", "ConfigScreen", "ConsoleScreen", "GuiChat");

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent &&
                packetEvent.getPacket() instanceof S2EPacketCloseWindow &&
                guiClosed(mc.currentScreen)) {
            packetEvent.setCanceled(true);
        }
    }

    private boolean guiClosed(GuiScreen currentScreen) {
        return switch (currentScreen) {
            case ClickScreen _ -> modes.get("ClickScreen");
            case ConfigScreen _ -> modes.get("ConfigScreen");
            case ConsoleScreen _ -> modes.get("ConsoleScreen");
            case GuiChat _ -> modes.get("GuiChat");
            default -> false;
        };
    }
}
