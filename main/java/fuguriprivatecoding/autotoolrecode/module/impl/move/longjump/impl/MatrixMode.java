package fuguriprivatecoding.autotoolrecode.module.impl.move.longjump.impl;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.player.LookEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.move.LongJump;
import fuguriprivatecoding.autotoolrecode.module.impl.move.longjump.AbstractLongJumpMode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class MatrixMode extends AbstractLongJumpMode {

    private boolean canBoost;
    private boolean flag;
    private boolean sent;
    private double x;
    private double z;
    private double y;
    private double firstDir;
    private int ticks;

    @Override
    public void onEnable(LongJump longJump) {
        this.canBoost = false;
        this.flag = false;
        this.sent = false;
        this.ticks = 0;
        this.x = mc.thePlayer.posX;
        this.z = mc.thePlayer.posZ;
        this.y = mc.thePlayer.posY;
        this.firstDir = mc.thePlayer.rotationYaw;
    }

    public MatrixMode() {
        super("Matrix");
    }

    @Override
    public void handleEvent(Event event, LongJump longJump) {
        if (event instanceof MoveEvent e) {
            if (!this.canBoost) {
                e.setForward(0);
                e.setStrafe(0);
            }
        }

        if (event instanceof PacketEvent e) {
            if (e.getDirection() == PacketDirection.OUTGOING) {
                if (e.getPacket() instanceof C0FPacketConfirmTransaction) {
                    e.cancel();
                    return;
                }
            }

            if (e.getDirection() == PacketDirection.INCOMING) {
                if (e.getPacket() instanceof S08PacketPlayerPosLook && this.canBoost) {
                    this.flag = true;
                }
            }
        }

        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE) {
            e.setOnGround(false);
            if (!this.sent) {
                e.setX(this.x);
                e.setY(this.y);
                e.setZ(this.z);
            }
        }

        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.POST) {
            if (!this.sent) {
                this.x += -Math.sin(Math.toRadians(this.firstDir)) * (0.2496 - (this.ticks % 3 == 0 ? 0.0806 : (double) 0.0F));
                this.z += Math.cos(Math.toRadians(this.firstDir)) * (0.2496 - (this.ticks % 3 == 0 ? 0.0806 : (double) 0.0F));
            }
        }

        if (event instanceof LookEvent e) {
            if (!this.sent) {
                e.setYaw((float) this.firstDir);
                e.setPitch(1);
            }
        }

        if (event instanceof UpdateEvent) {
            if (!this.sent) {
                mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;

                int maxTicks = Math.round(longJump.autoTick.isToggled() ? longJump.speed.getValue() * 5 : longJump.tick.getValue());

                if (this.ticks > maxTicks) {
                    this.sent = true;
                    this.ticks = 0;
                    this.canBoost = true;
                    mc.timer.timerSpeed = 1.0F;
                }
            }

            if (this.canBoost) {
                MoveUtils.strafe((double) longJump.speed.getValue(), 1);
                mc.thePlayer.motionY = 0.42;
                if (this.flag) {
                    longJump.toggle();
                }
            }

            ++this.ticks;
        }
    }
}
