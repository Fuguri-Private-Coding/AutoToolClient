package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class KeyBind extends Setting {

    int key;

    public KeyBind(String name, Module parent, int key) {
        super(name, parent);
        this.key = key;
    }

    public KeyBind(String name, Module parent, BooleanSupplier visible, int key) {
        super(name, parent, visible);
        this.key = key;
    }
}
