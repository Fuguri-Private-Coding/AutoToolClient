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

	final FloatSetting red = new FloatSetting("Red", this, 0f, 1f, 0f, 0.01f);
	final FloatSetting green = new FloatSetting("Green", this, 0f, 1f, 0.2f, 0.01f);
	final FloatSetting blue = new FloatSetting("Blue", this, 0f, 1f, 1f, 0.01f);
	final FloatSetting alpha = new FloatSetting("Alpha", this, 0f, 1f, 0f, 0.01f);

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

			Gui.drawRect(0, 0, 2, (int) offset, color.getRGB());

		}
	}

	void sort(final List<Module> toSort, final ClientFontRenderer fontToCalcWidth) {
		toSort.sort( (m1, m2) -> {
			final double width1 = fontToCalcWidth.getWidth(m1.getName());
			final double width2 = fontToCalcWidth.getWidth(m2.getName());

			return Double.compare(width2, width1);
		});
	}

	//	BooleanSetting background = new BooleanSetting("Background", this, false);
//	BooleanSetting skipRenderModules = new BooleanSetting("SkipVisualModules", this, true);
//
//	ClientShader clientShader;
//
//	@Override
//	public void onEvent(Event event) {
//		super.onEvent(event);
//		if (clientShader == null) {
//			clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
//			return;
//		}
//		if (event instanceof Render2DEvent) {
//			Bloom bloomModule = mm.getModule(Bloom.class);
//			if (bloomModule.isToggled() && bloomModule.arrayList.isToggled()) {
//				List<Runnable> list = getRunnables();
//				BloomUtils.drawBloom(list);
//			}
//			drawMain();
//		}
//	}
//
//	private List<Runnable> getRunnables() {
//		List<Runnable> list = new java.util.ArrayList<>();
//		list.add(() -> {
//			int offset = 0;
//			for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
//				if (skipRenderModules.isToggled() && module.getCategory() == Category.VISUAL || module.isHide()) continue;
//				Gui.drawRect(3, 3 + offset, 6 + mc.fontRendererObj.getStringWidth(module.getName()), 5 + offset + 10, new Color(255, 255, 255, 255).getRGB());
//				offset += mc.fontRendererObj.FONT_HEIGHT + 3;
//			}
//		});
//		return list;
//	}
//
//	public void drawMain() {
//		Client.INSTANCE.getModuleManager().modules.sort(
//				(o1, o2) -> {
//					int width1 = mc.fontRendererObj.getStringWidth(o1.getName());
//					int width2 = mc.fontRendererObj.getStringWidth(o2.getName());
//
//					return Integer.compare(width2, width1);
//				}
//		);
//		if (clientShader.isToggled() && clientShader.arrayList.isToggled()) {
//			TestBloomUtils.add(() -> PixelReplacerUtils.addToDraw(() -> {
//                int offset = 0;
//                for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
//                    if (skipRenderModules.isToggled() && module.getCategory() == Category.VISUAL || module.isHide()) continue;
//                    if (background.isToggled()) {
//                        Gui.drawRect(3, 3 + offset, 6 + mc.fontRendererObj.getStringWidth(module.getName()), 5 + offset + 10, new Color(0, 0, 0, 75).getRGB());
//                    }
//                    mc.fontRendererObj.drawString(module.getName(), 5, 5 + offset, Color.WHITE.getRGB());
//                    offset += mc.fontRendererObj.FONT_HEIGHT + 3;
//                }
//            }));
//		} else {
//			int offset = 0;
//			for (Module module : Client.INSTANCE.getModuleManager().getEnabledModules()) {
//				if (skipRenderModules.isToggled() && module.getCategory() == Category.VISUAL || module.isHide()) continue;
//				if (background.isToggled()) {
//					Gui.drawRect(3, 3 + offset, 6 + mc.fontRendererObj.getStringWidth(module.getName()), 5 + offset + 10, new Color(0, 0, 0, 75).getRGB());
//				}
//				mc.fontRendererObj.drawString(module.getName(), 5, 5 + offset, Color.WHITE.getRGB());
//				offset += mc.fontRendererObj.FONT_HEIGHT + 3;
//			}
//		}
//	}
}