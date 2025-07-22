package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import java.util.Random;

@ModuleInfo(name = "Velocity", category = Category.COMBAT, description = "Позволяет откидыватся меньше.")
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "Jump", "Intave")
            .setMode("Vanilla");

    final IntegerSetting chance = new IntegerSetting("Chance", this, () -> mode.getMode().equalsIgnoreCase("Jump"), 0,100,80);
    final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, () -> mode.getMode().equalsIgnoreCase("Jump"), 0,500,80) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return super.getValue();
        }
    };
    final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, () -> mode.getMode().equalsIgnoreCase("Jump"), 0,500,80) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return super.getValue();
        }
    };

    final FloatSetting XZ = new FloatSetting("XZ", this,() -> mode.getMode().equalsIgnoreCase("Vanilla"), -1, 1, 0, 0.1f);
    final FloatSetting Y = new FloatSetting("Y", this,() -> mode.getMode().equalsIgnoreCase("Vanilla"), 0, 1, 1, 0.1f);

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
                        && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                    e.cancel();
                    double needMotionX = s12.getMotionX() / 8000d;
                    double needMotionY = s12.getMotionY() / 8000d;
                    double needMotionZ = s12.getMotionZ() / 8000d;

                    double deltaMotionX = needMotionX - mc.thePlayer.motionX;
                    double deltaMotionY = needMotionY - mc.thePlayer.motionY;
                    double deltaMotionZ = needMotionZ - mc.thePlayer.motionZ;

                    deltaMotionX *= XZ.getValue();
                    deltaMotionY *= Y.getValue();
                    deltaMotionZ *= XZ.getValue();

                    mc.thePlayer.motionX += deltaMotionX;
                    mc.thePlayer.motionY += deltaMotionY;
                    mc.thePlayer.motionZ += deltaMotionZ;
                }
            }
            case "Intave" -> {
                if (event instanceof AttackEvent) {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= 0.6f;
                        mc.thePlayer.motionZ *= 0.6f;
                        mc.thePlayer.setSprinting(false);
                    }
                }
            }

            case "Jump" -> {
                if (event instanceof MoveButtonEvent e) {
                    if (mc.thePlayer.hurtTime == 10 && mc.thePlayer.hurtTime != lastHurtTime && rand.nextInt(100) <= chance.getValue()) {
                        delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());
                        gotHit = true;
                        timer.reset();
                    }
                    lastHurtTime = mc.thePlayer.hurtTime;

                    if (timer.reachedMS(delay) && !jumps && gotHit) {
                        e.setJump(true);
                        jumps = true;
                        timer.reset();
                        gotHit = false;
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
