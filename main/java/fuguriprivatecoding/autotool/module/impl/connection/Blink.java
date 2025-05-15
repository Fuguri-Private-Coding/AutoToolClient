package fuguriprivatecoding.autotool.module.impl.connection;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.PacketDirection;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.utils.Utils;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Blink", category = Category.CONNECTION)
public class Blink extends Module {

    private final List<Packet> buffer = new ArrayList<>();

    @Override
    public void onDisable() {
        buffer.forEach(packet -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet));
        buffer.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent e && e.getDirection() == PacketDirection.OUTGOING && !e.isCanceled() && Utils.isWorldLoaded()) {
            e.cancel();
            buffer.add(e.getPacket());
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(buffer.size());
    }
}
