package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Friends;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "MidClick", category = Category.MISC, description = "Френд-Зона по колесику кнопки мыши.")
public class MidClick extends Module {

    private final FloatSetting range = new FloatSetting("Range", this, 3f, 1000f, 1000f, 1f) {};
    public final CheckBox reverseFriends = new CheckBox("Reverse", this, true);

    boolean pressed;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent && mc.currentScreen == null) {
            if (Mouse.isButtonDown(2) && !pressed) {
                EntityPlayer entity = (EntityPlayer) RayCastUtils.raycastEntity(range.getValue(), CameraRot.INST.getYaw(), CameraRot.INST.getPitch(), EntityPlayer.class::isInstance);
                if (entity != null) Friends.onClick(entity.getName());
            }
            pressed = Mouse.isButtonDown(2);
        }
    }

    public static boolean isFriend(EntityPlayer entity) {
        MidClick midClick = Modules.getModule(MidClick.class);

        if (!midClick.isToggled())
            return false;

        return Friends.isFriend(entity.getName(), midClick.reverseFriends.isToggled());
    }
}
