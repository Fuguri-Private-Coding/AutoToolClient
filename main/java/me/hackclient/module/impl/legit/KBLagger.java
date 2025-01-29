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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "KBLagger",
        category = Category.LEGIT
)
public class KBLagger extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 10, 500, 200);

    final List<Doubles<Packet, Long>> clientPackets, serverPackets;

    public KBLagger() {
        clientPackets = new CopyOnWriteArrayList<>();
        serverPackets = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            if (mc.objectMouseOver == null)
                return;

            if (packetEvent.getDirection() != PackerDirection.INCOMING)
                return;

            if (mc.thePlayer.hurtTime != 0 && mc.objectMouseOver.entityHit != null) {
                Packet packet = packetEvent.getPacket();
                if (packet instanceof S12PacketEntityVelocity
                || packet instanceof S32PacketConfirmTransaction
                || packet instanceof S27PacketExplosion
                || packet instanceof S03PacketTimeUpdate
                || packet instanceof S00PacketKeepAlive) {
                    serverPackets.add(new Doubles<>(packet, packetEvent.getSendTime()));
                    packetEvent.setCanceled(true);
                }
            } else {
                serverPackets.forEach(pair ->  {
                        try {
                            pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                });
                serverPackets.clear();
            }
        }
        if (event instanceof RunGameLoopEvent) {
            serverPackets.forEach(pair -> {
                if (System.currentTimeMillis() - pair.getSecond() >= delay.getValue()) {
                    try {
                        pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
                    } catch (RuntimeException e) {
                        throw new RuntimeException(e);
                    }
                    serverPackets.remove(pair);
                }
            });
        }
    }
}