package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
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
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
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

    private final CheckBox smooth = new CheckBox("Smooth", this, false);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    Rot prevPos = new Rot();
    Rot pos = new Rot();

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent e) {
            prevPos = pos;
            pos = mc.thePlayer.getRotation();
        }

        RayTrace mouse;

        if (smooth.isToggled()) {
            Rot smoothPos = prevPos.add(pos.subtract(prevPos).multiplier(mc.timer.renderPartialTicks));
            mouse = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks), 4.5f, 4.5f, smoothPos, mc.timer.renderPartialTicks);
        } else {
            mouse = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(0f),4.5f,4.5f, pos, 0f);
        }

        if (mouse != null && CameraRot.INST.isUnlocked() && CameraRot.INST.isWillChange()) {
            switch (dotType.getMode()) {
                case "3D" -> {
                    if (event instanceof Render3DEvent) {
                        if (glow.isToggled()) {
                            BloomUtils.startWrite();
                            RenderUtils.drawDot(mouse.hitVec, size.getValue() / 10, glowColor.getFadedColor());
                            BloomUtils.stopWrite();
                        }
                        RenderUtils.drawDot(mouse.hitVec, size.getValue() / 10, color.getFadedColor());
                    }
                }

                case "2D" -> {
                    if (event instanceof Render2DEvent) {
                        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                        Vec3 pos = mouse.hitVec.subtract(RenderManager.getRenderPosition());
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
