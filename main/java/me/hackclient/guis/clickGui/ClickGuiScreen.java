package me.hackclient.guis.clickGui;

import me.hackclient.Client;
import me.hackclient.guis.config.ConfigEditorGui;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.impl.visual.Bloom;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.module.impl.visual.ClientShader;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.*;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.render.scissor.ScissorUtils;
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
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class ClickGuiScreen extends GuiScreen {

	final Color BACKGROUND_COLOR = new Color(15, 15, 15, 100);
	final Color MAIN_COLOR = new Color(0, 255, 209, 255);
	final int MAIN_COLOR_INT = MAIN_COLOR.getRGB();

	Vector2f pos, size, lastMouse;
	Bloom bloom = Client.INSTANCE.getModuleManager().getModule(Bloom.class);
	ClickGui clickGui = Client.INSTANCE.getModuleManager().getModule(ClickGui.class);
	ClientShader clientShader;

	Category selectedCategory = Category.COMBAT;
	Module selectedModule = null;

	boolean resizing, moving, binding;

	// Animations
	final Animation2D categoryLine, moduleLine, settingLine, cfgButton, cfgButton2;

	public ClickGuiScreen() {
		lastMouse = new Vector2f(0, 0);
		pos = new Vector2f(100, 100);
		size = new Vector2f(350, 150);

		mc = Minecraft.getMinecraft();

		cfgButton2 = new Animation2D();
		cfgButton = new Animation2D();
		categoryLine = new Animation2D();
		moduleLine = new Animation2D();
		settingLine = new Animation2D();
	}

	final ResourceLocation shesterenka = new ResourceLocation("minecraft", "hackclient/image/shesterenka.png");

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		RenderUtils.drawImage(shesterenka, 5, 5, 30, 30);
		if (clientShader == null) {
			clientShader = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
			return;
		}
		if (bloom.clickGui.isToggled() && bloom.isToggled()) {
			List<Runnable> list = new ArrayList<>();
			list.add(() -> RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, clickGui.backgroundRadius.getValue(), Color.WHITE));
			BloomUtils.drawBloom(list);
		}
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

		ScissorUtils.enableScissor();
		ScissorUtils.scissor(new ScaledResolution(mc), pos.x, pos.y, size.x, size.y);

		if (clientShader.isToggled() && clientShader.clickGui.isToggled()) {
			PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, clickGui.backgroundRadius.getValue(), new Color(15, 15, 15, clickGui.backgroundAlpha.getValue())));
		} else {
			RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, clickGui.backgroundRadius.getValue(), new Color(15, 15, 15, clickGui.backgroundAlpha.getValue()));
		}
		RoundedUtils.drawRect(pos.x + size.x - 5, pos.y + size.y - 5, 5, 5, 1, BACKGROUND_COLOR);
		fontRenderer.drawString(Client.INSTANCE.getName(), pos.x + 14, pos.y + 4, MAIN_COLOR_INT);

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
					"Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey())),
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
				if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					int length = multiBooleanSetting.getValues().size();
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						boolean notLast = value != multiBooleanSetting.getValues().get(length - 1);
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
							String.format("%.1f", floatSetting.getValue()),
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
		ScissorUtils.disableScissor();

		ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());

		boolean flag = mouseX > sc.getScaledWidth() / 2f - 25
				&& mouseX < sc.getScaledWidth() / 2f - 25 + 50
				&& mouseY > sc.getScaledHeight() - 18.5;

		if (mouseX > sc.getScaledWidth() / 2f - 25
		&& mouseX < sc.getScaledWidth() / 2f - 25 + 50
		&& mouseY > sc.getScaledHeight() - 50) {
			cfgButton.endY = 15 + 3 + 0.5f;
			cfgButton2.endY = 0;
		} else {
			cfgButton.endY = 0;
			cfgButton2.endY = 10;
		}

		RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 25, (float) (sc.getScaledHeight() - cfgButton.y), 50, 15, 5f, flag ? new Color(15, 15, 15, 110) : new Color(15, 15, 15, 100));
		RoundedUtils.drawRect(sc.getScaledWidth() / 2f - 10, (float) (sc.getScaledHeight() - cfgButton2.y), 20, 20, 5f, new Color(15, 15, 15, 110));
		mc.fontRendererObj.drawCenteredString("configs", sc.getScaledWidth() / 2f, (float) (sc.getScaledHeight() - cfgButton.y) + 3, -1);

		categoryLine.update(clickGui.animationSpeed.getValue());
		moduleLine.update(clickGui.animationSpeed.getValue());
		settingLine.update(clickGui.animationSpeed.getValue());
		cfgButton.update(clickGui.animationSpeed.getValue());
		cfgButton2.update(clickGui.animationSpeed.getValue());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
		if (mouseX > sc.getScaledWidth() / 2f - 25
		&& mouseX < sc.getScaledWidth() / 2f - 25 + 50
		&& mouseY > sc.getScaledHeight() - (15 + 3 + 0.5f)
		&& mouseButton == 0) {
			mc.displayGuiScreen(new ConfigEditorGui(this));
		}

		if (mouseX > pos.x + size.x || mouseY > pos.y + size.y) return;


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

		float widthsModule = 0;
		for (Module module : Client.INSTANCE.getModuleManager().modules) {
			float moduleWidth = fontRenderer.getStringWidth(module.getName());

			if (moduleWidth > widthsModule) {
				widthsModule = moduleWidth;
			}
		}
		float verticalLineXOffset = max(clientNameWidth, widthsModule) + 5;

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
            if (mouseX > pos.x + verticalLineXOffset + 5
			&& mouseX < pos.x + verticalLineXOffset + 5 + fontRenderer.getStringWidth("Keybind: " + (binding ? "▬" : Keyboard.getKeyName(selectedModule.getKey())))
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
				if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
					float xOffset = 0;
					float yOffset = 0;
					for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
						String mode = value.getFirst();
						boolean isSelected = value.getSecond();
						if (pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode) >= pos.x + size.x) {
							xOffset = 0;
							yOffset += 11;
						}
						if (mouseX > pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset
						&& mouseX < pos.x + verticalLineXOffset + 5 + settingWidth + 1 + xOffset + fontRenderer.getStringWidth(mode)
						&& mouseY > pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset
						&& mouseY < pos.y + 2 + 2 + fontRenderer.FONT_HEIGHT + 16.5f + offset + yOffset + 10) {
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
}