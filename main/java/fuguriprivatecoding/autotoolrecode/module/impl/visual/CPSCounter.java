package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "CPSCounter", category = Category.VISUAL, description = "Показывает текущую скорость кликов в секунду.")
public class CPSCounter extends Module {

    IntegerSetting posX = new IntegerSetting("PosX", this, 0, 100, 5);
    IntegerSetting posY = new IntegerSetting("PosY", this, 0, 100, 5);

    FloatSetting size = new FloatSetting("Size", this, 10, 50, 25, 0.1f);

    public static List<Long> leftCps = new CopyOnWriteArrayList<>();
    public static List<Long> rightCps = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            leftCps.removeIf(l -> l <= System.currentTimeMillis());
            rightCps.removeIf(l -> l <= System.currentTimeMillis());
        }

        if (event instanceof Render2DEvent e) {
            ScaledResolution sc = e.getScaledResolution();
            Vector2f pos = GuiUtils.getAbsolutePos(posX.getValue(), posY.getValue(), sc);

            MsdfFont font = Fonts.get("Bold");
            font.draw("cps: " + leftCps.size() + "/" + rightCps.size(), pos.x, pos.y, size.getValue(), Color.WHITE);
        }
    }
}
