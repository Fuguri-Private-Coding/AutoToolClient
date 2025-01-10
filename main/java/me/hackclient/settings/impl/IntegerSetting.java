package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class IntegerSetting extends Setting {
	int min, max, value;

	public IntegerSetting(String name, Module parent, int min, int max, int value) {
		super(name, parent);
		this.min = min;
		this.max = max;
		this.value = value;
	}

	public IntegerSetting(String name, Module parent, BooleanSupplier visible, int min, int max, int value) {
		super(name, parent);
		this.min = min;
		this.max = max;
		this.value = value;
		this.setVisible(visible);
	}

	public float normalize() {
		return (float) (value - min) / (max - min);
	}
}
