package fuguriprivatecoding.autotoolrecode.guis.clickgui.drop;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.scaling.ScaleUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.util.List;

import static java.lang.Math.signum;

public class ClickGuiPanel {

    public ClickGuiPanel(Category category, float initialX, float initialY) {
        this.category = category;
        this.xPosAnim.setValue(initialX);
        this.yPosAnim.setValue(initialY);
        modules = Client.INST.getModuleManager().getModulesByCategory(category);
    }

    private final List<Module> modules;

    private final Category category;

    boolean opened, moving;

    EasingAnimation xPosAnim = new EasingAnimation();
    EasingAnimation yPosAnim = new EasingAnimation();

    EasingAnimation heightAnim = new EasingAnimation();
    EasingAnimation heightAnimNormalized = new EasingAnimation();

    EasingAnimation openSettingsAnim = new EasingAnimation();

    private Module openedModule, lastOpenedModule;

    private float totalElementsHeight;

    private final Vector2f moveOffset = new Vector2f();

    EasingAnimation modulesScrollAnim = new EasingAnimation();
    EasingAnimation settingsScrollAnim = new EasingAnimation();

    int settingsScroll;
    int modulesScroll;

    float backgroundX;
    float backgroundY;
    float backgroundWidth;
    float backgroundHeight;

    float panelHeaderHeight = 25;

    float xOffset = 5;
    float yOffset = panelHeaderHeight;

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

    ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("SFPro");

