package me.hackclient.module;

import me.hackclient.module.impl.client.DiscordRPC;
import me.hackclient.module.impl.combat.*;
import me.hackclient.module.impl.connection.*;
import me.hackclient.module.impl.exploit.*;
import me.hackclient.module.impl.legit.*;
import me.hackclient.module.impl.misc.*;
import me.hackclient.module.impl.move.*;
import me.hackclient.module.impl.player.*;
import me.hackclient.module.impl.visual.*;
import me.hackclient.module.impl.visual.Test;

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
				 new Reach(),
				 new ChatBypass(),
				 new KillAura(),
				 new Dot(),
				 new Criticals(),
				 new Regen(),
				 new ChestStealer(),
				 new Jesus(),
				 new MoreKB(),
				 new BreakIndicator(),
				 new Velocity(),
				 new MurderDetector(),
				 new ClientHandler(),
				 new InvManager(),
				 new AutoLeave(),
				 new Trails(),
				 new ESP(),
				 new RotationHandler(),
				 new Shadows(),
				 new Fly(),
				 new AutoClicker(),
				 new AutoTool(),
				 new HighJump(),
				 new BridgeAssist(),
				 new Scaffold(),
				 new AntiFireball(),
				 new Test(),
				 new DiscordRPC(),
				 new LongJump(),
				 new Phase(),
				 new NoGuiClose(),
				 new AirStuck(),
				 new VClip(),
				 new NoSlow(),
				 new NoWeb(),
				 new KBLager(),
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
				 new TimerRange(),
				 new ClickGui(),
				 new CPSCounter(),
				 new Fixes(),
				 new FlagDetector(),
				 new AimAssist(),
				 new BPSCounter(),
				 new GodMode(),
				 new Blink(),
				 new Animations(),
				 new MotionBlur(),
				 new AutoPlace(),
				 new NoJumpDelay(),
				 new BackTrack(),
				 new FlyTimer(),
				 new MLG(),
				 new TimeChanger(),
				 new ClickSettings(),
				 new Particle(),
				 new me.hackclient.module.impl.misc.Test(),
				 new TrashTalk(),
				 new NameTags(),
				 new NoRender(),
				 new MidClick(),
				 new AutoBot(),
				 new CustomCape(),
				 new InvClicker(),
				 new CustomCamera(),
				 new KillEvents(),
				 new CustomScoreboard()
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
