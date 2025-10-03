package fuguriprivatecoding.autotoolrecode.guis.clickgui.panel;

import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;

public class Panel {

    Module module;
    EasingAnimation openAnimation = new EasingAnimation();

    float 

    public Panel(Module module) {
        this.module = module;
    }

    public void render(int x, int y) {

    }


    public boolean clickMouse(int x, int y, int button) {
        return false;
    }

    public boolean mouseReleased(int x, int y, int button) {

        return false;
    }






}
