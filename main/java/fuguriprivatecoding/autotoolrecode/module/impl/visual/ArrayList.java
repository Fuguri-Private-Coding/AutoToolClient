package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
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

    CheckBox suffix = new CheckBox("Suffix",this, false);
    ColorSetting suffixColor = new ColorSetting("Suffix Color", this, suffix::isToggled);

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
                module.getAnimation().update(animationSpeed.getValue(), Easing.LINEAR);

                switch (pos.getMode()) {
                    case "Right Up" ->
                            renderRightUp(xOffset + module.getAnimation().getValue(), yOffset,
                                module,
                                sc,
                                bgColor.getMixedColor(moduleList.indexOf(module)),
                                lineColor.getMixedColor(moduleList.indexOf(module)),
                                textColor.getMixedColor(moduleList.indexOf(module)),
                                moduleList
                    );

                    case "Left Up" ->
                        renderLeftUp(xOffset + module.getAnimation().getValue(), yOffset,
                                module,
                                bgColor.getMixedColor(moduleList.indexOf(module)),
                                lineColor.getMixedColor(moduleList.indexOf(module)),
                                textColor.getMixedColor(moduleList.indexOf(module)),
                                moduleList
                        );
                }
                yOffset += verticalSpacing.getValue();
            }
        }
    }

    private void renderRightUp(double xOffset, double yOffset, Module module, ScaledResolution sc, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor, List<Module> moduleList) {
        String moduleText = module.getName();

        boolean suffixCondition = suffix.isToggled() && !"".equals(module.getSuffix());

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText + (suffixCondition ?  " - " + module.getSuffix() : "")) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, bgColorShadow.getMixedColor(moduleList.indexOf(module)).getRGB());
                } else {
                    font.drawString(moduleText, (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText + (suffixCondition ?  " - " + module.getSuffix() : "")) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
                }
                if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, bgColorShadow.getMixedColor(moduleList.indexOf(module)).getRGB());
            });
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, Color.WHITE.getRGB());
                if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, Color.WHITE.getRGB());
            });
        }
        if (background.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText + (suffixCondition ?  " - " + module.getSuffix() : "")) - 4f), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset), (float) yOffset, bgColor.getMixedColor(moduleList.indexOf(module)).getRGB());
        if (suffixCondition) {
            font.drawString(" - " + module.getSuffix(), (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(" - " + module.getSuffix()) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, suffixColor.getFadedColor(), shadow.isToggled());
        }
        font.drawString(moduleText, (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText + (suffixCondition ?  " - " + module.getSuffix() : "")) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (int) (yOffset + verticalSpacing.getValue()), (int) (sc.getScaledWidth() - xOffset + 2), (float) yOffset, fadeLineColor.getRGB());
    }

    private void renderLeftUp(double xOffset, double yOffset, Module module, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor, List<Module> moduleList) {
        String moduleText = module.getName();

        boolean suffixCondition = suffix.isToggled() && !"".equals(module.getSuffix());

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText + (suffixCondition ? " - " + module.getSuffix() : "")) + 4 + xOffset), (float) yOffset, bgColorShadow.getMixedColor(moduleList.indexOf(module)).getRGB());
                } else {
                    font.drawString(moduleText, (float) (2.5f + xOffset), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, Color.WHITE, shadow.isToggled());
                }
                if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, bgColorShadow.getMixedColor(moduleList.indexOf(module)).getRGB());
            });
        }
        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText + (suffixCondition ? " - " + module.getSuffix() : "")) + 4 + xOffset), (float) yOffset, Color.WHITE.getRGB());
                if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, Color.WHITE.getRGB());
            });
        }
        if (background.isToggled()) Gui.drawRect((int) xOffset, (int) ((float) yOffset + verticalSpacing.getValue()), (int) ((float) font.getStringWidth(moduleText + (suffixCondition ? " - " + module.getSuffix() : "")) + 4 + xOffset), (float) yOffset, bgColor.getMixedColor(moduleList.indexOf(module)).getRGB());
        if (suffixCondition) {
            font.drawString(" - " + module.getSuffix(), (float) (2.5f + xOffset + font.getStringWidth(moduleText)), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, suffixColor.getFadedColor(), shadow.isToggled());
        }
        font.drawString(moduleText, (float) (2.5f + xOffset), (float) (2.5f + yOffset) + textYOffset.getValue() - 4.5f + verticalSpacing.getValue() / 2f, fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (int) ((float) yOffset + verticalSpacing.getValue()), (int) xOffset, (float) yOffset, fadeLineColor.getRGB());
    }

    void sort(final List<Module> toSort, final ClientFontRenderer fontToCalcWidth) {
        toSort.sort( (m1, m2) -> {
            boolean suffixConditionModule1 = suffix.isToggled() && !"".equals(m1.getSuffix());
            boolean suffixConditionModule2 = suffix.isToggled() && !"".equals(m2.getSuffix());

            final double width1 = fontToCalcWidth.getStringWidth(m1.getName() + (suffixConditionModule1 ? " - " + m1.getSuffix() : ""));
            final double width2 = fontToCalcWidth.getStringWidth(m2.getName() + (suffixConditionModule2 ? " - " + m2.getSuffix() : ""));

            return Double.compare(width2, width1);
        });
    }
}
