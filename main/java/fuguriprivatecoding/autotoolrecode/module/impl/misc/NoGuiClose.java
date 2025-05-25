package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.guis.config.ConfigGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.console.ConsoleGuiScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(name = "NoGuiClose", category = Category.MISC)
public class NoGuiClose extends Module {

    CheckBox onlyClientGui = new CheckBox("ClientGuis", this, true);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S2EPacketCloseWindow
                && (onlyClientGui.isToggled()
                ? mc.currentScreen instanceof ClickGuiScreen
                || mc.currentScreen instanceof ConsoleGuiScreen
                || mc.currentScreen instanceof ConfigGuiScreen
                || mc.currentScreen instanceof GuiChat
                : mc.currentScreen != null)) {
            packetEvent.setCanceled(true);
        }
    }
}
