package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Blur;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;

import static java.lang.Math.*;

public class ClickGuiScreen extends GuiScreen {

	int delay = 10;

	Vector2f pos, size, lastMouse, lastSize,lastPos;

	ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

	Color BACKGROUND_COLOR = new Color(0,0,0, clickGui.backgroundAlpha.getValue());
	Color MAIN_COLOR = new Color(255, 255, 209, 255);
	Color HEADER_COLOR = new Color(15, 15, 15, 255);
	Color CATEGORY_COLOR = new Color(255, 255, 255, 255);

	int MAIN_COLOR_INT = MAIN_COLOR.getRGB();

	KeyBind activeKeyBind;

	Category selectedCategory = Category.COMBAT;
	Module selectedModule = null;

	Shadows shadows;
	Blur blur;

	boolean resizing, moving, binding, closing, showConsoleAfterClose, showConfigAfterClose;

	int settingsScroll, settingsTotalHeight, modulesScroll, modulesTotalHeight;

	final Animation2D moduleLine, settingLine, background, sizeBackground, settingsScrolls, modulesScrolls;

	public ClickGuiScreen() {
		lastMouse = new Vector2f(0, 0);
		mc = Minecraft.getMinecraft();

		ScaledResolution sc = new ScaledResolution(mc);
		lastSize = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
		lastPos = new Vector2f(50f, 50f);

		size = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
		pos = new Vector2f(50f, 50f);

		sizeBackground = new Animation2D();
		background = new Animation2D();
		moduleLine = new Animation2D();
		settingLine = new Animation2D();
		settingsScrolls = new Animation2D();
		modulesScrolls = new Animation2D();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
		if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
		int currentScroll = Mouse.getDWheel();

		if (this.clickGui.fadeColor.isToggled()) {
			MAIN_COLOR = ColorUtils.fadeColor(clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue());
		} else {
			MAIN_COLOR = clickGui.color1.getColor();
		}

		BACKGROUND_COLOR = new Color(0,0,0,clickGui.backgroundAlpha.getValue());

        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 2) {
                closing = false;
                mc.currentScreen.onGuiClosed();
				mc.displayGuiScreen(null);
				if (showConsoleAfterClose) {
					mc.currentScreen = null;
					mc.displayGuiScreen(Client.INST.getConsole());
					showConsoleAfterClose = false;
				}
				if (showConfigAfterClose) {
					mc.currentScreen = null;
					mc.displayGuiScreen(Client.INST.getConfigGuiScreen());
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

		Client.INST.getModuleManager().getModules().sort((o1, o2) -> {
			int width1 = mc.fontRendererObj.getStringWidth(o1.getName());
			int width2 = mc.fontRendererObj.getStringWidth(o2.getName());

			return Integer.compare(width2, width1);
		});

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
		final float clientNameWidth = fontRenderer.getStringWidth(Client.INST.getName());

		float widthConsole = fontRenderer.getStringWidth("Console") / 2f;
		float widthConfig = fontRenderer.getStringWidth("Config") / 2f;

		modulesScrolls.endY	= modulesScroll;
		settingsScrolls.endY = settingsScroll;
		background.endX = pos.x;
		background.endY = pos.y;
		sizeBackground.endX = size.x;
		sizeBackground.endY = size.y;

		background.update(15f);
		sizeBackground.update(15f);
		modulesScrolls.update(15f);
		settingsScrolls.update(15f);

		ScaledResolution sc = new ScaledResolution(mc);

		if (shadows.isToggled() && shadows.module.get("ClickGui")) {
			BloomUtils.addToDraw(() -> {
				RoundedUtils.drawRect(5, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.black);
				RoundedUtils.drawRect(5 + 60, sc.getScaledHeight() - 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.black);
				RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), Color.black);
			});
		}
		if (blur.isToggled() && blur.module.get("ClickGui")) {
			GaussianBlurUtils.addToDraw(() -> {
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
		StencilUtils.renderStencil(
				() -> {
					RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 10, clickGui.backgroundRadius.getValue(), HEADER_COLOR);
				},
				() -> {
					RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 1f, HEADER_COLOR);
				}
		);
		RoundedUtils.drawRect(background.x, background.y + 5, sizeBackground.x, 10, 1f, HEADER_COLOR);
		RoundedUtils.drawRect(background.x + sizeBackground.x - 5, background.y + sizeBackground.y - 5, 5, 5, 1, BACKGROUND_COLOR);

		fontRenderer.drawString(name, background.x + 35, background.y + 4, CATEGORY_COLOR.getRGB());

		RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
		RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
		RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);

		RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
		RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
		RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

		ScissorUtils.disableScissor();

