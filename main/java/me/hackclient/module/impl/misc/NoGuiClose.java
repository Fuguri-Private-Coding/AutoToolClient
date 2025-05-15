package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.guis.config.ConfigGuiScreen;
import me.hackclient.guis.clickgui.ClickGuiScreen;
import me.hackclient.guis.console.ConsoleGuiScreen;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
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
