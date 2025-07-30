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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.gui.FontRenderer;
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
            .addModes("MuseoSans", "Roboto", "JetBrains")
            .setMode("MuseoSans")
            ;

    Mode pos = new Mode("Positions",this)
            .addModes("Left Up", "Right Up")
            .setMode("Left Up")
            ;

    CheckBox textFade = new CheckBox("Text Fade", this, false);
    ColorSetting textColor1 = new ColorSetting("Text Color1", this, 1,1,1,1);
    ColorSetting textColor2 = new ColorSetting("Text Color2", this,() -> textFade.isToggled(), 0,0,0,1);
    FloatSetting textColorOffset = new FloatSetting("Text Color Offset", this,() -> textFade.isToggled(),0f, 50, 1, 0.1f);
    FloatSetting textYOffset = new FloatSetting("Text Y-Offset", this,-5f, 5, 2, 0.01f);
    FloatSetting textSpeed = new FloatSetting("Text Speed", this,() -> textFade.isToggled(),0.1f, 20, 1, 0.1f);
    CheckBox shadow = new CheckBox("Text Shadow", this, true);

    CheckBox background = new CheckBox("Background",this, true);
    CheckBox bgFade = new CheckBox("Background Fade", this, () -> background.isToggled(), false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, () -> background.isToggled(), 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> background.isToggled() && bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgColorOffset = new FloatSetting("Background Color Offset", this,() -> background.isToggled() && bgFade.isToggled(),0f, 50, 1, 0.1f);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> background.isToggled() && bgFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox line = new CheckBox("Line",this, true);
    CheckBox lineFade = new CheckBox("Line Fade", this, () -> line.isToggled(), false);
    ColorSetting lineColor1 = new ColorSetting("Line Color1", this,() -> line.isToggled(), 0,0,0,1);
    ColorSetting lineColor2 = new ColorSetting("Line Color2", this,() -> line.isToggled() && lineFade.isToggled(), 0,0,0,1);
    FloatSetting lineColorOffset = new FloatSetting("Line Color Offset", this,() -> line.isToggled() && lineFade.isToggled(),0f, 50, 1, 0.1f);
    FloatSetting lineSpeed = new FloatSetting("Line Speed", this,() -> line.isToggled() && lineFade.isToggled(),0.1f, 20, 1, 0.1f);

    Glow shadows;

    Color fadeTextColor;
    Color fadeBackgroundColor;
    Color fadeLineColor;

    ClientFontRenderer font = Client.INST.getFonts().fonts.get("JetBrains");

    @EventTarget
    public void onEvent(Event event) {
        if (!font.name.equalsIgnoreCase(fonts.getMode())) font = Client.INST.getFonts().fonts.get(fonts.getMode());
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (event instanceof Render2DEvent) {
            List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INST.getModuleManager().getEnabledModules());
            ScaledResolution sc = new ScaledResolution(mc);

            sort(moduleList, font);

            double yOffset = yPosOffset.getValue();
            double xOffset = xPosOffset.getValue();
            for (Module module : moduleList) {
                updateColors(moduleList, module);

                double yFinalOffset = yOffset;
                switch (pos.getMode()) {
                    case "Right Up" -> {
                        if (shadows.isToggled() && shadows.module.get("ArrayList")) {
                            BloomUtils.addToDraw(() -> renderRightUp(xOffset, yFinalOffset, module, sc, Color.white, Color.white, Color.white));
                        }
                        renderRightUp(xOffset, yFinalOffset, module, sc, fadeBackgroundColor, fadeLineColor, fadeTextColor);
                    }

                    case "Left Up" -> {
                        if (shadows.isToggled() && shadows.module.get("ArrayList")) {
                            BloomUtils.addToDraw(() -> renderLeftUp(xOffset, yFinalOffset, module, Color.white, Color.white, Color.white));
                        }
                        renderLeftUp(xOffset, yFinalOffset, module, fadeBackgroundColor, fadeLineColor, fadeTextColor);
                    }
                }
                yOffset += 13;
            }
        }
    }

    private void renderRightUp(double xOffset, double yOffset, Module module, ScaledResolution sc, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor) {
        String moduleText = module.getName();
        if (background.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 4f), (float) (yOffset + 13f), sc.getScaledWidth() - xPosOffset.getValue(), (float) yOffset, fadeBackgroundColor.getRGB());
        font.drawString(moduleText, (float) (sc.getScaledWidth() - xOffset - (float) font.getStringWidth(moduleText) - 1.75f), (float) (2.5f + yOffset) + textYOffset.getValue(), fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (sc.getScaledWidth() - xOffset), (float) (yOffset + 13f), sc.getScaledWidth() - xPosOffset.getValue() + 2, (float) yOffset, fadeLineColor.getRGB());
    }

    private void renderLeftUp(double xOffset, double yOffset, Module module, Color fadeBackgroundColor, Color fadeLineColor, Color fadeTextColor) {
        String moduleText = module.getName();
        if (background.isToggled()) Gui.drawRect((int) xOffset,(float) yOffset + 13f, (float) font.getStringWidth(moduleText) + 4 + xPosOffset.getValue(), (float) yOffset, fadeBackgroundColor.getRGB());
        font.drawString(moduleText, (float) (2.5f + xOffset), (float) (2.5f + yOffset) + textYOffset.getValue(), fadeTextColor, shadow.isToggled());
        if (line.isToggled()) Gui.drawRect((int) (xOffset - 2), (float) yOffset + 13, xPosOffset.getValue(), (float) yOffset, fadeLineColor.getRGB());
    }

    private void updateColors(List<Module> moduleList, Module module) {
        fadeTextColor = textFade.isToggled() ? ColorUtils.mixColor(
                textColor1.getColor(), textColor2.getColor(), moduleList.indexOf(module),
                textColorOffset.getValue(), textSpeed.getValue()) : textColor1.getColor();

        if (background.isToggled()) {
            fadeBackgroundColor = bgFade.isToggled() ? ColorUtils.mixColor(
                    bgColor1.getColor(), bgColor2.getColor(), moduleList.indexOf(module),
                    bgColorOffset.getValue(), bgSpeed.getValue()) : bgColor1.getColor();
        }

        if (line.isToggled()) {
            fadeLineColor = lineFade.isToggled() ? ColorUtils.mixColor(
                    lineColor1.getColor(), lineColor2.getColor(), moduleList.indexOf(module),
                    lineColorOffset.getValue(), lineSpeed.getValue()) : lineColor1.getColor();
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
