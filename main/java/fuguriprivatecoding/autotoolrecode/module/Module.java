package fuguriprivatecoding.autotoolrecode.module;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.key.KeyListener;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Notifications;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.sound.Sounds;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Module implements Imports, SettingAble, EventListener, KeyListener {

	private final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	final String name = annotation.name();
	final Category category = annotation.category();
	@Setter int key = annotation.key();
	boolean toggled;
	final List<Setting> settings;
	@Setter boolean hide = annotation.hide();
	final String description = annotation.description();

	@Setter boolean isHovered;

	final EasingAnimation arrayListAnim = new EasingAnimation(0);
	final EasingAnimation toggleAnimation = new EasingAnimation();
    final EasingAnimation descAnim = new EasingAnimation();

    public Module() {
		settings = new ArrayList<>();
		setToggled(annotation.toggled());

		Modules.getInstance().register(this);
		registerToKeyBinds();
		registerToEvents();
	}

	public void setToggled(boolean toggled) {
		if (this.toggled != toggled) toggle();
	}

	public void toggle() {
		toggled = !toggled;

        ClientSettings clientSettings = Modules.getInstance().getModule(ClientSettings.class);

		if (clientSettings != null && clientSettings.toggleSound.isToggled()) {
			playSound();
		}

        tick(toggled);

        arrayListAnim.setEnd(toggled);
        toggleAnimation.setEnd(toggled);
        addNotification();
    }

    void addNotification() {
        Notifications notifications = Modules.getInstance().getModule(Notifications.class);
        if (!Client.getInstance().starting && notifications != null && notifications.isToggled() && !name.equalsIgnoreCase("ClickGui")) Notifications.addNotification(getName(), toggled);
    }

	void playSound() {
		if (Client.getInstance().starting || name.equalsIgnoreCase("ClickGui")) return;
        (toggled ? Sounds.getEnableSound() : Sounds.getDisableSound()).playSound(0.9f);
	}

	public void tick(boolean toggled) {

	}

    public void onEvent(Event event) {

	}

	@Override
	public void addSetting(Setting setting) {
		settings.add(setting);
	}

	@Override
	public void onTick(boolean pressed) {
		if (pressed) toggle();
	}

	@Override
	public boolean shouldListenEvents() {
		return Utils.nullCheck() && toggled;
	}

	@Override
	public boolean shouldListenKey() {
		return Utils.nullCheck() && mc.currentScreen == null;
	}

	public JsonObject getObject() {
		JsonObject object = new JsonObject();
		object.addProperty("toggl", toggled);
		object.addProperty("hide", hide);

		for (Setting setting : settings) {
			JsonObject settingObject = setting.getObject();
			object.add(setting.getName(), settingObject);
		}

		return object;
	}

	public void setObject(JsonObject object, boolean includeStates) {
		if (object != null) {
			if (includeStates) {
				setToggled(object.get("toggl").getAsBoolean());
				setHide(object.get("hide").getAsBoolean());
			}

			for (Setting setting : settings) {
				JsonObject settingObject = object.getAsJsonObject(setting.getName());
				if (settingObject != null) setting.setObject(settingObject);
			}
		}
	}
}
