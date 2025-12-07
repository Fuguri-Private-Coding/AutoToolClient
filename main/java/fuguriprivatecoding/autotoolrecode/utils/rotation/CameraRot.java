package fuguriprivatecoding.autotoolrecode.utils.rotation;

import lombok.Getter;
import lombok.Setter;

public class CameraRot extends Rot {

    public static final CameraRot INST = new CameraRot();

    private CameraRot() {

    }

    @Getter private boolean unlocked;
    @Setter private boolean willChange;

    public boolean needBackRotate() {
        return unlocked && !willChange;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked) this.willChange = true;
    }
}
