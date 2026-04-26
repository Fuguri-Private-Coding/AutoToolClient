package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "RotationHandler", category = Category.COMBAT, description = "Плавный разворот ротации при ее изменении.")
public class RotationHandler extends Module {

    final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 1, 180, 30, 1);
    final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 1, 180, 30, 1);
    final DoubleSlider mixDelta = new DoubleSlider("MixDelta", this, 0, 100, 1, 1f);

    Rot lastDelta = RotUtils.ZERO;

    @Override
    public void onEvent(Event event) {
        if (CameraRot.INST.needBackRotate()) {
            if (event instanceof TickEvent) {
                Rot delta = mc.thePlayer.getRotation().deltaTo(CameraRot.INST);
                if (isToggled()) delta = delta.limit((float) yawSpeed.getRandomizedDoubleValue(), (float) pitchSpeed.getRandomizedDoubleValue());

                delta.setYaw(MathHelper.lerp((float) mixDelta.getRandomizedIntValue() / 100f, lastDelta.getYaw(), delta.getYaw()));
                delta.setPitch(MathHelper.lerp((float) mixDelta.getRandomizedIntValue() / 100f, lastDelta.getPitch(), delta.getPitch()));

                lastDelta = delta;

                mc.thePlayer.moveRotation(delta.fix());

                delta = mc.thePlayer.getRotation().deltaTo(CameraRot.INST);

                if (delta.length() <= RotUtils.getMouseGCD()) {
                    CameraRot.INST.setUnlocked(false);
                }
            }

            if (event instanceof MoveEvent e) {
                MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));
            }
        }
    }

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }
}
