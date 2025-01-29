package me.hackclient.module.impl.legit;

import me.hackclient.Client;
import me.hackclient.combatmanager.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

@ModuleInfo(
    name = "AimAssist",
    category = Category.LEGIT
)
public class AimAssist extends Module {

    final BooleanSetting moveVertical = new BooleanSetting("MoveVertical", this, false);
    final FloatSetting hSpeed = new FloatSetting("HorizontalSpeed", this, 0.1f, 30.0f, 5.0f, 0.1f);
    final FloatSetting vSpeed = new FloatSetting("VerticalSpeed", this, moveVertical::isToggled, 0.1f, 15.0f, 2.5f, 0.1f);
    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 12.0f, 6.0f, 0.1f);
    final IntegerSetting fov = new IntegerSetting("Fov", this, 10, 180, 35);
    final BooleanSetting onlyMoveForward = new BooleanSetting("OnlyMoveForward", this, false);
    final BooleanSetting onlyWhenSprinting = new BooleanSetting("OnlyWhenSprinting", this, false);

    @Override
    public void onDisable() {
        super.onDisable();
        Client.INSTANCE.getCombatManager().setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MotionEvent) {
            Client.INSTANCE.getCombatManager().setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

            if (target == null || mc.currentScreen != null) {
                return;
            }

            if (RotationUtils.getFovToEntity(target) > fov.getValue()) {
                return;
            }

            if (onlyMoveForward.isToggled() && mc.thePlayer.moveForward <= 0) {
                return;
            }

            if (onlyWhenSprinting.isToggled() && !mc.thePlayer.isSprinting()) {
                return;
            }

            Vec3 targetPoint = target.getPositionEyes(1.0f);
            Rotation playerRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Delta delta = RotationUtils.getDeltaToPoint(playerRotation, targetPoint);

            delta.setYaw(MathHelper.clamp(delta.getYaw(), -hSpeed.getValue(), hSpeed.getValue()));
            delta.setPitch(MathHelper.clamp(delta.getPitch(), -vSpeed.getValue(), vSpeed.getValue()));

            delta = RotationUtils.fixDelta(delta);

            mc.thePlayer.rotationYaw += delta.getYaw();
            if (moveVertical.isToggled()) {
                mc.thePlayer.rotationPitch += delta.getPitch();
            }
        }
    }
}
