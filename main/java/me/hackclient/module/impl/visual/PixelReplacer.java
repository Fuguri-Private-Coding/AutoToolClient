package me.hackclient.module.impl.visual;

import lombok.Getter;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;

@ModuleInfo(
        name = "PixelReplacer",
        category = Category.VISUAL
)
@Getter
public class PixelReplacer extends Module {

    final IntegerSetting
    rOffset = new IntegerSetting("RedOffset", this, 0, 255, 0),
    gOffset = new IntegerSetting("GreenOffset", this, 0, 255, 0),
    bOffset = new IntegerSetting("BlueOffset", this, 0, 255, 0);
}
