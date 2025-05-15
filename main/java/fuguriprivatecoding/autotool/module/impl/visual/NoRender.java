package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;

@ModuleInfo(name = "NoRender", category = Category.VISUAL)
public class NoRender extends Module {

    public CheckBox scoreBoard = new CheckBox("ScoreBoard", this, true);
    public CheckBox HurtCam = new CheckBox("HurtCam", this, true);
    public CheckBox Fire = new CheckBox("Fire", this, true);
}
