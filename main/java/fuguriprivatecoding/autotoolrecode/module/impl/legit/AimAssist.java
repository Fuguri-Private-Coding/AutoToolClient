package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetFinder;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AimAssist", category = Category.LEGIT, description = "Помощь в прицеливании.")
public class AimAssist extends Module {

    final FloatSetting hSpeed = new FloatSetting("HorizontalSpeed", this, 0f, 30.0f, 5.0f, 0.1f) {};
    final CheckBox moveVertical = new CheckBox("MoveVertical", this, false);
    final FloatSetting vSpeed = new FloatSetting("VerticalSpeed", this, moveVertical::isToggled, 0f, 30.0f, 2.5f, 0.1f) {};
    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 12.0f, 6.0f, 0.1f) {};
    final IntegerSetting fov = new IntegerSetting("Fov", this, 10, 180, 35);
    final CheckBox onlyMoveForward = new CheckBox("OnlyMoveForward", this, false);
    final CheckBox onlyWhenSprinting = new CheckBox("OnlyWhenSprinting", this, false);
    final CheckBox onlyWhenMouseHolding = new CheckBox("OnlyWhenMouseHolding", this, false);

    @Override
    public void onDisable() {
        super.onDisable();
        Client.INST.getTargetStorage().setTarget(null);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MotionEvent) {
            Client.INST.getTargetStorage().setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));

            EntityLivingBase target = Client.INST.getTargetStorage().getTarget();

            if (target == null || mc.currentScreen != null) return;
            if (RotUtils.getFovToEntity(target) > fov.getValue()) return;
            if (onlyMoveForward.isToggled() && mc.thePlayer.moveForward <= 0) return;
            if (onlyWhenSprinting.isToggled() && !mc.thePlayer.isSprinting()) return;
            if (onlyWhenMouseHolding.isToggled() && !Mouse.isButtonDown(0)) return;

            Vec3 targetPoint = target.getPositionEyes(1.0f);
            Rot playerRotation = new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Delta delta = RotUtils.getDeltaToPoint(playerRotation, targetPoint);

            delta.setYaw(MathHelper.clamp(delta.getYaw(), -hSpeed.getValue(), hSpeed.getValue()));
            delta.setPitch(MathHelper.clamp(delta.getPitch(), -vSpeed.getValue(), vSpeed.getValue()));

            delta = RotUtils.fixDelta(delta);

            mc.thePlayer.rotationYaw += delta.getYaw();
            if (moveVertical.isToggled()) mc.thePlayer.rotationPitch += delta.getPitch();
        }
    }
}
