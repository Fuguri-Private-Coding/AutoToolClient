package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import java.util.ArrayList;
import java.util.List;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;

public class Module implements Imports, SettingAble {

	final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	@Getter final String name = annotation.name();
	@Getter final Category category = annotation.category();
	@Setter @Getter int key = annotation.key();
	@Getter boolean toggled;
	@Getter final List<Setting> settings;
	@Getter @Setter boolean hide = annotation.hide();
	@Getter String description = annotation.description();

	@Getter @Setter long hoverStartTime;
	@Getter @Setter boolean isHovered;
	@Getter EasingAnimation arrayListAnimation = new EasingAnimation(0);
	@Getter EasingAnimation toggleAnimation = new EasingAnimation();

    public Module() {
		settings = new ArrayList<>();
		setToggled(annotation.toggled());
	}

	public void toggle() {
		toggled = !toggled;
		float volume = 1;
		if (Client.INST.getModules() != null && Client.INST.getModules().getModule(ClientSettings.class) != null) volume = Client.INST.getModules().getModule(ClientSettings.class).toggleModuleVolume.getValue();

		if (toggled) {
			playSound(volume);
			Client.INST.getEvents().register(this);
			arrayListAnimation.setEnd(1);
			onEnable();
		} else {
			playSound(volume);
			Client.INST.getEvents().unregister(this);
			arrayListAnimation.setEnd(0);
			onDisable();
		}
	}

	void playSound(float volume) {
		if (Client.INST.isStarting() || name.equalsIgnoreCase("ClickGui")) return;
		if (toggled) Client.INST.getSounds().getEnableSound().asyncPlay(volume);else Client.INST.getSounds().getDisableSound().asyncPlay(volume);
	}

	public boolean toggled() {
        return toggled;
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	public void onEvent(Event event) {}

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
}
