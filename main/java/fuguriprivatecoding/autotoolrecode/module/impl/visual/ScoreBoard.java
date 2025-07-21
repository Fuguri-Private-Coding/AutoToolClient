package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;

@ModuleInfo(name = "ScoreBoard", category = Category.VISUAL)
public class ScoreBoard extends Module {

    public CheckBox remove = new CheckBox("Remove", this, true);
    public IntegerSetting posX = new IntegerSetting("Pos-X", this, 0,100,0);
    public IntegerSetting posY = new IntegerSetting("Pos-X", this, 0,100,0);

    public CheckBox fadeBoxColor = new CheckBox("FadeColor", this);
    public ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    public ColorSetting color2 = new ColorSetting("Color2", this, fadeBoxColor::isToggled, 1f,1f,1f,1f);
    public FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeBoxColor::isToggled,0.1f, 20, 1, 0.1f);
}
