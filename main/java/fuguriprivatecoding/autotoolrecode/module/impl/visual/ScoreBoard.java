package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Scoreboard", category = Category.VISUAL, description = "Позволяет изменять Scoreboard.")
public class ScoreBoard extends Module {

    public CheckBox remove = new CheckBox("Remove", this, true);

    BooleanSupplier visible = () -> !remove.isToggled();

    public IntegerSetting posX = new IntegerSetting("Pos-X", this, visible, 0,100,0);
    public IntegerSetting posY = new IntegerSetting("Pos-Y", this, visible, 0,100,0);

    public CheckBox fadeBoxColor = new CheckBox("FadeColor", this, visible);
    public ColorSetting color1 = new ColorSetting("Color1", this,visible, 1f,1f,1f,1f);
    public ColorSetting color2 = new ColorSetting("Color2", this, () -> visible.getAsBoolean() && fadeBoxColor.isToggled(),1f,1f,1f,1f);
    public FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, () -> visible.getAsBoolean() && fadeBoxColor.isToggled(),0.1f, 20, 1, 0.1f);
}
