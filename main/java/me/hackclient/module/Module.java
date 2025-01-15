package me.hackclient.module;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.Event;
import me.hackclient.settings.Setting;
import me.hackclient.utils.interfaces.InstanceAccess;

import java.util.ArrayList;
import java.util.List;

public class Module implements InstanceAccess, ConditionCallableObject {

	final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	@Getter final String name = annotation.name();
	@Getter final Category category = annotation.category();
	@Setter @Getter int key = annotation.key();
	@Getter boolean toggled = annotation.toggled();
	@Getter final List<Setting> settings;

	{
		callables.add(this);
	}

	public Module() {
		checkToggled();
		settings = new ArrayList<>();
	}

	public void toggle() {
		setToggled(!toggled);
	}
	public void onEnable() {}
	public void onDisable() {}

	void checkToggled() {
		if (toggled) {
			try {
				onEnable();
			} catch (Exception ignored) {}
		} else {
			try {
				onDisable();
			} catch (Exception ignored) {}
		}
	}

    public void setToggled(boolean toggled) {
		this.toggled = toggled;
		checkToggled();
	}

	public String getSuffix() {
		return "";
	}

	@Override
	public void onEvent(Event event) {

	}

	@Override
	public boolean handleEvents() {
		return isToggled() && mc.thePlayer != null && mc.theWorld != null;
	}
}
