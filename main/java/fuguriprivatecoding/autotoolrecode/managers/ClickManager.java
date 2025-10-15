package fuguriprivatecoding.autotoolrecode.managers;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ClickManager implements Imports {

    public ClickManager() {
        Client.INST.getEventManager().register(this);
    }

    @Getter @Setter boolean clicking;

    @Getter int clicks;
    ClickSettings clickSettings;

    @EventTarget
    public void onEvent(Event event) {
        if (clickSettings == null) clickSettings = Client.INST.getModuleManager().getModule(ClickSettings.class);

        if (event instanceof TickEvent tickEvent && !tickEvent.isCanceled()) {
            if (clickSettings.simulateDoubleClick.isToggled() && clicks > 5) {
                clicks += Math.random() <= 0.5 ? 1 : 0;
            }

            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = Client.INST.getCombatManager().getTargetOrSelectedEntity();
            clicking = needClick(target);

            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);

            if (rayCast != null && rayCast.isFriend() || !clicking) {
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

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
