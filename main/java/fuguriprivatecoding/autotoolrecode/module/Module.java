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
import fuguriprivatecoding.autotoolrecode.utils.client.sound.Sounds;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Module implements Imports, SettingAble, EventListener, KeyListener {

	final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	@Getter final String name = annotation.name();
	@Getter final Category category = annotation.category();
	@Setter @Getter int key = annotation.key();
	@Getter boolean toggled;
	@Getter final List<Setting> settings;
	@Getter @Setter boolean hide = annotation.hide();
	@Getter String description = annotation.description();

	@Getter @Setter boolean isHovered;

	@Getter EasingAnimation arrayListAnim = new EasingAnimation(0);
	@Getter EasingAnimation toggleAnimation = new EasingAnimation();
    @Getter EasingAnimation descAnim = new EasingAnimation();

    public Module() {
		settings = new ArrayList<>();
		setToggled(annotation.toggled());

		Modules.getInstance().register(this);
		registerToKeyBinds();
		registerToEvents();
	}

	public void setToggled(boolean toggled) {
		if (this.toggled != toggled)
			toggle();
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
        (toggled ? Sounds.getEnableVlSound() : Sounds.getDisableVlSound()).playSound(1);
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
	public void addSettings(Setting... settings) {
		this.settings.addAll(List.of(settings));
	}

	@Override
	public void onTick(boolean pressed) {
		if (pressed) toggle();
	}

	@Override
	public boolean shouldListenEvents() {
		return Utils.isWorldLoaded() && toggled;
	}

	@Override
	public boolean shouldListenKey() {
		return Utils.isWorldLoaded() && mc.currentScreen == null;
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
