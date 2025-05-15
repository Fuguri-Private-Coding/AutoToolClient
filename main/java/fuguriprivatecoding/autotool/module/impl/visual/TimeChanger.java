package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.event.events.Render3DEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
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
