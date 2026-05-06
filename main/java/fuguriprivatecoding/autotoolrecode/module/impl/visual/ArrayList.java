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
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

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
    }

    MsdfFont font = Fonts.get("SFProRegular");

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent render2DEvent) {
            List<Module> moduleList = Modules.getEnabledModules();

            int size = fontScale.getValue();
            if (font != Fonts.get(fonts.getMode())) {
                font = Fonts.get(fonts.getMode());
            }

            sort(moduleList, size);

            List<RenderEntry> entries = new java.util.ArrayList<>();

            boolean left = pos.is("LeftUp");

            ScaledResolution sc = render2DEvent.getScaledResolution();

            float hSpacing = horizontalSpacing.getValue();
            float vSpacing = verticalSpacing.getValue();

            float xOffset = this.xPos.getValue() + hSpacing;
            float yOffset = this.yPos.getValue();
            for (Module module : moduleList.reversed()) {
                EasingAnimation anim = module.getArrayListAnim();
                anim.update(animSpeed.getValue(), Easing.OUT_BACK);

                float slideValue = anim.getValue();
                float height = font.height(module.getName(), size);

                float width = font.width(module.getName(), size) + hSpacing * 2 + 8;

                entries.add(new RenderEntry(module, yOffset, slideValue, width));
                yOffset += (height + vSpacing) * Math.clamp(slideValue, 0, 1);
            }

            if (glow.isToggled()) {
                BloomUtils.startWrite();
                for (RenderEntry e : entries) {
                    float x = xOffset - e.width() + e.slideValue() * e.width();
                    drawLine(x, e.yOffset(), e.module(), moduleList, sc, left, size, e.slideValue(), true, false);
                }
                BloomUtils.stopWrite();
            }

            if (blur.isToggled()) {
                BlurUtils.startWrite();
                for (RenderEntry e : entries) {
                    float x = xOffset - e.width() + e.slideValue() * e.width();
                    drawLine(x, e.yOffset(), e.module(), moduleList, sc, left, size, e.slideValue(), false, true);
                }
                BlurUtils.stopWrite();
            }

            for (RenderEntry e : entries) {
                float x = xOffset - e.width() + e.slideValue() * e.width();
                drawLine(x, e.yOffset(), e.module(), moduleList, sc, left, size, e.slideValue(), false, false);
            }
        }
    }

    private void drawLine(float xOffset, float yOffset, Module module, List<Module> moduleList, ScaledResolution sc, boolean left, int size, float alpha, boolean glow, boolean blur) {
        float textWidth = font.width(module.getName(), size);
        float textHeight = font.height(module.getName(), size) + verticalSpacing.getValue();

        float textX = left ? xOffset :
            sc.getScaledWidth() - xOffset - textWidth;

        float textY = yOffset + 2 + textHeight / 2f - size / 2f;

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
            Color color = glow ? glowBgColor : blur ? Colors.WHITE.withAlphaClamp(alpha) : bgColor;
            Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, color.getRGB());
        }

        if (!blur && !glow) font.draw(module.getName(), textX, textY, size, 0, 0, textColor);
    }

    private void sort(final List<Module> list, float scale) {
        list.sort(Comparator.comparingDouble(module -> font.width(module.getName(), scale)));
    }

    public record RenderEntry(Module module, float yOffset, float slideValue, float width) {};
}
