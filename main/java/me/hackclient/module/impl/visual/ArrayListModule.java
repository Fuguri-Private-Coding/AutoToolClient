package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, toggled = true)
public class ArrayListModule extends Module {

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof Render2DEvent) {
			BloomModule bloomModule = mm.getModule(BloomModule.class);
			if (bloomModule.isToggled() && bloomModule.arrayList.isToggled()) {
				List<Runnable> list = new ArrayList<>();
				list.add(() -> {
					int offset = 0;
					for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
						mc.fontRendererObj.drawString(module.getName(), 5, 5 + offset, new Color(0, 255, 247, 255).getRGB(), true);
						offset += mc.fontRendererObj.FONT_HEIGHT + 1;
					}
				});
				BloomUtils.drawBloom(list);
			}
			drawMain();
		}
	}

	public void drawMain() {
		Client.INSTANCE.getModuleManager().modules.sort(
				(o1, o2) -> {
					int width1 = mc.fontRendererObj.getStringWidth(o1.getName());
					int width2 = mc.fontRendererObj.getStringWidth(o2.getName());

					return Integer.compare(width2, width1);
				}
		);
		int offset = 0;
		for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
			mc.fontRendererObj.drawString(module.getName(), 5, 5 + offset, Color.WHITE.getRGB(), true);
			offset += mc.fontRendererObj.FONT_HEIGHT + 1;
		}
	}
}
