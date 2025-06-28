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
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL)
public class ArrayList extends Module {

	final ColorSetting color = new ColorSetting("TextColor", this, 1f,1f,1f,1f);
	final CheckBox textShadow = new CheckBox("TextShadow", this, false);

	final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, 0,0,0,0.5f);

	final CheckBox suffix = new CheckBox("Suffix", this);

	final MultiMode categories = new MultiMode("HideCategories", this)
			.addModes("Combat", "Move", "Visual", "Connection", "Exploit", "Legit", "Player", "Misc", "Client")
			;

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
				if (categories.get("Combat") && module.getCategory() == Category.COMBAT) continue;
				if (categories.get("Move") && module.getCategory() == Category.MOVE) continue;
				if (categories.get("Visual") && module.getCategory() == Category.VISUAL) continue;
				if (categories.get("Connection") && module.getCategory() == Category.CONNECTION) continue;
				if (categories.get("Exploit") && module.getCategory() == Category.EXPLOIT) continue;
				if (categories.get("Legit") && module.getCategory() == Category.LEGIT) continue;
				if (categories.get("Player") && module.getCategory() == Category.PLAYER) continue;
				if (categories.get("Misc") && module.getCategory() == Category.MISC) continue;
				if (categories.get("Client") && module.getCategory() == Category.CLIENT) continue;
				if (module.isHide()) continue;

				if (shadows.isToggled() && shadows.module.get("ArrayList")) {
					double finalOffset = offset;
					BloomUtils.addToDraw(() -> Gui.drawRect(6,(float) finalOffset + 19f, font.getStringWidth(module.getName() + (suffix.isToggled() ? (!module.getSuffix().equalsIgnoreCase("") ? " - " + module.getSuffix() : "") : "")) + 10, (float) finalOffset + 6f, -1));
				}

				Gui.drawRect(6,(float) offset + 19f, (float) font.getStringWidth(module.getName() + (suffix.isToggled() ? (!module.getSuffix().equalsIgnoreCase("") ? " - " + module.getSuffix() : "") : "")) + 10, (float) offset + 6f, backgroundColor.getColor().getRGB());
				font.drawString(module.getName() + (suffix.isToggled() ? (!module.getSuffix().equalsIgnoreCase("") ? " - " + module.getSuffix() : "") : ""), 8.5f, (float) (8.5f + offset), color.getColor().getRGB(), textShadow.isToggled());
				offset += 13;
			}
		}
	}

	void sort(final List<Module> toSort, final FontRenderer fontToCalcWidth) {
		toSort.sort( (m1, m2) -> {
			final double width1 = fontToCalcWidth.getStringWidth(m1.getName() + (suffix.isToggled() ? (!m1.getSuffix().equalsIgnoreCase("") ? " - " + m1.getSuffix() : "") : ""));
			final double width2 = fontToCalcWidth.getStringWidth(m2.getName() + (suffix.isToggled() ? (!m2.getSuffix().equalsIgnoreCase("") ? " - " + m2.getSuffix() : "") : ""));

			return Double.compare(width2, width1);
		});
	}
}