package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.TimerRange;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import lombok.Getter;
import net.minecraft.util.RayTrace;

public class Clicks implements Imports, EventListener {

    public Clicks() {
        Events.register(this);
    }

    @Getter static int clicks;

    private static final ClickSettings clickSettings = Modules.getModule(ClickSettings.class);

    @Override public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();
        boolean clicking = needClick(target);

        if (event instanceof BestClickTimingEvent) {
            int iters = clicks;
            clicks = 0;

            if (clicking) {
                for (int i = 0; i < iters; i++) {
                    click(target);
                }
            }
        }

        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (target instanceof EntityPlayer player && !player.isValid() && clickSettings.noFriendDamage.isToggled()) {
                e.cancel();
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        if (mc.rayTrace.typeOfHit == RayTrace.RayType.BLOCK) return false;

        if (TimerRange.click || TimerRange.balance > 0 || TimerRange.teleporting) return false;

        if (target != null && BackTrack.needCancel(target)) {
            return false;
        }

        if (target == null || !clickSettings.isToggled()) {
            return true;
        }

        if (target instanceof EntityPlayer player && !player.isValid() && clickSettings.noFriendDamage.isToggled()) {
            return false;
        }

        int startHurtTime = clickSettings.startHurtTime.getRandomizedIntValue();
        int endHurtTime = clickSettings.endHurtTime.getRandomizedIntValue();

        return target.hurtTime <= startHurtTime || mc.thePlayer.hurtTime >= endHurtTime;
    }

    public static void addClick() {
        clicks++;
    }

    public static void click(EntityLivingBase target) {
        mc.clickMouse();
    }

    public static boolean shouldWallAttack(EntityLivingBase target, RayTrace hit, RayTrace hits) {
        double distance = DistanceUtils.getDistance(target);

        return distance <= 3 && hit.typeOfHit == RayTrace.RayType.BLOCK && hits != null && hits.typeOfHit == RayTrace.RayType.ENTITY;
    }
}
