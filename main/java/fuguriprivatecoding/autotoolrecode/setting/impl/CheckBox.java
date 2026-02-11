package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class CheckBox extends Setting {

	boolean toggled;

	EasingAnimation toggleAnimation = new EasingAnimation();

	public CheckBox(String name, SettingAble parent) {
		super(name, parent);
		toggled = false;
	}

	public CheckBox(String name, SettingAble parent, boolean toggled) {
		super(name, parent);
		this.toggled = toggled;
	}

	public CheckBox(String name, SettingAble parent, BooleanSupplier visible) {
		super(name, parent, visible);
		toggled = false;
	}

	public CheckBox(String name, SettingAble parent, BooleanSupplier visible, boolean toggled) {
		super(name, parent, visible);
		this.setVisible(visible);
		this.toggled = toggled;
	}

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        if (ImGui.checkbox(getName(), toggled)) {
//            toggled = !toggled;
//        }
//        ImGui.popID();
//    }


    @Override
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {
        float widthName = font.getStringWidth(getName() + ": ");

        font.drawString(getName() + ": ", x, y, Colors.WHITE.withAlphaClamp(alpha));

        EasingAnimation toggleAnim = toggleAnimation;

        toggleAnim.update(3f, Easing.OUT_CUBIC);
        toggleAnim.setEnd(toggled);

        Color toggleColor = ColorUtils.interpolateColor(Colors.RED.withAlphaClamp(alpha), Colors.GREEN.withAlphaClamp(alpha), toggleAnim.getValue());

        font.drawString(String.valueOf(toggled), x + widthName, y, toggleColor);

        return 13;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        float widthName = font.getStringWidth(getName());
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + widthName, y, font.getStringWidth(String.valueOf(toggled)), 10);

        if (hovered && key == 0) toggled = !toggled;

        return 13;
    }

    @Override
    public float mouseReleased(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        return 13;
    }

    @Override
    public void keyTyped(int key) {

    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();
        object.addProperty("toggled", toggled);
        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        toggled = object.get("toggled").getAsBoolean();
    }
}
