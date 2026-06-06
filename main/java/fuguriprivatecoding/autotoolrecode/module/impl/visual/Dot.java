package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PrePostTickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.render.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.RayTrace;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Dot", category = Category.VISUAL, description = "Показывает текущие измененные ротации.")
public class Dot extends Module {

    Mode dotType = new Mode("DotType", this)
        .addModes("3D", "2D")
        .setMode("2D")
        ;

    final FloatSetting size = new FloatSetting("Size", this, 0.1f, 1f, 0.5f, 0.1f) {};
    final FloatSetting rounding = new FloatSetting("Rounding", this, () -> dotType.is("2D"), 0f, 5f, 5f, 0.1f) {};

    public final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    Rot prev = new Rot();
    Rot rot = new Rot();

    @Override
    public void onEvent(Event event) {
        if (event instanceof PrePostTickEvent) {
            prev = mc.thePlayer.getPrevRotation();
            rot = mc.thePlayer.getRotation();
        }

        RayTrace trace = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks), 4.5, 4.5, prev.copy().plus(rot.copy().minus(prev.copy()).copy().multiple(mc.timer.renderPartialTicks)), mc.timer.renderPartialTicks);

        if (trace != null && CameraRot.INST.isUnlocked() && CameraRot.INST.isWillChange()) {
            switch (dotType.getMode()) {
                case "3D" -> {
                    if (event instanceof Render3DEvent) {
                        if (glow.isToggled()) {
                            BloomUtils.startWrite();
                            RenderUtils.drawDot(trace.hitVec, size.getValue() / 10, glowColor.getFadedColor());
                            BloomUtils.stopWrite();
                        }
                        RenderUtils.drawDot(trace.hitVec, size.getValue() / 10, color.getFadedColor());
                    }
                }

                case "2D" -> {
                    if (event instanceof Render2DEvent) {
                        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                        Vec3 pos = trace.hitVec.subtract(RenderManager.getRenderPosition());
                        float[] positions = Convertors.convert2D(pos, mc.gameSettings.guiScale);
                        mc.entityRenderer.setupOverlayRendering();

                        if (positions == null || positions[2] > 1) return;

                        float size = this.size.getValue() * 10;

                        float x = positions[0] - (size / 2f);
                        float y = positions[1] - (size / 2f);

                        if (glow.isToggled()) {
                            BloomUtils.startWrite();
                            RoundedUtils.drawRect(x, y, size, size, rounding.getValue(), glowColor.getFadedColor());
                            BloomUtils.stopWrite();
                        }
                        RoundedUtils.drawRect(x, y, size, size, rounding.getValue(), color.getFadedColor());
                    }
                }
            }
        }
    }
}
