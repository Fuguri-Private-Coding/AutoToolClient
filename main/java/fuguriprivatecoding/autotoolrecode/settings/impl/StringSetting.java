package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class StringSetting extends Setting {

    private static final String PREVIEW_TEXT = "Enter text...";

    EasingAnimation previewTextAnim = new EasingAnimation();

    String text;
    GuiTextField textField = new GuiTextField(0,null,0,0,0,0);
    boolean isActive = false;

    int length = 16;

    public StringSetting(String name, SettingAble parent) {
        super(name, parent);
    }

    public StringSetting(String name, SettingAble parent, int length) {
        super(name, parent);
        this.length = length;
    }

    public StringSetting(String name, SettingAble parent, BooleanSupplier visible) {
        super(name, parent, visible);
    }

    public StringSetting(String name, SettingAble parent, BooleanSupplier visible, int length) {
        super(name, parent, visible);
        this.length = length;
    }

    public void onKey(char ch, int key) {
        if (key == Keyboard.KEY_RETURN) {
            textField.setText("");
            isActive = false;
            return;
        }

        if (isActive) {
            textField.textboxKeyTyped(ch, key);
            text = textField.getText();
        }
    }

    public String getPreviewText() {
        return text == null || text.isEmpty() ? PREVIEW_TEXT : text;
    }
}
