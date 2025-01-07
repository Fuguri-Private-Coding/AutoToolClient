package me.hackclient.guis.clickGui;

import me.hackclient.Client;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;

import static java.lang.Math.*;

public class ClickGuiScreen extends GuiScreen {

	final Color BACKGROUND_COLOR = new Color(15, 15, 15, 100);
	final Color MAIN_COLOR = new Color(0, 255, 209, 255);
	final int MAIN_COLOR_INT = MAIN_COLOR.getRGB();
	final int BACKGROUND_COLOR_INT = BACKGROUND_COLOR.getRGB();

	Vector2f pos, size, lastMouse;

	Category selectedCategory = Category.COMBAT;
	Module selectedModule = null;

	boolean resizing, moving, binding;

	// Animations
	final Animation2D categoryLine, moduleLine, settingLine;

	public ClickGuiScreen() {
		lastMouse = new Vector2f(0, 0);
		pos = new Vector2f(100, 100);
		size = new Vector2f(350, 150);

		mc = Minecraft.getMinecraft();

		categoryLine = new Animation2D();
		moduleLine = new Animation2D();
		settingLine = new Animation2D();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		super.drawScreen(mouseX, mouseY, partialTicks);
		// TODO: добавить блум.
		if (resizing
		&& mouseX > pos.x + 100
		&& mouseY > pos.y + 100) {
			size.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
			lastMouse.set(mouseX, mouseY);
		}
		if (moving) {
			pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
			lastMouse.set(mouseX, mouseY);
		}

		final FontRenderer fontRenderer = mc.fontRendererObj;
		final float clientNameWidth = fontRenderer.getStringWidth(Client.INSTANCE.getName());
		float offset = 0;

		RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, 2, BACKGROUND_COLOR);
		RoundedUtils.drawRect(pos.x + size.x - 5, pos.y + size.y - 5, 5, 5, 1, BACKGROUND_COLOR);
		fontRenderer.drawString(Client.INSTANCE.getName(), pos.x + 14, pos.y + 4, MAIN_COLOR_INT);

