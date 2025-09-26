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
    public CheckBox smoothCamera = new CheckBox("SmoothCamera", this, true);
    public FloatSetting yAnimationSmooth = new FloatSetting("yAnimationSmoothSpeed", this, smoothCamera::isToggled, 0, 50, 50, 0.1f);
    public FloatSetting xAnimationSmooth = new FloatSetting("xAnimationSmoothSpeed", this, smoothCamera::isToggled, 0, 50, 50, 0.1f);
    public FloatSetting zAnimationSmooth = new FloatSetting("zAnimationSmoothSpeed", this, smoothCamera::isToggled, 0, 50, 50, 0.1f);
}