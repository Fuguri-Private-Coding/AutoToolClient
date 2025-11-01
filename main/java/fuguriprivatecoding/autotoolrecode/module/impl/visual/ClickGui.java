package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGui", category = Category.VISUAL, key = Keyboard.KEY_RSHIFT, description = "ХАЛЯЛЬ #НАСТРОЙКА КЛИК ГУИ ЙОУ.")
public class ClickGui extends Module {

    public Mode fonts = new Mode("Fonts", this);

	Mode guiMode = new Mode("GuiMode", this).addModes("ImGui", "JavaGui").setMode("ImGui");

	public FloatSetting animationSpeed = new FloatSetting("AnimationSpeed", this, 1, 20, 10, 0.5f) {};
	public IntegerSetting backgroundAlpha = new IntegerSetting("BackgroundAlpha", this, 0, 255, 100);

	public final CheckBox glow = new CheckBox("Glow", this);
	public final CheckBox blur = new CheckBox("Blur", this);

	public final ColorSetting color = new ColorSetting("Color", this);
	public final ColorSetting colorShadow = new ColorSetting("Color Shadow", this);

//	ImGuiScreen imGuiScreen = new ImGuiScreen();

    public ClickGui() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

	@Override
	public void onEnable() {
		switch (guiMode.getMode()) {
			case "ImGui" -> {
				mc.displayGuiScreen(Client.INST.getClickScreen());
//				mc.displayGuiScreen(imGuiScreen);
//				ImGuiManager.addWindow(ImGuiScreen.clickGuiWindow);
			}
			case "JavaGui" -> mc.displayGuiScreen(Client.INST.getClickScreen());
		}
		toggle();
	}
}
