package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.combatmanager.CombatManager;
import me.hackclient.combatmanager.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.rotation.RayCastUtils;


@ModuleInfo(
        name = "LagRange",
        category = Category.COMBAT
)
public class LagRange extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 20, 1000, 150);
    final BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", this, false);

    @Override
    public void onDisable() {
        super.onDisable();
        Client.INSTANCE.getCombatManager().setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MotionEvent) {
            if (!needTeleport()) return;

            try {
                Thread.sleep(delay.getValue());
            } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    boolean needTeleport() {
        CombatManager combatManager = Client.INSTANCE.getCombatManager();
        if (combatManager.getTarget() == null) { return false; }
        if (RayCastUtils.raycastEntity(3, entity -> true) != null) { return false; }
        if (RayCastUtils.raycastEntity(6, entity -> true) == null) { return false; }
        if (!mc.thePlayer.onGround && onlyOnGround.isToggled()) { return false; }

        double distance = DistanceUtils.getDistanceToEntity(combatManager.getTarget());
        double teleportDistance = mc.thePlayer.getBps(false) * (delay.getValue() / 1000f);

        if (distance - teleportDistance > 3) { return false; }

        return true;
    }

}
