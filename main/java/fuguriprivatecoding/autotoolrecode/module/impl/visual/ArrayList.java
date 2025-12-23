package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, description = "Показывает список включенных модулей.")
public class ArrayList extends Module {

    final Mode fonts = new Mode("Fonts", this);

    final Mode pos = new Mode("Positions",this)
            .addModes("LeftUp", "RightUp")
            .setMode("RightUp")
            ;

    final FloatSetting animSpeed = new FloatSetting("AnimSpeed", this,0.1f, 10, 2, 0.1f);

    final IntegerSetting xPos = new IntegerSetting("XPos", this,0, 100, 5);
    final IntegerSetting yPos = new IntegerSetting("YPos", this,0, 100, 5);

    final FloatSetting textYPos = new FloatSetting("TextYPos", this,-5f, 5, 0, 0.01f);
    final ColorSetting textColor = new ColorSetting("TextColor", this, 40);
    final CheckBox textShadow = new CheckBox("TextShadow", this, true);

    final CheckBox background = new CheckBox("Background", this);
    final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, background::isToggled, 40);

    final IntegerSetting verticalSpacing = new IntegerSetting("VerticalSpacing", this, 1,25,12);
    final IntegerSetting horizontalSpacing = new IntegerSetting("HorizontalSpacing", this, background::isToggled, -10,10,2);

    final CheckBox glow = new CheckBox("Glow", this, false);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, () -> background.isToggled() && glow.isToggled(), 40);

    final CheckBox blur = new CheckBox("Blur", this, false);

    public ArrayList() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRegular");
    }

    ClientFont font = Fonts.fonts.get("SFProRegular");

    @Override
    public void onEvent(Event event) {
        font = Fonts.fonts.get(fonts.getMode());
        if (event instanceof Render2DEvent) {
            List<Module> moduleList = new CopyOnWriteArrayList<>(Modules.getEnabledModules());
            ScaledResolution sc = new ScaledResolution(mc);

            sort(moduleList, font);

            float xOffset = this.xPos.getValue() + horizontalSpacing.getValue();
            float yOffset = this.yPos.getValue();
            for (Module module : moduleList) {
                EasingAnimation slideAnim = module.getArrayListAnim();
                slideAnim.update(animSpeed.getValue(), Easing.OUT_BACK);

                float width = font.getStringWidth(module.getName()) + horizontalSpacing.getValue() * 2 + 8;

                float slideValue = slideAnim.getValue();
                float alpha = slideAnim.getClampValue();

                drawLine(
                    xOffset - width + slideValue * width,
                    yOffset,
                    module,
                    sc,
                    moduleList,
                    alpha,
                    pos.is("LeftUp")
                );

                yOffset += verticalSpacing.getValue() * slideValue;
            }
        }
    }

    private void drawLine(float xOffset, float yOffset, Module module, ScaledResolution sc, List<Module> moduleList, float alpha, boolean left) {
        float textWidth = font.getStringWidth(module.getName());
        float textHeight = verticalSpacing.getValue();

        float textX = left ? xOffset + 0.5f :
            sc.getScaledWidth() - xOffset - textWidth + 0.5f;

        float textY = yOffset - textYPos.getValue() + textHeight / 2f - 2f;

        float bgX = left ? xOffset - horizontalSpacing.getValue() :
            sc.getScaledWidth() - xOffset - horizontalSpacing.getValue() - textWidth;

        float bgWidth = textWidth + horizontalSpacing.getValue() * 2f;

        Color mixedTextColor = textColor.getMixedColor(moduleList.indexOf(module));
        Color mixedBgColor = backgroundColor.getMixedColor(moduleList.indexOf(module));
        Color mixedGlowColor = glowColor.getMixedColor(moduleList.indexOf(module));

        Color textColor = new Colors(mixedTextColor).withMultiplyAlphaClamp(alpha);
        Color bgColor = new Colors(mixedBgColor).withMultiplyAlphaClamp(alpha);
        Color glowBgColor = new Colors(mixedGlowColor).withMultiplyAlphaClamp(alpha);

        if (background.isToggled()) {
            Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, bgColor.getRGB());

            if (glow.isToggled()) {
                BloomUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, glowBgColor.getRGB()));
            }

            if (blur.isToggled()) {
                BlurUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, Colors.WHITE.withAlpha(alpha).getRGB()));
            }
        }

        font.drawString(module.getName(), textX, textY, textColor, textShadow.isToggled());
    }

    void sort(final List<Module> toSort, ClientFont font) {
        toSort.sort((m1, m2) -> {
            final double width1 = font.getStringWidth(m1.getName());
            final double width2 = font.getStringWidth(m2.getName());

            return Double.compare(width2, width1);
        });
    }
}
