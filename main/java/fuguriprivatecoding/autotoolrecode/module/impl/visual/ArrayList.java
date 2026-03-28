package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
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

    final IntegerSetting fontScale = new IntegerSetting("FontScale", this, 1, 12,8);

    final CheckBox background = new CheckBox("Background", this);
    final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, background::isToggled, 40);

    final IntegerSetting verticalSpacing = new IntegerSetting("VerticalSpacing", this, 1,25,12);
    final IntegerSetting horizontalSpacing = new IntegerSetting("HorizontalSpacing", this, background::isToggled, -10,10,2);

    final CheckBox glow = new CheckBox("Glow", this, false);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, () -> background.isToggled() && glow.isToggled(), 40);

    final CheckBox blur = new CheckBox("Blur", this, false);

    public ArrayList() {
        Fonts.FONTS.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRegular");
    }

    MsdfFont font = Fonts.get("SFProRegular");

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            font = Fonts.get(fonts.getMode());
            List<Module> moduleList = new CopyOnWriteArrayList<>(Modules.getEnabledModules());
            ScaledResolution sc = new ScaledResolution(mc);

            int size = fontScale.getValue();

            float hSpacing = horizontalSpacing.getValue();
            float vSpacing = verticalSpacing.getValue();

            sort(moduleList, font, size);

            float xOffset = this.xPos.getValue() + hSpacing;
            float yOffset = this.yPos.getValue();
            for (Module module : moduleList) {
                EasingAnimation slideAnim = module.getArrayListAnim();
                slideAnim.update(animSpeed.getValue(), Easing.OUT_CUBIC);

                float width = font.width(module.getName(), size) + hSpacing * 2 + 8;

                float slideValue = slideAnim.getValue();
                float alpha = slideAnim.getClampValue();
                float moduleTextHeight = font.height(module.getName(), size);

                drawLine(
                    xOffset - width + slideValue * width,
                    yOffset,
                    module,
                    sc,
                    moduleList,
                    alpha,
                    pos.is("LeftUp"),
                    size
                );

                yOffset += (moduleTextHeight + vSpacing) * slideValue;
            }
        }
    }

    private void drawLine(float xOffset, float yOffset, Module module, ScaledResolution sc, List<Module> moduleList, float alpha, boolean left, int size) {
        float textWidth = font.width(module.getName(), size);
        float textHeight = font.height(module.getName(), size) + verticalSpacing.getValue();

        float textX = left ? xOffset :
            sc.getScaledWidth() - xOffset - textWidth;

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
            ColorUtils.glColor(bgColor);
            Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, bgColor.getRGB());

            if (glow.isToggled()) {
                BloomUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, glowBgColor.getRGB()));
            }

            if (blur.isToggled()) {
                BlurUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, Colors.WHITE.withAlpha(alpha).getRGB()));
            }
        }

        font.draw(module.getName(), textX, textY, size, 0, 0, textColor);
    }

    void sort(final List<Module> toSort, MsdfFont font, float scale) {
        toSort.sort((m1, m2) -> {
            final double width1 = font.width(m1.getName(), scale);
            final double width2 = font.width(m2.getName(), scale);

            return Double.compare(width2, width1);
        });
    }
}
