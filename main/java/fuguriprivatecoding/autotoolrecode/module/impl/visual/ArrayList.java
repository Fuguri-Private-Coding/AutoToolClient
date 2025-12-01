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
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ArrayList", category = Category.VISUAL, description = "Показывает список включенных модулей.")
public class ArrayList extends Module {

    Mode fonts = new Mode("Fonts", this);

    Mode pos = new Mode("Positions",this)
            .addModes("LeftUp", "RightUp")
            .setMode("RightUp")
            ;

    FloatSetting animSpeed = new FloatSetting("AnimSpeed", this,0.1f, 10, 5, 0.1f);

    IntegerSetting xOffset = new IntegerSetting("XOffset", this,0, 100, 0);
    IntegerSetting yOffset = new IntegerSetting("YOffset", this,0, 100, 0);

    ColorSetting textColor = new ColorSetting("TextsColor", this);
    FloatSetting textYOffset = new FloatSetting("TextYOffset", this,-5f, 5, 0, 0.01f);
    CheckBox textShadow = new CheckBox("TextShadow", this, true);

    CheckBox background = new CheckBox("Background", this);
    ColorSetting backgroundColor = new ColorSetting("BgsColor", this, background::isToggled);
    IntegerSetting verticalSpacing = new IntegerSetting("VerticalSpacing", this, 1,25,0);
    IntegerSetting horizontalSpacing = new IntegerSetting("HorizontalSpacing", this, background::isToggled, -10,10,0);

    FloatSetting scale = new FloatSetting("Scale", this,0.1f, 2, 1, 0.01f);

    CheckBox glow = new CheckBox("Glow", this, false);
    ColorSetting glowColor = new ColorSetting("GlowColor", this, () -> background.isToggled() && glow.isToggled());
    CheckBox blur = new CheckBox("Blur", this, false);

    public ArrayList() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRegular");
    }

    ClientFontRenderer font = Fonts.fonts.get(fonts.getMode());

    float width = 0;

    @Override
    public void onEvent(Event event) {
        font = Fonts.fonts.get(fonts.getMode());
        if (event instanceof Render2DEvent) {
            List<Module> moduleList = new CopyOnWriteArrayList<>(Modules.getEnabledModules());
            ScaledResolution sc = new ScaledResolution(mc);

            sort(moduleList, font);

            float xOffset = this.xOffset.getValue() + horizontalSpacing.getValue();
            float yOffset = this.yOffset.getValue();

            GL11.glPushMatrix();

            float x = pos.is("RightUp") ? sc.getScaledWidth() - (xOffset - horizontalSpacing.getValue() * 10) - width : xOffset - horizontalSpacing.getValue() * 10;

            double centerX = x + width / 2.0;
            double centerY = yOffset / 2.0;

            double offsetX = centerX * (1 - scale.getValue());
            double offsetY = centerY * (1 - scale.getValue());

            GL11.glTranslated(offsetX, offsetY, 0);
            GL11.glScaled(scale.getValue(), scale.getValue(), 1);

            for (Module module : moduleList) {
                EasingAnimation anim = module.getSlideAnim();
                anim.update(animSpeed.getValue(), Easing.OUT_CUBIC);

                float width = (float) font.getStringWidth(module.getName()) + horizontalSpacing.getValue() * 2 + 8;

                this.width = width;
                switch (pos.getMode()) {
                    case "RightUp" -> renderRightUp(
                        xOffset - width + anim.getValue() * width,
                        yOffset,
                        module,
                        sc,
                        moduleList,
                        anim.getValue()
                    );

                    case "LeftUp" -> renderLeftUp(xOffset - width + anim.getValue() * width,
                        yOffset,
                        module,
                        moduleList,
                        anim.getValue()
                    );
                }
                yOffset += verticalSpacing.getValue() * anim.getValue();
            }

            GL11.glPopMatrix();

        }
    }

    private void renderRightUp(float xOffset, float yOffset, Module module, ScaledResolution sc, List<Module> moduleList, float alpha) {
        float textWidth = (float) font.getStringWidth(module.getName());
        float textHeight = verticalSpacing.getValue();

        float textX = sc.getScaledWidth() - xOffset - textWidth + 0.5f;
        float textY = yOffset - textYOffset.getValue() + textHeight / 2f - 2f;

        float bgX = sc.getScaledWidth() - xOffset - horizontalSpacing.getValue() - textWidth;
        float bgWidth = textWidth + horizontalSpacing.getValue() * 2f;

        Color alphaTextColor = textColor.getMixedColor(moduleList.indexOf(module));
        Color alphaBgColor = backgroundColor.getMixedColor(moduleList.indexOf(module));
        Color alphaGlowBgColor = glowColor.getMixedColor(moduleList.indexOf(module));

        Color color = new Colors(alphaTextColor).withAlpha(alphaTextColor.getAlpha() / 255f * alpha);
        Color bgColor = new Colors(alphaBgColor).withAlpha(alphaBgColor.getAlpha() / 255f * alpha);
        Color glowbgColor = new Colors(alphaGlowBgColor).withAlpha(alphaGlowBgColor.getAlpha() / 255f * alpha);

        if (background.isToggled()) {
            Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, bgColor.getRGB());

            if (glow.isToggled()) {
                BloomUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, glowbgColor.getRGB()));
            }

            if (blur.isToggled()) {
                BlurUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, Colors.WHITE.withAlpha(alpha).getRGB()));
            }
        }

        font.drawString(module.getName(), textX, textY, color, textShadow.isToggled());
    }

    private void renderLeftUp(float xOffset, float yOffset, Module module, List<Module> moduleList, float alpha) {
        float textWidth = (float) font.getStringWidth(module.getName());
        float textHeight = verticalSpacing.getValue();

        float textX = xOffset + 0.5f;
        float textY = yOffset - textYOffset.getValue() + textHeight / 2f - 2f;

        float bgX = xOffset - horizontalSpacing.getValue();
        float bgWidth = textWidth + horizontalSpacing.getValue() * 2f;

        Color alphaTextColor = textColor.getMixedColor(moduleList.indexOf(module));
        Color alphaBgColor = backgroundColor.getFadedColor();
        Color alphaGlowBgColor = glowColor.getMixedColor(moduleList.indexOf(module));

        Color color = new Colors(alphaTextColor).withAlpha(alphaTextColor.getAlpha() / 255f * alpha);
        Color bgColor = new Colors(alphaBgColor).withAlpha(alphaBgColor.getAlpha() / 255f * alpha);
        Color glowbgColor = new Colors(alphaGlowBgColor).withAlpha(alphaGlowBgColor.getAlpha() / 255f * alpha);

        if (background.isToggled()) {
            Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, bgColor.getRGB());

            if (glow.isToggled()) {
                BloomUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, glowbgColor.getRGB()));
            }

            if (blur.isToggled()) {
                BlurUtils.addToDraw(() -> Gui.drawRect(bgX, yOffset, bgX + bgWidth, yOffset + textHeight, Colors.WHITE.withAlpha(alpha).getRGB()));
            }
        }

        font.drawString(module.getName(), textX, textY, color, textShadow.isToggled());
    }

    void sort(final List<Module> toSort, final ClientFontRenderer fontToCalcWidth) {
        toSort.sort( (m1, m2) -> {
            final double width1 = fontToCalcWidth.getStringWidth(m1.getName());
            final double width2 = fontToCalcWidth.getStringWidth(m2.getName());

            return Double.compare(width2, width1);
        });
    }
}
