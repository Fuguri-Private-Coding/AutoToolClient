package fuguriprivatecoding.autotoolrecode.module;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;

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
		if (Client.INST.getModuleManager() != null && Client.INST.getModuleManager().getModule(ClickGui.class) != null) {
			volume = Client.INST.getModuleManager().getModule(ClickGui.class).toggleModuleVolume.getValue();
		}

		if (toggled) {
			Client.INST.getSoundsManager().getEnableSound().asyncPlay(volume);
			Client.INST.getEventManager().register(this);
			onEnable();
		} else {
			Client.INST.getSoundsManager().getDisableSound().asyncPlay(volume);
			Client.INST.getEventManager().unregister(this);
			onDisable();
		}
	}

	public boolean toggled() {
        return toggled;
	}

	public void onEnable() {}
	public void onDisable() {}

	public void onEvent(Event event) {}

    public void setToggled(boolean toggled) {
		if (this.toggled != toggled) {
			toggle();
		}
	}

	public String getSuffix() {
		return "";
	}
}
