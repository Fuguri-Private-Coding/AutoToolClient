package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PrePostTickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.util.RayTrace;

@ModuleInfo(name = "Dot", category = Category.VISUAL, description = "Показывает текущие измененные ротации.")
public class Dot extends Module {

    @Override
    public void onEvent(Event event) {
//        if (event instanceof PrePostTickEvent) {
//            prev = rot;
//            rot = mc.thePlayer.getRotation();
//        }
//
//        if (event instanceof Render3DEvent && CameraRot.INST.isUnlocked() && CameraRot.INST.isWillChange()) {
//            RayTrace trace = RayCastUtils.getMouseOver(prev.plus(rot.minus(prev).multiplied(mc.timer.renderPartialTicks)), 4.5f, mc.timer.renderPartialTicks);
//
//            if (trace == null)
//                return;
//
//            if (glow.isToggled()) {
//                BloomUtils.startWrite();
//                RenderUtils.drawDot(trace.hitVec, size.getValue() / 10, glowColor.getFadedColor());
//                BloomUtils.stopWrite();
//            }
//
//            RenderUtils.drawDot(trace.hitVec, size.getValue() / 10, color.getFadedColor());
//        }
    }
}
