package fuguriprivatecoding.autotoolrecode.guis.clickgui.panel;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;

public class Panel {

    Module module;
    EasingAnimation openAnimation = new EasingAnimation();

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

    float backgroundX;
    float backgroundY;
    float backgroundWidth;
    float backgroundHeight;

    public Panel(Module module) {
        this.module = module;
    }

    public void render(int x, int y, int deltaScroll) {

    }

    public boolean clickMouse(int x, int y, int button) {
        return false;
    }

    public boolean mouseReleased(int x, int y, int button) {

        return false;
    }
}
