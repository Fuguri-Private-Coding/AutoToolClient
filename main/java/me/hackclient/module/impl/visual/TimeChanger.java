package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleInfo(name = "TimeChanger", category = Category.VISUAL)
public class TimeChanger extends Module {

    IntegerSetting time = new IntegerSetting("Time", this, 1, 20, 20);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S03PacketTimeUpdate) packetEvent.setCanceled(true);
        if (event instanceof Render3DEvent) {
            mc.theWorld.setWorldTime(time.getValue() * 1000L);
        }
    }
}
