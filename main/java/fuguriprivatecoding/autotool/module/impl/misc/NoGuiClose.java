package fuguriprivatecoding.autotool.module.impl.misc;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.guis.config.ConfigGuiScreen;
import fuguriprivatecoding.autotool.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotool.guis.console.ConsoleGuiScreen;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
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
