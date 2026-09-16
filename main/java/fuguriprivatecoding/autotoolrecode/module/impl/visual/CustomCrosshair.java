package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

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
