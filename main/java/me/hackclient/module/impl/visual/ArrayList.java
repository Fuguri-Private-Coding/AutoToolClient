package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, toggled = true)
public class ArrayList extends Module {

	final BooleanSetting showRenderModules = new BooleanSetting("ShowRenderModules", this, false);
	final BooleanSetting showLine = new BooleanSetting("Line", this, false);

	final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof Render2DEvent) {
			final FontRenderer font = mc.fontRendererObj;
			List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INSTANCE.getModuleManager().getEnabledModules());

			sort(moduleList, font);

			double offset = 0;
			for (Module module : moduleList) {
				if (module.isHide() || !showRenderModules.isToggled() && module.getCategory() == Category.VISUAL) {
					continue;
				}

				font.drawString(module.getName(), 5, (float) (5 + offset), color.getColor().getRGB(), true);
				offset += 12;
			}

			if (showLine.isToggled()) {
				Gui.drawRect(1, 0, 2, (int) offset, color.getColor().getRGB());
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