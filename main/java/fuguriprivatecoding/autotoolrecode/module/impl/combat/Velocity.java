package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import java.util.Random;

@ModuleInfo(name = "Velocity", category = Category.COMBAT, description = "Позволяет откидыватся меньше.")
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "Jump", "Intave")
            .setMode("Vanilla");

    final FloatSetting vanillaXZ = new FloatSetting("XZ", this,() -> mode.is("Vanilla"), -1, 1, 0, 0.1f);
    final FloatSetting vanillaY = new FloatSetting("Y", this,() -> mode.is("Vanilla"), 0, 1, 1, 0.1f);

    final FloatSetting intaveSprintXZ = new FloatSetting("IntaveXZ", this, () -> mode.is("Intave"),0,1,0.6f, 0.01f);
    final CheckBox intaveJump = new CheckBox("IntaveJump", this, () -> mode.is("Intave"));

    final IntegerSetting jumpChance = new IntegerSetting("Chance", this, () -> mode.is("Jump") || (mode.is("Intave") && intaveJump.isToggled()), 0,100,80);
    final DoubleSlider jumpDelay = new DoubleSlider("JumpDelay", this, () -> mode.is("Jump") || (mode.is("Intave") && intaveJump.isToggled()), 0, 500,80,1);

    private boolean jumps, gotHit;
    private int delay, lastHurtTime;

    private final StopWatch timer = new StopWatch();
    private final Random rand = new Random();

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Vanilla" -> {
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

                    deltaMotionX *= vanillaXZ.getValue();
                    deltaMotionY *= vanillaY.getValue();
                    deltaMotionZ *= vanillaXZ.getValue();

                    mc.thePlayer.motionX += deltaMotionX;
                    mc.thePlayer.motionY += deltaMotionY;
                    mc.thePlayer.motionZ += deltaMotionZ;
                }
            }
            case "Intave" -> {
                if (event instanceof AttackEvent) {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= intaveSprintXZ.getValue();
                        mc.thePlayer.motionZ *= intaveSprintXZ.getValue();
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
