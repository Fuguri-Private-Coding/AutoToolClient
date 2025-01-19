package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.combatmanager.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.rotation.Rotation;

@ModuleInfo(
        name = "KillAura",
        category = Category.COMBAT
)
public class KillAura extends Module {

    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 6.0f, 6.0f, 0.1f);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            Client.INSTANCE.getCombatManager().setTarget(TargetFinder.findTarget(distance.getValue(), true, false, false));
        }
        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
            motionEvent.setPitch(Rotation.getServerRotation().getPitch());
        }
    }
}
