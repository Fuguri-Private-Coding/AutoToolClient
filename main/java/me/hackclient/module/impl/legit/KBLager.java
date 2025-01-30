package me.hackclient.module.impl.legit;

import me.hackclient.event.Event;
import me.hackclient.event.PackerDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "KBLager",
        category = Category.LEGIT
)
public class KBLager extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 10, 500, 200);

    final List<Doubles<Packet, Long>> clientPackets, serverPackets;

    public KBLager() {
        clientPackets = new CopyOnWriteArrayList<>();
        serverPackets = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            if (mc.objectMouseOver == null)
                return;

            if (!mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ)))
                return;

            Packet packet = packetEvent.getPacket();

            if (packet instanceof S12PacketEntityVelocity
            || packet instanceof S32PacketConfirmTransaction
            || packet instanceof S27PacketExplosion
            || packet instanceof S14PacketEntity
            || packet instanceof S18PacketEntityTeleport
            || packet instanceof S03PacketTimeUpdate
            || packet instanceof S00PacketKeepAlive) {
                serverPackets.add(new Doubles<>(packet, packetEvent.getSendTime()));
                packetEvent.setCanceled(true);
            }
        }
        if (event instanceof RunGameLoopEvent) {
            if (serverPackets.isEmpty())
                return;

            serverPackets.forEach(pair -> {
                if (System.currentTimeMillis() - pair.getSecond() >= delay.getValue()) {
                    pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
                    serverPackets.remove(pair);
                }
            });
        }
    }
}