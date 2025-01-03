package me.hackclient.guis.clickGui;

import me.hackclient.Client;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.impl.visual.Bloom;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {

	boolean binding;

	Category selectedCategory = Category.COMBAT;
	Module selectedModule;
	Setting lastSetting;

	final Animation2D categoryLineAnimation;
	final Animation2D moduleLineAnimation;
	final Animation2D settingLineAnimation;

	public ClickGuiScreen() {
        settingLineAnimation = new Animation2D(0,0,0,0);
        categoryLineAnimation = new Animation2D(0, 0, 0, 0);
		moduleLineAnimation = new Animation2D(0, 0 ,0, 0);
	}

	List<Runnable> list = new ArrayList<>();

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Bloom bloomModule = InstanceAccess.mm.getModule(Bloom.class);
		if (bloomModule.isToggled() && bloomModule.clickGui.isToggled()) {
			list.add(() -> RoundedUtils.drawRect(70, 50 ,400, 200, 2 , new Color(255, 255, 255, 255)));
			BloomUtils.drawBloom(list);
			list.clear();
		}
		drawMain(mouseX, mouseY);
	}

	void drawMain(int mouseX, int mouseY) {
		RoundedUtils.drawRect(70, 50 ,400, 200, 2 , new Color(15, 15, 15, 100));
		int offset = 0;
		final int categoryOffset = 75;
		for (Category category : Category.values()) {
			mc.fontRendererObj.drawString(
					category.name,
					70 + 5 + offset + categoryOffset,
					55,
					new Color(252, 252, 252, 255).getRGB()
			);

			// Draw line for selected category
			if (category == selectedCategory) {
				float gavno = (float) (categoryLineAnimation.x + mc.fontRendererObj.getStringWidth(category.name));
				if (gavno > 400) {
					gavno = 400;
				}
				RoundedUtils.drawRect(
                        (float) categoryLineAnimation.x,
                        (float) categoryLineAnimation.y,
						gavno,
						(float) categoryLineAnimation.y + 1,
						3f,
						new Color(0, 255, 209, 255)
				);
				categoryLineAnimation.endX = 70 + 5 + offset + categoryOffset;
				categoryLineAnimation.endY = 55 + 10;
				categoryLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
			}
			offset += mc.fontRendererObj.getStringWidth(category.name) + 10;
		}

		// Watermark
		mc.fontRendererObj.drawString(
				Client.INSTANCE.getName(),
				70 + 3,
				50 + 6,
				new	Color(255, 255, 255, 255).getRGB()
		);

		// Line
		drawRect(70, 60 + 10, 400, 60 + 10 + 1, new Color(245, 245, 245, 163).getRGB());
		drawRect(70 + 75, 50, 70 + 75 + 1, 200,new Color(255, 255, 255, 152).getRGB());
		offset = 0;

		// Draw modules
		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory)) {
			mc.fontRendererObj.drawString(
					module.getName(),
					70 + 5,
					75 + offset,
					module.isToggled() ? new Color(0, 255, 209, 255).getRGB() : Color.WHITE.getRGB(),
					true
			);
			if (module == selectedModule) {

				// Draw line for selected modules
				RoundedUtils.drawRect(
						(float) moduleLineAnimation.x,
						(float) moduleLineAnimation.y,
						(float) moduleLineAnimation.x + 1,
						(float) moduleLineAnimation.y + 10,
						3f,
						new Color(0, 255, 209, 255)
				);
				moduleLineAnimation.endX = 70 + 2;
				moduleLineAnimation.endY = 74 + offset;
				moduleLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
			}
			offset += 11;
		}
		offset = 0;

		// Draw setting modules
		if (selectedModule != null) {
			RoundedUtils.drawRect(
					70 + 80,
					86,
					400 - 4,
					86 + 110,
					3f,
					new Color(0, 0, 0, 50)
			);
			mc.fontRendererObj.drawString(
					"KeyBind: " + (binding ? "..." : Keyboard.getKeyName(selectedModule.getKey())),
					70 + 80,
					75,
					-1
			);
			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible()) continue;
				if (setting == lastSetting) {
					settingLineAnimation.endX = 70 + 82;
					settingLineAnimation.endY = 90 + offset;
					settingLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
					RoundedUtils.drawRect(
							(float) settingLineAnimation.x,
							(float) settingLineAnimation.y - 1,
							(float) settingLineAnimation.x + 1,
							(float) settingLineAnimation.y + 9,
							3f,
							new Color(0, 255, 209, 255)
					);
				}
				mc.fontRendererObj.drawString(
						setting.getName() + ": ",
						70 + 85,
						90 + offset,
						-1
				);
				offset += 11;
				int nameLength = mc.fontRendererObj.getStringWidth(setting.getName() + ": ");
				if (setting instanceof BooleanSetting booleanSetting) {
					mc.fontRendererObj.drawString(
							String.valueOf(booleanSetting.isToggled()),
							70 + 85 + nameLength,
							79 + offset,
							booleanSetting.isToggled() ? new Color(0, 255, 209, 255).getRGB() : Color.RED.getRGB()
					);
				}
				if (setting instanceof IntegerSetting integerSetting) {
					double normalizedFactor = integerSetting.normalize();
					float length = 75;
					double sliderLength = length * normalizedFactor;
					RoundedUtils.drawRect(
							70 + 85 + nameLength,
							81 + offset,
							70 + 85 + nameLength + length,
							81 + 3 + offset,
							1.5f,
							new Color(101, 101, 101, 85)
					);
					RoundedUtils.drawRect(
							(70 + 85 + nameLength),
							(81 + offset),
							(float) (70 + 85 + nameLength + sliderLength),
							81 + 3 + offset,
							1.5f,
							new Color(0, 255, 209, 255)
					);
					mc.fontRendererObj.drawString(
							integerSetting.getValue() + "",
							(int) (70 + 87 + nameLength + length),
							79 + offset,
							-1
					);
					if (Mouse.isButtonDown(0)) {
						if (mouseX > 70 + 85 + nameLength
								&& mouseX < 70 + 85 + nameLength + length
								&& mouseY > 81 + offset && mouseY < 81 + 3 + offset) {
							float mouseFactor = (mouseX - (70 + 85 + nameLength)) / length;
							lastSetting = setting;
							integerSetting.setValue(
                                    Math.round(integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * mouseFactor)
                            );
						}
					}
				}
				if (setting instanceof FloatSettings floatSetting) {
					double normalizedFactor = floatSetting.normalize();
					float length = 75;
					double sliderLength = length * normalizedFactor;
					RoundedUtils.drawRect(
							70 + 85 + nameLength,
							81 + offset,
							70 + 85 + nameLength + length,
							81 + 3 + offset,
							1.5f,
							new Color(101, 101, 101, 85)
					);
					RoundedUtils.drawRect(
                            (70 + 85 + nameLength),
                            (81 + offset),
                            (float) (70 + 85 + nameLength + sliderLength),
							81 + 3 + offset,
							1.5f,
							new Color(0, 255, 209, 255)
					);
					mc.fontRendererObj.drawString(
							floatSetting.getValue() + "",
                            (int) (70 + 87 + nameLength + length),
							79 + offset,
							-1
					);
					if (Mouse.isButtonDown(0)) {
						if (mouseX > 70 + 85 + nameLength
						&& mouseX < 70 + 85 + nameLength + length
						&& mouseY > 81 + offset && mouseY < 81 + 3 + offset) {
							float mouseFactor = (mouseX - (70 + 85 + nameLength)) / length;
							lastSetting = setting;
							floatSetting.setValue(
									(floatSetting.getMin() +
                                    (floatSetting.getMax() - floatSetting.getMin())
                                    * mouseFactor)
                            );
						}
					}
				}
				if (setting instanceof ModeSetting modeSetting) {
					int xOffset = 0;
					int yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						String gavno2 = mode + (!modeSetting.getModes()[modeSetting.getModes().length - 1].equalsIgnoreCase(mode) ? "," : "");
						if (70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2) >= 400) {
							xOffset = 0;
							yOffset += 11;
						}
						mc.fontRendererObj.drawString(
								gavno2,
								70 + 85 + nameLength + xOffset,
								79 + offset + yOffset,
								mode.equalsIgnoreCase(modeSetting.getMode()) ? new Color(0, 255, 209, 255).getRGB() : new Color(75, 75, 75, 179).getRGB(),
								true
						);
						xOffset += mc.fontRendererObj.getStringWidth(gavno2) + 4;
					}
					offset += yOffset;
				}
			}
		}
	}

	// Category and module select
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int offset = 0;
		final int categoryOffset = 75;
		for (Category category : Category.values()) {
			if (mouseButton == 0
			&& mouseX > 70 + 5 + offset + categoryOffset
			&& mouseY > 55
			&& mouseX < 70 + 5 + offset + categoryOffset + mc.fontRendererObj.getStringWidth(category.name)
			&& mouseY < 55 + 10) {
				selectedCategory = category;
			}
			offset += mc.fontRendererObj.getStringWidth(category.name) + 10;
		}
		offset = 0;
		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory)) {
			if (mouseX > 70 + 3
				&& mouseY > 75 + offset
				&& mouseX <  70 + 3 + mc.fontRendererObj.getStringWidth(module.getName())
				&& mouseY < 75 + offset + 10) {
				if (mouseButton == 0) {
					module.toggle();
				} else if (mouseButton == 1) {
					selectedModule = module;
				}
			}
			offset += 11;
		}
		offset = 0;
		if (selectedModule == null) return;
