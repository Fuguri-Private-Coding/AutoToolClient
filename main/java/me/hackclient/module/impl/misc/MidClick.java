package me.hackclient.module.impl.misc;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "MidClick", category = Category.MISC)
public class MidClick extends Module {
    FloatSetting range = new FloatSetting("Range", this, 3f, 1000f, 1000f, 1f) {};
    public CheckBox reverseFriends = new CheckBox("Reverse", this, true);
    public CheckBox showInName = new CheckBox("ShowFriendPrefixInName", this, true);

    boolean down;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (Mouse.isButtonDown(2)) {
                if (!down) {
                    down = true;
                    Entity entity = RayCastUtils.raycastEntity(range.getValue(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, entity1 -> entity1 instanceof EntityPlayer);
                    if (entity instanceof EntityPlayer entity2) {
                        Client.INST.getFriendManager().onClick(entity2.getName());
                    }
                }
            } else {
                down = false;
            }
        }
    }
}