		float widthestModule = 0;
		for (Module module : Client.INSTANCE.getModuleManager().modules) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());

			if (moduleWidth > widthestModule) {
				widthestModule = moduleWidth;
			}
		}
		float verticalLineXOffset = max(clientNameWidth + 14, widthestModule) + 7;


		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory))	{
			fontRenderer.drawString(
					module.getName(),
					pos.x + 4,
					pos.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset,
					module.isToggled() ? MAIN_COLOR_INT : -1
					);
			offset += fontRenderer.FONT_HEIGHT + 2;
			if (module == selectedModule) {
				moduleLine.endY = pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset;
			}
		}

		offset = 0;
		for (Category category : Category.values()) {
			if (category == selectedCategory) {
				categoryLine.endX = pos.x + verticalLineXOffset + 5 + 5 + offset;
				categoryLine.endY = pos.y + 3 + fontRenderer.FONT_HEIGHT;
				RoundedUtils.drawRect((float) categoryLine.x, (float) categoryLine.y, fontRenderer.getStringWidth(selectedCategory.name), 1, 3, MAIN_COLOR);
			}
			fontRenderer.drawString(
					category.name,
					pos.x + verticalLineXOffset + 5 + 5 + offset,
					pos.y + 3,
					-1
			);
			offset += fontRenderer.getStringWidth(category.name) + 5;
		}

		RoundedUtils.drawRect(pos.x + verticalLineXOffset, pos.y, 1, size.y, 3, MAIN_COLOR);
		RoundedUtils.drawRect(pos.x, pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 2.5f, size.x, 1, 3, MAIN_COLOR);

		offset = 0;
		if (selectedModule != null) {
			RoundedUtils.drawRect(pos.x + 2, (float) moduleLine.y - 12, 1, 12, 3, MAIN_COLOR);
			fontRenderer.drawString(
					"Keybind: " + (binding ? "..." : Keyboard.getKeyName(selectedModule.getKey())),
					pos.x + verticalLineXOffset + 5,
					pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
					-1
					);

			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible())
					continue;

				float settingWidth = fontRenderer.getStringWidth(setting.getName() + ": ");
				fontRenderer.drawString(
						setting.getName() + ": ",
						pos.x + verticalLineXOffset + 5,
						pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
						-1
				);
				if (setting instanceof ModeSetting modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						int length = modeSetting.getModes().length;
						boolean isSelected = mode.equals(modeSetting.getMode());
						boolean notLast = !mode.equals(modeSetting.getModes()[length - 1]);
						if (pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= pos.x + size.x) {
							xOffset = 0;
							yOffset += 11;
						}
						fontRenderer.drawString(
								mode,
								pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
								pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
								isSelected ? MAIN_COLOR_INT : -1
						);
						if (notLast) {
							fontRenderer.drawString(
									",",
									pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
									pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
									-1
							);
						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof BooleanSetting booleanSetting) {
					fontRenderer.drawString(
							String.valueOf(booleanSetting.isToggled()),
							pos.x + verticalLineXOffset + 5 + settingWidth + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							booleanSetting.isToggled() ? Color.GREEN.darker().getRGB() : Color.RED.getRGB()
					);
				}
				if (setting instanceof IntegerSetting integerSetting) {
					float filledFactor = integerSetting.normalize();
					final float length = 75;
					final float sliderLength = filledFactor * length;
					RoundedUtils.drawRect(
							pos.x + verticalLineXOffset + 5 + settingWidth + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							length,
							4,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							pos.x + verticalLineXOffset + 5 + settingWidth + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							sliderLength,
							4,
							1.5f,
							MAIN_COLOR
					);
					fontRenderer.drawString(
							String.valueOf(integerSetting.getValue()),
							pos.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							-1
					);

					if (Mouse.isButtonDown(0)
					&& mouseX > pos.x + verticalLineXOffset + 5 + settingWidth
					&& mouseX < pos.x + verticalLineXOffset + 5 + settingWidth + length + 1
					&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
					&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 4) {
						float mx = mouseX - (pos.x + verticalLineXOffset + 5 + settingWidth);
						float p = mx / length;
						float normalize = integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * p;
						integerSetting.setValue(Math.round(normalize));
					}
				}
				if (setting instanceof FloatSetting floatSetting) {
					float filledFactor = floatSetting.normalize();
					final float length = 75;
					final float sliderLength = filledFactor * length;
					RoundedUtils.drawRect(
							pos.x + verticalLineXOffset + 5 + settingWidth + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							length,
							4,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							pos.x + verticalLineXOffset + 5 + settingWidth + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							sliderLength,
							4,
							1.5f,
							MAIN_COLOR
					);
					fontRenderer.drawString(
							String.format("%.3f", floatSetting.getValue()),
							pos.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 1,
							pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							-1
					);

					if (Mouse.isButtonDown(0)
							&& mouseX > pos.x + verticalLineXOffset + 5 + settingWidth
							&& mouseX < pos.x + verticalLineXOffset + 5 + settingWidth + length + 1
							&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
							&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 4) {
						float mx = mouseX - (pos.x + verticalLineXOffset + 5 + settingWidth);
						float p = mx / length;
						float normalize = floatSetting.getMin() + (floatSetting.getMax() - floatSetting.getMin()) * p;
						floatSetting.setValue(normalize);
					}
				}
				offset += 11;
			}
		}

		categoryLine.update(10f);
		moduleLine.update(10f);
		settingLine.update(10f);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		final FontRenderer fontRenderer = mc.fontRendererObj;
		final float clientNameWidth = fontRenderer.getStringWidth(Client.INSTANCE.getName());
		float offset = 0;

		if (mouseX > pos.x + size.x - 5
		&& mouseX < pos.x + size.x
		&& mouseY > pos.y + size.y - 5
		&& mouseY < pos.y + size.y) {
			resizing = true;
			lastMouse.set(mouseX, mouseY);
		}

		float widthestModule = 0;
		for (Module module : Client.INSTANCE.getModuleManager().modules) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());

			if (moduleWidth > widthestModule) {
				widthestModule = moduleWidth;
			}
		}
		float verticalLineXOffset = max(clientNameWidth, widthestModule) + 5;

		if (mouseX > pos.x
				&& mouseX < pos.x + verticalLineXOffset
				&& mouseY > pos.y
				&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT) {
			moving = true;
			lastMouse.set(mouseX, mouseY);
		}

		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory))	{
			float moduleWidth = fontRenderer.getStringWidth(module.getName());
			if (mouseX > pos.x + 3
			&& mouseX < pos.x + 3 + moduleWidth
			&& mouseY > pos.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset
			&& mouseY < pos.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset + 9) {
				switch (mouseButton) {
					case 0 -> module.toggle();
					case 1 -> selectedModule = module;
                    case 2 -> {
                        selectedModule = module;
                        binding = true;
                    }
                }
			}
			offset += fontRenderer.FONT_HEIGHT + 2;
		}

		offset = 0;
		for (Category category : Category.values()) {
			if (mouseX > pos.x + verticalLineXOffset + 5 + 5 + offset
			&& mouseX < pos.x + verticalLineXOffset + 5 + 5 + offset + fontRenderer.getStringWidth(category.name)
			&& mouseY > pos.y + 2
			&& mouseY < pos.y + 2 + fontRenderer.FONT_HEIGHT) {
				selectedCategory = category;
				selectedModule = null;
				moduleLine.x = 0;
				moduleLine.y = 0;
			}
			offset += fontRenderer.getStringWidth(category.name) + 5;
		}

		offset = 0;
		if (selectedModule != null) {
			float moduleWidth = fontRenderer.getStringWidth(selectedModule.getName());

            if (mouseX > pos.x + verticalLineXOffset + 5
			&& mouseX < pos.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Keybind: " + (binding ? "..." : Keyboard.getKeyName(selectedModule.getKey())))
			&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6
			&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9) {
				binding = true;
			}

			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible())
					continue;

				float settingWidth = fontRenderer.getStringWidth(setting.getName() + ": ");
				fontRenderer.drawString(
						setting.getName() + ": ",
						pos.x + verticalLineXOffset + 5,
						pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
						-1
				);
				if (setting instanceof ModeSetting modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
//						int length = modeSetting.getModes().length;
//						boolean isSelected = mode.equals(modeSetting.getMode());
//						boolean notLast = !mode.equals(modeSetting.getModes()[length - 1]);
						if (pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= pos.x + size.x) {
							xOffset = 0;
							yOffset += 11;
						}
						if (mouseX > pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset
						&& mouseX < pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode)
						&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset
						&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset + 10) {
							modeSetting.setMode(mode);
						}
