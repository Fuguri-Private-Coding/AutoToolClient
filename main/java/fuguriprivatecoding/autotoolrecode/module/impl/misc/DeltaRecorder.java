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
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;

@ModuleInfo(name = "DeltaRecorder", category = Category.MISC)
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
                    case "Delta" -> RotUtils.getDelta(CameraRot.INST.getPrevRot(), CameraRot.INST);
                    case "Rotation" -> RotUtils.getDelta(current, CameraRot.INST);
                    default -> Rot.ZERO;
                };

                killAura.recordedOffset.offsets.add(delta);
            } else  {
                current = mc.thePlayer.getRotation();
            }
        }

        if (event instanceof Render2DEvent) {
            ClientFont font = Fonts.fonts.get("SFPro");

            font.drawString("Recording: " + recording, 5, 5, Colors.WHITE);
        }
    }
}
