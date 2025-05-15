package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.*;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotool.utils.client.ClientUtils;
import fuguriprivatecoding.autotool.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotool.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

@ModuleInfo(name = "AntiFireball", category = Category.PLAYER)
public class AntiFireball extends Module {

    final StopWatch stopWatch;

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 6f, 0.5f) {};
    final CheckBox debug = new CheckBox("Debug", this, false);

    public AntiFireball() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistance(entity) > distance.getValue() || !stopWatch.reachedMS(delay.getValue())) continue;
                mc.playerController.attackEntity(mc.thePlayer, entity);
                mc.thePlayer.swingItem();
                stopWatch.reset();
                if (debug.isToggled()) ClientUtils.chatLog("Fireball detected.");
            }
        }
    }
}