//						fontRenderer.drawString(
//								mode,
//								pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
//								pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
//								isSelected ? MAIN_COLOR_INT : -1
//						);
//						if (notLast) {
//							fontRenderer.drawString(
//									",",
//									pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
//									pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
//									-1
//							);
//						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof BooleanSetting booleanSetting) {
					if (mouseX > pos.x + verticalLineXOffset + 5 + settingWidth + 1
					&& mouseX < pos.x + verticalLineXOffset + 5 + settingWidth + 1 + fontRenderer.getStringWidth(String.valueOf(booleanSetting.isToggled()))
					&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset
					&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + 10) {
						booleanSetting.setToggled(!booleanSetting.isToggled());
					}
				}
				offset += 11;
			}
		}

	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		resizing = false;
		moving = false;
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

//	boolean binding;
//
//	Category selectedCategory = Category.COMBAT;
//	Module selectedModule;
//	Setting lastSetting;
//
//	final Animation2D categoryLineAnimation;
//	final Animation2D moduleLineAnimation;
//	final Animation2D settingLineAnimation;
//
//	public ClickGuiScreen() {
//        settingLineAnimation = new Animation2D(0,0,0,0);
//        categoryLineAnimation = new Animation2D(0, 0, 0, 0);
//		moduleLineAnimation = new Animation2D(0, 0 ,0, 0);
//	}
//
//	List<Runnable> list = new ArrayList<>();
//
//	@Override
//	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		Bloom bloomModule = InstanceAccess.mm.getModule(Bloom.class);
//		if (bloomModule.isToggled() && bloomModule.clickGui.isToggled()) {
//			list.add(() -> RoundedUtils.drawRect(70, 50 ,400, 200, 2 , new Color(255, 255, 255, 255)));
//			BloomUtils.drawBloom(list);
//			list.clear();
//		}
//		drawMain(mouseX, mouseY);
//	}
//
//	void drawMain(int mouseX, int mouseY) {
//		RoundedUtils.drawRect(70, 50 ,400, 200, 2 , new Color(15, 15, 15, 100));
//		int offset = 0;
//		final int categoryOffset = 75;
//		for (Category category : Category.values()) {
//			mc.fontRendererObj.drawString(
//					category.name,
//					70 + 5 + offset + categoryOffset,
//					55,
//					new Color(252, 252, 252, 255).getRGB()
//			);
//
//			// Draw line for selected category
//			if (category == selectedCategory) {
//				float gavno = (float) (categoryLineAnimation.x + mc.fontRendererObj.getStringWidth(category.name));
//				if (gavno > 400) {
//					gavno = 400;
//				}
//				RoundedUtils.drawRect(
//                        (float) categoryLineAnimation.x,
//                        (float) categoryLineAnimation.y,
//						gavno,
//						1,
//						3f,
//						new Color(0, 255, 209, 255)
//				);
//				categoryLineAnimation.endX = 70 + 5 + offset + categoryOffset;
//				categoryLineAnimation.endY = 55 + 10;
//				categoryLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
//			}
//			offset += mc.fontRendererObj.getStringWidth(category.name) + 10;
//		}
//
//		// Watermark
//		mc.fontRendererObj.drawString(
//				Client.INSTANCE.getName(),
//				70 + 3,
//				50 + 6,
//				new	Color(255, 255, 255, 255).getRGB()
//		);
//
//		// Line
//		drawRect(70, 60 + 10, 400, 60 + 10 + 1, new Color(245, 245, 245, 163).getRGB());
//		drawRect(70 + 75, 50, 70 + 75 + 1, 200,new Color(255, 255, 255, 152).getRGB());
//		offset = 0;
//
//		// Draw modules
//		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory)) {
//			mc.fontRendererObj.drawString(
//					module.getName(),
//					70 + 5,
//					75 + offset,
//					module.isToggled() ? new Color(0, 255, 209, 255).getRGB() : Color.WHITE.getRGB(),
//					true
//			);
//			if (module == selectedModule) {
//
//				// Draw line for selected modules
//				RoundedUtils.drawRect(
//						(float) moduleLineAnimation.x,
//						(float) moduleLineAnimation.y,
//						(float) moduleLineAnimation.x + 1,
//						(float) moduleLineAnimation.y + 10,
//						3f,
//						new Color(0, 255, 209, 255)
//				);
//				moduleLineAnimation.endX = 70 + 2;
//				moduleLineAnimation.endY = 74 + offset;
//				moduleLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
//			}
//			offset += 11;
//		}
//		offset = 0;
//
//		// Draw setting modules
//		if (selectedModule != null) {
//			RoundedUtils.drawRect(
//					70 + 80,
//					86,
//					400 - 4,
//					86 + 110,
//					3f,
//					new Color(0, 0, 0, 50)
//			);
//			mc.fontRendererObj.drawString(
//					"KeyBind: " + (binding ? "..." : Keyboard.getKeyName(selectedModule.getKey())),
//					70 + 80,
//					75,
//					-1
//			);
//			for (Setting setting : selectedModule.getSettings()) {
//				if (!setting.isVisible()) continue;
//				if (setting == lastSetting) {
//					settingLineAnimation.endX = 70 + 82;
//					settingLineAnimation.endY = 90 + offset;
//					settingLineAnimation.update(Client.INSTANCE.getModuleManager().getModule(ClickGui.class).animationSpeed.getValue());
//					RoundedUtils.drawRect(
//							(float) settingLineAnimation.x,
//							(float) settingLineAnimation.y - 1,
//							(float) settingLineAnimation.x + 1,
//							(float) settingLineAnimation.y + 9,
//							3f,
//							new Color(0, 255, 209, 255)
//					);
//				}
//				mc.fontRendererObj.drawString(
//						setting.getName() + ": ",
//						70 + 85,
//						90 + offset,
//						-1
//				);
//				offset += 11;
//				int nameLength = mc.fontRendererObj.getStringWidth(setting.getName() + ": ");
//				if (setting instanceof BooleanSetting booleanSetting) {
//					mc.fontRendererObj.drawString(
//							String.valueOf(booleanSetting.isToggled()),
//							70 + 85 + nameLength,
//							79 + offset,
//							booleanSetting.isToggled() ? new Color(0, 255, 209, 255).getRGB() : Color.RED.getRGB()
//					);
//				}
//				if (setting instanceof IntegerSetting integerSetting) {
//					double normalizedFactor = integerSetting.normalize();
//					float length = 75;
//					double sliderLength = length * normalizedFactor;
//					RoundedUtils.drawRect(
//							70 + 85 + nameLength,
//							81 + offset,
//							70 + 85 + nameLength + length,
//							81 + 3 + offset,
//							1.5f,
//							new Color(101, 101, 101, 85)
//					);
//					RoundedUtils.drawRect(
//							(70 + 85 + nameLength),
//							(81 + offset),
//							(float) (70 + 85 + nameLength + sliderLength),
//							81 + 3 + offset,
//							1.5f,
//							new Color(0, 255, 209, 255)
//					);
//					mc.fontRendererObj.drawString(
//							integerSetting.getValue() + "",
//							(int) (70 + 87 + nameLength + length),
//							79 + offset,
//							-1
//					);
//					if (Mouse.isButtonDown(0)) {
//						if (mouseX > 70 + 85 + nameLength
//								&& mouseX < 70 + 85 + nameLength + length
//								&& mouseY > 81 + offset && mouseY < 81 + 3 + offset) {
//							float mouseFactor = (mouseX - (70 + 85 + nameLength)) / length;
//							lastSetting = setting;
//							integerSetting.setValue(
//                                    Math.round(integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * mouseFactor)
//                            );
//						}
//					}
//				}
//				if (setting instanceof FloatSettings floatSetting) {
//					double normalizedFactor = floatSetting.normalize();
//					float length = 75;
//					double sliderLength = length * normalizedFactor;
//					RoundedUtils.drawRect(
//							70 + 85 + nameLength,
//							81 + offset,
//							70 + 85 + nameLength + length,
//							81 + 3 + offset,
//							1.5f,
//							new Color(101, 101, 101, 85)
//					);
//					RoundedUtils.drawRect(
//                            (70 + 85 + nameLength),
//                            (81 + offset),
//                            (float) (70 + 85 + nameLength + sliderLength),
//							81 + 3 + offset,
//							1.5f,
//							new Color(0, 255, 209, 255)
//					);
//					mc.fontRendererObj.drawString(
//							floatSetting.getValue() + "",
//                            (int) (70 + 87 + nameLength + length),
//							79 + offset,
//							-1
//					);
//					if (Mouse.isButtonDown(0)) {
//						if (mouseX > 70 + 85 + nameLength
//						&& mouseX < 70 + 85 + nameLength + length
//						&& mouseY > 81 + offset && mouseY < 81 + 3 + offset) {
//							float mouseFactor = (mouseX - (70 + 85 + nameLength)) / length;
//							lastSetting = setting;
//							floatSetting.setValue(
//									(floatSetting.getMin() +
//                                    (floatSetting.getMax() - floatSetting.getMin())
//                                    * mouseFactor)
//                            );
//						}
//					}
//				}
//				if (setting instanceof ModeSetting modeSetting) {
//					int xOffset = 0;
//					int yOffset = 0;
//					for (String mode : modeSetting.getModes()) {
//						String gavno2 = mode + (!modeSetting.getModes()[modeSetting.getModes().length - 1].equalsIgnoreCase(mode) ? "," : "");
//						if (70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2) >= 400) {
//							xOffset = 0;
//							yOffset += 11;
//						}
//						mc.fontRendererObj.drawString(
//								gavno2,
//								70 + 85 + nameLength + xOffset,
//								79 + offset + yOffset,
//								mode.equalsIgnoreCase(modeSetting.getMode()) ? new Color(0, 255, 209, 255).getRGB() : new Color(75, 75, 75, 179).getRGB(),
//								true
//						);
//						xOffset += mc.fontRendererObj.getStringWidth(gavno2) + 4;
//					}
//					offset += yOffset;
//				}
//			}
//		}
//	}
//
//	// Category and module select
//	@Override
//	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
//		int offset = 0;
//		final int categoryOffset = 75;
//		for (Category category : Category.values()) {
//			if (mouseButton == 0
//			&& mouseX > 70 + 5 + offset + categoryOffset
//			&& mouseY > 55
//			&& mouseX < 70 + 5 + offset + categoryOffset + mc.fontRendererObj.getStringWidth(category.name)
//			&& mouseY < 55 + 10) {
//				selectedCategory = category;
//			}
//			offset += mc.fontRendererObj.getStringWidth(category.name) + 10;
//		}
//		offset = 0;
//		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory)) {
//			if (mouseX > 70 + 3
//				&& mouseY > 75 + offset
//				&& mouseX <  70 + 3 + mc.fontRendererObj.getStringWidth(module.getName())
//				&& mouseY < 75 + offset + 10) {
//				if (mouseButton == 0) {
//					module.toggle();
//				} else if (mouseButton == 1) {
//					selectedModule = module;
//				}
//			}
//			offset += 11;
//		}
//		offset = 0;
//		if (selectedModule == null) return;
////		mc.fontRendererObj.drawString(
////				"KeyBind: " + Keyboard.getKeyName(selectedModule.getKey()),
////				70 + 80,
////				75,
////				-1
////		);
//		if (mouseX > 70 + 80 + mc.fontRendererObj.getStringWidth("KeyBind: ")
//		&& mouseX < 70 + 80 + mc.fontRendererObj.getStringWidth("KeyBind: " + Keyboard.getKeyName(selectedModule.getKey()))
//		&& mouseY > 75
//		&& mouseY < 75 + 10) {
//			binding = true;
//		}
//		for (Setting setting : selectedModule.getSettings()) {
//			if (!setting.isVisible()) continue;
//			int nameLength = mc.fontRendererObj.getStringWidth(setting.getName() + ": ");
//			offset += 11;
//			if (setting instanceof BooleanSetting booleanSetting) {
//				if (mouseX > 70 + 85 + nameLength && mouseX < 70 + 85 + nameLength + mc.fontRendererObj.getStringWidth(String.valueOf(booleanSetting.isToggled()))
//				&& mouseY > 79 + offset && mouseY < 78 + offset + 10) {
//					lastSetting = setting;
//					booleanSetting.setToggled(!booleanSetting.isToggled());
//				}
//			}
//			if (setting instanceof ModeSetting modeSetting) {
//				int xOffset = 0;
//				int yOffset = 0;
//				for (String mode : modeSetting.getModes()) {
//					String gavno2 = mode + (!modeSetting.getModes()[modeSetting.getModes().length - 1].equalsIgnoreCase(mode) ? "," : "");
//					if (70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2) >= 400) {
//						xOffset = 0;
//						yOffset += 11;
//					}
//					/*mc.fontRendererObj.drawString(
//							gavno2,
//							70 + 85 + nameLength + xOffset,
//							79 + offset + yOffset,
//							mode.equalsIgnoreCase(modeSetting.getMode()) ? new Color(0, 255, 209, 255).getRGB() : new Color(75, 75, 75, 179).getRGB(),
//							true
//					);*/
//					if (mouseX > 70 + 85 + nameLength + xOffset
//					&& mouseX < 70 + 85 + nameLength + xOffset + mc.fontRendererObj.getStringWidth(gavno2)
//					&& mouseY > 79 + offset + yOffset
//					&& mouseY < 79 + offset + yOffset + 10) {
//						modeSetting.setMode(mode);
//						lastSetting = setting;
//					}
//					xOffset += mc.fontRendererObj.getStringWidth(gavno2) + 4;
//				}
//				offset += yOffset;
//			}
//		}
//	}
//

}