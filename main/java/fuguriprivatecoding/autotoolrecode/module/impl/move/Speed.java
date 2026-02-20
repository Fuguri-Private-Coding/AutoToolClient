package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.SpeedMode;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "Speed", category = Category.MOVE, description = "Позволяет вам двигаться быстрее.")
public class Speed extends Module {

    public Mode mode = new Mode("Mode", this)
        .addModes("45Degree", "Vanilla", "FunnyMcSkyPvp")
        .setMode("45Degree");

    public CheckBox rotateWithMovement = new CheckBox("RotateWithMovement", this, () -> mode.is("45Degree"), true);

    public CheckBox jump = new CheckBox("Jump", this, false);

    CheckBox resetMotion = new CheckBox("ResetMotionOnDisable", this, false);
    public FloatSetting speed = new FloatSetting("Speed", this,
        () -> mode.getMode().equalsIgnoreCase("Vanilla"),
        1f, 10f, 5f, 0.1f) {};

    private final Map<String, SpeedMode> speedModes = new HashMap<>();
    private SpeedMode currentSpeedMode;

    public Speed() {
        register(
            new Degree45Mode(),
            new VanillaMode(),
            new FunnyMcSkyPvpMode()
        );

        updateCurrentMode();
    }

    private void register(SpeedMode... speedModes) {
        for (SpeedMode speedMode : speedModes) {
            this.speedModes.put(speedMode.getName(), speedMode);
        }
    }

    private void updateCurrentMode() {
        currentSpeedMode = speedModes.get(mode.getMode());
    }

    @Override
    public void onDisable() {
        if (currentSpeedMode != null) {
            currentSpeedMode.onDisable(this);
        }

        if (resetMotion.isToggled()) {
            mc.thePlayer.stopMotion();
        }

        mc.timer.timerSpeed = 1f;
        mc.thePlayer.speedInAir = 0.02f;
    }

    @Override
    public void onEnable() {
        if (currentSpeedMode != null) {
            currentSpeedMode.onEnable(this);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (currentSpeedMode == null || !currentSpeedMode.getName().equals(mode.getMode())) {
            updateCurrentMode();
        }

        if (currentSpeedMode != null) {
            currentSpeedMode.handleEvent(event, this);
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}