    public void render(float openAnimProgress, int mouseX, int mouseY, int currentScroll) {
        if (GuiUtils.isHovered(mouseX, mouseY, backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                settingsScroll += currentScroll / 120 * 25;
            }
            modulesScroll += currentScroll / 120 * 25;
        }

        float altVisibleHeight = 255;
        float maxScroll = Math.max(totalElementsHeight - altVisibleHeight, 0);

        if (openSettingsAnim.getValue() == 0) {
            modulesScroll = (int) Math.clamp(modulesScroll, -maxScroll, 0);
        } else if (openSettingsAnim.getValue() == 1) {
            settingsScroll = (int) Math.clamp(settingsScroll, -maxScroll, 0);
        }

        modulesScrollAnim.setEnd(modulesScroll);
        modulesScrollAnim.update(5, Easing.OUT_CUBIC);

        settingsScrollAnim.setEnd(settingsScroll);
        settingsScrollAnim.update(5, Easing.OUT_CUBIC);

        if (moving) {
            xPosAnim.setEnd(mouseX - moveOffset.x);
            yPosAnim.setEnd(mouseY - moveOffset.y);
        }

        ScaledResolution sc = ScaleUtils.getScaledResolution();

        xPosAnim.update(8f, Easing.OUT_CUBIC);
        yPosAnim.update(8f, Easing.OUT_CUBIC);

        heightAnim.update(3, Easing.IN_OUT_CUBIC);
        heightAnim.setEnd(opened ? Math.min(totalElementsHeight, 255) : 0);

        heightAnimNormalized.setEnd(opened ? 1 : 0);
        heightAnimNormalized.update(3, Easing.IN_OUT_CUBIC);

        openSettingsAnim.update(5, Easing.IN_OUT_CUBIC);

        float invertOpenAnimProgress = 1 - openAnimProgress;
        float panelRadius = 10;

        Color panelColor = new Color(0.1f,0.1f,0.1f, clickGui.backgroundAlpha.getValue() / 255f);

        backgroundX = xPosAnim.getValue() - invertOpenAnimProgress * 10 / 2;
        backgroundY = yPosAnim.getValue() - invertOpenAnimProgress * 10 / 2;

        backgroundWidth = (100 + invertOpenAnimProgress * 10) + (openSettingsAnim.getValue() * heightAnimNormalized.getValue()) * 100;
        backgroundHeight = 20 + openSettingsAnim.getValue() * 20 + heightAnim.getValue() + invertOpenAnimProgress * 10;

        float invertHeightAnim = 1 - heightAnimNormalized.getValue();

        if (clickGui.glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                RoundedUtils.drawRect(
                    backgroundX + 1, backgroundY + 1,
                    backgroundWidth - 2, backgroundHeight - 2,
                    panelRadius,
                    clickGui.colorShadow.getFadedColor()
                );
            });
        }

        if (clickGui.blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                RoundedUtils.drawRect(
                    backgroundX, backgroundY,
                    backgroundWidth, backgroundHeight,
                    panelRadius,
                    Color.WHITE
                );

            });
        }

        RoundedUtils.drawRect(
            backgroundX, backgroundY,
            backgroundWidth, backgroundHeight,
            panelRadius,
            panelColor
        );

        RoundedUtils.drawRect(
            backgroundX,backgroundY,
            backgroundWidth, 20 + 15 * openSettingsAnim.getValue(),
            panelRadius * invertHeightAnim, panelRadius,panelRadius,panelRadius * invertHeightAnim,
            panelColor.darker()
        );

        yOffset = panelHeaderHeight + (15) * openSettingsAnim.getValue();

        fontRenderer.drawCenteredString(
            category.name,
            backgroundX + backgroundWidth / 2f,
            backgroundY + 8,
            Color.WHITE
        );

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(
            sc,
            backgroundX, backgroundY + 24,
            backgroundWidth,backgroundHeight - 26
        );

        float maxSettingWidth = backgroundWidth - xOffset * 2;

        float nameX = backgroundX - (heightAnimNormalized.getValue() > 0 ? (1 - openSettingsAnim.getValue()) * backgroundWidth : 0);
        float nameY = backgroundY + panelHeaderHeight * openSettingsAnim.getValue() + 1;

        if (lastOpenedModule != null) {
            fontRenderer.drawString(
                lastOpenedModule.getName(),
                nameX + 5 + 2, nameY,
                Color.WHITE
            );

            ResourceLocation exitSettings = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

            if (GuiUtils.isHovered(mouseX, mouseY, backgroundX, nameY - 6, backgroundWidth,15)) ColorUtils.glColor(Color.RED);
            RenderUtils.drawImage(exitSettings, (nameX + maxSettingWidth - 5), nameY - 6, 15,15,true);
        }

        if (heightAnimNormalized.getValue() > 0) {
            totalElementsHeight = 0;

            if (openSettingsAnim.getValue() < 1) {
                for (Module module : modules) {
                    float moduleX = backgroundX + xOffset + openSettingsAnim.getValue() * backgroundWidth;
                    float moduleY = backgroundY + yOffset + totalElementsHeight + modulesScrollAnim.getValue();
                    float moduleHeight = 20;
                    float moduleWidth = backgroundWidth - xOffset * 2;

                    boolean hovered = GuiUtils.isHovered(mouseX, mouseY, moduleX, moduleY, moduleWidth, moduleHeight);

                    Color baseColor = getBaseColor(module, hovered);

                    RoundedUtils.drawRect(moduleX, moduleY, moduleWidth, moduleHeight, 5f, baseColor);
                    fontRenderer.drawString(module.getName(), moduleX + 5, moduleY + 8, Color.WHITE);

                    totalElementsHeight += 25;
                }
            }

            if (openSettingsAnim.getValue() > 0) {
                float totalSettingsHeight = 0;

                ClientFontRenderer textFont = Client.INST.getFonts().fonts.get("SFProRegular");

                float rectX = backgroundX + xOffset;
                float rectY = backgroundY + yOffset;

                Color settingPanelColor = new Color(0.1f,0.1f,0.1f,clickGui.backgroundAlpha.getValue() / 255f * (openSettingsAnim.getValue()));

                RoundedUtils.drawRect(
                    rectX, rectY,
                    backgroundWidth - 10, backgroundHeight - yOffset - 5,
                    5f,
                    settingPanelColor
                );

                ScissorUtils.enableScissor();
                ScissorUtils.scissor(
                    sc,
                    rectX, rectY,
                    backgroundWidth - 10, backgroundHeight - yOffset - 5
                );

                for (Setting setting : lastOpenedModule.getSettings()) {
                    float settingX = backgroundX + xOffset - (1 - openSettingsAnim.getValue()) * backgroundWidth;
                    float settingY = backgroundY + yOffset + totalSettingsHeight + settingsScrollAnim.getValue();

                    Color clickGuiColor = clickGui.color.getMixedColor(lastOpenedModule.getSettings().indexOf(setting));
                    String settingName = setting.getName().replaceAll(" ", "");
                    float settingNameWidth = (float) textFont.getStringWidth(settingName) + 2;

                    setting.getVisibleAnim().setEnd(setting.isVisible() ? 1 : 0);
                    setting.getVisibleAnim().update(3f, Easing.IN_OUT_CUBIC);

                    if (setting.getVisibleAnim().getValue() == 0) continue;

                    float visibleProgress = setting.getVisibleAnim().getValue();

                    switch (setting) {
                        case FloatSetting floatSetting -> {
                            textFont.drawString(settingName, settingX + 5, settingY + 5, Color.WHITE);

                            float sliderX = settingX + 5 + settingNameWidth;
                            float sliderY = settingY + 5;
                            float sliderWidth = maxSettingWidth / 2f - 15;
                            float sliderHeight = 6;

                            RoundedUtils.drawRect(
                                sliderX, sliderY,
                                sliderWidth, sliderHeight,
                                3, settingPanelColor
                            );

                            RoundedUtils.drawRect(
                                sliderX, sliderY,
                                sliderWidth * floatSetting.normalize(), sliderHeight,
                                3, clickGuiColor
                            );

                            RoundedUtils.drawRect(
                                sliderX + sliderWidth * floatSetting.normalize() - 3, sliderY,
                                6,6,
                                3,
                                Color.WHITE
                            );

                            textFont.drawString(
                                String.format("%.2f", floatSetting.getValue()),
                                settingX - 1 + maxSettingWidth - fontRenderer.getStringWidth(String.format("%.2f", floatSetting.getValue())),
                                settingY + 5, Color.WHITE
                            );

                            if (GuiUtils.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight)) {
                                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                    floatSetting.setValue(floatSetting.getValue() + Math.signum(currentScroll) * floatSetting.getStep());
                                } else if (Mouse.isButtonDown(0)) {
                                    float relativeX = mouseX - sliderX;
                                    relativeX = Math.max(0, Math.min(relativeX, sliderWidth));
                                    float p = relativeX / sliderWidth;
                                    float value = floatSetting.getMin() + (floatSetting.getMax() - floatSetting.getMin()) * p;
                                    floatSetting.setValue(value);
                                }
                            }

                            totalSettingsHeight += 15 * visibleProgress;
                        }

                        case IntegerSetting integerSetting -> {
                            textFont.drawString(settingName, settingX + 5, settingY + 5, Color.WHITE);

                            float sliderX = settingX + 5 + settingNameWidth;
                            float sliderY = settingY + 5;
                            float sliderWidth = maxSettingWidth / 2f - 15;
                            float sliderHeight = 6;

                            RoundedUtils.drawRect(
                                sliderX, sliderY,
                                sliderWidth, sliderHeight,
                                3, settingPanelColor
                            );

                            RoundedUtils.drawRect(
                                sliderX, sliderY,
                                sliderWidth * integerSetting.normalize(), sliderHeight,
                                3, clickGuiColor
                            );

                            RoundedUtils.drawRect(
                                sliderX + sliderWidth * integerSetting.normalize() - 3, sliderY,
                                6,6,
                                3,
                                Color.WHITE
                            );

                            textFont.drawString(
                                String.valueOf(integerSetting.getValue()),
                                settingX - 1 + maxSettingWidth - fontRenderer.getStringWidth(String.valueOf(integerSetting.getValue())),
                                settingY + 5, Color.WHITE
                            );

                            if (GuiUtils.isHovered(mouseX, mouseY, sliderX, sliderY, sliderWidth, sliderHeight)) {
                                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                    integerSetting.setValue((int) (integerSetting.getValue() + signum(currentScroll)));
                                } else if (Mouse.isButtonDown(0)) {
                                    float relativeX = mouseX - sliderX;
                                    relativeX = Math.max(0, Math.min(relativeX, sliderWidth));
                                    float p = relativeX / sliderWidth;
                                    float value = integerSetting.getMin() + (integerSetting.getMax() - integerSetting.getMin()) * p;
                                    integerSetting.setValue((int) value);
                                }
                            }


                            totalSettingsHeight += 15 * visibleProgress;
                        }

                        case CheckBox checkBox -> {
                            textFont.drawString(settingName, settingX + 5, settingY + 5, Color.WHITE);

                            checkBox.getToggleAnimation().update(4f, Easing.OUT_CUBIC);

                            checkBox.getToggleAnimation().setEnd(checkBox.isToggled() ? 1 : 0);

                            RoundedUtils.drawRect(
                                settingX - 5 + maxSettingWidth - 20,
                                settingY + 5,
                                20, 6,
                                3,
                                settingPanelColor
                            );

                            RoundedUtils.drawRect(
                                settingX - 5 + maxSettingWidth - 20,
                                settingY + 5,
                                checkBox.getToggleAnimation().getValue() * 20, 6,
                                3,
                                clickGuiColor
                            );

                            RoundedUtils.drawRect(
                                settingX - 5 + maxSettingWidth - 20 + checkBox.getToggleAnimation().getValue() * 14,
                                settingY + 5,
                                6, 6,
                                3,
                                Color.WHITE
                            );

                            totalSettingsHeight += 15 * visibleProgress;
                        }

                        default -> {}
                    }
                }

                ScissorUtils.disableScissor();

                totalElementsHeight = totalSettingsHeight;
            }

            totalElementsHeight += 5;
        }

        ScissorUtils.disableScissor();
    }

    private Color getBaseColor(Module module, boolean hovered) {
        Color baseColor = new Color(0f, 0f, 0f, clickGui.backgroundAlpha.getValue() / 255f);

        if (hovered) {
            baseColor = new Color(baseColor.getRed() / 255f + 0.1f, baseColor.getGreen() / 255f + 0.1f, baseColor.getBlue() / 255f + 0.1f, clickGui.backgroundAlpha.getValue() / 255f);
        }

        if (module.isToggled()) {
            baseColor = new Color(baseColor.getRed() / 255f + 0.2f, baseColor.getGreen() / 255f + 0.2f, baseColor.getBlue() / 255f + 0.2f, clickGui.backgroundAlpha.getValue() / 255f);
        }
        return baseColor;
    }

    public boolean onMouse(int x, int y, int button) {
        if (GuiUtils.isHovered(x, y, backgroundX, backgroundY, backgroundWidth, panelHeaderHeight)) {
            switch (button) {
                case 0 -> {
                    moveOffset.set(x - backgroundX, y - backgroundY);
                    moving = true;
                    return true;
                }
                case 1 -> {
                    opened = !opened;
                    return true;
                }
            }
        }

        if (lastOpenedModule != null) {
            float nameY = backgroundY + panelHeaderHeight * openSettingsAnim.getValue() + 1;

            if (GuiUtils.isHovered(x, y, backgroundX, nameY - 6, backgroundWidth, 15) && button == 0) {
                openedModule = null;
                openSettingsAnim.setEnd(0);
                return true;
            }
        }

        float maxSettingWidth = backgroundWidth - xOffset * 2;

        if (!opened) return false;

        float totalElementHeight = 0;
        for (Module module : modules) {
            if (openSettingsAnim.getValue() == 0) {
                float moduleX = backgroundX + xOffset;
                float moduleY = backgroundY + yOffset + totalElementHeight + modulesScrollAnim.getValue();
                float moduleWidth = backgroundWidth - xOffset * 2;
                float moduleHeight = 20;

                boolean hovered = GuiUtils.isHovered(x, y, moduleX, moduleY, moduleWidth, moduleHeight);

                if (hovered) {
                    switch (button) {
                        case 0 -> module.toggle();
                        case 1 -> {
                            if (!module.getSettings().isEmpty()) {
                                openedModule = module;
                                lastOpenedModule = module;
                                settingsScroll = 0;
                                settingsScrollAnim.setValue(0);
                                openSettingsAnim.setEnd(1);
                            }
                        }
                    }
                    return true;
                }
            } else if (openSettingsAnim.getValue() > 0) {
                float totalSettingsHeight = 0;

                for (Setting setting : lastOpenedModule.getSettings()) {
                    float settingX = backgroundX + xOffset - (1 - openSettingsAnim.getValue()) * backgroundWidth;
                    float settingY = backgroundY + yOffset + totalSettingsHeight + settingsScrollAnim.getValue();

                    ClientFontRenderer textFont = Client.INST.getFonts().fonts.get("SFProRegular");

                    float settingNameWidth = (float) textFont.getStringWidth(setting.getName()) + 2;

                    if (setting.getVisibleAnim().getValue() == 0) continue;

                    float visibleProgress = setting.getVisibleAnim().getValue();

                    switch (setting) {
                        case FloatSetting _, IntegerSetting _ -> totalSettingsHeight += 15;
                        case CheckBox checkBox -> {
                            if (GuiUtils.isHovered(x, y, settingX - 5 + maxSettingWidth - 20, settingY + 5, 20, 6) && button == 0) {
                                checkBox.setToggled(!checkBox.isToggled());
                                return true;
                            }

                            totalSettingsHeight += 15 * visibleProgress;
                        }

                        default -> {}
                    }
                }
            }
            totalElementHeight += 25;
        }
        totalElementHeight += 5;

        return false;
    }

    public void onMouseRelease(int x, int y, int button) {
        moving = false;
    }

    public boolean keyTyped(char ch, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            boolean aa = openedModule != null;
            openedModule = null;
            openSettingsAnim.setEnd(0);
            return aa;
        }

        return false;
    }

}
