package me.hackclient.managers;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.impl.combat.ClickSettings;
import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class ClickManager implements Imports{

    public ClickManager() {
        Client.INSTANCE.getEventManager().register(this);
    }

    @Getter @Setter boolean clicking;

    @Getter int clicks;
    ClickSettings clickSettings;

    @EventTarget
    public void onEvent(Event event) {
        if (clickSettings == null) clickSettings = Client.INSTANCE.getModuleManager().getModule(ClickSettings.class);

        if (event instanceof TickEvent tickEvent && !tickEvent.isCanceled()) {
            int iters = clicks;
            clicks = 0;

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTargetOrSelectedEntity();
            clicking = needClick(target);
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (rayCast != null && rayCast.isFriend() || !clicking) { return; }

            for (int i = 0; i < iters; i++) {
                MovingObjectPosition mouse = RayCastUtils.rayCast(Client.INSTANCE.getCombatManager().getEntityReach(), Client.INSTANCE.getCombatManager().getBlockReach(), Rotation.getServerRotation());
                mc.clickMouseCustom(mouse, false);
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        int startRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minStartHurtTime.getValue(), clickSettings.maxStartHurtTime.getValue());
        int endRandomizedHurtTime = RandomUtils.nextInt(clickSettings.minEndHurtTime.getValue(), clickSettings.maxEndHurtTime.getValue());

        if (target == null) { return true; }

        if (target.hurtTime <= startRandomizedHurtTime) { return true; }
        if (target.hurtTime <= endRandomizedHurtTime) { return false; }

        if (mc.thePlayer.hurtTime > 0) return true;

        return clicking;
    }

    public void addClick() { clicks++; }

    public void addClick(int clicks) { this.clicks += clicks; }
}
