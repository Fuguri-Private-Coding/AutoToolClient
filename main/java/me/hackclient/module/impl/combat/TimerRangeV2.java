package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.connection.Ping;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0APacketAnimation;

@ModuleInfo(name = "TimerRangeV2", category = Category.COMBAT)
public class TimerRangeV2 extends Module {

    FloatSetting startDistance = new FloatSetting("StartDistance", this, 3.1f, 10.0f, 3.6f, 0.1f);
    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1, 10, 2);
    IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 1, 10, 2);
    BooleanSetting LegitClick = new BooleanSetting("LegitClick", this, false);
    BooleanSetting debug = new BooleanSetting("Debug", this, true);
    BooleanSetting onlyPing = new BooleanSetting("OnlyPing", this, true);

    boolean teleporting;
    double balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (teleporting) { return; }

        if (balance > 0) {
            if (event instanceof TickEvent tickEvent) {
                balance--;
                tickEvent.setCanceled(true);
            }
            if (event instanceof RunGameLoopEvent) {
                mc.timer.renderPartialTicks = 1;
            }
            return;
        }

        if (event instanceof TickEvent) {
            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

            if (target == null) { return; }
            if (RayCastUtils.raycastEntity(3, entity -> true) == target) { return; }
            if (RayCastUtils.raycastEntity(startDistance.getValue(), entity -> true) != target) { return; }
            //if (onlyPing.isToggled() && mm.getModule(Ping.class).packetBuffer.isEmpty()) { return; }
            if (target.hurtTime > maxTargetHurtTime.getValue()) { return; }
            if (mc.thePlayer.moveForward <= 0) return;

            teleporting = true; // Нужно для того чтобы в TimerRangeV2 не использовались никакие евенты в время телепорта, а в других модулях все евенты будут работать

            while (balance < limitTicks.getValue()) {
                mc.runTickSave();
                ++balance;

                if (debug.isToggled() && RayCastUtils.raycastEntity(startDistance.getValue(), entity -> true) != target
                && RayCastUtils.raycastEntity(6, entity -> true) == target) {
                    double distance = mc.thePlayer.getPositionEyes(1.0f).distanceTo(mc.objectMouseOver.hitVec);
                    ClientUtils.chatLog("Distance = " + String.format("%.3f", distance) + " at tick " + balance);
                }

                if (RayCastUtils.raycastEntity(3, entity -> true) == target) {
                    if (LegitClick.isToggled()) {
                        Client.INSTANCE.getClickManager().addClick();
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                        mc.playerController.attackEntity(mc.thePlayer, target);
                    }
                    if (debug.isToggled()) {
                        double distance = mc.thePlayer.getPositionEyes(1.0f).distanceTo(mc.objectMouseOver.hitVec);
                        ClientUtils.chatLog("Ended teleport at " + String.format("%.3f", distance));
                    }
                    break;
                }
            }
            teleporting = false;
        }
    }
}