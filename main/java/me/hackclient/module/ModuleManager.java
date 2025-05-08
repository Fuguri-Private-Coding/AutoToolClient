package me.hackclient.module;

import lombok.Getter;
import me.hackclient.module.impl.client.DiscordRPCModule;
import me.hackclient.module.impl.combat.*;
import me.hackclient.module.impl.connection.*;
import me.hackclient.module.impl.exploit.*;
import me.hackclient.module.impl.legit.*;
import me.hackclient.module.impl.misc.*;
import me.hackclient.module.impl.move.*;
import me.hackclient.module.impl.player.*;
import me.hackclient.module.impl.visual.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ModuleManager {

	@Getter private CopyOnWriteArrayList<Module> modules;
	public static ModuleManager INSTANCE;

	public ModuleManager() {
		INSTANCE = this;
		long currentNano = System.nanoTime();
		 register(
				 new AutoSoup(),
				 new Reach(),
				 new ChatBypass(),
				 new KillAura(),
				 new Dot(),
				 new Regen(),
				 new ChestStealer(),
				 new MoreKB(),
				 new Velocity(),
				 new MurderMysteryHelper(),
				 new InvManager(),
				 new Trails(),
				 new ESP(),
				 new BackTrack(),
				 new RotationHandler(),
				 new Shadows(),
				 new Fly(),
				 new AutoClicker(),
				 new AutoTool(),
				 new BridgeAssist(),
				 new Scaffold(),
				 new AntiFireball(),
				 new DiscordRPCModule(),
				 new Phase(),
				 new NoGuiClose(),
				 new AirStuck(),
				 new VClip(),
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
				 new TimerRange(),
				 new ClickGui(),
				 new Fixes(),
				 new FlagDetector(),
				 new AimAssist(),
				 new GodMode(),
				 new Blink(),
				 new Animations(),
				 new MotionBlur(),
				 new AutoPlace(),
				 new NoJumpDelay(),
				 new MLG(),
				 new TimeChanger(),
				 new ClickSettings(),
				 new Particle(),
				 new TimerRangeV2(),
				 new Test(),
				 new TrashTalk(),
				 new NameTags(),
				 new NoRender(),
				 new MidClick(),
				 new CustomCape(),
				 new InvClicker(),
				 new CustomCamera(),
				 new KillEffects(),
				 new BlockOverlay(),
				 new HUD(),
				 new ModelTrainer()
		);
		System.out.println("Init ModuleManager: " + (System.nanoTime() - currentNano) / 1000000D + " ms.");
	}

	private void register(Module... modulesToRegister) {
		modules = new CopyOnWriteArrayList<>(modulesToRegister);
	}

	public List<Module> getModulesByCategory(Category category) {
		return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) modules.stream()
                .filter(module -> module.getClass() == moduleClass)
                .findFirst().orElse(null);
    }

	@SuppressWarnings("unchecked")
    public <T extends Module> T getModule(String name) {
        return (T) modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

	public List<Module> getEnabledModules() {
		return modules.stream().filter(Module::isToggled).collect(Collectors.toList());
	}
}
