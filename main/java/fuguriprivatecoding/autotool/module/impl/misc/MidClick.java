package fuguriprivatecoding.autotool.module.impl.misc;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.utils.raytrace.RayCastUtils;
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
