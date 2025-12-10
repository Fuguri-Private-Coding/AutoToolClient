package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.AttackEvent;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

import java.util.Random;

@ModuleInfo(name = "Velocity", category = Category.COMBAT, description = "Позволяет откидыватся меньше.")
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Cancel", "Jump", "Intave", "Test")
            .setMode("Cancel");

    final IntegerSetting horizontal = new IntegerSetting("Horizontal", this,() -> mode.is("Cancel"), -100, 100, 0);
    final IntegerSetting vertical = new IntegerSetting("Vertical", this,() -> mode.is("Cancel"), 0, 100, 100);

    final FloatSetting intaveXZ = new FloatSetting("IntaveXZ", this, () -> mode.is("Intave"),0,1,0.6f, 0.01f);
    final CheckBox intaveJump = new CheckBox("IntaveJump", this, () -> mode.is("Intave"));

    final IntegerSetting jumpChance = new IntegerSetting("Chance", this, () -> mode.is("Jump") || (mode.is("Intave") && intaveJump.isToggled()), 0,100,80);
    final DoubleSlider jumpDelay = new DoubleSlider("JumpDelay", this, () -> mode.is("Jump") || (mode.is("Intave") && intaveJump.isToggled()), 0, 500,80,1);

    private boolean jumps, gotHit;
    private int delay, lastHurtTime;

    private final StopWatch timer = new StopWatch();
    private final Random rand = new Random();

    @Override
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Cancel" -> {
                if (event instanceof PacketEvent e
                        && e.getPacket() instanceof S12PacketEntityVelocity s12
                        && s12.getId() == mc.thePlayer.getEntityId()) {
                    e.cancel();
                    double needMotionX = s12.getMotionX() / 8000d;
                    double needMotionY = s12.getMotionY() / 8000d;
                    double needMotionZ = s12.getMotionZ() / 8000d;

                    double deltaMotionX = needMotionX - mc.thePlayer.motionX;
                    double deltaMotionY = needMotionY - mc.thePlayer.motionY;
                    double deltaMotionZ = needMotionZ - mc.thePlayer.motionZ;

                    deltaMotionX *= horizontal.getValue() / 100f;
                    deltaMotionY *= vertical.getValue() / 100f;
                    deltaMotionZ *= horizontal.getValue() / 100f;

                    mc.thePlayer.motionX += deltaMotionX;
                    mc.thePlayer.motionY += deltaMotionY;
                    mc.thePlayer.motionZ += deltaMotionZ;
                }
            }

            case "Test" -> {
                if (event instanceof PacketEvent e && e.getPacket() instanceof S12PacketEntityVelocity) {
                    Vec3 pos = new Vec3(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY - mc.thePlayer.motionY - 0.001,
                        mc.thePlayer.posZ
                    );

                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.029, mc.thePlayer.posZ);
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.029, mc.thePlayer.posZ, mc.thePlayer.onGround));
                }
            }

            case "Intave" -> {
                if (event instanceof AttackEvent) {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= intaveXZ.getValue();
                        mc.thePlayer.motionZ *= intaveXZ.getValue();
                        mc.thePlayer.setSprinting(false);
                    }
                }

                if (event instanceof MoveButtonEvent e && intaveJump.isToggled()) {
                    if (mc.thePlayer.hurtTime == 10 && mc.thePlayer.hurtTime != lastHurtTime && rand.nextInt(100) <= jumpChance.getValue()) {
                        delay = jumpDelay.getRandomizedIntValue();
                        gotHit = true;
                        timer.reset();
                    }
                    lastHurtTime = mc.thePlayer.hurtTime;

                    if (timer.reachedMS(delay) && !jumps && gotHit) {
                        e.setJump(true);
                        jumps = true;
                        gotHit = false;
                        timer.reset();
                    }

                    if (mc.thePlayer.onGround) jumps = false;
                }
            }

            case "Jump" -> {
                if (event instanceof MoveButtonEvent e) {
                    if (mc.thePlayer.hurtTime == 10 && mc.thePlayer.hurtTime != lastHurtTime && rand.nextInt(100) <= jumpChance.getValue()) {
                        delay = jumpDelay.getRandomizedIntValue();
                        gotHit = true;
                        timer.reset();
                    }
                    lastHurtTime = mc.thePlayer.hurtTime;

                    if (timer.reachedMS(delay) && !jumps && gotHit) {
                        e.setJump(true);
                        jumps = true;
                        gotHit = false;
                        timer.reset();
                    }

                    if (mc.thePlayer.onGround) jumps = false;
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}
