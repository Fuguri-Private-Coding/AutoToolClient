package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "BridgeAssist", category = Category.LEGIT, description = "Помогает в строительстве.")
public class BridgeAssist extends Module {

    FloatSetting edgeOffset = new FloatSetting("EdgeOffset", this, 0f,0.1f,0.05f, 0.01f);
    CheckBox sneakIfPressed = new CheckBox("SneakIfPressed", this);
    CheckBox pitchCheck = new CheckBox("PitchCheck", this);
    FloatSetting minPitch = new FloatSetting("MinPitch", this, pitchCheck::isToggled, 0f,90f,50f, 0.1f);
    FloatSetting maxPitch = new FloatSetting("MaxPitch", this, pitchCheck::isToggled, 0f,90f,90f, 0.1f);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MoveButtonEvent e) {
            if (pitchCheck.isToggled() && (mc.thePlayer.rotationPitch < minPitch.getValue() || mc.thePlayer.rotationPitch > maxPitch.getValue())) return;
            if (mc.thePlayer.capabilities.isFlying) return;
            BlockPos pos = getBlockPos(edgeOffset.getValue());
            if (sneakIfPressed.isToggled() && e.isSneak() && !mc.theWorld.isAirBlock(pos)) {
                e.setSneak(false);
            } else if (!sneakIfPressed.isToggled() && mc.theWorld.isAirBlock(pos)) {
                e.setSneak(true);
            }
        }
    }

    private BlockPos getBlockPos(float edgeOffset) {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY - 0.5;
        double z = mc.thePlayer.posZ;

        boolean movingX = Math.abs(mc.thePlayer.motionX) > 0.1;
        boolean movingZ = Math.abs(mc.thePlayer.motionZ) > 0.1;

        if (movingX || movingZ) {
            if (Math.abs(mc.thePlayer.motionX) > Math.abs(mc.thePlayer.motionZ)) {
                x += (mc.thePlayer.motionX > 0) ? -edgeOffset : edgeOffset;
            } else {
                z += (mc.thePlayer.motionZ > 0) ? -edgeOffset : edgeOffset;
            }
        }

        return new BlockPos(x, y, z);
    }
}
