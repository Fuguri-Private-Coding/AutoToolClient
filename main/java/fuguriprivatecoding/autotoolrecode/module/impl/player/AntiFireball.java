package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;

@ModuleInfo(name = "AntiFireball", category = Category.PLAYER, description = "Автоматически отбивает фаербол противника.")
public class AntiFireball extends Module {

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 4.5f, 0.1f);
    final CheckBox debug = new CheckBox("Debug", this, false);

    final StopWatch stopWatch = new StopWatch();

    public EntityFireball target;

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer.ticksExisted < 40)
            return;

        if (event instanceof TickEvent) {
            if (target != null && (target.hurtResistantTime > 0 || target.isDead || !mc.theWorld.getLoadedEntityList().contains(target) || DistanceUtils.getDistance(target) > distance.getValue() + 3.5f)) target = null;

            for (Entity target : mc.theWorld.loadedEntityList) {
                if (!(target instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistance(target) > distance.getValue() + 3.5f) {
                    continue;
                }

                this.target = entityFireball;
                if (debug.isToggled()) ClientUtils.chatLog("Fireball detected.");
            }
        }

        if (event instanceof LegitClickTimingEvent && target != null && stopWatch.reachedMS(delay.getValue())) {
            mc.playerController.attackEntity(mc.thePlayer, target);
            stopWatch.reset();
        }
    }
}
