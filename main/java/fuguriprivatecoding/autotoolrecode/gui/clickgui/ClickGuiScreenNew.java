package fuguriprivatecoding.autotoolrecode.gui.clickgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.Scroll;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.io.IOException;

import static java.lang.Math.round;
import static java.lang.Math.signum;

public class ClickGuiScreenNew extends GuiScreen {

    public static ClickGuiScreenNew INST;

    EasingAnimation openAnim = new EasingAnimation();

    EasingAnimation openSettingsAnim = new EasingAnimation();

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);

    ResourceLocation exitLogo = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

    Category selectedCategory = Category.COMBAT;

    Module selectedModule;

    float x, y, width, height;
    float moduleScrollTotalHeight;

    boolean closing, moving;

    Scroll moduleScroll = new Scroll(30);

    public static void init() {
        INST = new ClickGuiScreenNew();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sc = ScaleUtils.getScaledResolution();

        x = 50;
        y = 50;
        width = sc.getScaledWidth() - 100;
        height = sc.getScaledHeight() - 100;

        openAnim.setEnd(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        Colors rectColor = Colors.BLACK.withAlphaClamp(openAnim.getValue());
        Colors elementColor = new Colors(clickGui.color.getFadedColor()).withAlphaClamp(openAnim.getValue());
        Colors elementTextColor = Colors.WHITE.withAlphaClamp(openAnim.getValue());
        Colors elementGrayColor = new Colors(15,15,15,255).withAlphaClamp(openAnim.getValue());
        Colors elementGlowColor = new Colors(clickGui.colorShadow.getFadedColor()).withAlphaClamp(openAnim.getValue());

        ClientFontRenderer font = Fonts.fonts.get("SFPro");

        boolean hoverModules = GuiUtils.isHovered(mouseX, mouseY, x + 100, y, width - 100, height);
        moduleScroll.handleScrollInput(hoverModules);
        moduleScroll.update(moduleScrollTotalHeight + 5, height);

        GL11.glPushMatrix();
        double scale = openAnim.getValue();

        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        double offsetX = centerX * (1 - scale);
        double offsetY = centerY * (1 - scale);

        GL11.glTranslated(offsetX, offsetY, 0);
        GL11.glScaled(scale, scale, 1);

        updateGuiAnimations();

        if (closing && openAnim.getValue() <= 0.2) {
            closing = false;
            mc.displayGuiScreen(null);
        }

        RoundedUtils.drawRect(x + 100, y, width - 100, height, 0, 0, 10, 10, rectColor);
        RoundedUtils.drawRect(x, y, 100, height, 10, 10, 0, 0, rectColor.withAlpha(0.5f));

        BloomUtils.addToDraw(() -> {
            RenderUtils.drawMixedRoundedRect(x, y, width, height, 10f, Colors.CORAL, Colors.WHITE, 90, 180, 270, 90, 5f);
        });

        BlurUtils.addToDraw(() -> {
            RoundedUtils.drawRect(x, y, 100, height, 10, 10, 0, 0, rectColor);
        });

        GL11.glPushMatrix();
        GL11.glScaled(1.5,1.5,0);
        font.drawString(Client.INST.CLIENT_NAME, x - 2, y - 7.5f, elementColor);
        GL11.glPopMatrix();

        ClientFontRenderer catFonts = Fonts.fonts.get("SFProRegular");

        float categoryOffset = 0;
        for (Category category : Category.values()) {
            EasingAnimation toggleAnim = category.getToggleAnim();
            toggleAnim.update(4, Easing.OUT_CUBIC);
            toggleAnim.setEnd(selectedCategory == category);

            Colors categoryColor = new Colors(ColorUtils.interpolateColor(rectColor, elementColor, toggleAnim.getValue()));
            Colors categoryTextColor = new Colors(ColorUtils.interpolateColor(elementTextColor, rectColor, toggleAnim.getValue()));

            RoundedUtils.drawRect(x + 10, y + 30 + categoryOffset, 80, 12.5f, 2.5f,categoryColor);

            ColorUtils.glColor(categoryTextColor);
            RenderUtils.drawImage(category.logo, x + 10 + 1.5f, y + 30 + 1.5f + categoryOffset, 10f,10f,true);

            catFonts.drawString(category.getName(), x + 10 + 15, y + 30 + 4 + categoryOffset, categoryTextColor);
            categoryOffset += 17.5f;
        }

        ClientFontRenderer settingsFont = Fonts.fonts.get("SFProRegular");
        if (selectedModule != null) {
            float settingsOffset = 0;
            RoundedUtils.drawRect(x + 100, y, width - 100, 15, 0, 0, 10, 0, elementGrayColor);

            boolean hoverExit = GuiUtils.isHovered(mouseX, mouseY, x + width - 20, y, 15, 15);

            Color exitSettingsColor = hoverExit ? Colors.RED.withAlphaClamp(openAnim.getValue()) : Colors.WHITE.withAlphaClamp(openAnim.getValue());

            ColorUtils.glColor(exitSettingsColor);
            RenderUtils.drawImage(exitLogo, x + width - 20, y, 15, 15, true);

            font.drawString(selectedModule.getName(), x + 100 + 5, y + 6, elementTextColor);

            for (Setting setting : selectedModule.getSettings()) {
                EasingAnimation visibleAnim = setting.getVisibleAnim();
                visibleAnim.update(4f, Easing.OUT_CUBIC);
                visibleAnim.setEnd(setting.isVisible());

                if (visibleAnim.getValue() <= 0) continue;

                String name = setting.getName() + ": ";

                float widthName = (float) settingsFont.getStringWidth(name);

                float settingX = x + 100 + 5;
                float settingY = y + 5 + settingsOffset + 20;

                switch (setting) {
                    case CheckBox checkBox -> {
                        settingsFont.drawString(name, settingX, settingY, Colors.WHITE.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()));

                        EasingAnimation toggleAnim = checkBox.getToggleAnimation();

                        toggleAnim.update(3f, Easing.OUT_CUBIC);
                        toggleAnim.setEnd(checkBox.isToggled());

                        Color toggleColor = ColorUtils.interpolateColor(Colors.RED.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()), Colors.GREEN.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()), toggleAnim.getValue());

                        settingsFont.drawString(String.valueOf(checkBox.isToggled()), settingX + widthName, settingY, toggleColor);
                    }

                    case KeyBind keyBind -> {
                        settingsFont.drawString(name, settingX, settingY, Colors.WHITE.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()));
                        settingsFont.drawString(Keyboard.getKeyName(keyBind.getKey()),settingX + widthName, settingY, elementColor);
                    }

                    case IntegerSetting integerSetting -> {
                        settingsFont.drawString(name, settingX, settingY, Colors.WHITE.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()));

                        EasingAnimation sliderAnim = integerSetting.getSliderAnim();

                        sliderAnim.update(4f, Easing.OUT_CUBIC);
                        sliderAnim.setEnd(integerSetting.value);

                        integerSetting.setAnimatedValue(sliderAnim.getValue());

                        float animatedFilledFactor = integerSetting.getAnimatedNormalize();
                        final float length = 75;
                        final float sliderLength = animatedFilledFactor * length;

                        RoundedUtils.drawRect(settingX + widthName, settingY, length, 4, 1.5f, elementGrayColor);
                        RoundedUtils.drawRect(settingX + widthName, settingY, sliderLength, 4, 1.5f, elementColor);
                        RoundedUtils.drawRect(settingX + widthName + sliderLength - 2, settingY - 1, 6, 6, 3f, Color.WHITE);
                        settingsFont.drawString(String.valueOf(integerSetting.getValue()), settingX + length + 5 + widthName, settingY, elementTextColor);

                        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, settingX - 2 + widthName, settingY - 2, length + 4, 8);

                        if (hovered) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                integerSetting.setValue((int) (integerSetting.getValue() + signum(DeltaTracker.getDeltaScroll())));
                            } else if (Mouse.isButtonDown(0)) {
                                float mx = mouseX - (settingX + widthName);
                                float p = mx / length;
                                float normalize = integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * p;
                                integerSetting.setValue(round(normalize));
                            }
                        }
                    }

                    case FloatSetting floatSetting -> {
                        settingsFont.drawString(name, settingX, settingY, Colors.WHITE.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()));

                        EasingAnimation sliderAnim = floatSetting.getSliderAnim();

                        sliderAnim.update(4f, Easing.OUT_CUBIC);
                        sliderAnim.setEnd(floatSetting.value);

                        floatSetting.setAnimatedValue(sliderAnim.getValue());

                        float animatedFilledFactor = floatSetting.getAnimatedNormalize();
                        final float length = 75;
                        final float sliderLength = animatedFilledFactor * length;

                        RoundedUtils.drawRect(settingX + widthName, settingY, length, 4, 1.5f, elementGrayColor);
                        RoundedUtils.drawRect(settingX + widthName, settingY, sliderLength, 4, 1.5f, elementColor);
                        RoundedUtils.drawRect(settingX + widthName + sliderLength - 2, settingY - 1, 6, 6, 3f, Color.WHITE);
                        settingsFont.drawString(String.format("%.2f", floatSetting.getValue()), settingX + widthName + length + 5, settingY, elementTextColor);

                        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, settingX + widthName - 2, settingY - 2, length + 4, 8);

                        if (hovered) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                floatSetting.setValue(floatSetting.getValue() + signum(DeltaTracker.getDeltaScroll()) * floatSetting.getStep());
                            } else if (Mouse.isButtonDown(0)) {
                                float mx = mouseX - (settingX + widthName);
                                float p = mx / length;
                                float normalize = floatSetting.getMin() + (floatSetting.getMax() - floatSetting.getMin()) * p;
                                floatSetting.setValue(normalize);
                            }
                        }
                    }

                    case DoubleSlider doubleSlider -> {
                        settingsFont.drawString(name, settingX, settingY, Colors.WHITE.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()));
                        EasingAnimation sliderMinAnim = doubleSlider.getSliderMinAnim();
                        EasingAnimation sliderMaxAnim = doubleSlider.getSliderMaxAnim();

                        sliderMinAnim.update(4f, Easing.OUT_CUBIC);
                        sliderMinAnim.setEnd((float) doubleSlider.minValue);
                        sliderMaxAnim.update(4f, Easing.OUT_CUBIC);
                        sliderMaxAnim.setEnd((float) doubleSlider.maxValue);

                        doubleSlider.setAnimatedValueMin(sliderMinAnim.getValue());
                        doubleSlider.setAnimatedValueMax(sliderMaxAnim.getValue());

                        final float length = 75;
                        float animatedFilledFactorMin = (float) doubleSlider.getAnimatedNormalizeMin();
                        float animatedFilledFactorMax = (float) doubleSlider.getAnimatedNormalizeMax();
                        final float sliderLengthMin = animatedFilledFactorMin * length;
                        final float sliderLengthMax = animatedFilledFactorMax * length;

                        RoundedUtils.drawRect(settingX, settingY + 10, length, 4, 1.5f, elementGrayColor);
                        RoundedUtils.drawRect(settingX, settingY + 10, sliderLengthMin, 4, 1.5f, elementColor);
                        RoundedUtils.drawRect(settingX + sliderLengthMin - 2, settingY - 1 + 10, 6, 6, 3f, Color.WHITE);
                        settingsFont.drawString(String.format("%.2f", doubleSlider.getMinValue()), settingX + length + 5, settingY + 10, elementTextColor);

                        RoundedUtils.drawRect(settingX, settingY + 10 + 10, length, 4, 1.5f, elementGrayColor);
                        RoundedUtils.drawRect(settingX, settingY + 10 + 10, sliderLengthMax, 4, 1.5f, elementColor);
                        RoundedUtils.drawRect(settingX + sliderLengthMax - 2, settingY - 1 + 10 + 10, 6, 6, 3f, Color.WHITE);
                        settingsFont.drawString(String.format("%.2f", doubleSlider.getMaxValue()), settingX + length + 5, settingY + 10 + 10, elementTextColor);

                        boolean hoveredMin = GuiUtils.isHovered(mouseX, mouseY, settingX - 2, settingY - 2 + 10, length + 4, 8);
                        boolean hoveredMax = GuiUtils.isHovered(mouseX, mouseY, settingX - 2, settingY - 2 + 10 + 10, length + 4, 8);

                        if (hoveredMin) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                doubleSlider.setMinValue(doubleSlider.getMinValue() + signum(DeltaTracker.getDeltaScroll()) * doubleSlider.getStep());
                            } else if (Mouse.isButtonDown(0)) {
                                float mx = mouseX - settingX;
                                float p = mx / length;
                                float normalize = (float) (doubleSlider.getMin() + (doubleSlider.getMax() - doubleSlider.getMin()) * p);
                                doubleSlider.setMinValue(normalize);
                            }
                        }

                        if (hoveredMax) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                doubleSlider.setMaxValue(doubleSlider.getMaxValue() + signum(DeltaTracker.getDeltaScroll()) * doubleSlider.getStep());
                            } else if (Mouse.isButtonDown(0)) {
                                float mx = mouseX - settingX;
                                float p = mx / length;
                                float normalize = (float) (doubleSlider.getMin() + (doubleSlider.getMax() - doubleSlider.getMin()) * p);
                                doubleSlider.setMaxValue(normalize);
                            }
                        }

                        settingsOffset += 20;
                    }
                    default -> {}
                }

                settingsOffset += 15;

