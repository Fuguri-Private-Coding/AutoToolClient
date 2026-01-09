package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetFinder;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "AimAssist", category = Category.LEGIT, description = "Помощь в прицеливании.")
public class AimAssist extends Module {

    final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Body")
        ;

    final BooleanSupplier boxSize = () -> hitVec.is("Best") || hitVec.is("Nearest");
    final IntegerSetting hBoxSize = new IntegerSetting("HBoxSize", this, boxSize, 1, 100, 100);
    final IntegerSetting vBoxSize = new IntegerSetting("VBoxSize", this, boxSize, 1, 100, 100);

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 20, 5, 0.1f);
    final CheckBox moveVertical = new CheckBox("MoveVertical", this, false);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, moveVertical::isToggled, 0, 20, 5, 0.1f);

    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 12.0f, 6.0f, 0.1f) {};
    final IntegerSetting fov = new IntegerSetting("Fov", this, 10, 180, 35);

    final MultiMode workingWhile = new MultiMode("WorkingWhile", this)
        .addModes("MoveForward", "Sprinting", "MouseHolding")
        ;

    @Override
    public void onDisable() {
        TargetStorage.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.POST) {
            TargetStorage.setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));

            EntityLivingBase target = TargetStorage.getTarget();

            if (target == null || mc.currentScreen != null) return;
            if (RotUtils.getFovToEntity(target) > fov.getValue()) return;

            if (workingWhile.get("MoveForward") && mc.thePlayer.moveForward <= 0) return;
            if (workingWhile.get("Sprinting") && !mc.thePlayer.isSprinting()) return;
            if (workingWhile.get("MouseHolding") && !Mouse.isButtonDown(0)) return;

            Rot lr = mc.thePlayer.getRotation();

            AxisAlignedBB box = RotUtils.getHitBox(target, hBoxSize.getValue(), vBoxSize.getValue());

            Rot needRot = getRotation(target, box, lr);

            Rot speed = new Rot(
                yawSpeed.getRandomizedIntValue(),
                moveVertical.isToggled() ? pitchSpeed.getRandomizedIntValue() : 0
            );

            if (needRot != null) {
                Rot delta = RotUtils.getDelta(lr, needRot);

                RotUtils.limitDelta(delta, speed);
                delta = RotUtils.fixDelta(delta);

                mc.thePlayer.moveRotation(
                    delta.getYaw(),
                    delta.getPitch()
                );
            }
        }
    }

    private Rot getRotation(EntityLivingBase target, AxisAlignedBB box, Rot lr) {
        return switch (hitVec.getMode()) {
            case "Best" -> RotUtils.getBestRotation(box);
            case "Nearest" -> RotUtils.getNearestRotations(lr, box);
            case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
            case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
            default -> null;
        };
    }
}
