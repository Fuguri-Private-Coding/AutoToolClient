package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;

@ModuleInfo(name = "NoRender", category = Category.VISUAL)
public class NoRender extends Module {

    public BooleanSetting scoreBoard = new BooleanSetting("ScoreBoard", this, true);
    public BooleanSetting HurtCam = new BooleanSetting("HurtCam", this, true);
    public BooleanSetting Fire = new BooleanSetting("Fire", this, true);
}
