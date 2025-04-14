package me.hackclient.module.impl.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.*;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.packet.TimeUtils;
import me.hackclient.utils.packet.TimedVar;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.*;
import net.minecraft.network.login.client.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import me.hackclient.event.Event;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;

@ModuleInfo(
        name = "FakeLag",
        category = Category.CONNECTION
)
public class FakeLag extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay",this,50, 1000, 400);
    final BooleanSetting spoofRealPing = new BooleanSetting("Spoof Real Ping",this, false);

    final TimeUtils advancedTimer = new TimeUtils();
    EntityLivingBase advancedTarget;

    final List<TimedVar<Packet>> inbound = new CopyOnWriteArrayList<>();
    final List<TimedVar<Packet>> outbound = new CopyOnWriteArrayList<>();

    final List<Vec3> clientPoses = new ArrayList<>();
    State state = State.CLIENT;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (mc.thePlayer == null || mc.theWorld == null) return;
        switch (event) {
            case PacketEvent e -> {
                final Packet packet = e.getPacket();
                final PacketDirection direction = e.getDirection();

                if (spoofRealPing.isToggled() && packet instanceof C00PacketKeepAlive) {
                    return;
                }

                switch (packet) {
                    case C00Handshake _, C00PacketLoginStart _, C00PacketServerQuery _ -> {
                        return;
                    }

                    default -> {}
                }


                if (direction == PacketDirection.OUTGOING) {
                    if (state != State.CLIENT) {
                        break;
                    }

                    if (packet instanceof C03PacketPlayer c03 && c03.isMoving()) {
                        clientPoses.add(c03.getPosVec());
                    }

                    boolean reset = false;

                    switch (packet) {
                        case C02PacketUseEntity c02 -> {
                            if (c02.getAction() == C02PacketUseEntity.Action.ATTACK && c02.getEntityFromWorld(mc.theWorld) instanceof EntityLivingBase ent) {
                                clearOutBound();
                                advancedTarget = ent;
                                advancedTimer.reset();
                                state = State.SERVER;
                                reset = true;
                            }
                        }
                        case C08PacketPlayerBlockPlacement _ -> {
                            clearOutBound();
                            reset = true;
                        }
                        default -> {
                        }
                    }

                    if (reset) {
                        break;
                    }

                    e.cancel();
                    outbound.add(new TimedVar<>(packet));
                } else if (direction == PacketDirection.INCOMING) {
                    if (state != State.SERVER) {
                        break;
                    }

                    boolean reset = false;

                    switch (packet) {
                        case S06PacketUpdateHealth _, S29PacketSoundEffect _, S19PacketEntityStatus _,
                             S0BPacketAnimation _ -> reset = true;
                        default -> {
                        }
                    }

                    if (reset) {
                        break;
                    }

                    e.cancel();
                    inbound.add(new TimedVar<>(packet));
                }
            }

            case MotionEvent _ -> {
                while (clientPoses.size() > (delay.getValue() / 50)) {
                    clientPoses.removeFirst();
                }
            }

            case Render3DEvent _ -> {
                handleAll();
                if (advancedTarget != null) {
                    AxisAlignedBB box = advancedTarget.getEntityBoundingBox();
                    AxisAlignedBB realBox = box.offset(advancedTarget.getRealPos().subtract(advancedTarget.getPositionVector()));

                    if (advancedTimer.reached(5000) || DistanceUtils.getDistanceToHitBox(box) >= DistanceUtils.getDistanceToHitBox(realBox) || DistanceUtils.getDistanceToHitBox(realBox) > 6) {
                        clearInBound();
                        advancedTarget = null;
                        state = State.CLIENT;
                    }
                }

                float partialTicks = mc.timer.renderPartialTicks;

                if (clientPoses.size() >= 2 && mc.gameSettings.thirdPersonView != 0) {
                    Vec3 lastPos = clientPoses.getFirst();
                    Vec3 pos = clientPoses.get(1);

                    boolean b = clientPoses.size() <= (delay.getValue() / 50);

                    final double x = b ? pos.xCoord : lastPos.xCoord + (pos.xCoord - lastPos.xCoord) * partialTicks;
                    final double y = b ? pos.yCoord : lastPos.yCoord + (pos.yCoord - lastPos.yCoord) * partialTicks;
                    final double z = b ? pos.zCoord : lastPos.zCoord + (pos.zCoord - lastPos.zCoord) * partialTicks;

                    RendererLivingEntity.NAME_TAG_RANGE = 0;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 0;

                    renderWithAbsolutePosition(() -> mc.getRenderManager().renderEntityWithPosYaw(mc.thePlayer, x, y, z, mc.thePlayer.rotationYaw, partialTicks));

                    RendererLivingEntity.NAME_TAG_RANGE = 64;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 32;

                    RenderHelper.disableStandardItemLighting();
                    mc.entityRenderer.disableLightmap();
                }

                if (state == State.SERVER && /* && !outbound.isEmpty() && */advancedTarget != null) {
                    double x = advancedTarget.realX;
                    double y = advancedTarget.realY;
                    double z = advancedTarget.realZ;

                    RendererLivingEntity.NAME_TAG_RANGE = 0;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 0;

                    renderWithAbsolutePosition(() -> mc.getRenderManager().renderEntityWithPosYaw(advancedTarget, x, y, z, advancedTarget.rotationYawHead, partialTicks));

                    RendererLivingEntity.NAME_TAG_RANGE = 64;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 32;

                    RenderHelper.disableStandardItemLighting();
                    mc.entityRenderer.disableLightmap();
                }
            }
            default -> {}
        }
    }

    void handleAll() {
        handleInBound();
        handleOutBound();
    }

    void handleOutBound() {
        outbound.removeIf(var -> {
            if (TimeUtils.reached(var.getTime(), delay.getValue())) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(var.getVar());
                return true;
            }
            return false;
        });
    }

    void handleInBound() {
        inbound.removeIf(var -> {
            if (TimeUtils.reached(var.getTime(), delay.getValue())) {
                try {
                    var.getVar().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
                } catch (Exception _) {}
                return true;
            }
            return false;
        });
    }

    public static void renderWithAbsolutePosition(Runnable runnable) {
        final RenderManager renderManager = mc.getRenderManager();
        double x = renderManager.viewerPosX, y = renderManager.viewerPosY, z = renderManager.viewerPosZ;

        GlStateManager.translate(-x, -y, -z);
        runnable.run();
        GlStateManager.translate(x, y, z);
    }

    void clearOutBound() {
        outbound.forEach(var -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(var.getVar()));
        outbound.clear();
    }

    void clearInBound() {
        inbound.forEach(var -> {
            try {
                var.getVar().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
            } catch (Exception _) {}
        });
        inbound.clear();
    }

    enum State {
        CLIENT, SERVER
    }
}
