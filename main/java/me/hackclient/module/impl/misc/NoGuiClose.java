package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.guis.browser.BrowserGuiScreen;
import me.hackclient.guis.clickgui.ClickGuiScreen;
import me.hackclient.guis.config.ConfigEditorGui;
import me.hackclient.guis.console.ConsoleGuiScreen;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(
        name = "NoGuiClose",
        category = Category.MISC
)
public class NoGuiClose extends Module {

    BooleanSetting onlyClientGui = new BooleanSetting("ClientGui", this, true);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S2EPacketCloseWindow
                && (onlyClientGui.isToggled()
                ? mc.currentScreen instanceof ClickGuiScreen
                || mc.currentScreen instanceof ConsoleGuiScreen
                || mc.currentScreen instanceof ConfigEditorGui
                || mc.currentScreen instanceof BrowserGuiScreen
                || mc.currentScreen instanceof GuiChat
                : mc.currentScreen != null)) {
            packetEvent.setCanceled(true);
        }
    }
}
