package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class BooleanSetting extends Setting {

	boolean toggled;

	public BooleanSetting(String name, Module parent) {
		super(name, parent);
		toggled = false;
	}

	public BooleanSetting(String name, Module parent, boolean toggled) {
		super(name, parent);
		this.toggled = toggled;
	}

	public BooleanSetting(String name, Module parent, BooleanSupplier visible) {
		super(name, parent, visible);
		toggled = false;
	}

	public BooleanSetting(String name, Module parent, BooleanSupplier visible, boolean toggled) {
		super(name, parent, visible);
		this.setVisible(visible);
		this.toggled = toggled;
	}
}
