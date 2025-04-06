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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.pathfinding.PathFinder;
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
    private final List<VecWithTime> posBuffer = new CopyOnWriteArrayList<>();

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
                if (posBuffer.isEmpty() || mc.gameSettings.thirdPersonView == 0) {
                    break;
                }

                EntityPlayerSP player = mc.thePlayer;
                RenderManager renderManager = mc.renderManager;
                EntityRenderer entityRenderer = mc.entityRenderer;

                Vec3 lastPos = posBuffer.getFirst().pos();

                double x = lastPos.xCoord - renderManager.viewerPosX;
                double y = lastPos.yCoord - renderManager.viewerPosY;
                double z = lastPos.zCoord - renderManager.viewerPosZ;

                entityRenderer.enableLightmap();

                int i = player.getBrightnessForRender(mc.timer.renderPartialTicks);

                if (player.isBurning()) {
                    i = 15728880;
                }

                int j = i % 65536;
                int k = i / 65536;

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                entityRenderer.disableLightmap();
                renderManager.doRenderEntity(player, x, y, z, player.rotationYaw, mc.timer.renderPartialTicks, true);
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
