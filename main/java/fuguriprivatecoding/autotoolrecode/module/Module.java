package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Notifications;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.client.sound.Sounds;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.Client;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Module implements Imports, SettingAble, EventListener {

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
	}

	public void toggle() {
		toggled = !toggled;

        ClientSettings clientSettings = Modules.getModule(ClientSettings.class);
        float volume = clientSettings != null ? clientSettings.toggleModuleVolume.getValue() : 1;

        playSound(volume);

        if (toggled) {
			Events.register(this);
			onEnable();
		} else {
			Events.unregister(this);
			onDisable();
		}

        arrayListAnim.setEnd(toggled);
        toggleAnimation.setEnd(toggled);
        addNotification();
    }

    void addNotification() {
        Notifications notifications = Modules.getModule(Notifications.class);
        if (!Client.INST.isStarting() && notifications != null && notifications.isToggled() && !name.equalsIgnoreCase("ClickGui")) Notifications.addNotification(getName(), toggled);
    }

	void playSound(float volume) {
		if (Client.INST.isStarting() || name.equalsIgnoreCase("ClickGui")) return;
        (toggled ? Sounds.getEnableVlSound() : Sounds.getDisableVlSound()).asyncPlay(volume);
	}

	public void onEnable() {

	}
	public void onDisable() {

	}

    @Override
    public boolean listen() {
        return Utils.isWorldLoaded() && toggled;
    }

    public void onEvent(Event event) {

	}

    public void setToggled(boolean toggled) {
		if (this.toggled != toggled) {
			toggle();
		}
	}

	public String getSuffix() {
		return "";
	}

	@Override
	public void addSetting(Setting setting) {
		settings.add(setting);
	}

	@Override
	public void addSettings(Setting... settings) {
		this.settings.addAll(List.of(settings));
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
