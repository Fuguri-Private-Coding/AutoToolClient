package fuguriprivatecoding.autotoolrecode.module.impl.visual.hud;

import fuguriprivatecoding.autotoolrecode.module.impl.visual.hud.impl.DynamicIsland;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public abstract class HUDElement implements SettingAble {

    @Getter
    private static final List<HUDElement> ELEMENTS = new ArrayList<>();

    public static void init() {
        ELEMENTS.addAll(List.of(
                new DynamicIsland(new Vector2f(0, 0.65f))
        ));
    }

    @Getter
    private final List<Setting> settings = new ArrayList<>();

    public Vector2f pos, size;

    protected HUDElement(Vector2f pos) {
        this.pos = pos.div(100);
    }

    public abstract void render(ScaledResolution sc, int mouseX, int mouseY);

    public void move(Vector2f delta) {
        pos.add(delta);
    }

    @Override
    public void addSettings(Setting... settings) {
        this.settings.addAll(List.of(settings));
    }

    @Override
    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }


}