//		mc.fontRendererObj.drawString(
//				"KeyBind: " + Keyboard.getKeyName(selectedModule.getKey()),
//				70 + 80,
//				75,
//				-1
//		);
		if (mouseX > 70 + 80 + mc.fontRendererObj.getStringWidth("KeyBind: ")
		&& mouseX < 70 + 80 + mc.fontRendererObj.getStringWidth("KeyBind: " + Keyboard.getKeyName(selectedModule.getKey()))
		&& mouseY > 75
		&& mouseY < 75 + 10) {
			binding = true;
		}
		for (Setting setting : selectedModule.getSettings()) {
			if (!setting.isVisible()) continue;
			int nameLength = mc.fontRendererObj.getStringWidth(setting.getName() + ": ");
			offset += 11;
			if (setting instanceof BooleanSetting booleanSetting) {
				if (mouseX > 70 + 85 + nameLength && mouseX < 70 + 85 + nameLength + mc.fontRendererObj.getStringWidth(String.valueOf(booleanSetting.isToggled()))
				&& mouseY > 79 + offset && mouseY < 78 + offset + 10) {
					lastSetting = setting;
					booleanSetting.setToggled(!booleanSetting.isToggled());
				}
			}
			if (setting instanceof ModeSetting modeSetting) {
				int xOffset = 0;
				int yOffset = 0;
				for (String mode : modeSetting.getModes()) {
					String gavno2 = mode + (!modeSetting.getModes()[modeSetting.getModes().length - 1].equalsIgnoreCase(mode) ? "," : "");
					if (70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2) >= 400) {
						xOffset = 0;
						yOffset += 11;
					}
					/*mc.fontRendererObj.drawString(
							gavno2,
							70 + 85 + nameLength + xOffset,
							79 + offset + yOffset,
							mode.equalsIgnoreCase(modeSetting.getMode()) ? new Color(0, 255, 209, 255).getRGB() : new Color(75, 75, 75, 179).getRGB(),
							true
					);*/
					if (mouseX > 70 + 85 + nameLength + xOffset
					&& mouseX < 70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2)
					&& mouseY > 79 + offset + yOffset
					&& mouseY < 79 + offset + yOffset + 10) {
						modeSetting.setMode(mode);
						lastSetting = setting;
					}
					xOffset += mc.fontRendererObj.getStringWidth(gavno2) + 4;
				}
				offset += yOffset;
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (selectedModule == null) {
			binding = false;
		}
		if (binding) {
			binding = false;
			if (keyCode == Keyboard.KEY_ESCAPE) {
				selectedModule.setKey(Keyboard.KEY_NONE);
				return;
			}
			selectedModule.setKey(keyCode);
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void onGuiClosed() {
		Client.INSTANCE.getModuleManager().getModule(ClickGui.class).setToggled(false);
	}
}