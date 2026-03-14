package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT, description = "ХАЛЯЛЬ #НАСТРОЙКА КЛИК ГУИ ЙОУ.")
public class ClickGui extends Module {

    public Mode fonts = new Mode("Fonts", this);

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 255, 100);

	public final CheckBox glow = new CheckBox("Glow", this);
	public final CheckBox blur = new CheckBox("Blur", this);

	public final ColorSetting color = new ColorSetting("Color", this, 40);
	public final ColorSetting colorShadow = new ColorSetting("Color Shadow", this);

    public ClickGui() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

	@Override
	public void onEnable() {
		mc.displayGuiScreen(ClickScreen.INST);
		toggle();
	}
}
