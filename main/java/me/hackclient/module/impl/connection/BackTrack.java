package me.hackclient.module.impl.connection;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.visual.ClientShader;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.utils.animation.Animation3D;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "BackTrack", category = Category.CONNECTION)
public class BackTrack extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "LagBased",
            new String[] {
                    "LagBased",
            }
    );

    final ModeSetting lagMode = new ModeSetting(
            "LagMode",
            this,
            "Smooth",
            new String[] {
                    "Smooth",
                    "Pulse"
            }
    );

    final IntegerSetting delay = new IntegerSetting("Delay", this, 10, 5000, 50);
    final BooleanSetting limitDistance = new BooleanSetting("LimitDistance", this, true);
    final FloatSetting maxDistance = new FloatSetting("MaxDistance", this, limitDistance::isToggled, 3.0f, 6.0f, 4.5f, 0.1f);
    final IntegerSetting timeToCancel = new IntegerSetting("TicksToStopWork", this, 1, 10, 1);
    final BooleanSetting renderOnlyIfWorking = new BooleanSetting("RenderOnlyIfWorking", this, true);
    final BooleanSetting renderClientPos = new BooleanSetting("RenderClientPos", this, false);
    final BooleanSetting renderServerPos = new BooleanSetting("RenderServerPos", this, true);

    final Animation3D animation3D;

    final StopWatch attackTimer, resetTimer;

    ClientShader clientShader;

    EntityLivingBase target;

    public BackTrack() {
        resetTimer = new StopWatch();
        attackTimer = new StopWatch();
        animation3D = new Animation3D();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (clientShader == null) {
            clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
            return;
        }
        if (event instanceof AttackEvent attackEvent
                && attackEvent.getHittingEntity() instanceof EntityLivingBase newTarget) {
            attackTimer.reset();
            target = newTarget;
        }

        if (event instanceof RunGameLoopEvent) {
            if (attackTimer.reachedMS(timeToCancel.getValue() * 1000L)) {
                target = null;
            }
            if (target == null) {
                resetPackets();
                attackTimer.reset();
            } else {
                handlePackets();
                double distance = mc.thePlayer.getDistance(target.realX / 32, target.realY / 32, target.realZ / 32);
                if (distance > (limitDistance.isToggled() ? maxDistance.getValue() : Double.MAX_VALUE) || distance < 3 || distance <= mc.thePlayer.getDistanceToEntity(target)) {
                    resetPackets();
                    return;
                }
            }
        }

        if (event instanceof PacketEvent packetEvent && packetEvent.getDirection() == PacketDirection.INCOMING
        && mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ))) {
            Packet packet = packetEvent.getPacket();

            if (packet instanceof S14PacketEntity s14
                    && mc.theWorld.getEntityByID(s14.entityId) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.realX += s14.func_149062_c();
                entityLivingBase.realY += s14.func_149061_d();
                entityLivingBase.realZ += s14.func_149064_e();
            }

            if (packet instanceof S18PacketEntityTeleport s18
                    && mc.theWorld.getEntityByID(s18.getEntityId()) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.realX = s18.getX();
                entityLivingBase.realY = s18.getY();
                entityLivingBase.realZ = s18.getZ();
            }

            if (target != null && (packet instanceof S32PacketConfirmTransaction || packet instanceof S12PacketEntityVelocity || packet instanceof S14PacketEntity || packet instanceof S18PacketEntityTeleport || packet instanceof S19PacketEntityHeadLook || packet instanceof S08PacketPlayerPosLook || packet instanceof S01PacketPong || packet instanceof S03PacketTimeUpdate)) {
                PacketHandler.serverPacketBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                packetEvent.setCanceled(true);
            }
        }

        if (event instanceof Render3DEvent) {
            if (renderOnlyIfWorking.isToggled() && target == null)
                return;

            RenderUtils.start3D();
            if (renderClientPos.isToggled()) {
                GL11.glColor4f(1f, 0, 0, 1f);
                RenderUtils.renderHitBox(
                        target.getEntityBoundingBox()
                                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ),
                        GL11.GL_LINE_LOOP
                );
                GL11.glColor4f(1f, 1f, 1f, 1f);
            }
            if (renderServerPos.isToggled()) {
                GL11.glColor4f(1f, 1f, 1f, 1f);
                animation3D.update(20);
                animation3D.endX = target.realX;
                animation3D.endY = target.realY;
                animation3D.endZ = target.realZ;
                if (clientShader.isToggled() && clientShader.backtrack.isToggled()) {
                    PixelReplacerUtils.addToDraw(() -> RenderUtils.renderHitBox(
                            target.getEntityBoundingBox()
                                    .offset(animation3D.x / 32 - target.posX, animation3D.y / 32 - target.posY, animation3D.z / 32 - target.posZ)
                                    .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ),
                            GL11.GL_LINE_LOOP
                    ));
                } else {
                    RenderUtils.renderHitBox(
                            target.getEntityBoundingBox()
                                    .offset(animation3D.x / 32 - target.posX, animation3D.y / 32 - target.posY, animation3D.z / 32 - target.posZ)
                                    .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ),
                            GL11.GL_LINE_LOOP
                    );
                }
            }
            RenderUtils.stop3D();
        }
    }

    void handlePackets() {
        switch (lagMode.getMode()) {
            case "Pulse" -> {
                if (resetTimer.reachedMS(delay.getValue())) {
                    resetTimer.reset();
                    resetPackets();
                }
            }
            case "Smooth" -> PacketHandler.serverPacketBuffer.forEach(packetLongDoubles -> {
                if (System.currentTimeMillis() - packetLongDoubles.getSecond() >= delay.getValue()) {
                    processPacket(packetLongDoubles.getFirst());
                    PacketHandler.serverPacketBuffer.remove(packetLongDoubles);
                }
            });
        }
    }

    void processPacket(Packet packet) {
        packet.processPacket(mc.getNetHandler().getNetworkManager().packetListener);
    }

    void resetPackets() {
        PacketHandler.serverPacketBuffer.forEach(packetLongDoubles -> processPacket(packetLongDoubles.getFirst()));
        PacketHandler.serverPacketBuffer.clear();
    }
}