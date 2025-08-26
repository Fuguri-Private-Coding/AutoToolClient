package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Blur;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.lang.Math.*;

public class ClickGuiScreen extends GuiScreen {

	int delay = 10;

	Vector2f pos, size, lastMouse, lastSize, lastPos, clickedCategoryPos, clickedModulePos;

	ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);
	
	ClientSettings clientSettings = Client.INST.getModuleManager().getModule(ClientSettings.class);

	Color BACKGROUND_COLOR = new Color(0,0,0, clickGui.backgroundAlpha.getValue());
	public Color MAIN_COLOR = new Color(255, 255, 209, 255);
	Color HEADER_COLOR = new Color(15, 15, 15, 255);
	Color CATEGORY_COLOR = new Color(255, 255, 255, 255);

	int MAIN_COLOR_INT = MAIN_COLOR.getRGB();

	KeyBind activeKeyBind;

	Category selectedCategory = Category.COMBAT;
	Module selectedModule = null;

	Category clickedCategory;
	Module clickedModule;

	boolean resizing, moving, binding, closing, showConsoleAfterClose, showConfigAfterClose, showHotKeysAfterClose;

	int settingsScroll, settingsTotalHeight, modulesScroll, modulesTotalHeight;

	final Animation2D settingLine, background, sizeBackground, modulesScrolls;

	EasingAnimation guis = new EasingAnimation();
	EasingAnimation moduleLine = new EasingAnimation();
	EasingAnimation settingsScrolls = new EasingAnimation();

	public ClickGuiScreen() {
		lastMouse = new Vector2f(0, 0);
		mc = Minecraft.getMinecraft();

		ScaledResolution sc = new ScaledResolution(mc);
		lastSize = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
		lastPos = new Vector2f(50f, 50f);
		clickedCategoryPos = new Vector2f();
		clickedModulePos = new Vector2f();

		size = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
		pos = new Vector2f(50f, 50f);

		sizeBackground = new Animation2D();
		background = new Animation2D();
		settingLine = new Animation2D();
		modulesScrolls = new Animation2D();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int currentScroll = Mouse.getDWheel();

		MAIN_COLOR = clickGui.color.isFade() ?
				ColorUtils.fadeColor(clickGui.color.getColor(), clickGui.color.getFadeColor(), clickGui.color.getSpeed())
				: clickGui.color.getColor();

		BACKGROUND_COLOR = new Color(0,0,0,clickGui.backgroundAlpha.getValue());

        if (closing) {
			moduleLine.setEnd(0);
			moduleLine.update(5, Easing.OUT_BACK);
			guis.setEnd(0);
			guis.update(3, Easing.OUT_BACK);
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 2) {
				closing = false;
				clickedModule = null;
				clickedCategory = null;
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
				if (showHotKeysAfterClose) {
					mc.currentScreen = null;
					mc.displayGuiScreen(Client.INST.getHotTextGui());
					showHotKeysAfterClose = false;
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
			background.translatePos(mouseX - lastMouse.x, mouseY - lastMouse.y);
			lastMouse.set(mouseX, mouseY);
		}

		final ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("MuseoSans");
		final float clientNameWidth = (float) fontRenderer.getStringWidth(Client.INST.getName());

		modulesScrolls.endY	= modulesScroll;
		settingsScrolls.setEnd(settingsScroll);
		background.endX = pos.x;
		background.endY = pos.y;
		sizeBackground.endX = size.x;
		sizeBackground.endY = size.y;

		background.update(15f);
		sizeBackground.update(15f);
		modulesScrolls.update(15f);
		settingsScrolls.update(15f / 4, Easing.OUT_BACK);

		ScaledResolution sc = new ScaledResolution(mc);

		if (clickGui.glow.isToggled()) {
			BloomRealUtils.addToDraw(() -> {
				RenderUtils.drawMixedRoundedRect(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
				RenderUtils.drawMixedRoundedRect(sc.getScaledWidth() / 2f - 25, sc.getScaledHeight() - 10 + guis.getValue(), 50, 2, 0, clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
				RenderUtils.drawMixedRoundedRect(sc.getScaledWidth() / 2f - 50, sc.getScaledHeight() - guis.getValue(), 100, 20, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
			});
		}

		if (clickGui.blur.isToggled()) {
			GaussianBlurUtils.addToDraw(() -> {
				RenderUtils.drawMixedRoundedRect(sc.getScaledWidth() / 2f - 50, sc.getScaledHeight() - guis.getValue(), 100, 20, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
				RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), Color.black);
			});
		}

		RenderUtils.drawRoundedOutLineRectangle(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue() * 1.7f, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

		RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 0,clientSettings.backgroundRadius.getValue() / 1.25f,clientSettings.backgroundRadius.getValue() / 1.25f,0, HEADER_COLOR);
		RoundedUtils.drawRect(background.x + sizeBackground.x - 5, background.y + sizeBackground.y - 5, 5, 5, 0f, 4f,0f,clientSettings.backgroundRadius.getValue() / 1.25f, Color.BLACK);

		fontRenderer.drawString(name, background.x + 35, background.y + 4 + 1, CATEGORY_COLOR);

		boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;

		RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, quit ? Color.WHITE : Color.BLACK);
		RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, fullscreen ? Color.WHITE : Color.BLACK);
		RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, collapse ? Color.WHITE : Color.BLACK);

		RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
		RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
		RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

		ScissorUtils.disableScissor();

		float widthsModule = 0;
		for (Module module : Client.INST.getModuleManager().getModules()) {
			float moduleWidth = (float) fontRenderer.getStringWidth(module.getName() + (!module.isHide() ? " ✓" : " ×"));
			if (moduleWidth > widthsModule) widthsModule = moduleWidth;
		}

		float verticalLineXOffset = max(clientNameWidth + 14, widthsModule) + 7;

		boolean settingScroll = mouseX > background.x + verticalLineXOffset + 5 && mouseX < background.x + verticalLineXOffset + 5 + sizeBackground.x - verticalLineXOffset - 5 && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5 + sizeBackground.y - (2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5);
		boolean moduleScroll = mouseX > background.x && mouseX < background.x + widthsModule && mouseY > background.y + 15 && mouseY < background.y + sizeBackground.y;

		if (settingScroll) {
			if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				int scrollValue = currentScroll / 120 * clientSettings.scroll.getValue();
				settingsScroll -= clientSettings.invertScroll.isToggled() ? -scrollValue : scrollValue;
			}

			float settingsVisibleHeight = sizeBackground.y - (2 + 2 + fontRendererObj.FONT_HEIGHT + 10 + 5);
			float maxScroll = Math.max(settingsTotalHeight - settingsVisibleHeight,0);

			if (settingsScroll > 0) settingsScroll = 0;
			if (settingsScroll < -maxScroll) settingsScroll = (int) -maxScroll;
		}

		if (moduleScroll) {
			if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				int scrollValue = currentScroll / 120 * clientSettings.scroll.getValue();
				modulesScroll -= clientSettings.invertScroll.isToggled() ? -scrollValue : scrollValue;
			}

			float moduleVisibleHeight = sizeBackground.y - 18;
			float maxScroll = Math.max(modulesTotalHeight - moduleVisibleHeight, 0);

			if (modulesScroll > 0) modulesScroll = 0;
			if (modulesScroll < -maxScroll) modulesScroll = (int) -maxScroll;
		}

		float offset = modulesScrolls.y;

		modulesTotalHeight = 0;

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15, widthsModule + 5, sizeBackground.y - 15);

		List<Module> moduleList = Client.INST.getModuleManager().getModulesByCategory(selectedCategory);

		for (Module module : moduleList) {
			fontRenderer.drawString(
					module.getName() + (!module.isHide() ? " ✓" : " ×"),
					background.x + 4,
					background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset + 2,
					module.isToggled() ? MAIN_COLOR : CATEGORY_COLOR
			);
			offset += fontRenderer.FONT_HEIGHT + 2;
			modulesTotalHeight += fontRenderer.FONT_HEIGHT + 2;
			if (module == selectedModule) moduleLine.setEnd(background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset);
		}

		if (selectedModule != null) RoundedUtils.drawRect(background.x + 2, moduleLine.getValue() - 12 + 3, 0.05f, 6, 0, MAIN_COLOR);
		ScissorUtils.disableScissor();

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

		offset = 0;
		for (Category category : Category.values()) {
			fontRenderer.drawString(
					category.name,
					background.x + verticalLineXOffset + 5 + 5 + offset,
					background.y + 4 + 1,
					category == selectedCategory ? MAIN_COLOR : CATEGORY_COLOR
			);
			offset += (float) (fontRenderer.getStringWidth(category.name) + 5);
		}

		RoundedUtils.drawRect(background.x + verticalLineXOffset, background.y + 15, 0.05f, sizeBackground.y - 15, 0f, Color.BLACK);

		offset = settingsScrolls.getValue();

		settingsTotalHeight = 0;
		if (selectedModule != null) {
			fontRenderer.drawString(
					"Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey())),
					background.x + verticalLineXOffset + 5,
					background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
						CATEGORY_COLOR
			);
			fontRenderer.drawString(
					"Hide: " + selectedModule.isHide(),
					background.x + sizeBackground.x - 5 - fontRenderer.getStringWidth("Hide: " + selectedModule.isHide()),
					background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
						CATEGORY_COLOR
			);
			fontRenderer.drawString(
					"LoadFromConfig: " + selectedModule.isLoadFromConfig(),
					background.x + sizeBackground.x - 5 - fontRenderer.getStringWidth("Hide: " + selectedModule.isHide()) - fontRenderer.getStringWidth("LoadFromConfig: " + selectedModule.isLoadFromConfig()) - 5,
					background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 6,
					CATEGORY_COLOR
			);
			ScissorUtils.enableScissor();
			ScissorUtils.scissor(new ScaledResolution(mc), background.x + verticalLineXOffset + 5, background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5, sizeBackground.x - verticalLineXOffset - 5, sizeBackground.y - (2 + 2 + fontRenderer.FONT_HEIGHT + 10 + 5));
			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible()) continue;

                float settingWidth = (float) fontRenderer.getStringWidth(setting.getName() + " ");
				fontRenderer.drawString(
						setting.getName() + " ",
						background.x + verticalLineXOffset + 5,
						background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset - 0.5f,
						CATEGORY_COLOR
				);
				if (setting instanceof MultiMode multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					int length = multiBooleanSetting.getValues().size();

					multiBooleanSetting.updateAnimation();

					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean notLast = value != multiBooleanSetting.getValues().get(length - 1);

						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
							xOffset = 0;
							yOffset += 11;
						}

						multiBooleanSetting.setUnselectedColor(Color.WHITE);
						multiBooleanSetting.setSelectedColor(MAIN_COLOR);

						Color modeColor = multiBooleanSetting.getModeColor(value);

						fontRenderer.drawString(
								mode,
								background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
								background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
								modeColor
						);

						if (notLast) {
							fontRenderer.drawString(
									",",
									background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
									background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
									CATEGORY_COLOR
							);
						}
						xOffset += (float) (fontRenderer.getStringWidth(mode) + 5);
					}
					offset += yOffset;
					settingsTotalHeight += (int) yOffset;
				}
				if (setting instanceof Mode modeSetting) {
					float xOffset = 0;
					float yOffset = 0;

					modeSetting.updateAnimation();

					for (String mode : modeSetting.getModes()) {
						int length = modeSetting.getModes().size();
						boolean notLast = !mode.equals(modeSetting.getModes().get(length - 1));

						if (background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= background.x + sizeBackground.x - 10) {
							xOffset = 0;
							yOffset += 11;
						}

						modeSetting.setUnselectedColor(Color.WHITE);
						modeSetting.setSelectedColor(MAIN_COLOR);

						Color modeColor = modeSetting.getModeColor(mode);

						fontRenderer.drawString(
								mode,
								background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset,
								background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
								modeColor
						);

						if (notLast) {
							fontRenderer.drawString(
									",",
									background.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode),
									background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset,
									Color.WHITE
							);
						}
						xOffset += (float) (fontRenderer.getStringWidth(mode) + 5);
					}
					offset += yOffset;
					settingsTotalHeight += (int) yOffset;
				}
				if (setting instanceof CheckBox booleanSetting) {
					float toggleProgress = booleanSetting.getToggleProgress();
					Color toggleColor = ColorUtils.interpolateColor(Color.RED, Color.GREEN.darker(), toggleProgress);

					fontRenderer.drawString(
							String.valueOf(booleanSetting.isToggled()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							toggleColor
					);

					booleanSetting.updateToggleAnimation();
				}
				if (setting instanceof IntegerSetting integerSetting) {
					integerSetting.updateAnimation();

					float animatedFilledFactor = integerSetting.getAnimatedNormalize();
					final float length = 75;
					final float sliderLength = animatedFilledFactor * length;

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

					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + sliderLength - 2,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 3.5f,
							6,
							6,
							3f,
							Color.WHITE
					);

					fontRenderer.drawString(
							String.valueOf(integerSetting.getValue()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 6,
							background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							CATEGORY_COLOR
					);

					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth - 5
							&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + length + 5
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
					floatSetting.updateAnimation();

					float animatedFilledFactor = floatSetting.getAnimatedNormalize();
					final float length = 75;
					final float sliderLength = animatedFilledFactor * length;

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

					RoundedUtils.drawRect(
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + sliderLength - 2,
							background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 3.5f,
							6,
							6,
							3f,
							Color.WHITE
					);

					fontRenderer.drawString(
							String.format("%.2f", floatSetting.getValue()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1 + length + 6,
							background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset,
							CATEGORY_COLOR
					);

					if (mouseX > background.x + verticalLineXOffset + 5 + settingWidth - 5
							&& mouseX < background.x + verticalLineXOffset + 5 + settingWidth + length + 5
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
					float startY = background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f;
					float sliderX = background.x + verticalLineXOffset + 5 + 32.5f + 1;
					float sliderWidth = 75;
					float sliderHeight = 4;
					float verticalSpacing = 3;
					float previewSize = 12;

					int currentOffset = 0;

                    float fadeButtonY = startY + currentOffset;

					float triangleX = background.x + verticalLineXOffset + 5 + settingWidth + 1;
					float triangleY = fadeButtonY - 1;

					Color color = new Color(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), 1f);
					Color fadeColor = new Color(colorSetting.getFadeRed(), colorSetting.getFadeGreen(), colorSetting.getFadeBlue(), 1f);

					colorSetting.setTargetRadius(colorSetting.isHide());
					colorSetting.updateAnimation();

					Vector4f triangleRadius = colorSetting.getAnimatedRadius();
					RoundedUtils.drawRect(
							triangleX,
							triangleY,
							6,
							6,
							triangleRadius.x, triangleRadius.y, triangleRadius.z, triangleRadius.w,
							colorSetting.isFade() ? ColorUtils.fadeColor(color, fadeColor, colorSetting.getSpeed()) : color
					);

					if (colorSetting.isHide()) {
						offset += 14;
						settingsTotalHeight += 14;
						continue;
					} else {
						fontRenderer.drawString(
								"Fade: ",
								background.x + verticalLineXOffset + 5 + 3,
								fadeButtonY + 12,
								Color.WHITE
						);

						fontRenderer.drawString(
								String.valueOf(colorSetting.isFade()),
								background.x + verticalLineXOffset + 5 + 3 + fontRenderer.getStringWidth("Fade: "),
								fadeButtonY + 12,
								colorSetting.isFade() ? Color.GREEN.darker() : Color.RED
						);

						currentOffset += (int) (fontRenderer.FONT_HEIGHT + verticalSpacing) + 10;

						Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};
						float[] values = {colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), colorSetting.getAlpha()};

						for (int i = 0; i < 4; i++) {
							float sliderY = startY + currentOffset;

							if (Mouse.isButtonDown(0)) {
								if (mouseX > sliderX && mouseX < sliderX + sliderWidth &&
										mouseY > sliderY && mouseY < sliderY + sliderHeight) {
									float deltaX = mouseX - sliderX;
									switch (i) {
										case 0: colorSetting.setRed(deltaX / sliderWidth); break;
										case 1: colorSetting.setGreen(deltaX / sliderWidth); break;
										case 2: colorSetting.setBlue(deltaX / sliderWidth); break;
										case 3: colorSetting.setAlpha(deltaX / sliderWidth); break;
									}
								}
							}

							RoundedUtils.drawRect(sliderX, sliderY, sliderWidth, sliderHeight, 0.5f, BACKGROUND_COLOR);
							RoundedUtils.drawRect(sliderX, sliderY, sliderWidth * values[i], sliderHeight, 0.5f, colors[i]);
							currentOffset += (int) (sliderHeight + verticalSpacing);
						}

						float mainPreviewY = startY + currentOffset;
						RoundedUtils.drawRect(sliderX - previewSize * 2 - 5 - 1, mainPreviewY - previewSize * 2 - 3 - 1, previewSize * 2 + 2, previewSize * 2 + 2, 0f, BACKGROUND_COLOR);
						RoundedUtils.drawRect(sliderX - previewSize * 2 - 5, mainPreviewY - previewSize * 2 - 3, previewSize * 2, previewSize * 2, 0f, colorSetting.getColor());

						currentOffset += (int) verticalSpacing;

						if (colorSetting.isFade()) {
							float[] fadeValues = {colorSetting.getFadeRed(), colorSetting.getFadeGreen(),
									colorSetting.getFadeBlue(), colorSetting.getFadeAlpha()};

							for (int i = 0; i < 4; i++) {
								float sliderY = startY + currentOffset;

								if (Mouse.isButtonDown(0)) {
									if (mouseX > sliderX && mouseX < sliderX + sliderWidth &&
											mouseY > sliderY && mouseY < sliderY + sliderHeight) {
										float deltaX = mouseX - sliderX;
										switch (i) {
											case 0: colorSetting.setFadeRed(deltaX / sliderWidth); break;
											case 1: colorSetting.setFadeGreen(deltaX / sliderWidth); break;
											case 2: colorSetting.setFadeBlue(deltaX / sliderWidth); break;
											case 3: colorSetting.setFadeAlpha(deltaX / sliderWidth); break;
										}
									}
								}

								RoundedUtils.drawRect(sliderX, sliderY, sliderWidth, sliderHeight, 0.5f, BACKGROUND_COLOR);
								RoundedUtils.drawRect(sliderX, sliderY, sliderWidth * fadeValues[i], sliderHeight, 0.5f, new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 255));

								currentOffset += (int) (sliderHeight + verticalSpacing);
							}

							float fadePreviewY = startY + currentOffset;
							RoundedUtils.drawRect(sliderX - previewSize * 2 - 5 - 1, fadePreviewY - previewSize * 2 - 3 - 1, previewSize * 2 + 2f, previewSize * 2 + 2f, 0f, BACKGROUND_COLOR);
							RoundedUtils.drawRect(sliderX - previewSize * 2 - 5, fadePreviewY - previewSize * 2 - 3, previewSize * 2, previewSize * 2, 2f, colorSetting.getFadeColor());

							float mixedPreviewX = sliderX + previewSize * 2 + 5;
							RoundedUtils.drawRect(mixedPreviewX + previewSize * 4 + 5 - 1.5f, fadePreviewY - previewSize * 3 - 7.5f - 1.5f, previewSize * 2 + 2f, previewSize * 2 + 2.5f, 0f, BACKGROUND_COLOR);

							for (int i = 0; i < 23; i++) {
								RoundedUtils.drawRect(mixedPreviewX + previewSize * 4 + 5 + i, fadePreviewY - previewSize * 3 - 7.5f, 1, previewSize * 2, 0f, ColorUtils.mixColor(colorSetting.getColor(), colorSetting.getFadeColor(), i, colorSetting.getOffset(), colorSetting.getSpeed()));
							}

							float controlsY = startY + currentOffset - 40;
							float controlWidth = 50;
							float controlHeight = 4;

							float mixedPreviewAbsoluteX = mixedPreviewX + previewSize * 4 + 5;

							float controlsX = mixedPreviewAbsoluteX + previewSize * 2 + 10;

							RoundedUtils.drawRect(controlsX, controlsY, controlWidth, controlHeight, 0.5f, BACKGROUND_COLOR);
							RoundedUtils.drawRect(controlsX, controlsY, controlWidth * (colorSetting.getOffset() / 20f), controlHeight, 0.5f, new Color(220, 180, 255));

							if (mouseX > controlsX && mouseX < controlsX + controlWidth &&
									mouseY > controlsY && mouseY < controlsY + controlHeight) {
								if (Mouse.isButtonDown(0)) {
									float deltaX = mouseX - controlsX;
									colorSetting.setOffset(Math.round(deltaX / controlWidth * 20));
								}
								if (currentScroll != 0 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
									float newOffset = colorSetting.getOffset() + (currentScroll > 0 ? 1 : -1);
									colorSetting.setOffset(Math.max(0, Math.min(20, newOffset)));
								}
							}

							fontRenderer.drawString("Offset: " + (int)colorSetting.getOffset(), controlsX + controlWidth + 5, controlsY, new Color(220, 180, 255));

							RoundedUtils.drawRect(controlsX, controlsY + 10, controlWidth, controlHeight, 0.5f, BACKGROUND_COLOR);
							RoundedUtils.drawRect(controlsX, controlsY + 10, controlWidth * (colorSetting.getSpeed() / 20f), controlHeight, 0.5f, new Color(255, 220, 180));

							if (mouseX > controlsX && mouseX < controlsX + controlWidth &&
									mouseY > controlsY + 10 && mouseY < controlsY + controlHeight + 10) {
								if (Mouse.isButtonDown(0)) {
									float deltaX = mouseX - controlsX;
									colorSetting.setSpeed(Math.round(deltaX / controlWidth * 20));
								}
								if (currentScroll != 0 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
									float newSpeed = colorSetting.getSpeed() + (currentScroll > 0 ? 1 : -1);
									colorSetting.setSpeed(Math.max(0, Math.min(20, newSpeed)));
								}
							}

							fontRenderer.drawString("Speed: " + (int)colorSetting.getSpeed(), controlsX + controlWidth + 5, controlsY + 10, new Color(255, 220, 180));

						}
					}

					offset += colorSetting.isHide() ? 14 : currentOffset;
					settingsTotalHeight += colorSetting.isHide() ? 14 :  currentOffset;
				}
				if (setting instanceof KeyBind keyBind) {
					fontRenderer.drawString(
							activeKeyBind == keyBind ? "▬" : Keyboard.getKeyName(keyBind.getKey()),
							background.x + verticalLineXOffset + 5 + settingWidth + 1,
							background.y + 2 + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset, MAIN_COLOR
					);
				}
				offset += 14;
				settingsTotalHeight += 14;
			}
			ScissorUtils.disableScissor();
		}

		if (clickedCategory != null) {
			RenderUtils.drawRoundedOutLineRectangle(clickedCategoryPos.x, clickedCategoryPos.y, 100, 43, 2 / 1.4f, Color.BLACK.getRGB(), MAIN_COLOR.getRGB(), Color.BLACK.getRGB());
			fontRenderer.drawString("HideCategory", clickedCategoryPos.x + 3, clickedCategoryPos.y + 3 + 2, Color.WHITE, true);
			fontRenderer.drawString("LoadFromConfig", clickedCategoryPos.x + 3, clickedCategoryPos.y + 13 + 2, Color.WHITE, true);
			fontRenderer.drawString("Import", clickedCategoryPos.x + 3, clickedCategoryPos.y + 23 + 2, Color.WHITE, true);
			fontRenderer.drawString("Export", clickedCategoryPos.x + 3, clickedCategoryPos.y + 33 + 2, Color.WHITE, true);
		}

		if (clickedModule != null) {
			RenderUtils.drawRoundedOutLineRectangle(clickedModulePos.x, clickedModulePos.y, 40, 33, 2 / 1.4f, Color.BLACK.getRGB(), MAIN_COLOR.getRGB(), Color.BLACK.getRGB());
			fontRenderer.drawString("Import", clickedModulePos.x + 3, clickedModulePos.y + 3 + 2, Color.WHITE, true);
			fontRenderer.drawString("Export", clickedModulePos.x + 3, clickedModulePos.y + 13 + 2, Color.WHITE, true);
			fontRenderer.drawString("Hide", clickedModulePos.x + 3, clickedModulePos.y + 23 + 2, Color.WHITE, true);
		}

		float offsetModuleDesc = modulesScrolls.y;

		FontRenderer fonts = mc.fontRendererObj;

		for (Module module : moduleList) {
			if (mouseX < background.x || mouseX > background.x + sizeBackground.x || mouseY < background.y + 15 || mouseY > background.y + sizeBackground.y) continue;
			float moduleWidth = (float) fonts.getStringWidth(module.getName() + (!module.isHide() ? " ✓" : " ×"));
			boolean moduleCondition = mouseX > background.x + 3 && mouseX < background.x + 3 + moduleWidth && mouseY > background.y + 3 + 2 + fonts.FONT_HEIGHT + 5 + offsetModuleDesc && mouseY < background.y + 3 + 2 + fonts.FONT_HEIGHT + 5 + offsetModuleDesc + 9;
			if (moduleCondition && !module.getDescription().equalsIgnoreCase("") && clickedModule == null) {
				if (!module.isHovered()) {
					module.setHoverStartTime(System.currentTimeMillis());
					module.setHovered(true);
				} else {
					long hoverTime = System.currentTimeMillis() - module.getHoverStartTime();
					if (hoverTime >= 500) {
						float descriptionWidth = (float) fonts.getStringWidth(module.getDescription());
						RenderUtils.drawRoundedOutLineRectangle(mouseX, mouseY, descriptionWidth + 6, 14, 2f / 1.4f, Color.BLACK.getRGB(), MAIN_COLOR.getRGB(), Color.BLACK.getRGB());
						mc.fontRendererObj.drawString(module.getDescription(), mouseX + 3, mouseY + 3, Color.WHITE.getRGB(), true);
					}
				}
			} else {
				module.setHovered(false);
			}
			offsetModuleDesc += fontRenderer.FONT_HEIGHT + 2;
		}

		ScissorUtils.disableScissor();

		boolean sidePanel = mouseY > sc.getScaledHeight() - 25 && mouseY < sc.getScaledHeight() && mouseX > sc.getScaledWidth() / 2f - 50 && mouseX < sc.getScaledWidth() / 2f - 50 + 100;

		if (sidePanel) {
			guis.setEnd(25);
		} else {
			guis.setEnd(0);
		}

		RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 25, sc.getScaledHeight() - 10 + guis.getValue(), 50, 2, 0, Color.BLACK.getRGB(), Color.black.getRGB(), Color.BLACK.getRGB());
		RenderUtils.drawRoundedOutLineRectangle(sc.getScaledWidth() / 2f - 50, sc.getScaledHeight() - guis.getValue(), 100, 20, clientSettings.backgroundRadius.getValue() * 2.5f, BACKGROUND_COLOR.getRGB(), Color.black.getRGB(),Color.black.getRGB());

		boolean console = mouseX > sc.getScaledWidth() / 2f - 50 + 10 &&
				mouseX < sc.getScaledWidth() / 2f - 50 + 10 + 15 &&
				mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;

		boolean config = mouseX > sc.getScaledWidth() / 2f - 50 + 43 &&
				mouseX < sc.getScaledWidth() / 2f - 50 + 43 + 15 &&
				mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;

		boolean hotKey = mouseX > sc.getScaledWidth() / 2f - 50 + 75 &&
				mouseX < sc.getScaledWidth() / 2f - 50 + 75 + 15 &&
				mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;

		ResourceLocation terminal = new ResourceLocation("minecraft", "hackclient/image/terminal.png");
		ResourceLocation configs = new ResourceLocation("minecraft", "hackclient/image/configs.png");
		ResourceLocation hotKeys = new ResourceLocation("minecraft", "hackclient/image/keyboard.png");

		if (console) ColorUtils.glColor(MAIN_COLOR); else ColorUtils.glColor(Color.WHITE);
		RenderUtils.drawImage(terminal, (int) (sc.getScaledWidth() / 2f - 50 + 10), (int) (sc.getScaledHeight() - guis.getValue() + 3), 15, 15, true);
		if (config) ColorUtils.glColor(MAIN_COLOR); else ColorUtils.glColor(Color.WHITE);
		RenderUtils.drawImage(configs, (int) (sc.getScaledWidth() / 2f - 50 + 43), (int) (sc.getScaledHeight() - guis.getValue() + 3), 15, 15, true);
		if (hotKey) ColorUtils.glColor(MAIN_COLOR); else ColorUtils.glColor(Color.WHITE);
		RenderUtils.drawImage(hotKeys, (int) (sc.getScaledWidth() / 2f - 50 + 75), (int) (sc.getScaledHeight() - guis.getValue() + 3), 15, 15, true);

		Easing positionEasingFunc = Easing.IN_OUT_BACK;

		guis.update(clickGui.animationSpeed.getValue() / 5, positionEasingFunc);
		moduleLine.update(clickGui.animationSpeed.getValue() / 4, positionEasingFunc);
		settingLine.update(clickGui.animationSpeed.getValue());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
		ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("MuseoSans");

		boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
		boolean console = mouseX > sc.getScaledWidth() / 2f - 50 + 10 && mouseX < sc.getScaledWidth() / 2f - 50 + 10 + 15 && mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;
		boolean config = mouseX > sc.getScaledWidth() / 2f - 50 + 43 && mouseX < sc.getScaledWidth() / 2f - 50 + 43 + 15 && mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;
		boolean hotKeys = mouseX > sc.getScaledWidth() / 2f - 50 + 75 && mouseX < sc.getScaledWidth() / 2f - 50 + 75 + 15 && mouseY > sc.getScaledHeight() - guis.getValue() && mouseY < sc.getScaledHeight() - guis.getValue() + 18;

		if (Mouse.isButtonDown(0)) {
			if (quit) {
				lastPos.set(pos);
				lastSize.set(size);
                closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() + 10);
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
				pos.set(-sizeBackground.x - 10, lastPos.y);
			}

			if (config) {
				showConfigAfterClose = true;
				lastPos.set(pos);
				lastSize.set(size);
				closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() + 10);
			}

			if (hotKeys) {
				showHotKeysAfterClose = true;
				lastPos.set(pos);
				lastSize.set(size);
				closing = true;
				size.set(0, 0);
				pos.set(sc.getScaledWidth() + sizeBackground.x, lastPos.y);
			}
		}

		if (clickedCategory != null && clickedCategoryPos.x != 0 && clickedCategoryPos.y != 0) {
			boolean clickRectangle = mouseX > clickedCategoryPos.x - 5 && mouseX < clickedCategoryPos.x + 110 && mouseY > clickedCategoryPos.y - 5 && mouseY < clickedCategoryPos.y + 58;
			boolean clickHideCategory = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedCategoryPos.x + 3 + fontRenderer.getStringWidth("HideCategory") && mouseY > clickedCategoryPos.y + 3 && mouseY < clickedCategoryPos.y + 3 + fontRenderer.FONT_HEIGHT;
			boolean clickLoadFromConfig = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedCategoryPos.x + 3 + fontRenderer.getStringWidth("LoadFromConfig") && mouseY > clickedCategoryPos.y + 13 && mouseY < clickedCategoryPos.y + 13 + fontRenderer.FONT_HEIGHT;
			boolean clickImportCategory = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedCategoryPos.x + 3 + fontRenderer.getStringWidth("Import") && mouseY > clickedCategoryPos.y + 23 && mouseY < clickedCategoryPos.y + 23 + fontRenderer.FONT_HEIGHT;
			boolean clickExportFromConfig = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedCategoryPos.x + 3 + fontRenderer.getStringWidth("Export") && mouseY > clickedCategoryPos.y + 33 && mouseY < clickedCategoryPos.y + 33 + fontRenderer.FONT_HEIGHT;

			if (clickRectangle) {
				List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INST.getModuleManager().getModulesByCategory(clickedCategory));
				for (Module module : moduleList) {
					if (clickHideCategory) module.setHide(!module.isHide());
					if (clickLoadFromConfig) module.setLoadFromConfig(!module.isLoadFromConfig());
				}
				if (clickImportCategory) Client.INST.getConfigManager().importSettingsInCategory(clickedCategory);
				if (clickExportFromConfig) Client.INST.getConfigManager().exportSettingsInCategory(clickedCategory);
			} else {
				clickedCategoryPos.set(0,0);
				clickedCategory = null;
			}
		}

		if (clickedModule != null && clickedModulePos.x != 0 && clickedModulePos.y != 0) {
			boolean clickRectangle = mouseX > clickedModulePos.x - 5 && mouseX < clickedModulePos.x + 50 && mouseY > clickedModulePos.y - 5 && mouseY < clickedModulePos.y + 38;
			boolean clickImport = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedModulePos.x + 3 + fontRenderer.getStringWidth("Import") && mouseY > clickedModulePos.y + 3 && mouseY < clickedModulePos.y + 3 + fontRenderer.FONT_HEIGHT;
			boolean clickExport = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedModulePos.x + 3 + fontRenderer.getStringWidth("Export") && mouseY > clickedModulePos.y + 13 && mouseY < clickedModulePos.y + 13 + fontRenderer.FONT_HEIGHT;
			boolean clickHide = mouseX > clickedCategoryPos.x + 3 && mouseX < clickedModulePos.x + 3 + fontRenderer.getStringWidth("Hide") && mouseY > clickedModulePos.y + 23 && mouseY < clickedModulePos.y + 23 + fontRenderer.FONT_HEIGHT;

			if (clickRectangle) {
				if (clickImport) Client.INST.getConfigManager().importSettingsInModule(clickedModule);
				if (clickExport) Client.INST.getConfigManager().exportSettingsInModule(clickedModule);
				if (clickHide) selectedModule.setHide(!selectedModule.isHide());
			} else {
				clickedModulePos.set(0,0);
				clickedModule = null;
			}
		}

		if (clickedCategory != null || clickedModule != null) return;

		if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y) return;

		final float clientNameWidth = (float) fontRenderer.getStringWidth(Client.INST.getName());

		boolean resize = mouseX > background.x + sizeBackground.x - 5 && mouseX < background.x + sizeBackground.x && mouseY > background.y + sizeBackground.y - 5 && mouseY < background.y + sizeBackground.y;

		if (resize) {
			resizing = true;
			lastMouse.set(mouseX, mouseY);
		}

		float widthsModule = 0;
		for (Module module : Client.INST.getModuleManager().getModules()) {
			float moduleWidth = (float) fontRenderer.getStringWidth(module.getName() + (!module.isHide() ? " ✓" : " ×"));
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
			float moduleWidth = (float) fontRenderer.getStringWidth(module.getName() + (!module.isHide() ? " ✓" : " ×"));
			boolean moduleCondition = mouseX > background.x + 3 && mouseX < background.x + 3 + moduleWidth && mouseY > background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset && mouseY < background.y + 3 + 2 + fontRenderer.FONT_HEIGHT + 5 + offset + 9;
			if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y || mouseY < background.y + 15) continue;
			if (moduleCondition) {
				switch (mouseButton) {
					case 0 -> module.toggle();
					case 1 -> {
						if (selectedModule == null || !selectedModule.equals(module)) {
							settingsScrolls.setEnd(-400);
							settingsScrolls.update(clickGui.animationSpeed.getValue() / 5, Easing.OUT_BACK);
							settingsScroll = 0;
							selectedModule = module;
						} else {
							clickedModulePos.set(mouseX, mouseY);
							clickedModule = module;
						}
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
			if (selectCategory) {
				switch (mouseButton) {
					case 0 -> {
						selectedCategory = category;
						selectedModule = null;
						modulesScroll = 0;
					}

					case 1 -> {
						clickedCategoryPos.set(mouseX, mouseY);
						clickedCategory = category;
					}
				}
			}
			offset += (float) (fontRenderer.getStringWidth(category.name) + 5);
		}

		offset = settingsScrolls.getValue();
		if (selectedModule != null) {
			boolean bind = mouseX > background.x + verticalLineXOffset + 5 && mouseX < background.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey()))) && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
			boolean hide = mouseX > background.x + sizeBackground.x - 5 - fontRenderer.getStringWidth("Hide: " + selectedModule.isHide()) && mouseX < background.x + sizeBackground.x - 5 && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
			boolean loadFromConfig = mouseX > background.x + sizeBackground.x - 5 - fontRenderer.getStringWidth("Hide: " + selectedModule.isHide()) - fontRenderer.getStringWidth("LoadFromConfig: " + selectedModule.isLoadFromConfig()) - 5 && mouseX < background.x + sizeBackground.x - 5 - fontRenderer.getStringWidth("Hide: " + selectedModule.isHide()) - fontRenderer.getStringWidth("LoadFromConfig: " + selectedModule.isLoadFromConfig()) - 5 + fontRenderer.getStringWidth("LoadFromConfig: " + selectedModule.isLoadFromConfig()) && mouseY > background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 && mouseY < background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 6 + 9;
			if (bind) binding = true;
			if (hide) selectedModule.setHide(!selectedModule.isHide());
			if (loadFromConfig) selectedModule.setLoadFromConfig(!selectedModule.isLoadFromConfig());

			for (Setting setting : selectedModule.getSettings()) {
				if (!setting.isVisible())
					continue;

				float settingWidth = (float) fontRenderer.getStringWidth(setting.getName() + " ");
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
						xOffset += (float) (fontRenderer.getStringWidth(mode) + 5);
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
						xOffset += (float) (fontRenderer.getStringWidth(mode) + 5);
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
				if (setting instanceof ColorSetting colorSetting) {
					float startY = background.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + fontRenderer.FONT_HEIGHT / 2f - 2.5f;
					int currentOffset = 0;
					float triangleX = background.x + verticalLineXOffset + settingWidth + 3;

					float fadeButtonY = startY + currentOffset;

					if (Mouse.isButtonDown(0)) {
						float fadeButtonX = background.x + verticalLineXOffset + 5 + 3;
						if (!colorSetting.isHide() && mouseX > fadeButtonX + fontRenderer.getStringWidth("Fade: ") && mouseX < fadeButtonX + fontRenderer.getStringWidth("Fade: ") + fontRenderer.getStringWidth(String.valueOf(colorSetting.isFade())) &&
								mouseY > fadeButtonY + 12 && mouseY < fadeButtonY + 12 + fontRenderer.FONT_HEIGHT) {
							colorSetting.setFade(!colorSetting.isFade());
						}
						if (mouseX > triangleX && mouseX < triangleX + 10 && mouseY > fadeButtonY - 2 && mouseY < fadeButtonY + 8) {
							colorSetting.setHide(!colorSetting.isHide());
						}
					}

					if (colorSetting.isHide()) {
						offset += 14;
						settingsTotalHeight += 14;
						continue;
					} else {
						currentOffset += fontRenderer.FONT_HEIGHT + 3 + 10;

						currentOffset += (4 + 3) * 4;

						if (colorSetting.isFade()) {
							currentOffset += (4 + 3) * 4;
						}
					}

					offset += currentOffset + 3;
					settingsTotalHeight += currentOffset + 3;
				}
				offset += 14;
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
			if (keyCode == Keyboard.KEY_ESCAPE) {
				activeKeyBind.setKey(Keyboard.KEY_NONE);
				activeKeyBind = null;
				return;
			} else {
				activeKeyBind.setKey(keyCode);
				activeKeyBind = null;
			}
		}

		if (keyCode == Keyboard.KEY_ESCAPE && !closing) {
			ScaledResolution sc = new ScaledResolution(mc);
			lastPos.set(pos);
			lastSize.set(size);
			closing = true;
			size.set(0, 0);
			pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() + 10);
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