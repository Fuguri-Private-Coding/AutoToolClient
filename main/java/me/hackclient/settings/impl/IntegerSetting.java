package me.hackclient.settings.impl;

import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

public class IntegerSetting extends Setting {
	private int min, max, value;

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

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public float normalize() {
		return (float) (value - min) / (max - min);
	}
}
