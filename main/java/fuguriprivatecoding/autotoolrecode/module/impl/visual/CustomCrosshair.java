package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.FresnelUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RectUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.rect.Rect;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "CustomCrosshair", category = Category.VISUAL, description = "Позволяет сделать свой прицел.")
public class CustomCrosshair extends Module {

    Mode mode = new Mode("Mode",this)
        .addModes("Cross", "Dot")
        .setMode("Cross")
        ;

    ColorSetting color = new ColorSetting("Color", this);

    FloatSetting length = new FloatSetting("Length", this, () -> mode.is("Cross"), 1, 50, 4, 0.1f);
    FloatSetting scale = new FloatSetting("Scale", this, 0,20,2, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
//
//            FresnelUtils.drawScreen(50, 50, 100, 100,
//                    10f, 2f, 15f, Colors.WHITE.withAlpha(0f), 1f,
//                    2f, true, 1f, 0.1f, Colors.WHITE.withAlpha(1f)
//            );
//
//            BlurUtils.startWrite();
//            RectUtils.drawRect(50, 50, 100, 100, 10f, Colors.WHITE.withAlpha(1f));
//            BlurUtils.stopWrite();
//            RectUtils.drawRect(50, 50, 100, 100, 10f, Colors.WHITE.withAlpha(0.2f));

            switch (mode.getMode()) {
                case "Cross" -> {
                    float firstX = sc.getScaledWidth() / 2f - length.getValue();
                    float firstY = sc.getScaledHeight() / 2f - scale.getValue() / 2f;
                    float firstWidth = length.getValue() * 2;
                    float firstHeight = scale.getValue();

                    float secondX = sc.getScaledWidth() / 2f - scale.getValue() / 2f;
                    float secondY = sc.getScaledHeight() / 2f - length.getValue();
                    float secondHeight = length.getValue() * 2;
                    float secondWidth = scale.getValue();

                    RoundedUtils.drawRect(firstX, firstY, firstWidth, firstHeight, 0, color.getFadedColor());
                    RoundedUtils.drawRect(secondX, secondY, secondWidth, secondHeight, 0, color.getFadedColor());
                }

                case "Dot" -> {
                    float x = sc.getScaledWidth() / 2f - scale.getValue();
                    float y = sc.getScaledHeight() / 2f - scale.getValue();
                    float width = scale.getValue() * 2;
                    float height = scale.getValue() * 2;

                    RoundedUtils.drawRect(x, y, width, height,height / 2, color.getFadedColor());
                }
            }
        }
    }
}
