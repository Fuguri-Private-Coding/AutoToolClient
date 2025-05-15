package me.hackclient.settings;

import me.hackclient.module.Module;

import java.util.function.BooleanSupplier;

public class Setting implements ISetting {
	final String name;
	BooleanSupplier visible;

	public Setting(String name, Module parent) {
		this.name = name;
		visible = () -> true;
		parent.getSettings().add(this);
	}

	public Setting(String name, Module parent, BooleanSupplier visible) {
		this.name = name;
		this.visible = visible;
		parent.getSettings().add(this);
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
}
