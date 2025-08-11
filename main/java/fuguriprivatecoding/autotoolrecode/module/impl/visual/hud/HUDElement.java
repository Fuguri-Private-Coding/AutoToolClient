package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud;

import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

@Getter
@Setter
public abstract class HUDElement implements Imports {

    public Animation2D pos = new Animation2D(0,0,0,0);
    public Vector2f size;
    public String name;

    Color textFadeColor, bgFadeColor;

    public void render() {
        getPos().update(10f);
    }

    public void addSettings(Module parent) {}
}
