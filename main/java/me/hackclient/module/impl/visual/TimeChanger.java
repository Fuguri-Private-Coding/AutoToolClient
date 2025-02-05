package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleInfo(name = "TimeChanger", category = Category.VISUAL, toggled = true)
public class TimeChanger extends Module {

    IntegerSetting time = new IntegerSetting("Time", this, 1, 20, 20);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S03PacketTimeUpdate) {
            packetEvent.setCanceled(true);
        }
        if (event instanceof Render3DEvent) {
            mc.theWorld.setWorldTime(time.getValue() * 1000L);
        }
    }
}
