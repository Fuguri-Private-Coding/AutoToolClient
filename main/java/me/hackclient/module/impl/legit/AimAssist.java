package me.hackclient.module.impl.legit;

import me.hackclient.Client;
import me.hackclient.event.EventTarget;
import me.hackclient.utils.target.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rot;
import me.hackclient.utils.rotation.RotUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "AimAssist", category = Category.LEGIT)
public class AimAssist extends Module {

    final CheckBox moveVertical = new CheckBox("MoveVertical", this, false);
    final FloatSetting hSpeed = new FloatSetting("HorizontalSpeed", this, 0.1f, 30.0f, 5.0f, 0.1f) {};
    final FloatSetting vSpeed = new FloatSetting("VerticalSpeed", this, moveVertical::isToggled, 0.1f, 15.0f, 2.5f, 0.1f) {};
    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 12.0f, 6.0f, 0.1f) {};
    final IntegerSetting fov = new IntegerSetting("Fov", this, 10, 180, 35);
    final CheckBox onlyMoveForward = new CheckBox("OnlyMoveForward", this, false);
    final CheckBox onlyWhenSprinting = new CheckBox("OnlyWhenSprinting", this, false);

    @Override
    public void onDisable() {
        super.onDisable();
        Client.INST.getCombatManager().setTarget(null);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MotionEvent) {
            Client.INST.getCombatManager().setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));

            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (target == null || mc.currentScreen != null) return;
            if (RotUtils.getFovToEntity(target) > fov.getValue()) return;
            if (onlyMoveForward.isToggled() && mc.thePlayer.moveForward <= 0) return;
            if (onlyWhenSprinting.isToggled() && !mc.thePlayer.isSprinting()) return;

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
