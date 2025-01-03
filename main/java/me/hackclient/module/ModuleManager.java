package me.hackclient.module;

import me.hackclient.module.impl.combat.*;
import me.hackclient.module.impl.connection.*;
import me.hackclient.module.impl.misc.*;
import me.hackclient.module.impl.move.*;
import me.hackclient.module.impl.player.*;
import me.hackclient.module.impl.visual.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ModuleManager {
	public CopyOnWriteArrayList<Module> modules;
	public static ModuleManager INSTANCE;

	public ModuleManager() {
		INSTANCE = this;
		 register(
				 new AutoSoup(),
				 new Bloom(),
				 new AntiBot(),
				 new KillAura(),
				 new MoreKB(),
				 new Teams(),
				 new Velocity(),
				 new Fly(),
				 new HighJump(),
				 new LongJump(),
				 new NoSlow(),
				 new NoWeb(),
				 new Speed(),
				 new Sprint(),
				 new Timer(),
				 new Disabler(),
				 new ArrayList(),
				 new MoreSwing(),
				 new Ping(),
				 new TargetESP(),
				 new FullBright(),
				 new ClientSpoofer(),
				 new TickBase(),
				 new ClickGui(),
				 new DelayFix(),
				 new Handler(),
				 new FlagDetector(),
				 new Animations(),
				 new MotionBlur(),
				 new ClientLogo(),
				 new AutoPlace(),
				 new NoJumpDelay(),
				 new FlyTimer(),
				 new MLG(),
				 //new KillEvents(),
				 new NameTags(),
				 new NoRender(),
				 new MidClick(),
				 new AutoBot()
		);
	}

	private void register(Module... modulesToRegister) {
		modules = new CopyOnWriteArrayList<>(modulesToRegister);
	}

	public List<Module> getModulesByCategory(Category category) {
		return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
	}

	public <T extends Module> T getModule(Class<T> moduleClass) {
		return (T) modules.stream()
				.filter(module -> module.getClass() == moduleClass)
				.findFirst().orElse(null);
	}

	public <T extends Module> T getModule(String name) {
		return (T) modules.stream()
				.filter(module -> module.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}

	public List<Module> getEnabledModules() {
		return modules.stream().filter(Module::isToggled).collect(Collectors.toList());
	}
}
