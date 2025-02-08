package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;

@ModuleInfo(
        name = "TPAura",
        category = Category.COMBAT
)
public class TPAura extends Module {

    final StopWatch stopWatch;

    final IntegerSetting packets = new IntegerSetting("Packets", this, 1, 10, 1);

    public TPAura() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent && RayCastUtils.raycastEntity(12, entity -> true) != null) {
            Entity ent = RayCastUtils.raycastEntity(12, entity -> true);
            if (stopWatch.reachedMS(1000)) {
                stopWatch.reset();
                for (int i = 0; i < packets.getValue(); i++) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(ent.posX, ent.posY, ent.posZ, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), ent.onGround));
                }
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(ent, C02PacketUseEntity.Action.ATTACK));
            }
        }
    }
}
