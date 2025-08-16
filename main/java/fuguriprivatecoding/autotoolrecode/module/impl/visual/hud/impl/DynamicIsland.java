package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.HUDElement;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DynamicIsland extends HUDElement implements Imports {

//    private final List<DynamicIslandElement> dynamicIslandElements = List.of(
//            new DynamicIslandElement(
//                    Client.INST::getFullName,
//                    () -> true,
//                    0
//            ),
//            new DynamicIslandElement(
//                    () -> String.valueOf(mc.thePlayer.getBps(false)),
//                    MoveUtils::isMoving,
//                    1
//            ),
//            new DynamicIslandElement(
//                    () -> "Current Target - " + Client.INST.getCombatManager().getTarget(),
//                    () -> Client.INST.getCombatManager().getTarget() != null,
//                    2
//            ),
//            new DynamicIslandElement(
//                    () -> "Blocks Left - " + mc.thePlayer.inventory.getCurrentItem().stackSize + "\n" + "Speed - " + mc.thePlayer.getBps(false),
//                    () -> new Vector2f((float) Math.max(getFont().getStringWidth("Blocks Left - " + mc.thePlayer.inventory.getCurrentItem().stackSize), getFont().getStringWidth("Speed - " + mc.thePlayer.getBps(false))), 24),
//                    () -> Client.INST.getModuleManager().getModule(Scaffold.class).isToggled(),
//                    3
//            )
//    );

    Mode fonts = new Mode("Fonts", this)
            .addModes("MuseoSans", "Roboto", "JetBrains", "SFPro")
            .setMode("MuseoSans")
            ;

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, 0, 100, 5, 0.1f);

    FloatSetting width = new FloatSetting("Width", this, 0f,50f,10f,0.1f);
    FloatSetting animationSpeed = new FloatSetting("Animation Speed", this, 0f,50f,15f,0.1f);

    CheckBox textFade = new CheckBox("Text Fade", this, false);
    ColorSetting textColor1 = new ColorSetting("Text Color1", this, 1,1,1,1);
    ColorSetting textColor2 = new ColorSetting("Text Color2", this,() -> textFade.isToggled(), 0,0,0,1);
    FloatSetting textSpeed = new FloatSetting("Text Speed", this,() -> textFade.isToggled(),0.1f, 20, 1, 0.1f);

    FloatSetting bgRadius = new FloatSetting("Background Radius", this,0f, 20, 7.5f, 0.1f);
    CheckBox bgFade = new CheckBox("Background Fade", this, false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> bgFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox glow = new CheckBox("Glow", this, true);
    CheckBox blur = new CheckBox("Blur", this, true);

    CheckBox bgFadeShadow = new CheckBox("Background Fade Shadow", this, () -> glow.isToggled(), false);
    ColorSetting bgColor1Shadow = new ColorSetting("Background Color1 Shadow", this, () -> glow.isToggled(), 0,0,0,1);
    ColorSetting bgColor2Shadow = new ColorSetting("Background Color2 Shadow", this,() -> glow.isToggled() && bgFadeShadow.isToggled(), 0,0,0,1);
    FloatSetting bgSpeedShadow = new FloatSetting("Background Speed Shadow", this,() -> glow.isToggled() && bgFadeShadow.isToggled(),0.1f, 20, 1, 0.1f);

    Color fadeTextColor;
    Color fadeBackgroundColor;

    String currentText = Client.INST.getFullName();

    Animation2D currentSize = new Animation2D();
    Animation needY = new Animation();
    Animation radius = new Animation();

    public DynamicIsland(Vector2f pos) {
        super(new Vector2f(50, pos.y));
    }

    @Override
    public void render(ScaledResolution sc, int mouseX, int mouseY) {
        float absoluteX = pos.x * sc.getScaledWidth();
        float absoluteY = pos.y * sc.getScaledHeight();


        currentSize.update(10f);
        radius.update(10f);

        radius.endX = 5f;

        currentSize.endY = 10;
//        currentSize.endX = (float) font.getStringWidth(currentText) + 8;

        RoundedUtils.drawCenteredRect(absoluteX, absoluteY, currentSize.x, currentSize.y, radius.x, Color.BLACK);

//        font.drawCenteredString(currentText, absoluteX, absoluteY + 2.5f, Color.WHITE);
    }



//    @Getter
//    @AllArgsConstructor
//    @RequiredArgsConstructor
//    private class DynamicIslandElement {
//        private final Supplier<String> text;
//        private Supplier<Vector2f> size = () -> new Vector2f(text != null ? (float) (getFont().getStringWidth(text.get()) + 8) : 10, 12);
//        private final BooleanSupplier use;
//        private final int priority;
//    }

    private ClientFontRenderer getFont() {
        return Client.INST.getFonts().fonts.get(fonts.getMode());
    }
}
