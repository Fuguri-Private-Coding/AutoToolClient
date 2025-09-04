package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.network.play.server.S27PacketExplosion;

@ModuleInfo(name = "Fly", category = Category.MOVE, description = "Позволяет вам летать.")
public class Fly extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "IntaveFlag")
            .setMode("Vanilla");

    final FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 1f, 0.6f, 0.1f) {};
    final FloatSetting boostMultiple = new FloatSetting("BoostMultiple", this, () -> mode.getMode().equalsIgnoreCase("IntaveFlag"), 0f, 1f, 1f, 0.01f) {};
    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, () -> mode.getMode().equalsIgnoreCase("IntaveFlag"), 5, 40, 5) {};

    int jumps, boostTicks;
    boolean boosting;
    @Override

    public void onDisable() {
        boosting = false;
        boostTicks = 0;
        if (mode.getMode().equalsIgnoreCase("Vanilla")) {
            mc.thePlayer.stopMotion();
            mc.thePlayer.capabilities.flySpeed = 0.05f;
            mc.thePlayer.capabilities.isFlying = false;
            jumps = 0;
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Vanilla" -> {
                if (event instanceof UpdateEvent) {
                    mc.thePlayer.capabilities.isFlying = true;
                    mc.thePlayer.capabilities.flySpeed = speed.getValue();
                }
            }
            case "IntaveFlag" -> {
                if (event instanceof PacketEvent e && e.getPacket() instanceof S27PacketExplosion) {
                    boostTicks = 0;
                    boosting = true;
                }

                if (event instanceof UpdateEvent && mc.thePlayer != null && mc.theWorld != null) {
                    if (boosting) {
                        double boostValue = 2.2 + Math.random() * boostMultiple.getValue();
                        double yawRad = Math.toRadians(mc.thePlayer.rotationYaw);

                        mc.thePlayer.motionX = -Math.sin(yawRad) * boostValue + ((Math.random() - 0.5) * 0.07);
                        mc.thePlayer.motionZ =  Math.cos(yawRad) * boostValue + ((Math.random() - 0.5) * 0.07);
                        mc.thePlayer.motionY = 0.42 + (Math.random() - 0.5) * 0.08;
                        mc.thePlayer.fallDistance = 0f;
                        boostTicks++;
                        if (boostTicks >= maxTicks.getValue()) {
                            boosting = false;
                            mc.thePlayer.motionX = 0.0;
                            mc.thePlayer.motionZ = 0.0;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}