//                settingsOffset += setting.draw(settingX, settingY, settingsFont, elementColor.withAlphaClamp(openSettingsAnim.getValue() * openAnim.getValue()),openSettingsAnim.getValue() * openAnim.getValue()) * visibleAnim.getValue();
            }
        } else if (selectedCategory != null) {
            StencilUtils.setUpTexture(x + 100 + 2, y + 2, width - 100 - 4, height - 4, 7.5f);
            StencilUtils.writeTexture();

            moduleScrollTotalHeight = 0;
            float moduleOffset = moduleScroll.getScrollAnim().getValue();
            for (Module module : Modules.getModulesByCategory(selectedCategory)) {
                EasingAnimation toggleAnim = module.getToggleAnimation();
                toggleAnim.update(4, Easing.OUT_CUBIC);
                toggleAnim.setEnd(module.isToggled());

                Colors moduleNameColor = new Colors(clickGui.color.getMixedColor(Modules.getModulesByCategory(selectedCategory).indexOf(module))).withAlphaClamp(openAnim.getValue());
                Colors moduleColor = new Colors(ColorUtils.interpolateColor(elementGrayColor, moduleNameColor, toggleAnim.getValue()));

                RenderUtils.drawRoundedOutLineRectangle(x + 100 + 5, y + 5 + moduleOffset, width - 100 - 10, 25, 7.5f * 1.7f, elementGrayColor.withAlpha(toggleAnim.getValue()).getRGB(), moduleColor.getRGB(), moduleColor.getRGB());
                font.drawString(module.getName(), x + 100 + 5 + 5, y + 5 + 5 + moduleOffset, moduleNameColor);
                catFonts.drawString(module.getDescription(), x + 100 + 5 + 5, y + 5 + 15 + moduleOffset, elementTextColor);

                moduleOffset += 30;
                moduleScrollTotalHeight += 30;
            }

            StencilUtils.endWriteTexture();
        }

        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float categoryOffset = 0;
        for (Category category : Category.values()) {
            boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + 10, y + 30 + categoryOffset, 80, 12.5f);

            if (hovered) {
                switch (mouseButton) {
                    case 0 -> selectedCategory = category;
                }
            }

            categoryOffset += 17.5f;
        }

        ClientFontRenderer font = Fonts.fonts.get("SFProRegular");
        if (selectedModule != null) {
            float settingsOffset = 0;
            for (Setting setting : selectedModule.getSettings()) {
                EasingAnimation visibleAnim = setting.getVisibleAnim();
                visibleAnim.update(4f, Easing.OUT_CUBIC);
                visibleAnim.setEnd(setting.isVisible());

                if (visibleAnim.getValue() <= 0) continue;

                float settingX = x + 100 + 5;
                float settingY = y + 5 + settingsOffset;

                settingsOffset += setting.mouseClicked(mouseX, mouseY, settingX, settingY, mouseButton, font) * visibleAnim.getValue();

            }
        } else if (selectedCategory != null) {
            boolean moduleListHovered = GuiUtils.isHovered(mouseX, mouseY, x + 100, y + 2, width - 100, height - 4);

            if (moduleListHovered) {
                float moduleOffset = moduleScroll.getScrollAnim().getValue();
                for (Module module : Modules.getModulesByCategory(selectedCategory)) {
                    boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + 100 + 5, y + 5 + moduleOffset, width - 100 - 10, 25f);

                    if (hovered) {
                        switch (mouseButton) {
                            case 0 -> module.toggle();
                            case 1 -> {
                                openSettingsAnim.setEnd(1f);
                                selectedModule = module;
                            }
                        }
                    }
                    moduleOffset += 30;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !closing) {
            openAnim.setEnd(0);
            closing = true;
        }
    }

    private void updateGuiAnimations() {
        moduleScroll.getScrollAnim().update(3f, Easing.OUT_CUBIC);
        openSettingsAnim.update(2, Easing.OUT_BACK);
        openAnim.update(2f, Easing.OUT_BACK);
    }
}
