package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
public class ModeSetting extends Setting {

    @Setter String mode;
    final String[] modes;

    public ModeSetting(String name, Module parent, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = modes;
    }

    public ModeSetting(String name, Module parent, BooleanSupplier visible, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = modes;
        this.setVisible(visible);
    }
}
