package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
public class ModeSetting extends Setting {

    @Setter String mode;
    @Setter List<String> modes;

    public ModeSetting(String name, Module parent, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = List.of(modes);
    }

    public ModeSetting(String name, Module parent, BooleanSupplier visible, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = List.of(modes);
        this.setVisible(visible);
    }
}
