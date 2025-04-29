package me.hackclient.guis.clickgui;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.TickEvent;
import me.hackclient.guis.config.ConfigGuiScreen;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.*;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.scissor.ScissorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;

import static java.lang.Math.*;

public class ClickGuiScreen extends GuiScreen implements ConditionCallableObject {

	int delay = 10;

	Vector2f pos, size, lastMouse;

	Vector2f lastSize = new Vector2f(200, 200);
	Vector2f lastPos = new Vector2f(200, 200);

	ClickGui clickGui = Client.INSTANCE.getModuleManager().getModule(ClickGui.class);

	Color BACKGROUND_COLOR = new Color(15, 15, 15, clickGui.backgroundAlpha.getValue());
	Color MAIN_COLOR = new Color(255, 255, 209, 255);
	Color HEADER_COLOR = new Color(15, 15, 15, 255);
	Color CATEGORY_COLOR = new Color(255, 255, 255, 255);

	int MAIN_COLOR_INT = MAIN_COLOR.getRGB();

	Category selectedCategory = Category.COMBAT;
	Module selectedModule = null;

	Shadows shadows;

	boolean resizing, moving, binding, closing, showConsoleAfterClose, showConfigAfterClose;

	int scroll;

	// Animations
	final Animation2D moduleLine, settingLine, background, sizeBackground;

