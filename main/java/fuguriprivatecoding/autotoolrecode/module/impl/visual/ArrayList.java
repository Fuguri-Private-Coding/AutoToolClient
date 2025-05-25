package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL)
public class ArrayList extends Module {

	final CheckBox showRenderModules = new CheckBox("ShowRenderModules", this, false);

	final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);
	final CheckBox textShadow = new CheckBox("TextShadow", this, false);

	final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, 0,0,0,0.5f);

	Shadows shadows;

	@EventTarget
	public void onEvent(Event event) {
		if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
		if (event instanceof Render2DEvent) {
			final FontRenderer font = mc.fontRendererObj;
			List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INST.getModuleManager().getEnabledModules());

			sort(moduleList, font);

			double offset = 0;
			for (Module module : moduleList) {
				if (module.isHide() || !showRenderModules.isToggled() && module.getCategory() == Category.VISUAL) continue;

				if (shadows.isToggled() && shadows.module.get("ArrayList")) {
					double finalOffset = offset;
					BloomUtils.addToDraw(() -> Gui.drawRect(6,(float) finalOffset + 19f, font.getStringWidth(module.getName()) + 10, (float) finalOffset + 6f, -1));
				}

				Gui.drawRect(6,(float) offset + 19f, (float) font.getStringWidth(module.getName()) + 10, (float) offset + 6f, backgroundColor.getColor().getRGB());

				font.drawString(module.getName(), 8.5f, (float) (8.5f + offset), color.getColor().getRGB(), textShadow.isToggled());
				offset += 13;
			}
		}
	}

	void sort(final List<Module> toSort, final FontRenderer fontToCalcWidth) {
		toSort.sort( (m1, m2) -> {
			final double width1 = fontToCalcWidth.getStringWidth(m1.getName());
			final double width2 = fontToCalcWidth.getStringWidth(m2.getName());

			return Double.compare(width2, width1);
		});
	}
}