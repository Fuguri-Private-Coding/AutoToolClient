package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.IntegerSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Bloom", category = Category.VISUAL, key = Keyboard.KEY_B)
public class BloomModule extends Module {
    public IntegerSetting radius = new IntegerSetting("Radius", this, 6, 50, 15);
    public FloatSettings strength = new FloatSettings("Strength", this, 0.5f, 2.5f, 0.5f, 0.1f);
    public FloatSettings compression = new FloatSettings("Compression", this, 0.5f, 2.5f, 1f, 0.1f);
    public BooleanSetting arrayList = new BooleanSetting("ArrayList", this, true);
    public BooleanSetting clickGui = new BooleanSetting("ClickGui", this, true);
    public BooleanSetting keyBinds = new BooleanSetting("KeyBinds", this, false);
}