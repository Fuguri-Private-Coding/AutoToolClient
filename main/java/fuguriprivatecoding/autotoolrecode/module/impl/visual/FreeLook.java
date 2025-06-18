package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "FreeLook", category = Category.VISUAL)
public class FreeLook extends Module {

    private int previousPerspective;
    public float originalYaw, originalPitch, lastYaw, lastPitch;

    @Override
    public void onDisable() {
        mc.thePlayer.rotationYaw = originalYaw;
        mc.thePlayer.rotationPitch = originalPitch;
        mc.gameSettings.thirdPersonView = previousPerspective;
    }

    @Override
    public void onEnable() {
        previousPerspective = mc.gameSettings.thirdPersonView;
        originalYaw = lastYaw = Rot.getServerRotation().getYaw();
        originalPitch = lastPitch = Rot.getServerRotation().getPitch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            if (getKey() == Keyboard.KEY_NONE || !Keyboard.isKeyDown(getKey())) {
                setToggled(false);
                return;
            }

            mc.mouseHelper.mouseXYChange();
            final float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            final float f1 = (float) (f * f * f * 1.5);
            lastYaw += mc.mouseHelper.deltaX * f1;
            lastPitch -= mc.mouseHelper.deltaY * f1;

            lastPitch = MathHelper.clamp(lastPitch, -90, 90);
            mc.gameSettings.thirdPersonView = 1;
        }
    }
}