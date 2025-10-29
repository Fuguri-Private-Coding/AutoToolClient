package fuguriprivatecoding.autotoolrecode.settings.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class CheckBox extends Setting {

	boolean toggled;

	EasingAnimation toggleAnimation = new EasingAnimation();

	public CheckBox(String name, SettingAble parent) {
		super(name, parent);
		toggled = false;
	}

	public CheckBox(String name, SettingAble parent, boolean toggled) {
		super(name, parent);
		this.toggled = toggled;
	}

	public CheckBox(String name, SettingAble parent, BooleanSupplier visible) {
		super(name, parent, visible);
		toggled = false;
	}

	public CheckBox(String name, SettingAble parent, BooleanSupplier visible, boolean toggled) {
		super(name, parent, visible);
		this.setVisible(visible);
		this.toggled = toggled;
	}

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();
        object.addProperty("toggled", toggled);
        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        toggled = object.get("toggled").getAsBoolean();
    }
}
