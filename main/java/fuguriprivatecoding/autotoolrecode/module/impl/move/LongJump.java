package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.MoveEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "LongJump",category = Category.MOVE)
public class LongJump extends Module {


    int stage = 0;
    boolean bool0 = false;
    boolean bool1 = false;
    boolean bool2;
    Vec3 vec0;

    @Override
    public void onEnable() {
        stage = 0;
        bool0 = false;
        bool1 = false;
        bool2 = false;
        vec0 = null;
        if(mc.thePlayer != null) vec0 = mc.thePlayer.getPositionVector();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MoveEvent e) {
            if (MoveUtils.isMoving()) {
                if(bool1 && mc.thePlayer.onGround) {
                    stage = 0;
                    bool1 = false;
                    bool2 = false;
                    bool0 = false;
                    vec0 = mc.thePlayer.getPositionVector();
                }
                if(mc.thePlayer.onGround && stage == 0) {
                    mc.thePlayer.jump();
                    if(!mc.thePlayer.isSprinting()) {
                        MoveUtils.strafe2(0.5);
                        MoveUtils.limit2speed(0.4899 + RandomUtils.nextDouble(0.00003, 0.00007));
                    }
                }
                mc.thePlayer.motionY += 0.0034;
                if(!bool0 && MathUtils.distance(vec0.xCoord, vec0.zCoord, mc.thePlayer.posX, mc.thePlayer.posZ) <= 2.79 && MoveUtils.getSpeedNew() < 0.27) MoveUtils.limit2speed(0.199);
                if(stage >= 2 && MathUtils.distance(vec0.xCoord, vec0.zCoord, mc.thePlayer.posX, mc.thePlayer.posZ) >= 2.71 && !bool2) {
                    double strafe = 0.999;
                    PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + MoveUtils.getXT() * strafe, mc.thePlayer.posY + 0.4233, mc.thePlayer.posZ + MoveUtils.getZT() * strafe, false));
                    bool2 = true;
                }
                if(!bool2) stage++;
                if(bool0) {
                    mc.thePlayer.motionY = (0.42);
                    double strafe = 2.7100000;
                            if(MoveUtils.getSpeedNew() > 0.4) strafe = 3.7000;
                            if(MoveUtils.getSpeedNew() > 0.5) strafe = 4.6200;
                            if(MoveUtils.getSpeedNew() > 0.55) strafe = 4.9000;
                            if(MoveUtils.getSpeedNew() > 0.6) strafe = 5.3000;
                            if(MoveUtils.getSpeedNew() > 0.65) strafe = 5.8100;
                            if(MoveUtils.getSpeedNew() > 0.8) strafe = 6.8000;
                            if(MoveUtils.getSpeedNew() > 0.9) strafe = 7.8000;
                            if(MoveUtils.getSpeedNew() > 1.0) strafe = 8.8000;
                    MoveUtils.strafe2(strafe);
                    this.setToggled(false);
                }
            }
        }
        if (event instanceof PacketEvent e) {
            if(e.getPacket() instanceof C03PacketPlayer packet && !bool0 && MathUtils.distance(vec0.xCoord, vec0.zCoord, mc.thePlayer.posX, mc.thePlayer.posZ) <= 2.79) {
                packet.setOnGround(false);
            }
            if(e.getPacket() instanceof S08PacketPlayerPosLook packet && !bool0 && !bool1) {
                e.cancel();
                mc.thePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
                PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), false));
                bool0 = true;
            }
        }
    }
}
