package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.font.ClientFontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, toggled = true)
public class ArrayList extends Module {

	final ModeSetting selectedFont = new ModeSetting(
			"Font",
			this,
			"Roboto",
			new String[] {
					"JetBrains",
					"Roboto"
			}
	);

	final BooleanSetting showRenderModules = new BooleanSetting("ShowRenderModules", this, false);
	final BooleanSetting showLine = new BooleanSetting("Line", this, false);


	final FloatSetting red = new FloatSetting("Red", this, 0f, 1f, 0f, 0.01f);
	final FloatSetting green = new FloatSetting("Green", this, 0f, 1f, 0.0f, 0.01f);
	final FloatSetting blue = new FloatSetting("Blue", this, 0f, 1f, 0.4f, 0.01f);
	final FloatSetting alpha = new FloatSetting("Alpha", this, 0f, 1f, 1f, 0.01f);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);

		Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());

		if (event instanceof Render2DEvent) {
			final ClientFontRenderer font = Client.INSTANCE.getFontsRepository().fonts.get(selectedFont.getMode());
			List<Module> moduleList = new CopyOnWriteArrayList<>(mm.getEnabledModules());

			sort(moduleList, font);

			double offset = 0;
			for (Module module : moduleList) {
				if (module.isHide() || !showRenderModules.isToggled() && module.getCategory() == Category.VISUAL) {
					continue;
				}

				font.drawString(module.getName(), 5, 5 + offset, color, true);
				offset += 12;
			}

			if (showLine.isToggled()) {
				Gui.drawRect(0, 0, 2, (int) offset, color.getRGB());
			}
		}
	}

	void sort(final List<Module> toSort, final ClientFontRenderer fontToCalcWidth) {
		toSort.sort( (m1, m2) -> {
			final double width1 = fontToCalcWidth.getWidth(m1.getName());
			final double width2 = fontToCalcWidth.getWidth(m2.getName());

			return Double.compare(width2, width1);
		});
	}
}