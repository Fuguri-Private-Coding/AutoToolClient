package me.hackclient.module.impl.legit;

import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;

@ModuleInfo(
    name = "AimAssist",
    category = Category.LEGIT
)
public class AimAssist extends Module {

    final BooleanSetting moveVertical = new BooleanSetting("MoveVertical", this, false);
    final FloatSetting hSpeed = new FloatSetting("HorizontalSpeed", this, 0.1f, 30.0f, 5.0f, 0.1f);
    final FloatSetting vSpeed = new FloatSetting("VerticalSpeed", this, moveVertical::isToggled, 0.1f, 15.0f, 2.5f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MotionEvent) {
            EntityLivingBase target = (EntityLivingBase) mc.theWorld.loadedEntityList.parallelStream()
                    .filter(entity -> entity instanceof EntityPlayer)
                    .filter(entity -> !(entity instanceof EntityPlayerSP))
                    //.filter(entity -> )
                    .filter(entity -> DistanceUtils.getDistanceToEntity(entity) < 6)
                    .min(Comparator.comparing(RotationUtils::getFovToEntity))
                    .orElse(null);

            if (target == null) {
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
