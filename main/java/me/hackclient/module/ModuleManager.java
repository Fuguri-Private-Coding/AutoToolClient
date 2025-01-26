package me.hackclient.module;

import me.hackclient.module.impl.combat.*;
import me.hackclient.module.impl.connection.*;
import me.hackclient.module.impl.legit.*;
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
//				 new AntiBot(),
				 new KillAura(),
				 new Dot(),
				 new TestESP(),
				 new MoreKB(),
//				 new Teams(),
				 new Velocity(),
				 new TESTESP2(),
				 new Fly(),
				 new AutoClicker(),
				 new HighJump(),
				 new BridgeAssist(),
				 new ClientShader(),
				 new LongJump(),
				 new TestPing(),
				 new NoSlow(),
				 new NoWeb(),
				 new Speed(),
				 new Sprint(),
				 new Timer(),
				 new Disabler(),
				 new LagRange(),
				 new ArrayList(),
				 new MoreSwing(),
				 new Ping(),
				 new TargetESP(),
				 new FullBright(),
				 new ClientSpoofer(),
				 new TimerRange(),
				 new ClickGui(),
				 new Fixes(),
//				 new Handler(),
				 new FlagDetector(),
				 new AimAssist(),
				 new BPSCounter(),
				 new SwagMode(),
				 new Blink(),
				 new Animations(),
				 new MotionBlur(),
				 new ClientLogo(),
				 new AutoPlace(),
				 new NoJumpDelay(),
				 new BackTrack(),
				 new FlyTimer(),
				 new MLG(),
				 new TimeChanger(),
				 new Particle(),
				 new Test(),
				 new TrashTalk(),
				 new TimerRangeV2(),
				 //new KillEvents(),
				 new NameTags(),
				 new NoRender(),
				 new MidClick(),
				 new AutoBot(),
				 new CustomCape()
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
