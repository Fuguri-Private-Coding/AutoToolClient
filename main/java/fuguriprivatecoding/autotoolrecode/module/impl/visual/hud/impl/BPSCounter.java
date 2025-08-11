package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.HUDElement;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class BPSCounter extends HUDElement {

    FloatSetting radius, width, height, textSpeed, bgSpeed;

    ColorSetting textColor1, textColor2, bgColor1, bgColor2;

    CheckBox textFade, bgFade, glow, blur;

    Mode fonts;

    @Override
    public void addSettings(Module parent) {
        radius = new FloatSetting("BPSCounter Radius", parent, 1,10,8,0.1f);
        width = new FloatSetting("BPSCounter Width", parent, 0,100,20,0.1f);
        height = new FloatSetting("BPSCounter Height", parent, 0,50,8,0.1f);

        textFade = new CheckBox("BPSCounter Text Fade", parent, false);
        textColor1 = new ColorSetting("BPSCounter Text Color1", parent, 1, 1, 1, 1);
        textColor2 = new ColorSetting("BPSCounter Text Color2", parent, () -> textFade.isToggled(), 0, 0, 0, 1);
        textSpeed = new FloatSetting("BPSCounter Text Speed", parent, () -> textFade.isToggled(), 0.1f, 20, 1, 0.1f);

        bgFade = new CheckBox("BPSCounter Background Fade", parent, false);
        bgColor1 = new ColorSetting("BPSCounter Background Color1", parent, 0, 0, 0, 1);
        bgColor2 = new ColorSetting("BPSCounter Background Color2", parent, bgFade::isToggled, 0, 0, 0, 1);
        bgSpeed = new FloatSetting("BPSCounter Background Speed", parent, bgFade::isToggled, 0.1f, 20, 1, 0.1f);

        fonts = new Mode("BPSCounter Fonts", parent)
                .addModes("MuseoSans", "Roboto", "JetBrains", "SFPro")
                .setMode("MuseoSans")
        ;

        glow = new CheckBox("BPSCounter Glow", parent);
        blur = new CheckBox("WaterMark Blur", parent);
    }

    @Override
    public void render() {
        super.render();
        setTextFadeColor(textFade.isToggled() ? ColorUtils.fadeColor(
                textColor1.getColor(), textColor2.getColor(), textSpeed.getValue()) : textColor1.getColor());

        setBgFadeColor(bgFade.isToggled() ? ColorUtils.fadeColor(
                bgColor1.getColor(), bgColor2.getColor(), bgSpeed.getValue()) : bgColor1.getColor());

        ClientFontRenderer font = Client.INST.getFonts().fonts.get(fonts.getMode());
        String text = String.format("%.3f", mc.thePlayer.getBps(false));
        setSize(new Vector2f((float) (width.getValue() + font.getStringWidth(text)), height.getValue()));

        Animation2D pos = getPos();
        Vector2f size = getSize();

        if (glow.isToggled()) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, radius.getValue(), Color.WHITE));
        }

        if (blur.isToggled()) {
            GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, radius.getValue(), Color.WHITE));
        }

        RoundedUtils.drawRect(pos.x, pos.y, size.x, size.y, radius.getValue(), getBgFadeColor());
        font.drawString(text, pos.x + 1 - font.getStringWidth(text) / 2f + size.x / 2f, pos.y - 2 + size.y / 2f, getTextFadeColor());
    }
}
