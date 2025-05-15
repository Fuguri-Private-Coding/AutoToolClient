package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.Mode;
import me.hackclient.settings.impl.MultiMode;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Ping", category = Category.CONNECTION)
public class Ping extends Module {

    private final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 50, 1000, 400);

    private final IntegerSetting delayBeforeNextLagAfterReset = new IntegerSetting("DelayBeforeNextLagAfterReset", this, 0, 1000, 500);
    private final MultiMode actions = new MultiMode("ActionsToReset", this)
            .add("Attack", true)
            .add("Damage")
            .add("Velocity")
            .add("Flag")
            .add("UsingItem")
            .add("PlaceBlock")
            .add("ChangeSprint")
            .add("OpenedGui");

    Mode renderModes = new Mode("RenderMode", this)
            .addModes("Player", "Box", "OFF")
            .setMode("Player");

    private long lastResetTime;
    private long delayBeforeNextLag;
    private final ConcurrentLinkedQueue<PacketWithTime> buffer = new ConcurrentLinkedQueue<>();
    private final List<VecWithTime> posBuffer = new CopyOnWriteArrayList<>();

    Vec3 lastPos, currentPos;

    @Override
    public void onDisable() {
        resetAllPackets();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.isIntegratedServerRunning()) return;
        long currentTime = System.currentTimeMillis();
        if ((mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) && actions.get("OpenedGui")) reset();
        switch (event) {
            case ChangeSprintEvent _ -> {
                if (actions.get("ChangeSprint")) {
                    reset();
                }
            }

            case PacketEvent e -> {
                if (currentTime - lastResetTime < delayBeforeNextLag) {
                    resetAllPackets();
                    break;
                }

                if (actions.get("Damage") && mc.thePlayer.hurtTime != 0) {
                    reset();
                    break;
                }

                if (actions.get("UsingItem") && mc.thePlayer.isUsingItem()) {
                    reset();
                    break;
                }

                Packet packet = e.getPacket();

                switch (packet) {
                    case C02PacketUseEntity handlingPacket -> {
                        if (actions.get("Attack") && handlingPacket.getAction() == C02PacketUseEntity.Action.ATTACK) {
                            reset();
                            return;
                        }
                    }

                    case S12PacketEntityVelocity handlingPacket -> {
                        if (actions.get("Velocity") && handlingPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                            reset();
                            return;
                        }
                    }

                    case S08PacketPlayerPosLook _ -> {
                        if (actions.get("Flag")) {
                            reset();
                            return;
                        }
                    }

                    case C08PacketPlayerBlockPlacement _ -> {
                        if (actions.get("PlaceBlock")) {
                            reset();
                            return;
                        }
                    }

                    default -> {}
                }

                if (e.getDirection() == PacketDirection.OUTGOING) {
                    e.cancel();
                    buffer.add(new PacketWithTime(packet, currentTime));
                    if (packet instanceof C03PacketPlayer c03) {
                        if (c03.isMoving()) {
                            posBuffer.add(new VecWithTime(c03.getPosVec(), currentTime));
                        }
                    }
                }
            }
            case RunGameLoopEvent _ -> handlePackets();
            case MotionEvent _ -> {
                while (posBuffer.size() > (maxDelay.getValue() / 50)) {
                    posBuffer.removeFirst();
                }
            }
            case TickEvent _ -> {
                lastPos = currentPos;
                currentPos = posBuffer.getFirst().pos;
            }
            case Render3DEvent _ -> {
                if (posBuffer.isEmpty() || mc.gameSettings.thirdPersonView == 0 || lastPos == null || currentPos == null) {
                    break;
                }

                EntityPlayerSP player = mc.thePlayer;

                double x = lastPos.xCoord + (currentPos.xCoord - lastPos.xCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                double y = lastPos.yCoord + (currentPos.yCoord - lastPos.yCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                double z = lastPos.zCoord + (currentPos.zCoord - lastPos.zCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                switch (renderModes.getMode()) {
                    case "Box" -> {
                        RenderUtils.start3D();
                        Vec3 smoothPos = new Vec3(x, y, z);
                        Vec3 diff = smoothPos.subtract(player.getPositionVector());
                        RenderUtils.renderHitBox(player.getEntityBoundingBox().offset(diff));
                        RenderUtils.stop3D();
                    }
                    case "Player" -> {
                        mc.entityRenderer.enableLightmap();
                        mc.getRenderManager().doRenderEntity(
                                player,
                                x, y, z,
                                player.getRotationYawHead(),
                                mc.timer.renderPartialTicks,
                                true
                        );

                        RenderHelper.disableStandardItemLighting();
                        mc.entityRenderer.disableLightmap();
                    }
                }
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
        posBuffer.removeIf(pos -> System.currentTimeMillis() - pos.time() >= maxDelay.getValue());
    }

    private void resetAllPackets() {
        buffer.forEach(packetWithTime -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packetWithTime.packet()));
        buffer.clear();
        posBuffer.clear();
    }

    private void reset() {
        resetAllPackets();
        lastResetTime = System.currentTimeMillis();
        delayBeforeNextLag = delayBeforeNextLagAfterReset.getValue();
    }

    private record PacketWithTime(Packet packet, long time) {}
    private record VecWithTime(Vec3 pos, long time) {}
}
