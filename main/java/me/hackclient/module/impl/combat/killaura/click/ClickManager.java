package me.hackclient.module.impl.combat.killaura.click;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.impl.combat.ClickSettings;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class ClickManager implements InstanceAccess, ConditionCallableObject {

    { callables.add(this); }

    @Getter @Setter boolean clicking;

    @Getter int clicks;
    ClickSettings clickSettings;

    @Override
    public void onEvent(Event event) {
        if (clickSettings == null) clickSettings = Client.INSTANCE.getModuleManager().getModule(ClickSettings.class);

        if (event instanceof TickEvent) {
            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTargetOrSelectedEntity();
            clicking = needClick(target);
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (rayCast != null && rayCast.isFriend() || !clicking) { return; }

            for (int i = 0; i < iters; i++) {
                mc.clickMouse();
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        int startRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minStartHurtTime.getValue(), clickSettings.maxStartHurtTime.getValue());
        int endRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minEndHurtTime.getValue(), clickSettings.maxEndHurtTime.getValue());

        if (target == null) { return true; }

        if (target.hurtTime <= startRandomizedHurtTime) { return true; }

        if (target.hurtTime >= endRandomizedHurtTime) { return false; }

        return false;
    }

    @Override
    public boolean handleEvents() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
