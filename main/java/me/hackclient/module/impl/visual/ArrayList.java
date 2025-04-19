package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import net.minecraft.client.gui.FontRenderer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, toggled = true)
public class ArrayList extends Module {

	final BooleanSetting showRenderModules = new BooleanSetting("ShowRenderModules", this, false);
	final BooleanSetting showLine = new BooleanSetting("Line", this, false);

	final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);
	final BooleanSetting textShadow = new BooleanSetting("TextShadow", this, false);

	final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, 0,0,0,0.5f);
	final FloatSetting backgroundRadius = new FloatSetting("BackgroundRadius", this, 0.5f,5f,1f,0.5f);

	Shadows shadows;

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
		if (event instanceof Render2DEvent) {
			final FontRenderer font = mc.fontRendererObj;
			List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INSTANCE.getModuleManager().getEnabledModules());

			sort(moduleList, font);

			double offset = 0;
			for (Module module : moduleList) {
				if (module.isHide() || !showRenderModules.isToggled() && module.getCategory() == Category.VISUAL) continue;

				if (shadows.isToggled() && shadows.arrayList.isToggled()) {
					double finalOffset = offset;
					BloomUtils.addToDraw(() -> {
						RoundedUtils.drawRect(6,(float) finalOffset + 6f, font.getStringWidth(module.getName()) + 4, font.FONT_HEIGHT + 4f, backgroundRadius.getValue(), shadows.color.getColor());
						if (showLine.isToggled()) RoundedUtils.drawRect(4, 6, 2, (float) finalOffset + 12, 2f, shadows.color.getColor());
					});
				}

				RoundedUtils.drawRect(6,(float) offset + 6f, font.getStringWidth(module.getName()) + 4, font.FONT_HEIGHT + 4f, backgroundRadius.getValue(), backgroundColor.getColor());

				font.drawString(module.getName(), 8.5f, (float) (8.5f + offset), color.getColor().getRGB(), textShadow.isToggled());
				offset += 13;
			}

			if (showLine.isToggled()) {
				RoundedUtils.drawRect(4, 6, 2, (float) offset, 2f, color.getColor());
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