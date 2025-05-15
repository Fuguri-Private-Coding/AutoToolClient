package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;

@ModuleInfo(name = "CustomCamera", category = Category.VISUAL)
public class CustomCamera extends Module {
    public FloatSetting cameraDistance = new FloatSetting("CameraDistance",this, 1f, 5f, 1f, 0.1f) {};
    public CheckBox cameraClip = new CheckBox("CameraClip", this, true);
}