package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "MidClick", category = Category.MISC, description = "Добавление друзей по колесику кнопки мыши.")
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
                        Client.INST.getFriends().onClick(entity2.getName());
                    }
                }
            } else {
                down = false;
            }
        }
    }
}
