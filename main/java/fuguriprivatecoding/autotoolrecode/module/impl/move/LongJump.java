package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.move.longjump.LongJumpMode;
import fuguriprivatecoding.autotoolrecode.module.impl.move.longjump.impl.MatrixMode;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "LongJump",category = Category.MOVE, description = "Позволяет вам далеко прыгать.")
public class LongJump extends Module {

    Mode jumpMode = new Mode("JumpMode", this)
        .addModes("Matrix")
        ;

    public final FloatSetting speed = new FloatSetting("Speed", this, () -> jumpMode.is("Matrix"), 0, 10, 5, 0.01f);
    public final IntegerSetting tick = new IntegerSetting("Ticks", this, () -> jumpMode.is("Matrix"), 0, 80, 25);

    private final Map<String, LongJumpMode> speedModes = new HashMap<>();
    private LongJumpMode currentLongJumpMode;

    public LongJump() {
        register(
            new MatrixMode()
        );

        updateCurrentMode();
    }

    private void register(LongJumpMode... longJumpModes) {
        for (LongJumpMode longJumpMode : longJumpModes) {
            this.speedModes.put(longJumpMode.getName(), longJumpMode);
        }
    }

    private void updateCurrentMode() {
        currentLongJumpMode = speedModes.get(jumpMode.getMode());
    }

    @Override
    public void onDisable() {
        if (currentLongJumpMode != null) {
            currentLongJumpMode.onDisable(this);
        }
    }

    @Override
    public void onEnable() {
        if (currentLongJumpMode != null) {
            currentLongJumpMode.onEnable(this);
        }
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (currentLongJumpMode == null || !currentLongJumpMode.getName().equals(jumpMode.getMode())) {
            updateCurrentMode();
        }

        if (currentLongJumpMode != null) {
            currentLongJumpMode.handleEvent(event, this);
        }
    }
}
