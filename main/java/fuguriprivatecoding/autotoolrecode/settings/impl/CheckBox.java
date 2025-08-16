package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class CheckBox extends Setting {

	boolean toggled;

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
}
