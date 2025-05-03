package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Freeze", category = Category.MOVE)
public class Freeze extends Module {

    Vec3 motion, pos;

    @Override
    public void onEnable() {
        super.onEnable();
        motion = mc.thePlayer.getMotionVector();
        pos = mc.thePlayer.getPositionVector();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.motionX = motion.xCoord;
        mc.thePlayer.motionY = motion.yCoord;
        mc.thePlayer.motionZ = motion.zCoord;
        mc.thePlayer.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent) {
            packetEvent.setCanceled(true);
        }
        if (event instanceof UpdateEvent) {
            mc.thePlayer.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }
    }
}
