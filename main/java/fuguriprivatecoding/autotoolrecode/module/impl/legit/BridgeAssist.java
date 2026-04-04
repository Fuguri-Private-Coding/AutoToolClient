package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "BridgeAssist", category = Category.LEGIT, description = "Помогает в строительстве.")
public class BridgeAssist extends Module {

    FloatSetting edgeOffset = new FloatSetting("EdgeOffset", this, 0f,0.1f,0.05f, 0.01f);
    CheckBox sneakIfPressed = new CheckBox("SneakIfPressed", this);
    CheckBox pitchCheck = new CheckBox("PitchCheck", this);

    DoubleSlider pitch = new DoubleSlider("Pitch",this, pitchCheck::isToggled, 0,90, 90,0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof MoveButtonEvent e) {
            if (pitchCheck.isToggled()) {
                float playerPitch = mc.thePlayer.rotationPitch;
                if (playerPitch < pitch.getMinValue() || playerPitch > pitch.getMaxValue()) {
                    return;
                }
            }

            if (mc.thePlayer.capabilities.isFlying) return;

            BlockPos targetPos = MoveUtils.getDirectionalBlockPos(edgeOffset.getValue(), 0.5f);
            boolean isAirBlock = mc.theWorld.isAirBlock(targetPos);

            if (sneakIfPressed.isToggled()) {
                if (e.isSneak() && !isAirBlock) {
                    e.setSneak(false);
                }
            } else {
                if (isAirBlock) {
                    e.setSneak(true);
                }
            }
        }
    }
}
