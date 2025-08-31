package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;

@ModuleInfo(name = "CustomCamera", category = Category.VISUAL, description = "Позволяет отдалить или приблизить камеру.")
public class CustomCamera extends Module {
    public FloatSetting cameraDistance = new FloatSetting("CameraDistance",this, 1f, 5f, 1f, 0.1f) {};
    public CheckBox cameraClip = new CheckBox("CameraClip", this, true);
    public final CheckBox gta5 = new CheckBox("GTA5", this, false);
    public FloatSetting translate = new FloatSetting("Translate", this, gta5::isToggled, 0, 2, 0.75f, 0.01f);
}