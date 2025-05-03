package me.hackclient.module;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.Client;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.settings.Setting;
import me.hackclient.utils.interfaces.Imports;

import java.util.ArrayList;
import java.util.List;

public class Module implements Imports {

	final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	@Getter final String name = annotation.name();
	@Getter final Category category = annotation.category();
	@Setter @Getter int key = annotation.key();
	@Getter boolean toggled;
	@Getter final List<Setting> settings;
	@Getter @Setter boolean hide = annotation.hide();

	public Module() {
		settings = new ArrayList<>();
		setToggled(annotation.toggled());
	}

	public void toggle() {
		toggled = !toggled;
		float volume = 1;
		if (Client.INSTANCE.getModuleManager() != null && Client.INSTANCE.getModuleManager().getModule(ClickGui.class) != null) {
			volume = Client.INSTANCE.getModuleManager().getModule(ClickGui.class).toggleModuleVolume.getValue();
		}

		if (toggled) {
			Client.INSTANCE.getSoundsManager().getEnableSound().asyncPlay(volume);
			Client.INSTANCE.getEventManager().register(this);
			onEnable();
		} else {
			Client.INSTANCE.getSoundsManager().getDisableSound().asyncPlay(volume);
			Client.INSTANCE.getEventManager().unregister(this);
			onDisable();
		}
	}

	public void onEnable() {}
	public void onDisable() {}

    public void setToggled(boolean toggled) {
		if (this.toggled != toggled) {
			toggle();
		}
	}

	public String getSuffix() {
		return "";
	}
}
