package me.hackclient.module.impl.misc;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.utils.rotation.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "MidClick", category = Category.MISC, toggled = true)
public class MidClick extends Module {
    FloatSettings range = new FloatSettings("Range", this, 3f, 1000f, 1000f, 1f);
    public BooleanSetting reverseFriends = new BooleanSetting("ReverseFriend", this, true);
    public BooleanSetting showInName = new BooleanSetting("ShowFriendPrefixInName", this, true);

    boolean down;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            if (Mouse.isButtonDown(2)) {
                if (!down) {
                    down = true;
                    Entity entity = RayCastUtils.raycastEntity(range.getValue(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, entity1 -> entity1 instanceof EntityPlayer);
                    if (entity instanceof EntityPlayer entity2) {
                        Client.INSTANCE.getFriendManager().onClick(entity2.getName());
                    }
                }
            } else {
                down = false;
            }
        }
    }
}
