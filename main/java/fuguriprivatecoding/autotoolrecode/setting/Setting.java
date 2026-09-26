package fuguriprivatecoding.autotoolrecode.setting;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;

import java.util.function.BooleanSupplier;

public abstract class Setting implements SaveLoadable {
	@Getter protected final String name;
	protected BooleanSupplier visible;

	public Setting(String name, SettingAble parent) {
		this.name = name;
		parent.addSetting(this);
	}

	public Setting(String name, SettingAble parent, BooleanSupplier visible) {
		this.name = name;
		this.visible = visible;
		parent.addSetting(this);
	}

	@SuppressWarnings("unchecked")
	public <E extends Setting> E visibleIf(BooleanSupplier visible) {
		this.visible = visible;
		return (E) this;
	}

	public boolean isVisible() {
		return visible == null || visible.getAsBoolean();
	}
}
