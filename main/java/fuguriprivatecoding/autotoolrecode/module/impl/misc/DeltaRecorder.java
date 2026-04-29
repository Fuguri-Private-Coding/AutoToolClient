package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.KillAura;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.KeyBind;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.RayTrace;
import net.minecraft.util.Vec3;

import java.awt.*;

@ModuleInfo(name = "DeltaRecorder", category = Category.MISC, description = "Записывает ротацию/дельту камеры для KillAura")
public class DeltaRecorder extends Module {

    Mode recordType = new Mode("RecordType", this)
        .addModes("Delta", "Rotation")
        .setMode("Rotation")
        ;

    CheckBox reset = new CheckBox("Reset", this, false);

    KeyBind startRecording = new KeyBind("StartRecodingBind", this, 0);

    @Override
    public void onDisable() {
        recording = false;
    }

    Rot current;

    boolean recording = false;

    @Override
    public void onEvent(Event event) {
        var killAura = Modules.getModule(KillAura.class);
        if (event instanceof TickEvent && reset.isToggled()) {
            killAura.recordedOffset.offsets.clear();
            reset.setToggled(false);
        }

        if (event instanceof KeyEvent e && e.getKey() == startRecording.getKey()) {
            recording = !recording;
        }

        if (event instanceof TickEvent) {
            if (recording) {
                Rot delta = switch (recordType.getMode()) {
                    case "Delta" -> CameraRot.INST.getPrevRot().deltaTo(CameraRot.INST.copy()).copy();
                    case "Rotation" -> current.deltaTo(CameraRot.INST.copy()).copy();
                    default -> RotUtils.ZERO;
                };

                killAura.recordedOffset.offsets.add(delta);
            } else  {
                current = mc.thePlayer.getRotation().copy();
            }
        }

        if (event instanceof Render2DEvent) {
            ClientFont font = Fonts.fonts.get("SFPro");

            font.drawString("Recording: " + recording, 5, 5, Colors.WHITE);

            if (recording) {
                RayTrace mouse = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks),4.5f,4.5f, current, mc.timer.renderPartialTicks);
                if (mouse == null) return;

                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                Vec3 pos = mouse.hitVec.subtract(RenderManager.getRenderPosition());
                float[] positions = Convertors.convert2D(pos, mc.gameSettings.guiScale);
                mc.entityRenderer.setupOverlayRendering();

                if (positions == null || positions[2] > 1) return;

                float size = 0.5f * 10;

                float x = positions[0] - (size / 2f);
                float y = positions[1] - (size / 2f);

                RoundedUtils.drawRect(x, y, size, size, 0, Color.WHITE);
            }
        }
    }
}
