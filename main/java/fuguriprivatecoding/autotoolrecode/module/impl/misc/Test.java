package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.value.Doubles;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Test", category = Category.MISC, description = "тестовый модуль.")
public class Test extends Module {

//    private EntityLivingBase target;

    @Override
    public void onDisable() {
//        target = null;
//        mc.thePlayer.noClip = false;
        for (C0FPacketConfirmTransaction c0f : c0fs) {
            PacketUtils.sendPacket(c0f);
        }
        c0fs.clear();
        perdet = false;
    }

    private boolean perdet;
    private List<C0FPacketConfirmTransaction> c0fs = new CopyOnWriteArrayList<>();
    private long lastHitTime;

    @Override
    public void onEvent(Event event) {
//        if (event instanceof PacketEvent e) {
//            Packet packet = e.getPacket();
//
//            if (packet instanceof C0FPacketConfirmTransaction c0f) {
//                e.cancel();
//                c0fs.add(c0f);
//            }
//
//            if (packet instanceof S12PacketEntityVelocity pcket && pcket.getId() == mc.thePlayer.getEntityId() && mc.thePlayer.onGround) {
//                e.cancel();
//                perdet = true;
//                lastHitTime = System.currentTimeMillis();
//                mc.timer.timerSpeed = 0.2f;
//                MoveUtils.stopMotion();
//            }
//
//            if (packet instanceof S08PacketPlayerPosLook && mc.thePlayer.onGround) {
//                perdet = false;
//                mc.timer.timerSpeed = 1f;
//            }

//            if (packet instanceof C03PacketPlayer && perdet) {
//                e.cancel();
//            }
//        }

//        if (event instanceof MotionEvent e) {
//            if (perdet) {
//                e.setY(e.getY() - 0.05);
//            }
//        }

//        if (event instanceof RunGameLoopEvent) {
//            if (!perdet) {
//                for (C0FPacketConfirmTransaction c0f : c0fs) {
//                    PacketUtils.sendPacket(c0f);
//                }
//                c0fs.clear();
//            }
//        }

//        if (event instanceof TickEvent) {
//            updateTarget();
//        }

//        if (event instanceof UpdateEvent) {
//            mc.thePlayer.noClip = true;
//        }

//        boolean render = true;

//        if (target != null) {
//            if (event instanceof Render3DEvent && render) {
//                List<Doubles<Float, Vec3>> vectors = RotUtils.getTestLegitVec(target);
//
//                RenderUtils.start3D();
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
//                GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ);
//                GL11.glPointSize(5);
//                GL11.glBegin(GL11.GL_POINTS);
//
//                for (Doubles<Float, Vec3> vector : vectors) {
//                    GL11.glColor3f(vector.getFirst(), 1 - vector.getFirst(), 0);
//                    GL11.glVertex3d(vector.getSecond().xCoord, vector.getSecond().yCoord, vector.getSecond().zCoord);
//                }
//
//                GL11.glEnd();
//                GL11.glPointSize(1);
//                GL11.glTranslated(RenderManager.renderPosX, RenderManager.renderPosY, RenderManager.renderPosZ);
//                GL11.glColor4f(1, 1, 1, 1);
//                RenderUtils.stop3D();
//            }

//            if (event instanceof MotionEvent) {
//                List<Doubles<Float, Vec3>> vectors = RotUtils.getTestLegitVec(target);
//                vectors.sort(Comparator.comparingDouble(Doubles::getFirst));
//
//                Doubles<Float, Vec3> selectedVec = vectors.getFirst();
//
//                System.out.println(selectedVec.getFirst());
//                Rot testRot = RotUtils.getRotationToPoint(selectedVec.getSecond());
//
//                Rot delta = RotUtils.getDelta(mc.thePlayer.getRotation(), testRot).divine(1.5f, 1.5f).fix();
//
//                mc.thePlayer.rotationYaw += delta.getYaw();
//                mc.thePlayer.rotationPitch = Math.clamp(mc.thePlayer.rotationPitch + delta.getPitch(), -90, 90);
//            }
//        }
    }

    public boolean shouldNoclip() {
        return isToggled();
    }

//    private void updateTarget() {
//        target = null;
//
//        double bestDistance = Double.MAX_VALUE;
//
//        for (Entity entity : mc.theWorld.loadedEntityList) {
//            if (entity == mc.thePlayer || !(entity instanceof EntityLivingBase livingEntity))
//                continue;
//
//            double distance = mc.thePlayer.getDistanceToEntity(entity);
//
//            if (distance > 6 || distance > bestDistance)
//                continue;
//
//            target = livingEntity;
//            bestDistance = distance;
//        }
//    }
}