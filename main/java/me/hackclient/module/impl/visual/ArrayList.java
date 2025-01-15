package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.DrawEntityEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.TextFadeUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.List;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, toggled = true)
public class ArrayList extends Module {

	BooleanSetting background = new BooleanSetting("Background", this, false);

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
//		if (event instanceof DrawEntityEvent drawEntityEvent
//		&& drawEntityEvent.getDrawingEntity() != null) {
//			TextFadeUtils.draw(() -> mc.getRenderManager().renderEntitySimple(drawEntityEvent.getDrawingEntity(), mc.timer.renderPartialTicks), Color.MAGENTA, Color.CYAN);
//		}
		if (event instanceof Render2DEvent) {
			Bloom bloomModule = mm.getModule(Bloom.class);
			if (bloomModule.isToggled() && bloomModule.arrayList.isToggled()) {
				List<Runnable> list = new java.util.ArrayList<>();
				list.add(() -> {
					int offset = 0;
					for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
						Gui.drawRect(
								3,
								3 + offset,
								6 + mc.fontRendererObj.getStringWidth(module.getName()),
								5 + offset + 10,
								new Color(255, 255, 255, 255).getRGB()
						);
						offset += mc.fontRendererObj.FONT_HEIGHT + 3;
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
			if (background.isToggled()) {
				Gui.drawRect(
						3,
						3 + offset,
						6 + mc.fontRendererObj.getStringWidth(module.getName()),
						5 + offset + 10,
						new Color(0, 0, 0, 75).getRGB()
				);
			}

			int finalOffset = offset;
			mc.fontRendererObj.drawString(module.getName(), 5, 5 + finalOffset, Color.WHITE.getRGB());
			offset += mc.fontRendererObj.FONT_HEIGHT + 3;
		}

	}
}