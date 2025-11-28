package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
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
    public float draw(float x, float y, ClientFontRenderer font, Color elementColor, float alpha) {
        float nameWidth = (float) font.getStringWidth(getName() + "->");

        toggleAnimation.update(4f, Easing.OUT_CUBIC);
        toggleAnimation.setEnd(toggled);

        font.drawString(getName(), x, y, Colors.WHITE.withAlphaClamp(alpha));

        RoundedUtils.drawRect(x + nameWidth, y, 20, 10, 5, Colors.BLACK.withAlphaClamp(alpha));
        RoundedUtils.drawRect(x + nameWidth, y, 20 * toggleAnimation.getValue(), 10, 5, elementColor);
        RoundedUtils.drawRect(x + nameWidth + (15 * toggleAnimation.getValue()), y, 10, 10, 5, Color.WHITE);

        return 15;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFontRenderer font) {
        float nameWidth = (float) font.getStringWidth(getName());
        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + nameWidth, y, 40, 10);

        if (hovered && key == 0) toggled = !toggled;

        return 15;
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
