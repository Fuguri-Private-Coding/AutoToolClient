package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.util.RayTrace;

@ModuleInfo(name = "Dot", category = Category.VISUAL, description = "Показывает текущие измененные ротации.")
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0.1f, 1f, 0.5f, 0.1f) {};

    public final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    Rot prevPos = new Rot();
    Rot pos = new Rot();

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            prevPos = pos;
            pos = mc.thePlayer.getRotation();
        }

        if (event instanceof Render3DEvent && CameraRot.INST.isUnlocked()) {
            RayTrace mouse = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(-1),6,6, pos, -1);
            if (mouse != null) {
                if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawDot(mouse.hitVec, size.getValue() / 10, glowColor.getFadedColor()));
                RenderUtils.drawDot(mouse.hitVec, size.getValue() / 10, color.getFadedColor());
            }
        }
    }
}
