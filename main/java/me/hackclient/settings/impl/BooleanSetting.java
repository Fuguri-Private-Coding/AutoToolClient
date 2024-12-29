package me.hackclient.settings.impl;

import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting {

	private boolean toggled;

	public BooleanSetting(String name, Module parent) {
		super(name, parent);
		toggled = false;
	}

	public BooleanSetting(String name, Module parent, boolean toggled) {
		super(name, parent);
		this.toggled = toggled;
	}

	public BooleanSetting(String name, Module parent, BooleanSupplier visible) {
		super(name, parent);
		toggled = false;
		this.setVisible(visible);
	}

	public BooleanSetting(String name, Module parent, BooleanSupplier visible, boolean toggled) {
		super(name, parent);
		this.setVisible(visible);
		this.toggled = toggled;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}
}
