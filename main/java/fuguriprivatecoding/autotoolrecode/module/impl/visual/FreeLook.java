package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "FreeLook", category = Category.VISUAL, description = "Позволяет осматриватся во круг.")
public class FreeLook extends Module {

    CheckBox changePerspective = new CheckBox("Change Perspective", this, true);

    public float rotYaw = 0;
    public float rotPitch = 0;

    private int previousPerspective;

    @Override
    public void onEnable() {
        if (changePerspective.isToggled()) {
            previousPerspective = mc.gameSettings.thirdPersonView;
        }
    }

    @Override
    public void onDisable() {
        if (changePerspective.isToggled()) {
            mc.gameSettings.thirdPersonView = previousPerspective;
            previousPerspective = 0;
        }
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