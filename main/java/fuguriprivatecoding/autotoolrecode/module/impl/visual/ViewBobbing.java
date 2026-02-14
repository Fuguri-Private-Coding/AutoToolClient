package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "ViewBobbing", category = Category.VISUAL, description = "Изменяет покачивание камеры.")
public class ViewBobbing extends Module {
    public FloatSetting strength = new FloatSetting("Strength", this, 0, 2, 1, 0.01f);
    public CheckBox removeScreenBobbing = new CheckBox("RemoveScreenBobbing", this, true);
}
