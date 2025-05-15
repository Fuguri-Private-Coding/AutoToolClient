package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;

@ModuleInfo(name = "NoRender", category = Category.VISUAL)
public class NoRender extends Module {

    public CheckBox scoreBoard = new CheckBox("ScoreBoard", this, true);
    public CheckBox HurtCam = new CheckBox("HurtCam", this, true);
    public CheckBox Fire = new CheckBox("Fire", this, true);
}
