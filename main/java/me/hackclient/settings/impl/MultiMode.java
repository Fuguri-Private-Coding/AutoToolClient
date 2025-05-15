package me.hackclient.settings.impl;

import lombok.Getter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import me.hackclient.utils.doubles.Doubles;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public class MultiMode extends Setting {

    @Getter
    CopyOnWriteArrayList<Doubles<String, Boolean>> values;

    public MultiMode(String name, Module parent) {
        super(name, parent);
        values = new CopyOnWriteArrayList<>();
    }

    public MultiMode(String name, Module parent, BooleanSupplier visible) {
        super(name, parent);
        setVisible(visible);
        values = new CopyOnWriteArrayList<>();
    }

    public MultiMode add(String name, boolean value) {
        values.add(new Doubles<>(name, value));
        return this;
    }

    public MultiMode add(String name) {
        values.add(new Doubles<>(name, false));
        return this;
    }

    public boolean get(String name) {
        Doubles<String, Boolean> value = values.stream().filter(v -> v.getFirst().equals(name))
                .findFirst().orElse(null);

        if (value == null) {
            throw new IllegalStateException();
        }

        return value.getSecond();
    }

    public void set(String name, boolean newValue) {
        for (Doubles<String, Boolean> value : values) {
            if (value == null) {
                throw new IllegalStateException();
            }

            if (!value.getFirst().equals(name))
                continue;

            value.setSecond(newValue);
        }
    }
}
