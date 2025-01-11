package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PackerDirection;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

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

    final IntegerSetting timeToLag = new IntegerSetting("Time", this, 10, 1000, 100);
    final IntegerSetting timeToCancel = new IntegerSetting("TicksToStopWork", this, 1, 10, 2);

    final StopWatch attackTimer;
    final List<Doubles<Packet, Long>> packetBuffer;

    EntityLivingBase target;

    public BackTrack() {
        attackTimer = new StopWatch();
        packetBuffer = new ArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

        if (event instanceof PacketEvent packetEvent && packetEvent.getDirection() == PackerDirection.INCOMING) {
            Packet packet = packetEvent.getPacket();

            if (packet instanceof S14PacketEntity s14
                    && mc.theWorld.getEntityByID(s14.entityId) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.realX += s14.func_149062_c();
                entityLivingBase.realY += s14.func_149061_d();
                entityLivingBase.realZ += s14.func_149064_e();
                if (entityLivingBase.equals(target)) {
                    packetEvent.setCanceled(true);
                    packetBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                }
            }

            if (packet instanceof S18PacketEntityTeleport s18
                    && mc.theWorld.getEntityByID(s18.getEntityId()) instanceof EntityLivingBase entityLivingBase) {
                entityLivingBase.realX = s18.getX();
                entityLivingBase.realY = s18.getY();
                entityLivingBase.realZ = s18.getZ();
                if (entityLivingBase.equals(target)) {
                    packetEvent.setCanceled(true);
                    packetBuffer.add(new Doubles<>(packet, System.currentTimeMillis()));
                }
            }
        }

        if (event instanceof RunGameLoopEvent) {
            if (attackTimer.reachedMS(timeToCancel.getValue() * 1000L)) {
                target = null;
                packetBuffer.forEach( pair -> pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler()));
                packetBuffer.clear();
            }

            if (!packetBuffer.isEmpty()) {
                mc.addScheduledTask(() -> {
                    packetBuffer.removeIf(pair -> {
                        if (System.currentTimeMillis() - pair.getSecond() >= timeToLag.getValue()) {
                            pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
                            return true;
                        }
                        return false;
                    });
                });
            }
        }

        if (event instanceof AttackEvent attackEvent
                && attackEvent.getHittingEntity() instanceof EntityLivingBase newTarget) {
            attackTimer.reset();
            target = newTarget;
        }

        if (target != null) {
            double x1 = target.realX / 32;
            double y1 = target.realY / 32;
            double z1 = target.realZ / 32;

            if (event instanceof RunGameLoopEvent) {
                if (mc.thePlayer.getDistance(x1, y1, z1) < mc.thePlayer.getDistanceToEntity(target)) {
                    packetBuffer.forEach( pair -> pair.getFirst().processPacket(mc.getNetHandler().getNetworkManager().getNetHandler()));
                    packetBuffer.clear();
                    target = null;
                }
            }
            if (event instanceof Render3DEvent) {
                RenderUtils.start3D();
                GL11.glColor4f(1f, 1f, 1f, 1f);
                RenderUtils.renderHitBox(
                        target.getEntityBoundingBox()
                                .offset(x1 - target.posX, y1 - target.posY, z1 - target.posZ)
                                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ),
                        GL11.GL_LINE_LOOP
                );
                RenderUtils.stop3D();
            }
        }
    }

    @Override
    public String getSuffix() {
        return timeToLag.getValue() + "ms";
    }
}