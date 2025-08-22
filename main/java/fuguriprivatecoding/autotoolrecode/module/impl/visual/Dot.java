package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;

@ModuleInfo(name = "Dot", category = Category.VISUAL, description = "Показывает текущие измененные ротации.")
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0.1f, 1f, 0.5f, 0.1f) {};
    final CheckBox onlyChangeRotationModules = new CheckBox("OnlyChangeRotationModules", this, true);

    public final ColorSetting color = new ColorSetting("Color", this);

    Glow shadows;
    Vec3 prevPos = Vec3.ZERO;
    Vec3 pos = Vec3.ZERO;
    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (!Rot.isChanged() && onlyChangeRotationModules.isToggled()) { return; }
        MovingObjectPosition mouse = mc.objectMouseOver;
        if (event instanceof Render3DEvent) {
            Vec3 smooth = prevPos.add(pos.subtract(prevPos).multiple(mc.timer.renderPartialTicks));

            fadeColor = color.isFade() ?
                    ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                    : color.getColor();

            if (shadows.isToggled() && shadows.module.get("Dot")) BloomUtils.addToDraw(() -> RenderUtils.drawDot(smooth, size.getValue() / 10, Color.white));
            RenderUtils.drawDot(smooth, size.getValue() / 10, fadeColor);
        }
        if (event instanceof TickEvent) {
            prevPos = pos;
            pos = mouse.hitVec;
        }
    }
}
