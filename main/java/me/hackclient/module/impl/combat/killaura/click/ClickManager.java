package me.hackclient.module.impl.combat.killaura.click;

import lombok.Getter;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ClickManager implements InstanceAccess, ConditionCallableObject {

    { callables.add(this); }

    @Getter int clicks;
    int delay;

    @Override
    public void onEvent(Event event) {
        if (event instanceof LegitClickTimingEvent) {
            EntityLivingBase rayCast = (EntityLivingBase) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityLivingBase);
            for (int i = 0; i < clicks; i++) {
                if (rayCast instanceof EntityPlayer entityPlayer && entityPlayer.isFriend()) { break; }
                if (delay == 0 || mc.thePlayer.hurtTime > 6) {
                    mc.clickMouse();
                    if (delay == 0) {
                        if (rayCast != null) {
                            delay = 10;
                        }
                    }
                }
            }
            clicks = 0;
        }
        if (event instanceof TickEvent) {
            if (delay > 0) {
                delay--;
            }
        }
    }

    @Override
    public boolean handleEvents() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
