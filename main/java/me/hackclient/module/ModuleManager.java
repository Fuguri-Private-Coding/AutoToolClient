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
				 new AutoSoupModule(),
				 new BloomModule(),
				 new AntiBotModule(),
				 new KillAuraModule(),
				 new MoreKBModule(),
				 new TeamsModule(),
				 new VelocityModule(),
				 new FlyModule(),
				 new HighJumpModule(),
				 new LongJumpModule(),
				 new NoSlowModule(),
				 new NoWebModule(),
				 new SpeedModule(),
				 new SprintModule(),
				 new TimerModule(),
				 new DisablerModule(),
				 new ArrayListModule(),
				 new MoreSwingModule(),
				 new PingModule(),
				 new NoHurtCamModule(),
				 new TargetESPModule(),
				 new FullBrightModule(),
				 new ClientSpooferModule(),
				 new TimerRangeModule(),
				 new ClickGuiModule(),
				 new DelayFixModule(),
				 new HandlerModule(),
				 new NoRenderModule(),
				 new MidClickModule()
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
