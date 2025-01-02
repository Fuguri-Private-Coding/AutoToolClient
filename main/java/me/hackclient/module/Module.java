package me.hackclient.module;

import me.hackclient.event.ConditionCallableObject;
import me.hackclient.event.Event;
import me.hackclient.settings.Setting;
import me.hackclient.utils.interfaces.InstanceAccess;

import java.util.ArrayList;
import java.util.List;

public class Module implements InstanceAccess, ConditionCallableObject {

	private final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);

	private final String name = annotation.name();
	private final Category category = annotation.category();

	private int key = annotation.key();
	private boolean toggled = annotation.toggled();

	private final List<Setting> settings;

	public Module() {
		callables.add(this);
		checkToggled();
		settings = new ArrayList<>();
	}

	public void toggle() {
		setToggled(!toggled);
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	private void checkToggled() {
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

	public List<Setting> getSettings() {
		return settings;
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
		checkToggled();
	}

	@Override
	public void onEvent(Event event) {

	}

	@Override
	public boolean handleEvents() {
		return isToggled() && mc.thePlayer != null && mc.theWorld != null;
	}
}
