package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.TimerRange;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

        if (event instanceof BestClickTimingEvent) {
            boolean clicking = needClick(target);

            int iters = clicks;
            clicks = 0;

            if (clicking) {
                for (int i = 0; i < iters; i++) {
                    mc.clickMouse();
                }
            }
        }

        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (target instanceof EntityPlayer player && isFriend(player)) {
                e.cancel();
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        if (mc.rayTrace.typeOfHit == RayTrace.RayType.BLOCK) return false;
        if (target != null && BackTrack.needCancel(target)) return false;

        if (TimerRange.isWorking())
            return false;

        if (target == null || !clickSettings.isToggled()) {
            return true;
        }

        if (target instanceof EntityPlayer player && isFriend(player)) {
            return false;
        }

        int startHurtTime = clickSettings.startHurtTime.getRandomizedIntValue();
        int endHurtTime = clickSettings.endHurtTime.getRandomizedIntValue();

        return target.hurtTime <= startHurtTime || mc.thePlayer.hurtTime >= endHurtTime;
    }

    public static void addClick() {
        clicks++;
    }

    private boolean isFriend(EntityPlayer player) {
        return !player.isValid() && clickSettings.noFriendDamage.isToggled();
    }
}
