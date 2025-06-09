package fuguriprivatecoding.autotoolrecode.managers;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

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
            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = Client.INST.getCombatManager().getTargetOrSelectedEntity();
            clicking = needClick(target);
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (rayCast != null && rayCast.isFriend() || !clicking) { return; }

            for (int i = 0; i < iters; i++) {
                MovingObjectPosition mouse = RayCastUtils.rayCast(Client.INST.getCombatManager().getEntityReach(), Client.INST.getCombatManager().getBlockReach(), Rot.getServerRotation());
                mc.clickMouse();
                //mc.clickMouseCustom(mouse, false);
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        int startRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minStartHurtTime.getValue(), clickSettings.maxStartHurtTime.getValue());
        int endRandomizeHurtTime = RandomUtils.nextInt(clickSettings.minEndHurtTime.getValue(), clickSettings.maxEndHurtTime.getValue());
        if (target == null) { return true; }
        if (target.hurtTime <= startRandomizedHurtTime) { return true; }
        if (!clickSettings.isToggled()) return true;
        return mc.thePlayer.hurtTime >= endRandomizeHurtTime;
    }

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
