package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Getter
public class Mode extends Setting {

    String mode;
    @Setter List<String> modes = new ArrayList<>();

    public Mode(String name, Module parent) {
        super(name, parent);
    }

    public Mode(String name, Module parent, BooleanSupplier visible) {
        super(name, parent);
        this.setVisible(visible);
    }

    public Mode setMode(String mode) {
        if (!modes.contains(mode)) {
            System.out.println("Cant set mode " + mode);
            return this;
        }
        this.mode = mode;
        return this;
    }

    public Mode addMode(String mode) {
        modes.add(mode);
        return this;
    }

    public Mode addModes(String... modes) {
        this.modes.addAll(List.of(modes));
        return this;
    }
}
