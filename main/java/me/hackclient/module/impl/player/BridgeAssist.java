package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.util.MathHelper;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "BridgeAssist", category = Category.PLAYER)
public class BridgeAssist extends Module {

    final List<Rotation> rotations;
    final FloatSetting minDelta = new FloatSetting("MinRotationDiff", this, 0.1f, 10.0f, 5.0f, 0.1f) {};

    public BridgeAssist() {
        rotations = new CopyOnWriteArrayList<>();
        rotations.add(new Rotation(-45.0f, 77.0f));
        rotations.add(new Rotation(135.0f, 77.0f));
        rotations.add(new Rotation(-135.0f, 77.0f));
        rotations.add(new Rotation(45.0f, 77.0f));
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof MotionEvent) {
            Rotation playerRot = new Rotation(
                    MathHelper.wrapDegree(mc.thePlayer.rotationYaw),
                    mc.thePlayer.rotationPitch
            );

            Rotation nearestPerfectAngle = rotations.stream().min(Comparator.comparing(rotation -> RotationUtils.getDelta(playerRot, rotation).hypot())).orElse(null);
            if (nearestPerfectAngle == null) {
                return;
            }

            Delta delta = RotationUtils.getDelta(playerRot, nearestPerfectAngle);
            delta = RotationUtils.fixDelta(delta);

            if (delta.hypot() < minDelta.getValue()) {
                mc.thePlayer.rotationYaw += delta.getYaw();
                mc.thePlayer.rotationPitch += delta.getPitch();
            }
        }
    }
}
