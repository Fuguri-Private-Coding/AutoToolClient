package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigScreen;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.guis.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(name = "NoGuiClose", category = Category.MISC, description = "ЗАПРЕТ НА ЗАКРЫТИЕ ОКОН.")
public class NoGuiClose extends Module {

    MultiMode modes = new MultiMode("Modes", this)
            .addModes("ClickGui", "ConfigGui", "ConsoleGui", "ChatGui")
            ;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent &&
                packetEvent.getPacket() instanceof S2EPacketCloseWindow &&
                guiClosed(mc.currentScreen)) {
            packetEvent.setCanceled(true);
        }
    }

    private boolean guiClosed(GuiScreen currentScreen) {
        switch (currentScreen) {
            case ClickScreen _ -> {
                if (modes.get("ClickGui")) return true;
            }


            case ConfigScreen _ -> {
                if (modes.get("ConfigGui")) return true;
            }

            case ConsoleScreen _ -> {
                if (modes.get("ConsoleGui")) return true;
            }

            case GuiChat _ -> {
                if (modes.get("ChatGui")) return true;
            }

            default -> {}
        }
        return false;
    }
}
