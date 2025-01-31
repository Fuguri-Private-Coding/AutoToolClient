package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.ChangeSprintEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.network.Packet;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.Vec3;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "TestPing",
        category = Category.CONNECTION
)
public class TestPing extends Module {

    IntegerSetting delay = new IntegerSetting("Delay", this, 100, 1000, 500);

    final CopyOnWriteArrayList<Doubles<Packet, Long>> buffer;
    final CopyOnWriteArrayList<Doubles<Vec3, Long>> posBuffer;

    public TestPing() {
        buffer = new CopyOnWriteArrayList<>();
        posBuffer = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof ChangeSprintEvent) {
            resetPackets();
        }
        if (event instanceof PacketEvent packetEvent) {
            if (packetEvent.isCanceled())
                return;

            if (mc.isSingleplayer())
                return;

            long sendTime = packetEvent.getSendTime();
            Packet packet = packetEvent.getPacket();

            if (packet instanceof C01PacketChatMessage
                    || packet instanceof C00PacketServerQuery
                    || packet instanceof C00PacketLoginStart) {
                return;
            }

            if (packet instanceof S06PacketUpdateHealth s06 && s06.getHealth() <= 0f) {
                resetPackets();
                return;
            }

            if (packet instanceof S08PacketPlayerPosLook) {
                resetPackets();
                return;
            }

            if (mc.currentScreen != null) {
                resetPackets();
                return;
            }

            if (packetEvent.getDirection() == PacketDirection.OUTGOING) {
                buffer.add(new Doubles<>(packet, sendTime));
                if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
                    posBuffer.add(new Doubles<>(c03.getPosVec(), sendTime));
                }
            }
        }
        if (event instanceof RunGameLoopEvent) {
            buffer.forEach( doubles -> {
                if (System.currentTimeMillis() - doubles.getSecond() >= delay.getValue()) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(doubles.getFirst());
                    buffer.remove(doubles);
                }
            });
        }
    }

    void resetPackets() {
        buffer.forEach( doubles -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(doubles.getFirst()));
        buffer.clear();
    }
}