		float widthsModule = 0;
		for (Module module : Client.INST.getModuleManager().getModules()) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());
			if (moduleWidth > widthsModule) widthsModule = moduleWidth;
		}

		float verticalLineXOffset = max(clientNameWidth + 14, widthsModule) + 7;

		boolean settingScroll = mouseX > background.x + verticalLineXOffset + 5 && mouseX < background.x + verticalLineXOffset + 5 + sizeBackground.x - verticalLineXOffset - 5 && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5 + sizeBackground.y - (2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5);
		boolean moduleScroll = mouseX > background.x && mouseX < background.x + widthsModule && mouseY > background.y + 15 && mouseY < background.y + sizeBackground.y;

		if (settingScroll) {
			if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) settingsScroll -= currentScroll / 120 * 10;

			float settingsVisibleHeight = sizeBackground.y - (2 + 2 + fontRendererObj.FONT_HEIGHT + 10 + 5);
			float maxScroll = Math.max(settingsTotalHeight - settingsVisibleHeight,0);

			if (settingsScroll > 0) settingsScroll = 0;
			if (settingsScroll < -maxScroll) settingsScroll = (int) -maxScroll;
		}

		if (moduleScroll) {
			if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) modulesScroll -= currentScroll / 120 * 10;

			float moduleVisibleHeight = sizeBackground.y - 18;
			float maxScroll = Math.max(modulesTotalHeight - moduleVisibleHeight, 0);

			if (modulesScroll > 0) modulesScroll = 0;
			if (modulesScroll < -maxScroll) modulesScroll = (int) -maxScroll;
		}

		float offset = modulesScrolls.y;

		modulesTotalHeight = 0;

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15, widthsModule + 2, sizeBackground.y - 15);

		for (Module module : Client.INST.getModuleManager().getModulesByCategory(selectedCategory))	{
			fontRenderer.drawString(
					module.getName(),
					background.x + 4,
					background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset,
					module.isToggled() ? MAIN_COLOR_INT : CATEGORY_COLOR.getRGB()
					);
			offset += fontRenderer.FONT_HEIGHT + 2;
			modulesTotalHeight += fontRenderer.FONT_HEIGHT + 2;
			if (module == selectedModule) moduleLine.endY = background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset;
		}

		if (selectedModule != null) RoundedUtils.drawRect(background.x + 2, moduleLine.y - 12, 1, 12, 3, MAIN_COLOR);

		ScissorUtils.disableScissor();

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

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

		offset = settingsScrolls.y;

		settingsTotalHeight = 0;
		if (selectedModule != null) {
			fontRenderer.drawString(
					"Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey())),
					background.x + verticalLineXOffset + 5,
					background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
						CATEGORY_COLOR.getRGB()
					);
			ScissorUtils.enableScissor();
			ScissorUtils.scissor(new ScaledResolution(mc), background.x + verticalLineXOffset + 5, background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5, sizeBackground.x - verticalLineXOffset - 5, sizeBackground.y - (2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5));
			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible()) continue;

                float settingWidth = fontRenderer.getStringWidth(setting.getName() + ": ");
				fontRenderer.drawString(
						setting.getName() + ": ",
						background.x + verticalLineXOffset + 5,
						background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
						CATEGORY_COLOR.getRGB()
				);
				if (setting instanceof MultiMode multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					int length = multiBooleanSetting.getValues().size();
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						boolean notLast = value != multiBooleanSetting.getValues().get(length - 1);
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
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
					settingsTotalHeight += (int) yOffset;
				}
				if (setting instanceof Mode modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						int length = modeSetting.getModes().size();
						boolean isSelected = mode.equals(modeSetting.getMode());
						boolean notLast = !mode.equals(modeSetting.getModes().get(length - 1));
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
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
					settingsTotalHeight += (int) yOffset;
				}
				if (setting instanceof CheckBox booleanSetting) {
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
					settingsTotalHeight += 15;
				}
				if (setting instanceof KeyBind keyBind) {
					fontRenderer.drawString(
							activeKeyBind == keyBind ? "▬" : Keyboard.getKeyName(keyBind.getKey()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset, MAIN_COLOR.getRGB()
					);
				}
				offset += 11;
				settingsTotalHeight += 11;
			}
			ScissorUtils.disableScissor();
		} else {
			ResourceLocation image = new ResourceLocation("minecraft", "hackclient/image/modulenull.png");
			RenderUtils.drawImage(image, (int) (background.x + verticalLineXOffset + 5), (int) background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6, 250, 250);
		}
		ScissorUtils.disableScissor();

		moduleLine.update(clickGui.animationSpeed.getValue());
		settingLine.update(clickGui.animationSpeed.getValue());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());

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
		final float clientNameWidth = fontRenderer.getStringWidth(Client.INST.getName());

		boolean resize = mouseX > background.x + sizeBackground.x - 5 && mouseX < background.x + sizeBackground.x && mouseY > background.y + sizeBackground.y - 5 && mouseY < background.y + sizeBackground.y;

		if (resize) {
			resizing = true;
			lastMouse.set(mouseX, mouseY);
		}

		float widthsModule = 0;
		for (Module module : Client.INST.getModuleManager().getModules()) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());
			if (moduleWidth > widthsModule) widthsModule = moduleWidth;
		}

		float verticalLineXOffset = max(clientNameWidth, widthsModule) + 5;

		boolean move = mouseX > background.x && mouseX < background.x + verticalLineXOffset && mouseY > background.y && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT;

		if (move) {
			if (quit || fullscreen || collapse) return;
			moving = true;
			lastMouse.set(mouseX, mouseY);
		}

		float offset = modulesScrolls.y;

		for (Module module : Client.INST.getModuleManager().getModulesByCategory(selectedCategory))	{
			float moduleWidth = fontRenderer.getStringWidth(module.getName());
			boolean moduleCondition = mouseX > background.x + 3 && mouseX < background.x + 3 + moduleWidth && mouseY > background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset && mouseY < background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset + 9;
			if (moduleCondition) {
				switch (mouseButton) {
					case 0 -> module.toggle();
					case 1 -> {
						settingsScrolls.y = -400;
						settingsScroll = 0;
						selectedModule = module;
					}
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
			switch (mouseButton) {
				case 0 -> {
					if (selectCategory) {
						selectedCategory = category;
						selectedModule = null;
						moduleLine.x = 0;
						moduleLine.y = 0;
					}
				}
				case 1 -> {
					if (selectCategory) {

					}
				}
			}

			offset += fontRenderer.getStringWidth(category.name) + 5;
		}

		offset = settingsScrolls.y;
		if (selectedModule != null) {
			boolean bind = mouseX > background.x + verticalLineXOffset + 5 && mouseX < background.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey()))) && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
			//boolean hide = mouseX > background.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Hide: " + (selectedModule.isHide())) + 5 + 10 && mouseX < background.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Hide: " + (selectedModule.isHide())) + 5 + 10 + fontRenderer.getStringWidth("Hide: " + (selectedModule.isHide())) + 5 && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
            if (bind) binding = true;
			//if (hide) selectedModule.setHide(!selectedModule.isHide());

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

				if (setting instanceof MultiMode multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
							xOffset = 0;
							yOffset += 11;
						}
						boolean clickMode = mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset && mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset + 10;
						if (clickMode) {
							multiBooleanSetting.set(mode, !isSelected);
						}
						if (clickMode && Mouse.isButtonDown(0) && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
							multiBooleanSetting.set(mode, !isSelected);
						}
						xOffset += fontRenderer.getStringWidth(mode) + 5;
					}
					offset += yOffset;
				}
				if (setting instanceof Mode modeSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (String mode : modeSetting.getModes()) {
						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
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
				if (setting instanceof CheckBox booleanSetting) {
					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
							&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + fontRenderer.getStringWidth(String.valueOf(booleanSetting.isToggled()))
							&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset
							&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + 10) {
						booleanSetting.setToggled(!booleanSetting.isToggled());
					}
				}
				if (setting instanceof KeyBind keyBind) {
					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth + 1
							&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + 1 + fontRenderer.getStringWidth(activeKeyBind == keyBind ? "▬" : Keyboard.getKeyName(keyBind.getKey()))
							&& mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset
							&& mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + 10) {
						activeKeyBind = keyBind;
					}
				}
				if (setting instanceof ColorSetting) offset += 14;
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
		if (selectedModule == null) binding = false;

		if (binding) {
			binding = false;
			if (keyCode == Keyboard.KEY_ESCAPE) {
				selectedModule.setKey(Keyboard.KEY_NONE);
				return;
			}
			selectedModule.setKey(keyCode);
		} else if (activeKeyBind != null) {
			if (keyCode == Keyboard.KEY_ESCAPE) activeKeyBind.setKey(0);else activeKeyBind.setKey(keyCode);
			activeKeyBind = null;
		}

		if (keyCode == Keyboard.KEY_ESCAPE && !closing) {
			ScaledResolution sc = new ScaledResolution(mc);
			lastPos.set(pos);
			lastSize.set(size);
			closing = true;
			size.set(0, 0);
			pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
        }
	}

	@Override
	public void onGuiClosed() {
		Client.INST.getEventManager().unregister(this);
		Client.INST.getConfigManager().saveAsync(Client.INST.getConfigManager().getDefaultConfig());
	}

	@Override
	public void initGui() {
		Client.INST.getEventManager().register(this);
		sizeBackground.reset();
		background.reset();
		pos.set(lastPos);
		size.set(lastSize);
	}

	@EventTarget
	public void onEvent(Event event) {
		if (event instanceof TickEvent) {
			if (delay > 0) {
				delay--;
				return;
			}
			if (delay == 0) delay = 30;
		}
	}
}