package fuguriprivatecoding.autotoolrecode.handle;

import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import lombok.Getter;

public class Clicks implements Imports, EventListener {

    public Clicks() {
        Events.register(this);
    }

    @Getter static int clicks;

    private static final ClickSettings clickSettings = Modules.getModule(ClickSettings.class);

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof LegitClickTimingEvent) {
            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();
            boolean clicking = needClick(target);

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
        if (target == null || !clickSettings.isToggled()) {
            return true;
        }

        int startHurtTime = clickSettings.startHurtTime.getRandomizedIntValue();
        int endHurtTime = clickSettings.endHurtTime.getRandomizedIntValue();

        return target.hurtTime <= startHurtTime || mc.thePlayer.hurtTime >= endHurtTime;
    }

    public static void addClick() {
        if (clickSettings.simulateDoubleClick.isToggled() && clicks > 0) {
            float chance = clickSettings.chanceDoubleClick.getValue() / 100f;

            if (Math.random() <= chance) {
                clicks++;
            }
        }

        clicks++;
    }
}
