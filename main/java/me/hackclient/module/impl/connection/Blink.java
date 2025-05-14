package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.util.Vec3;

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
