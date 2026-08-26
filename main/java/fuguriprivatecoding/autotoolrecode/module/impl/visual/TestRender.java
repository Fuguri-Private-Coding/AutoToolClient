package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.FresnelUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RectUtils;

@ModuleInfo(name = "TestRender", category = Category.VISUAL)
public class TestRender extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            FresnelUtils.drawScreen(50, 50, 100, 100,
                    15, 2f, 10f, Colors.WHITE.withAlpha(0f), 1f,
                    2f, true, 1f, 0.1f, Colors.WHITE.withAlpha(1f)
            );

            BlurUtils.startWrite();
            RectUtils.drawRect(50, 50, 100, 100, 15, Colors.WHITE.withAlpha(1f));
            BlurUtils.stopWrite();

            RenderUtils.drawMixedRoundedRect(50, 50, 100, 100, 15, Colors.YELLOW.withAlpha(0.2f), Colors.GREEN.withAlpha(0.2f), 3f);

//
//            RectUtils.drawRect(50, 50, 100, 100, 15f, Colors.BLACK.withAlpha(0.2f));



        }
    }
}