	public ClickGuiScreen() {
		callables.add(this);
		lastMouse = new Vector2f(0, 0);
		pos = new Vector2f(100, 100);
		size = new Vector2f(350, 150);

		mc = Minecraft.getMinecraft();

		sizeBackground = new Animation2D();
		background = new Animation2D();
		moduleLine = new Animation2D();
		settingLine = new Animation2D();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int currentScroll = Mouse.getDWheel();
		scroll -= Mouse.getDWheel() / 120 * 10;
		if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
		MAIN_COLOR = clickGui.color.getColor();
		BACKGROUND_COLOR = new Color(15,15,15,clickGui.backgroundAlpha.getValue());
        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 2) {
                closing = false;
                mc.currentScreen.onGuiClosed();
				mc.displayGuiScreen(null);
				if (showConsoleAfterClose) {
					mc.currentScreen = null;
					mc.displayGuiScreen(Client.INSTANCE.getConsole());
					showConsoleAfterClose = false;
				}
				if (showConfigAfterClose) {
					mc.currentScreen = null;
					mc.displayGuiScreen(new ConfigGuiScreen());
					showConfigAfterClose = false;
				}
            }
        }

		String name = switch (delay) {
			case 0, 1, 2 -> "AutoTool_";
			case 3, 4 -> "AutoToo";
			case 5, 6 -> "AutoTo";
			case 7, 8 -> "AutoT";
			case 9, 10 -> "Auto";
			case 11, 12, 13 -> "Aut";
			case 14, 15, 16 -> "Au";
			case 17, 18, 19 -> "A";
			case 20 -> "_";
			default -> "§k" + "AutoTool".substring(0, min(delay - 20, 8));
		};

		MAIN_COLOR_INT = MAIN_COLOR.getRGB();
				Client.INSTANCE.getModuleManager().modules.sort(
				(o1, o2) -> {
					int width1 = mc.fontRendererObj.getStringWidth(o1.getName());
					int width2 = mc.fontRendererObj.getStringWidth(o2.getName());

					return Integer.compare(width2, width1);
				}
		);

		if (resizing
		&& mouseX > pos.x + 100
		&& mouseY > background.y + 100) {
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

		float widthConsole = fontRenderer.getStringWidth("Console") / 2f;
		float widthConfig = fontRenderer.getStringWidth("Config") / 2f;

		background.endX = pos.x;
		background.endY = pos.y;
		sizeBackground.endX = size.x;
		sizeBackground.endY = size.y;

		background.update(15f);
		sizeBackground.update(15f);

		ScaledResolution sc = new ScaledResolution(mc);

		if (shadows.isToggled() && shadows.clickGui.isToggled()) {
			BloomUtils.addToDraw(() -> {
				RoundedUtils.drawRect(5, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.black);
				RoundedUtils.drawRect(5 + 60, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.black);
				RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), Color.black);
			});
		}

		RoundedUtils.drawRect(5, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), BACKGROUND_COLOR);
		RoundedUtils.drawRect(5 + 60, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), BACKGROUND_COLOR);

		fontRenderer.drawString("Console", 5 + 25 - widthConsole, sc.getScaledHeight() - 15 - 1, -1);
		fontRenderer.drawString("Config", 5 + 60 + 25 - widthConfig, sc.getScaledHeight() - 15 - 1 , -1);

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

		RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), BACKGROUND_COLOR);
		RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, clickGui.backgroundRadius.getValue(), HEADER_COLOR);
		RoundedUtils.drawRect(background.x + sizeBackground.x - 5, background.y + sizeBackground.y - 5, 5, 5, 1, BACKGROUND_COLOR);

		fontRenderer.drawString(name, background.x + 35, background.y + 4, CATEGORY_COLOR.getRGB());

		RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
		RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
		RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);

		RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
		RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
		RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

		float widthsModule = 0;
		for (Module module : Client.INSTANCE.getModuleManager().modules) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());

			if (moduleWidth > widthsModule) {
				widthsModule = moduleWidth;
			}
		}
		float verticalLineXOffset = max(clientNameWidth + 14, widthsModule) + 7;

		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory))	{
			fontRenderer.drawString(
					module.getName(),
					background.x + 4,
					background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset,
					module.isToggled() ? MAIN_COLOR_INT : CATEGORY_COLOR.getRGB()
					);
			offset += fontRenderer.FONT_HEIGHT + 2;
			if (module == selectedModule) {
				moduleLine.endY = background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset;
			}
		}

		offset = 0;
		for (Category category : Category.values()) {
			fontRenderer.drawString(
					category.name,
					background.x + verticalLineXOffset + 5 + 5 + offset,
					background.y + 4,
					category == selectedCategory ? MAIN_COLOR.getRGB() : CATEGORY_COLOR.getRGB()
			);
			offset += fontRenderer.getStringWidth(category.name) + 5;
		}

		RoundedUtils.drawRect(background.x + verticalLineXOffset, background.y + 15, 2, sizeBackground.y - 15, 0.2f, BACKGROUND_COLOR);

		offset = 0;
		if (selectedModule != null) {
			RoundedUtils.drawRect(background.x + 2, moduleLine.y - 12, 1, 12, 3, MAIN_COLOR);
			fontRenderer.drawString(
					"Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey())),
					background.x + verticalLineXOffset + 5,
					background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
						CATEGORY_COLOR.getRGB()
					);
			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible()) {
					continue;
				}

				float settingWidth = fontRenderer.getStringWidth(setting.getName() + ": ");
				fontRenderer.drawString(
						setting.getName() + ": ",
						background.x + verticalLineXOffset + 5,
						background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
						CATEGORY_COLOR.getRGB()
				);
				if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					int length = multiBooleanSetting.getValues().size();
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						boolean notLast = value != multiBooleanSetting.getValues().get(length - 1);
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x) {
							xOffset = 0;
							yOffset += 11;
						}
						fontRenderer.drawString(
								mode,
								background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
								background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
								isSelected ? MAIN_COLOR_INT : CATEGORY_COLOR.getRGB()
						);
						if (notLast) {
							fontRenderer.drawString(
									",",
									background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
									background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
									CATEGORY_COLOR.getRGB()
							);
						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof ModeSetting modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						int length = modeSetting.getModes().size();
						boolean isSelected = mode.equals(modeSetting.getMode());
						boolean notLast = !mode.equals(modeSetting.getModes().get(length - 1));
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x) {
							xOffset = 0;
							yOffset += 11;
						}
						fontRenderer.drawString(
								mode,
								background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
								background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
								isSelected ? MAIN_COLOR_INT : CATEGORY_COLOR.getRGB()
						);
						if (notLast) {
							fontRenderer.drawString(
									",",
									background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
									background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
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
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							booleanSetting.isToggled() ? Color.GREEN.darker().getRGB() : Color.RED.getRGB()
					);
				}
				if (setting instanceof IntegerSetting integerSetting) {
					float filledFactor = integerSetting.normalize();
					final float length = 75;
					final float sliderLength = filledFactor * length;
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							length,
							4,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							sliderLength,
							4,
							1.5f,
							MAIN_COLOR
					);
					fontRenderer.drawString(
							String.valueOf(integerSetting.getValue()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							CATEGORY_COLOR.getRGB()
					);

					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth
					&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + length + 1
					&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
					&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 4) {
						if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
							integerSetting.setValue((int) (integerSetting.getValue() + signum(currentScroll)));
						} else if (Mouse.isButtonDown(0)) {
							float mx = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth);
							float p = mx / length;
							float normalize = integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * p;
							integerSetting.setValue(round(normalize));
						}
					}
				}
				if (setting instanceof FloatSetting floatSetting) {
					float filledFactor = floatSetting.normalize();
					final float length = 75;
					final float sliderLength = filledFactor * length;
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							length,
							4,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							sliderLength,
							4,
							1.5f,
							MAIN_COLOR
					);
					fontRenderer.drawString(
							String.format("%.2f", floatSetting.getValue()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							CATEGORY_COLOR.getRGB()
					);

					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth
							&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + length + 1
							&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
							&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 4) {
						if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
							floatSetting.setValue(floatSetting.getValue() + Math.signum(currentScroll) * floatSetting.getStep());
						} else if (Mouse.isButtonDown(0)) {
							float mx = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth);
							float p = mx / length;
							float normalize = floatSetting.getMin() + (floatSetting.getMax() - floatSetting.getMin()) * p;
							floatSetting.setValue(normalize);
						}
					}
				}
				if (setting instanceof ColorSetting colorSetting) {
					if (Mouse.isButtonDown(0)) {
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
						&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + 75
						&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
						&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 3) {
							float deltaX = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth + 1);
							colorSetting.setRed(deltaX / 75);
						}
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
								&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + 75
								&& mouseY > background.y + 5 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
								&& mouseY < background.y + 5 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 3) {
							float deltaX = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth + 1);
							colorSetting.setGreen(deltaX / 75);
						}
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
								&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + 75
								&& mouseY > background.y + 10 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
								&& mouseY < background.y + 10 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 3) {
							float deltaX = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth + 1);
							colorSetting.setBlue(deltaX / 75);
						}
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
								&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + 75
								&& mouseY > background.y + 15 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f
								&& mouseY < background.y + 15 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f + 3) {
							float deltaX = mouseX - (background.x + verticalLineXOffset + 5 + settingWidth + 1);
							colorSetting.setAlpha(deltaX / 75);
						}
					}
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75,
							3,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75 * colorSetting.getRed(),
							3,
							1.5f,
							Color.red
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 5 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75,
							3,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 5 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75 * colorSetting.getGreen(),
							3,
							1.5f,
							Color.green
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 10 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75,
							3,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 10 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75 * colorSetting.getBlue(),
							3,
							1.5f,
							Color.blue
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 15 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75,
							3,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 15 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							75 * colorSetting.getAlpha(),
							3,
							1.5f,
							Color.white
					);
					RoundedUtils.drawRect(
							background.x + 80 + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 - 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							22,
							22,
							1.5f,
							BACKGROUND_COLOR
					);
					RoundedUtils.drawRect(
							background.x + 80 + 1 + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 - 1 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f,
							20,
							20,
							1.5f,
							colorSetting.getColor()
					);
					offset += 15;
				}
				offset += 11;
			}
		}
		ScissorUtils.disableScissor();

		moduleLine.update(clickGui.animationSpeed.getValue());
		settingLine.update(clickGui.animationSpeed.getValue());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
		RoundedUtils.drawRect(5, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), BACKGROUND_COLOR);
		RoundedUtils.drawRect(5 + 55, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), BACKGROUND_COLOR);

		boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean console = mouseX > 5 && mouseX < 50 && mouseY > sc.getScaledHeight() - 20 && mouseY < sc.getScaledHeight() - 5;
		boolean config = mouseX > 5 + 60 && mouseX < 50 + 60 && mouseY > sc.getScaledHeight() - 20 && mouseY < sc.getScaledHeight() - 5;

		if (Mouse.isButtonDown(0)) {
			if (quit) {
				lastPos.set(pos);
				lastSize.set(size);
                closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
			}

			if (fullscreen) {
				size.set(sc.getScaledWidth() - 10, sc.getScaledHeight() - 10);
				pos.set(5f, 5f);
			}

			if (collapse) {
				size.set(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
				pos.set(50f, 50f);
			}

			if (console) {
				showConsoleAfterClose = true;
				lastPos.set(pos);
				lastSize.set(size);
				closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
			}
			if (config) {
				showConfigAfterClose = true;
				lastPos.set(pos);
				lastSize.set(size);
				closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
			}
		}

		if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y) return;

		final FontRenderer fontRenderer = mc.fontRendererObj;
		final float clientNameWidth = fontRenderer.getStringWidth(Client.INSTANCE.getName());
		float offset = 0;

		boolean resize = mouseX > background.x + sizeBackground.x - 5 && mouseX < background.x + sizeBackground.x && mouseY > background.y + sizeBackground.y - 5 && mouseY < background.y + sizeBackground.y;

		if (resize) {
			resizing = true;
			lastMouse.set(mouseX, mouseY);
		}

		float widthsModule = 0;
		for (Module module : Client.INSTANCE.getModuleManager().modules) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());

			if (moduleWidth > widthsModule) {
				widthsModule = moduleWidth;
			}
		}
		float verticalLineXOffset = max(clientNameWidth, widthsModule) + 5;

		boolean move = mouseX > background.x && mouseX < background.x + verticalLineXOffset && mouseY > background.y && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT;

		if (move) {
			if (quit || fullscreen || collapse) return;
			moving = true;
			lastMouse.set(mouseX, mouseY);
		}

		for (Module module : Client.INSTANCE.getModuleManager().getModulesByCategory(selectedCategory))	{
			float moduleWidth = fontRenderer.getStringWidth(module.getName());
			boolean moduleCondition = mouseX > background.x + 3 && mouseX < background.x + 3 + moduleWidth && mouseY > background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset && mouseY < background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset + 9;
			if (moduleCondition) {
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
			boolean selectCategory = mouseX > background.x + verticalLineXOffset + 5 + 5 + offset && mouseX < background.x + verticalLineXOffset + 5 + 5 + offset + fontRenderer.getStringWidth(category.name) && mouseY > background.y + 2 && mouseY < background.y + 2 + fontRenderer.FONT_HEIGHT;
			if (selectCategory) {
				selectedCategory = category;
				selectedModule = null;
				moduleLine.x = 0;
				moduleLine.y = 0;
			}
			offset += fontRenderer.getStringWidth(category.name) + 5;
		}

		offset = 0;
		if (selectedModule != null) {
			boolean bind = mouseX > background.x + verticalLineXOffset + 5 && mouseX < background.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey()))) && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
            if (bind) binding = true;

			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible())
					continue;

				float settingWidth = fontRenderer.getStringWidth(setting.getName() + ": ");
				fontRenderer.drawString(
						setting.getName() + ": ",
						background.x + verticalLineXOffset + 5,
						background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
						-1
				);
				if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x) {
							xOffset = 0;
							yOffset += 11;
						}
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset
						&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode)
						&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset
						&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset + 10) {
							multiBooleanSetting.set(mode, !isSelected);
						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof ModeSetting modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x) {
							xOffset = 0;
							yOffset += 11;
						}
						if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset
						&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode)
						&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset
						&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset + 10) {
							modeSetting.setMode(mode);
						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof BooleanSetting booleanSetting) {
					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
					&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + fontRenderer.getStringWidth(String.valueOf(booleanSetting.isToggled()))
					&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset
					&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + 10) {
						booleanSetting.setToggled(!booleanSetting.isToggled());
					}
				}
				if (setting instanceof ColorSetting) {
					offset += 14;
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

		if (keyCode == Keyboard.KEY_ESCAPE && !closing) {
			ScaledResolution sc = new ScaledResolution(mc);
			lastPos.set(pos);
			lastSize.set(size);
			closing = true;
			size.set(0, 0);
			pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
			return;
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void onGuiClosed() {
		Client.INSTANCE.getModuleManager().getModule(ClickGui.class).setToggled(false);
	}

	@Override
	public void initGui() {
		sizeBackground.reset();
		background.reset();
		pos.set(lastPos);
		size.set(lastSize);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof TickEvent) {
			if (delay > 0) {
				delay--;
				return;
			}
			if (delay == 0) delay = 30;
		}
	}

	@Override
	public boolean handleEvents() {
		return mc.thePlayer != null && mc.theWorld != null && mc.currentScreen instanceof ClickGuiScreen;
	}
}