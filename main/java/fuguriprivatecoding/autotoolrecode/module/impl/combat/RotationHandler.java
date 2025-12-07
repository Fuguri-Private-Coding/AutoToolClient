package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;

@ModuleInfo(name = "RotationHandler", category = Category.COMBAT, description = "Плавный разворот ротации при ее изменении.")
public class RotationHandler extends Module {

    final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 1, 180, 30, 1);
    final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 1, 180, 30, 1);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent && CameraRot.INST.needBackRotate()) {
            Delta delta = RotUtils.getDelta(mc.thePlayer.getRotation(), CameraRot.INST);
            if (isToggled()) delta = delta.limit((float) yawSpeed.getRandomizedDoubleValue(), (float) pitchSpeed.getRandomizedDoubleValue());

            mc.thePlayer.moveRotation(delta.fix());

            delta = RotUtils.getDelta(mc.thePlayer.getRotation(), CameraRot.INST);

            if (delta.hypot() <= RotUtils.getMouseGCD()) {
                CameraRot.INST.setUnlocked(false);
            }
        }
    }

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }
}
