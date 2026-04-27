package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.AttackEvent;
import fuguriprivatecoding.autotoolrecode.handle.Player;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import java.util.Random;

@ModuleInfo(name = "Velocity", category = Category.COMBAT, description = "Позволяет откидыватся меньше.")
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Cancel", "Jump", "Intave")
            .setMode("Cancel");

    final IntegerSetting horizontal = new IntegerSetting("Horizontal", this,() -> mode.is("Cancel"), -100, 100, 0);
    final IntegerSetting vertical = new IntegerSetting("Vertical", this,() -> mode.is("Cancel"), 0, 100, 100);

    final CheckBox onlyOnLadder = new CheckBox("OnlyOnLadder", this, () -> mode.is("Cancel"), false);

    final FloatSetting intaveXZ = new FloatSetting("IntaveXZ", this, () -> mode.is("Intave"),0,1,0.6f, 0.01f);
    final CheckBox intaveJump = new CheckBox("IntaveJump", this, () -> mode.is("Intave"));

    final IntegerSetting jumpChance = new IntegerSetting("Chance", this, () -> mode.is("Jump") || (mode.is("Intave") && intaveJump.isToggled()), 0,100,80);

    private final Random rand = new Random();

    @Override
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Cancel" -> {
                if (event instanceof PacketEvent e) {
                    Packet packet = e.getPacket();

                    if (packet instanceof S12PacketEntityVelocity s12 && s12.getId() == mc.thePlayer.getEntityId()) {
                        if (onlyOnLadder.isToggled()) {
                            if (mc.thePlayer.isOnLadder()) e.cancel();
                        } else {
                            e.cancel();
                        }

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
                    if (needJump()) {
                        e.setJump(true);
                    }
                }
            }

            case "Jump" -> {
                if (event instanceof MoveButtonEvent e) {
                    if (needJump()) {
                        e.setJump(true);
                    }
                }
            }
        }
    }

    private boolean needJump() {
        return Player.fallDistance == 0 && mc.thePlayer.hurtTime == 9 && mc.thePlayer.onGround && mc.thePlayer.isSprinting() && rand.nextInt(100) <= jumpChance.getValue();
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}
