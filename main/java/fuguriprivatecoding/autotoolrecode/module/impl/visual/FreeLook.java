package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "FreeLook", category = Category.VISUAL, description = "Позволяет осматриватся во круг.")
public class FreeLook extends Module {

    CheckBox changePerspective = new CheckBox("ChangePerspective", this, true);

    private int previousPerspective;

    @Override
    public void onEnable() {
        if (changePerspective.isToggled()) {
            previousPerspective = mc.gameSettings.thirdPersonView;
        }
        CameraRot.INST.setUnlocked(true);
    }

    @Override
    public void onDisable() {
        if (changePerspective.isToggled()) {
            mc.gameSettings.thirdPersonView = previousPerspective;
            previousPerspective = 0;
        }
        CameraRot.INST.setUnlocked(false);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            if (getKey() == Keyboard.KEY_NONE || !Keyboard.isKeyDown(getKey())) {
                setToggled(false);
                return;
            }

            if (changePerspective.isToggled()) mc.gameSettings.thirdPersonView = 1;
        }
    }
}