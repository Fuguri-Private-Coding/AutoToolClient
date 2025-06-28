package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

@ModuleInfo(name = "AntiFireball", category = Category.PLAYER)
public class AntiFireball extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 6f, 0.5f) {};
    final CheckBox debug = new CheckBox("Debug", this, false);

    final StopWatch stopWatch = new StopWatch();

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
