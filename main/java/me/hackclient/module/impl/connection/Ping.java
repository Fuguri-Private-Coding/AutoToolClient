package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "Ping",
        category = Category.CONNECTION
)
public class Ping extends Module {

    // Debug
    private final BooleanSetting debug = new BooleanSetting("Debug", this, false);

    private final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 50, 1000, 400);

    private final IntegerSetting delayBeforeNextLagAfterReset = new IntegerSetting("DelayBeforeNextLagAfterReset", this, 0, 1000, 500);
    private final MultiBooleanSetting actions = new MultiBooleanSetting("ActionsToReset", this)
            .add("Attack", true)
            .add("Damage")
            .add("Velocity")
            .add("Flag");

    private long lastResetTime;
    private long delayBeforeNextLag;
    private final ConcurrentLinkedQueue<PacketWithTime> buffer = new ConcurrentLinkedQueue<>();
    private final List<PacketWithTime> posBuffer = new CopyOnWriteArrayList<>();

    @Override
    public void onDisable() {
        resetAllPackets();
    }

    @Override
    public void onEvent(Event event) {
        long currentTime = System.currentTimeMillis();

        switch (event) {
            case PacketEvent e -> {
                if (currentTime - lastResetTime < delayBeforeNextLag) {
                    resetAllPackets();
                    break;
                }

                if (actions.get("Damage") && mc.thePlayer.hurtTime != 0) {
                    reset();
                    break;
                }

                Packet packet = e.getPacket();

                switch (packet) {
                    case C02PacketUseEntity handlingPacket -> {
                        if (actions.get("Attack") && handlingPacket.getAction() == C02PacketUseEntity.Action.ATTACK) {
                            reset();
                        }
                    }
                    case S12PacketEntityVelocity handlingPacket -> {
                        if (actions.get("Velocity") && handlingPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                            reset();
                        }
                    }
                    case S08PacketPlayerPosLook _ -> {
                        if (actions.get("Flag")) {
                            reset();
                        }
                    }
                    default -> {}
                }

                if (e.getDirection() == PacketDirection.OUTGOING) {
                    e.cancel();
                    buffer.add(new PacketWithTime(packet, currentTime));
                }
            }
            case Render2DEvent _ -> {
                int prevSize = buffer.size();
                handlePackets();
                int postSize = buffer.size();

                if (debug.isToggled()) {
                    mc.fontRendererObj.drawString(
                            "Prev packets size: " + prevSize + "\n"
                            + "Post packets size: " + postSize + "\n"
                            + "Send: " + (prevSize - postSize) + "\n",
                            200, 200, -1, true
                    );
                }

            }
            case Render3DEvent _ -> {
                if (buffer.isEmpty()) {
                    break;
                }

                //Vec3 lastPos = posBuffer.getFirst();

            }
            default -> {}
        }
    }

    private void handlePackets() {
        buffer.removeIf(packetWithTime -> {
           if (System.currentTimeMillis() - packetWithTime.time() >= maxDelay.getValue()) {
               mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet());
               return true;
           }
           return false;
        });
    }

    private void resetAllPackets() {
        buffer.forEach(packetWithTime -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet()));
        buffer.clear();
    }

    private void reset() {
        resetAllPackets();
        lastResetTime = System.currentTimeMillis();
        delayBeforeNextLag = delayBeforeNextLagAfterReset.getValue();
    }

    private record PacketWithTime(Packet packet, long time) {}
    private record VecWithTime(Vec3 pos, long time) {}
}
