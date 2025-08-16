package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud;

import fuguriprivatecoding.autotoolrecode.settings.Setting;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public abstract class HUDElement {

    private final List<Setting> settings = new ArrayList<>();

    public Vector2f pos, size;

    protected HUDElement(Vector2f pos) {
        this.pos = pos;
    }

    public abstract void render(ScaledResolution sc, int mouseX, int mouseY);

    public void move(Vector2f delta) {
        pos.add(delta);
    }

    protected void addSettings(Setting... settings) {
        this.settings.addAll(List.of(settings));
    }
}
