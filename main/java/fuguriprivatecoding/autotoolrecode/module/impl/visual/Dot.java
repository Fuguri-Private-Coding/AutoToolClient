package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.RayTrace;

@ModuleInfo(name = "Dot", category = Category.VISUAL, description = "Показывает текущие измененные ротации.")
public class Dot extends Module {

    private final IntegerSetting size = new IntegerSetting("Size", this, 0, 100, 50);

    private final ColorSetting color = new ColorSetting("Color", this);
    private final CheckBox glow = new CheckBox("Glow", this, true);
    private final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    Rot prev, rot;

    @Override
    public void onEvent(Event event) {
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.POST) {
            prev = rot;
            rot = mc.thePlayer.getRotations();
        }

        if (event instanceof Render3DEvent && CameraRot.INST.isUnlocked() && CameraRot.INST.isWillChange()) {
            RayTrace trace = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks), 4.5f, 4.5f, prev.plus(rot.minus(prev).multiplied(mc.timer.renderPartialTicks)), mc.timer.renderPartialTicks);

            if (trace == null)
                return;

            AxisAlignedBB bb = new AxisAlignedBB(trace.hitVec.subtract(RenderManager.getRenderPosition()), trace.hitVec.subtract(RenderManager.getRenderPosition()))
                ;

            RenderUtils.start3D();
            if (glow.isToggled()) {
                BloomUtils.startWrite();
                RenderUtils.drawBoundingBox(bb.expand(size.getValue() / 1000d), glowColor.getFadedColor());
                BloomUtils.stopWrite();
            }

            RenderUtils.drawBoundingBox(bb.expand(size.getValue() / 1000d), color.getFadedColor());
            RenderUtils.stop3D();
        }
    }
}
