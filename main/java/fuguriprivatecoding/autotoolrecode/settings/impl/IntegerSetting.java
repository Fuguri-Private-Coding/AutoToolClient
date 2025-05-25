package fuguriprivatecoding.autotoolrecode.settings.impl;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class IntegerSetting extends Setting {
	public int min, max, value;

	public IntegerSetting(String name, Module parent, int min, int max, int value) {
		super(name, parent);
		this.min = min;
		this.max = max;
		this.value = value;
	}

	public IntegerSetting(String name, Module parent, BooleanSupplier visible, int min, int max, int value) {
		super(name, parent, visible);
		this.min = min;
		this.max = max;
		this.value = value;
	}

	public void setValue(int value) {
		this.value = Math.clamp(value, min, max);
	}

	public float normalize() {
		return (float) (value - min) / (max - min);
	}
}
