package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

@ModuleInfo(
        name = "AntiFireball",
        category = Category.PLAYER
)
public class AntiFireball extends Module {

    final StopWatch stopWatch;

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 6f, 0.5f) {};
    final BooleanSetting debug = new BooleanSetting("Debug", this, false);

    public AntiFireball() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistanceToEntity(entity) > distance.getValue() || !stopWatch.reachedMS(delay.getValue())) continue;

                mc.thePlayer.swingItem();
                mc.playerController.attackEntity(mc.thePlayer, entity);
                stopWatch.reset();
                if (debug.isToggled()) ClientUtils.chatLog("Fireball detected.");
            }
        }
    }
}
