package fuguriprivatecoding.autotoolrecode.settings;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;

import java.util.function.BooleanSupplier;

public abstract class Setting implements ISetting {
	final String name;
	BooleanSupplier visible;

	@Getter
	EasingAnimation visibleAnim = new EasingAnimation();

	public Setting(String name, SettingAble parent) {
		this.name = name;
		visible = () -> true;
		parent.addSetting(this);
	}

	public Setting(String name, SettingAble parent, BooleanSupplier visible) {
		this.name = name;
		this.visible = visible;
		parent.addSetting(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = () -> visible;
	}

	@Override
	public void setVisible(BooleanSupplier visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return visible.getAsBoolean();
	}

    public abstract JsonObject getObject();
    public abstract void setObject(JsonObject object);
}
