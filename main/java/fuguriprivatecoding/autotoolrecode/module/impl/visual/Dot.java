package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEventPost;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayTraceUtils;
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
    Rot prevPos = new Rot();
    Rot pos = new Rot();
    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (!Rot.isChanged() && onlyChangeRotationModules.isToggled()) { return; }
        if (event instanceof Render3DEvent) {
            MovingObjectPosition mouse = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks),6,6, prevPos.add(pos.subtract(prevPos).multiplier(mc.timer.renderPartialTicks)));

            if (mouse != null) {
                Vec3 smooth = mouse.hitVec;

                fadeColor = color.isFade() ?
                        ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                        : color.getColor();

                if (shadows.isToggled() && shadows.module.get("Dot")) BloomUtils.addToDraw(() -> RenderUtils.drawDot(smooth, size.getValue() / 10, Color.white));
                RenderUtils.drawDot(smooth, size.getValue() / 10, fadeColor);
            }
        }
        if (event instanceof MotionEventPost e) {
            prevPos = new Rot(pos.getYaw(), pos.getPitch());
            pos = new Rot(e.getYaw(), e.getPitch());
        }
    }
}
