package fuguriprivatecoding.autotoolrecode.module.impl.connection;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.network.Packet;
import net.minecraft.util.Vec3;

import java.awt.*;
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
