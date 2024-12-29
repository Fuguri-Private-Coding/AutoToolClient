package me.hackclient.settings.impl;

import me.hackclient.module.Module;
import me.hackclient.settings.Setting;

import java.util.function.BooleanSupplier;

public class ModeSetting extends Setting {

    private String mode;
    private final String[] modes;

    public ModeSetting(String name, Module parent, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = modes;
    }
    //ddf
    public ModeSetting(String name, Module parent, BooleanSupplier visible, String mode, String[] modes) {
        super(name, parent);
        this.mode = mode;
        this.modes = modes;
        this.setVisible(visible);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String[] getModes() {
        return modes;
    }
}
