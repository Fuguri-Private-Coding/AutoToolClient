package me.hackclient.module.impl.legit;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.Utils;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "KBLager",
        category = Category.LEGIT
)
public class KBLager extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 10, 500, 200);

    final List<Doubles<Packet, Long>> serverPackets;

    final StopWatch stopWatch;

    public KBLager() {
        stopWatch = new StopWatch();
        serverPackets = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            if (!Utils.isWorldLoaded() || Client.INSTANCE.getCombatManager().getTargetOrSelectedEntity() == null)
                return;

            final Packet packet = packetEvent.getPacket();

            if (packet instanceof S08PacketPlayerPosLook) {
                stopWatch.reset();
                return;
            }

            if (!stopWatch.reachedMS(500))
                return;

            if (packetEvent.getDirection() == PacketDirection.INCOMING) {
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