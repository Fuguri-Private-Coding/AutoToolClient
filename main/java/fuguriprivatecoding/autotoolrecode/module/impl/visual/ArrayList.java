package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, description = "Показывает список включенных модулей.")
public class ArrayList extends Module {

    IntegerSetting yPosOffset = new IntegerSetting("Y-Pos Offset", this,0, 100, 0);
    IntegerSetting xPosOffset = new IntegerSetting("X-Pos Offset", this,0, 100, 0);

    Mode fonts = new Mode("Fonts", this)
            .addModes("MuseoSans", "Roboto", "JetBrains", "SFPro")
            .setMode("MuseoSans")
            ;

    Mode pos = new Mode("Positions",this)
            .addModes("Left Up", "Right Up")
            .setMode("Left Up")
            ;

    FloatSetting animationSpeed = new FloatSetting("Animation Speed", this, 0,10,3,0.1f);

    final ColorSetting textColor = new ColorSetting("Text Color", this);
    FloatSetting textYOffset = new FloatSetting("Text Y-Offset", this,-5f, 5, 0, 0.01f);
    CheckBox shadow = new CheckBox("Text Shadow", this, true);
    IntegerSetting verticalSpacing = new IntegerSetting("Vertical Spacing", this, 1,25,0);

    CheckBox glow = new CheckBox("Glow", this, false);
    CheckBox blur = new CheckBox("Blur", this, false);

    CheckBox background = new CheckBox("Background",this, true);
    final ColorSetting bgColor = new ColorSetting("Background Color", this, () -> background.isToggled());

    final ColorSetting bgColorShadow = new ColorSetting("Background Shadow Color", this, () -> background.isToggled());

    CheckBox line = new CheckBox("Line",this, true);
    final ColorSetting lineColor = new ColorSetting("Line Color", this, () -> line.isToggled());

    Color fadeTextColor;
    Color fadeBackgroundColor;
    Color fadeBackgroundShadowColor;
    Color fadeLineColor;

    ClientFontRenderer font = Client.INST.getFonts().fonts.get("JetBrains");

    @EventTarget
    public void onEvent(Event event) {
        if (!font.name.equalsIgnoreCase(fonts.getMode())) font = Client.INST.getFonts().fonts.get(fonts.getMode());
        if (event instanceof Render2DEvent) {
            List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INST.getModuleManager().getEnabledModules());
            ScaledResolution sc = new ScaledResolution(mc);

            sort(moduleList, font);

            float yOffset = yPosOffset.getValue();
            double xOffset = xPosOffset.getValue();
            for (Module module : moduleList) {
                updateColors(moduleList, module);

                module.getAnimation().update(animationSpeed.getValue(), Easing.OUT_BACK);

                switch (pos.getMode()) {
                    case "Right Up" -> renderRightUp(xOffset + module.getAnimation().getValue(), yOffset, module, sc, fadeBackgroundColor, fadeLineColor, fadeTextColor);
                    case "Left Up" -> renderLeftUp(xOffset + module.getAnimation().getValue(), yOffset, module, fadeBackgroundColor, fadeLineColor, fadeTextColor);
                }
                yOffset += verticalSpacing.getValue();
            }
        }
    }

    private void renderRightUp(double xOffset, double yOffset, Module module, ScaledResolution sc, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor) {
        String moduleText = module.getName();
        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, fadeBackgroundShadowColor.getRGB());
                } else {
                    font.drawString(moduleText, (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
                }
                if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, fadeBackgroundShadowColor.getRGB());
            });
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, Color.WHITE.getRGB());
                if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, Color.WHITE.getRGB());
            });
        }
        if (background.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, fadeBackgroundColor.getRGB());
        font.drawString(moduleText, (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, fadeLineColor.getRGB());
    }

    private void renderLeftUp(double xOffset, double yOffset, Module module, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor) {
        String moduleText = module.getName();
        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText) + 4 + xOffset), (float) yOffset, fadeBackgroundShadowColor.getRGB());
                } else {
                    font.drawString(moduleText, (float) (2.5f + xOffset), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, Color.WHITE, shadow.isToggled());
                }
                if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, fadeBackgroundShadowColor.getRGB());
            });
        }
        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText) + 4 + xOffset), (float) yOffset, Color.WHITE.getRGB());
                if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, Color.WHITE.getRGB());
            });
        }
        if (background.isToggled()) Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText) + 4 + xOffset), (float) yOffset, fadeBackgroundColor.getRGB());
        font.drawString(moduleText, (float) (2.5f + xOffset), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, fadeLineColor.getRGB());
    }

    private void updateColors(List<Module> moduleList, Module module) {
        fadeTextColor = textColor.isFade() ? ColorUtils.mixColor(
                textColor.getColor(), textColor.getFadeColor(), moduleList.indexOf(module),
                textColor.getOffset(), textColor.getSpeed()) : textColor.getColor();

        if (background.isToggled()) {
            fadeBackgroundColor = bgColor.isFade() ? ColorUtils.mixColor(
                    bgColor.getColor(), bgColor.getFadeColor(), moduleList.indexOf(module),
                    bgColor.getOffset(), bgColor.getSpeed()) : bgColor.getColor();

            fadeBackgroundShadowColor = bgColorShadow.isFade() ? ColorUtils.mixColor(
                    bgColorShadow.getColor(), bgColorShadow.getFadeColor(), moduleList.indexOf(module),
                    bgColorShadow.getOffset(), bgColorShadow.getSpeed()) : bgColorShadow.getColor();
        }

        if (line.isToggled()) {
            fadeLineColor = lineColor.isFade() ? ColorUtils.mixColor(
                    lineColor.getColor(), lineColor.getFadeColor(), moduleList.indexOf(module),
                    lineColor.getOffset(), lineColor.getSpeed()) : lineColor.getColor();
        }
    }

    void sort(final List<Module> toSort, final ClientFontRenderer fontToCalcWidth) {
        toSort.sort( (m1, m2) -> {
            final double width1 = fontToCalcWidth.getStringWidth(m1.getName());
            final double width2 = fontToCalcWidth.getStringWidth(m2.getName());

            return Double.compare(width2, width1);
        });
    }
}
