package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class Clicks implements Imports, EventListener {

    public Clicks() {
        Events.register(this);
    }

    @Getter @Setter boolean clicking;
    @Getter static int clicks;

    ClickSettings clickSettings;

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
    public void onEvent(Event event) {
        if (clickSettings == null) clickSettings = Modules.getModule(ClickSettings.class);

        if (event instanceof LegitClickTimingEvent) {
            if (clickSettings.simulateDoubleClick.isToggled() && clicks > 5) {
                clicks += Math.random() <= 0.5 ? 1 : 0;
            }

            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();
            clicking = needClick(target);

            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);

            if (rayCast != null && (rayCast.isFriend() || rayCast.isTeam()) || !clicking) {
                return;
            }

            for (int i = 0; i < iters; i++) {
                mc.clickMouse();
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        int startRandomizedHurtTime = clickSettings.startHurtTime.getRandomizedIntValue();
        int endRandomizeHurtTime = clickSettings.endHurtTime.getRandomizedIntValue();
        if (target == null) { return true; }
        if (target.hurtTime <= startRandomizedHurtTime) { return true; }
        if (!clickSettings.isToggled()) return true;
        return mc.thePlayer.hurtTime >= endRandomizeHurtTime;
    }

    public static void addClick() { clicks++; }
}
