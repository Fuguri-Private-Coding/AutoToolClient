package me.hackclient.module.impl.combat.killaura.click;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.Client;
import me.hackclient.combatmanager.CombatManager;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.impl.combat.ClickSettings;
import me.hackclient.module.impl.combat.KillAura;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ClickManager implements InstanceAccess, ConditionCallableObject {

    { callables.add(this); }

    @Getter int clicks;
    ClickSettings clickSettings;

    @Override
    public void onEvent(Event event) {
        if (clickSettings == null) clickSettings = Client.INSTANCE.getModuleManager().getModule(ClickSettings.class);
        if (event instanceof TickEvent) {
            if (clicks < 1) {
                clicks = 0;
            }
        }
        if (event instanceof LegitClickTimingEvent) {
            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();
            if (target == null) return;
            EntityLivingBase rayCast = (EntityLivingBase) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityLivingBase);
            int startRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minStartHurtTime.getValue(), clickSettings.maxStartHurtTime.getValue());
            int endRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minEndHurtTime.getValue(), clickSettings.maxEndHurtTime.getValue());
            for (int i = 0; i < clicks; i++) {
                if (rayCast instanceof EntityPlayer entityPlayer && entityPlayer.isFriend()) { break; }
                if (target.hurtTime <= startRandomizedHurtTime || mc.thePlayer.hurtTime > endRandomizedHurtTime) {
                    mc.clickMouse();
                }
            }
            clicks = 0;
        }
    }

    @Override
    public boolean handleEvents() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
