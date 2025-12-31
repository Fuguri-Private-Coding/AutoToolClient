package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
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
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AimAssist", category = Category.LEGIT, description = "Помощь в прицеливании.")
public class AimAssist extends Module {

    final FloatSetting hSpeed = new FloatSetting("HorizontalSpeed", this, 0f, 30.0f, 5.0f, 0.1f) {};

    final CheckBox moveVertical = new CheckBox("MoveVertical", this, false);
    final FloatSetting vSpeed = new FloatSetting("VerticalSpeed", this, moveVertical::isToggled, 0f, 30.0f, 2.5f, 0.1f) {};
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
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE) {
            TargetStorage.setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));

            EntityLivingBase target = TargetStorage.getTarget();

            if (target == null || mc.currentScreen != null) return;
            if (RotUtils.getFovToEntity(target) > fov.getValue()) return;

            if (workingWhile.get("MoveForward") && mc.thePlayer.moveForward <= 0) return;
            if (workingWhile.get("Sprinting") && !mc.thePlayer.isSprinting()) return;
            if (workingWhile.get("MouseHolding") && !Mouse.isButtonDown(0)) return;

            Vec3 targetPoint = target.getPositionEyes(1.0f);
            Rot playerRotation = new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Rot delta = RotUtils.getDeltaToPoint(playerRotation, targetPoint);

            delta = delta.limit(hSpeed.getValue(), vSpeed.getValue());

            delta = RotUtils.fixDelta(delta);

            mc.thePlayer.rotationYaw += delta.getYaw();
            if (moveVertical.isToggled()) mc.thePlayer.rotationPitch += delta.getPitch();
        }
    }
}
