package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.settings.impl.FloatSetting;

@ModuleInfo(name = "CustomCamera", category = Category.VISUAL)
public class CustomCamera extends Module {
    public FloatSetting cameraDistance = new FloatSetting("CameraDistance",this, 1f, 5f, 1f, 0.1f) {};
    public CheckBox cameraClip = new CheckBox("CameraClip", this, true);
}