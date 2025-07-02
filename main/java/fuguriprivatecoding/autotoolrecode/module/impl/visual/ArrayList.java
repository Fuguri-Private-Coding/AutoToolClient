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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL)
public class ArrayList extends Module {

    IntegerSetting yPosOffset = new IntegerSetting("Y-Pos Offset", this,0, 100, 0);
    IntegerSetting xPosOffset = new IntegerSetting("X-Pos Offset", this,0, 100, 0);

    Mode pos = new Mode("Positions",this)
            .addModes("Right Up", "Left Up")
            .setMode("Left Up")
            ;

    CheckBox fade = new CheckBox("Text Fade", this, false);
    ColorSetting color1 = new ColorSetting("Text Color1", this, 0,0,0,1);
    ColorSetting color2 = new ColorSetting("Text Color2", this,() -> fade.isToggled(), 0,0,0,1);
    FloatSetting colorOffset = new FloatSetting("Text Offset", this,() -> fade.isToggled(),0.1f, 50, 1, 0.1f);
    FloatSetting speed = new FloatSetting("Text Speed", this,() -> fade.isToggled(),0.1f, 20, 1, 0.1f);
    CheckBox shadow = new CheckBox("Text Shadow", this, true);

    CheckBox background = new CheckBox("Background",this, true);
    CheckBox bgFade = new CheckBox("Background Fade", this, () -> background.isToggled(), false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, () -> background.isToggled(), 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> background.isToggled() && bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgColorOffset = new FloatSetting("Background Offset", this,() -> background.isToggled() && bgFade.isToggled(),0.1f, 50, 1, 0.1f);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> background.isToggled() && bgFade.isToggled(),0.1f, 20, 1, 0.1f);

    Shadows shadows;

    Color fadeTextColor;
    Color fadeBackgroundColor;

    FontRenderer font = mc.fontRendererObj;
    ScaledResolution sc = new ScaledResolution(mc);

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render2DEvent) {
            List<Module> moduleList = new CopyOnWriteArrayList<>(Client.INST.getModuleManager().getEnabledModules());

            sort(moduleList, font);

            double offset = yPosOffset.getValue();
            for (Module module : moduleList) {
                fadeTextColor = fade.isToggled() ? ColorUtils.mixColor(
                        color1.getColor(), color2.getColor(), moduleList.indexOf(module),
                        colorOffset.getValue(), speed.getValue()) : color1.getColor();

                if (background.isToggled()) {
                    fadeBackgroundColor = bgFade.isToggled() ? ColorUtils.mixColor(
                            bgColor1.getColor(), bgColor2.getColor(), moduleList.indexOf(module),
                            bgColorOffset.getValue(), bgSpeed.getValue()) : bgColor1.getColor();
                }

                double finalOffset = offset;
                switch (pos.getMode()) {
                    case "Right Up" -> {
                        if (shadows.isToggled() && shadows.module.get("ArrayList")) {
                            BloomUtils.addToDraw(() -> {
                                if (background.isToggled()) {
                                    Gui.drawRect(sc.getScaledWidth() - xPosOffset.getValue() - (float) font.getStringWidth(module.getName()) - 4f, (float) (finalOffset + 13f), sc.getScaledWidth() - xPosOffset.getValue(), (float) finalOffset, -1);
                                } else {
                                    font.drawString(module.getName(), sc.getScaledWidth() - xPosOffset.getValue() - (float) font.getStringWidth(module.getName()) - 1.75f, (float) (2.5f + finalOffset), -1, shadow.isToggled());
                                }
                            });
                        }
                        if (background.isToggled()) Gui.drawRect(sc.getScaledWidth() - xPosOffset.getValue() - (float) font.getStringWidth(module.getName()) - 4f, (float) (offset + 13f), sc.getScaledWidth() - xPosOffset.getValue(), (float) offset, fadeBackgroundColor.getRGB());
                        font.drawString(module.getName(), sc.getScaledWidth() - xPosOffset.getValue() - (float) font.getStringWidth(module.getName()) - 1.75f, (float) (2.5f + offset), fadeTextColor.getRGB(), shadow.isToggled());
                    }

                    case "Left Up" -> {
                        if (shadows.isToggled() && shadows.module.get("ArrayList")) {
                            BloomUtils.addToDraw(() -> {
                                if (background.isToggled()) {
                                    Gui.drawRect(xPosOffset.getValue(), (float) finalOffset + 13, (float) font.getStringWidth(module.getName()) + 4 + xPosOffset.getValue(), (float) finalOffset, -1);
                                } else {
                                    font.drawString(module.getName(), 2.5f + xPosOffset.getValue(), (float) (2.5f + finalOffset), -1, shadow.isToggled());
                                }
                            });
                        }
                        if (background.isToggled()) Gui.drawRect(xPosOffset.getValue(),(float) offset + 13f, (float) font.getStringWidth(module.getName()) + 4 + xPosOffset.getValue(), (float) offset, fadeBackgroundColor.getRGB());
                        font.drawString(module.getName(), 2.5f + xPosOffset.getValue(), (float) (2.5f + offset), fadeTextColor.getRGB(), shadow.isToggled());
                    }
                }
                offset += 13;
            }
        }
    }

    void sort(final List<Module> toSort, final FontRenderer fontToCalcWidth) {
        toSort.sort( (m1, m2) -> {
            final double width1 = fontToCalcWidth.getStringWidth(m1.getName());
            final double width2 = fontToCalcWidth.getStringWidth(m2.getName());

            return Double.compare(width2, width1);
        });
    }
}
