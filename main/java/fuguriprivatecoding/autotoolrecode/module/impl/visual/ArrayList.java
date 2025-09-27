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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.StencilUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, description = "Показывает список включенных модулей.")
public class ArrayList extends Module {

    IntegerSetting yPosOffset = new IntegerSetting("Y-Pos Offset", this,0, 100, 0);
    IntegerSetting xPosOffset = new IntegerSetting("X-Pos Offset", this,0, 100, 0);

    Mode fonts = new Mode("Fonts", this);

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
    IntegerSetting horizontalSpacing = new IntegerSetting("Horizontal Spacing", this, -10,10,0);

    CheckBox glow = new CheckBox("Glow", this, false);
    CheckBox blur = new CheckBox("Blur", this, false);

    CheckBox background = new CheckBox("Background",this, true);
    final ColorSetting bgColor = new ColorSetting("Background Color", this, () -> background.isToggled());

    final ColorSetting bgColorShadow = new ColorSetting("Background Shadow Color", this, () -> background.isToggled());

    CheckBox line = new CheckBox("Line",this, true);
    final ColorSetting lineColor = new ColorSetting("Line Color", this, () -> line.isToggled());

    ClientFontRenderer font = Client.INST.getFonts().fonts.get("JetBrains");

    public ArrayList() {
        Client.INST.getFonts().fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

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
                module.getArrayListAnimation().update(animationSpeed.getValue(), Easing.LINEAR);

                module.updateToggleAnimation();

                switch (pos.getMode()) {
                    case "Right Up" -> renderRightUp(xOffset + module.getArrayListAnimation().getValue(), yOffset,
                        module,
                        sc,
                        lineColor.getMixedColor(moduleList.indexOf(module)),
                        textColor.getMixedColor(moduleList.indexOf(module)),
                        moduleList
                    );

                    case "Left Up" -> renderLeftUp(xOffset + module.getArrayListAnimation().getValue(), yOffset,
                        module,
                        lineColor.getMixedColor(moduleList.indexOf(module)),
                        textColor.getMixedColor(moduleList.indexOf(module)),
                        moduleList
                    );
                }
                yOffset += verticalSpacing.getValue() * module.getToggleProgress();
            }
        }
    }

    private void renderRightUp(double xOffset, double yOffset, Module module, ScaledResolution sc, Color fadeLineColor, Color fadeTextColor, List<Module> moduleList) {
        String moduleText = module.getName();
        boolean suffixCondition = suffix.isToggled() && !"".equals(module.getSuffix());

        String fullText = moduleText + (suffixCondition ? " - " + module.getSuffix() : "");
        float textWidth = (float) font.getStringWidth(fullText);

        float increasingWidth = horizontalSpacing.getValue();

        float backgroundStartX = (float) (sc.getScaledWidth() - xOffset - textWidth - 4f - increasingWidth);
        float backgroundStartXыыы = (float) (sc.getScaledWidth() - 6 - textWidth - 4f - increasingWidth);
        float backgroundEndX = (float) (sc.getScaledWidth() - xOffset);
        float backgroundEndXыыы = (float) (sc.getScaledWidth() - 6);
        float textX = backgroundStartX + 2.25f + increasingWidth / 2f;

        float backgroundHeight = verticalSpacing.getValue();
        float textY = (float) (2.5f + yOffset + textYOffset.getValue() - 4.5f + backgroundHeight / 2f);

        int moduleIndex = moduleList.indexOf(module);
        Color bgShadowColor = bgColorShadow.getMixedColor(moduleIndex);
        Color bgMainColor = bgColor.getMixedColor(moduleIndex);

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, bgShadowColor.getRGB());
                } else {
                    font.drawString(moduleText, textX, textY, fadeTextColor, shadow.isToggled());
                }

                if (line.isToggled()) {
                    Gui.drawRect((int) backgroundEndX, (int) (yOffset + backgroundHeight), (int) (backgroundEndX + 2), (int) yOffset, bgShadowColor.getRGB());
                }
            });
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, Color.WHITE.getRGB());
                }

                if (line.isToggled()) {
                    Gui.drawRect((int) backgroundEndX, (int) (yOffset + backgroundHeight), (int) (backgroundEndX + 2), (int) yOffset, Color.WHITE.getRGB());
                }
            });
        }

        if (background.isToggled()) {
            Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, bgMainColor.getRGB());
        }

        font.drawString(moduleText, textX, textY, fadeTextColor, shadow.isToggled());

        if (suffixCondition) {
            String suffixText = " - " + module.getSuffix();
            float suffixX = (float) (backgroundEndX - font.getStringWidth(suffixText) - 1.75f);
            font.drawString(suffixText, suffixX, textY, suffixColor.getFadedColor(), shadow.isToggled());
        }

        if (line.isToggled()) {
            Gui.drawRect((int) backgroundEndX, (int) (yOffset + backgroundHeight), (int) (backgroundEndX + 2), (int) yOffset, fadeLineColor.getRGB());
        }
    }

    private void renderLeftUp(double xOffset, double yOffset, Module module, Color fadeLineColor, Color fadeTextColor, List<Module> moduleList) {
        String moduleText = module.getName();
        boolean suffixCondition = suffix.isToggled() && !"".equals(module.getSuffix());

        String fullText = moduleText + (suffixCondition ? " - " + module.getSuffix() : "");
        float textWidth = (float) font.getStringWidth(fullText);

        float increasingWidth = horizontalSpacing.getValue();

        float backgroundStartX = (float) xOffset;
        float backgroundEndX = (float) (xOffset + textWidth + 4f + increasingWidth);
        float textX = (float) (xOffset + 2.5f + increasingWidth / 2f);

        float backgroundHeight = verticalSpacing.getValue();
        float textY = (float) (2.5f + yOffset + textYOffset.getValue() - 4.5f + backgroundHeight / 2f);

        int moduleIndex = moduleList.indexOf(module);
        Color bgShadowColor = bgColorShadow.getMixedColor(moduleIndex);
        Color bgMainColor = bgColor.getMixedColor(moduleIndex);

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, bgShadowColor.getRGB());
                } else {
                    font.drawString(moduleText, textX, textY, Color.WHITE, shadow.isToggled());
                }

                if (line.isToggled()) {
                    Gui.drawRect((int) (backgroundStartX - 2), (int) (yOffset + backgroundHeight), (int) backgroundStartX, (int) yOffset, bgShadowColor.getRGB());
                }
            });
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> {
                if (background.isToggled()) {
                    Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, Color.WHITE.getRGB());
                }

                if (line.isToggled()) {
                    Gui.drawRect((int) (backgroundStartX - 2), (int) (yOffset + backgroundHeight), (int) backgroundStartX, (int) yOffset, Color.WHITE.getRGB());
                }
            });
        }

        if (background.isToggled()) {
            Gui.drawRect((int) backgroundStartX, (int) (yOffset + backgroundHeight), (int) backgroundEndX, (int) yOffset, bgMainColor.getRGB());
        }

        if (suffixCondition) {
            float suffixX = (float) (textX + font.getStringWidth(moduleText));
            font.drawString(" - " + module.getSuffix(), suffixX, textY, suffixColor.getFadedColor(), shadow.isToggled());
        }

        font.drawString(moduleText, textX, textY, fadeTextColor, shadow.isToggled());

        if (line.isToggled()) {
            Gui.drawRect((int) (backgroundStartX - 2), (int) (yOffset + backgroundHeight), (int) backgroundStartX, (int) yOffset, fadeLineColor.getRGB());
        }
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
