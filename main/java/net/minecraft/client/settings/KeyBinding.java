package net.minecraft.client.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;

public class KeyBinding implements Comparable<KeyBinding>
{
    private static final List<KeyBinding> keybindArray = Lists.<KeyBinding>newArrayList();
    private static final IntHashMap hash = new IntHashMap();
    private static final Set<String> keybindSet = Sets.newHashSet();
    @Getter
    private final String keyDescription;
    @Getter
    private final int keyCodeDefault;
    @Getter
    private final String keyCategory;
    @Setter
    @Getter
    int keyCode;
    public boolean pressed;
    @Getter @Setter int pressTime;

    public static void onTick(int keyCode)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = (KeyBinding)hash.lookup(keyCode);

            if (keybinding != null)
            {
                ++keybinding.pressTime;
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = (KeyBinding)hash.lookup(keyCode);

            if (keybinding != null)
            {
                keybinding.pressed = pressed;
            }
        }
    }

    public static void unPressAllKeys()
    {
        for (KeyBinding keybinding : keybindArray)
        {
            keybinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash()
    {
        hash.clearMap();

        for (KeyBinding keybinding : keybindArray)
        {
            hash.addKey(keybinding.keyCode, keybinding);
        }
    }

    public static Set<String> getKeybinds()
    {
        return keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category)
    {
        this.keyDescription = description;
        this.keyCode = keyCode;
        this.keyCodeDefault = keyCode;
        this.keyCategory = category;
        keybindArray.add(this);
        hash.addKey(keyCode, this);
        keybindSet.add(category);
    }

    public boolean isKeyDown()
    {
        return this.pressed;
    }

    public boolean isPressed()
    {
        if (this.pressTime == 0)
        {
            return false;
        }
        else
        {
            --this.pressTime;
            return true;
        }
    }

    private void unpressKey()
    {
        this.pressTime = 0;
        this.pressed = false;
    }

    public int compareTo(KeyBinding p_compareTo_1_)
    {
        int i = I18n.format(this.keyCategory, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyCategory, new Object[0]));

        if (i == 0)
        {
            i = I18n.format(this.keyDescription, new Object[0]).compareTo(I18n.format(p_compareTo_1_.keyDescription, new Object[0]));
        }

        return i;
    }
}
