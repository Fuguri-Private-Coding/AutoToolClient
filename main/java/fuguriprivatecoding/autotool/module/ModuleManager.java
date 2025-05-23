package fuguriprivatecoding.autotool.module;

import fuguriprivatecoding.autotool.module.impl.combat.*;
import fuguriprivatecoding.autotool.module.impl.connection.*;
import fuguriprivatecoding.autotool.module.impl.exploit.*;
import fuguriprivatecoding.autotool.module.impl.legit.*;
import fuguriprivatecoding.autotool.module.impl.misc.*;
import fuguriprivatecoding.autotool.module.impl.move.*;
import fuguriprivatecoding.autotool.module.impl.player.*;
import fuguriprivatecoding.autotool.module.impl.visual.*;
import lombok.Getter;
import fuguriprivatecoding.autotool.module.impl.client.DiscordRPCModule;
import fuguriprivatecoding.autotool.module.impl.client.IRCModule;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class ModuleManager {

	private CopyOnWriteArrayList<Module> modules;
	public static ModuleManager INSTANCE;

	public ModuleManager() {
		INSTANCE = this;
		 register(
				 new AutoSoup(),
				 new Reach(),
				 new KillAura(),
				 new Dot(),
				 new ChestStealer(),
				 new MoreKB(),
				 new Velocity(),
				 new MurderMystery(),
				 new InvManager(),
				 new IRCModule(),
				 new Trails(),
				 new ESP(),
				 new BackTrack(),
				 new RotationHandler(),
				 new Shadows(),
				 new Fly(),
				 new AutoClicker(),
				 new AutoTool(),
				 new Scaffold(),
				 new AntiFireball(),
				 //new NewScaffold(),
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
				 new Blink(),
				 new Animations(),
				 new MotionBlur(),
				 new AutoPlace(),
				 new TimeChanger(),
				 new ClickSettings(),
				 new Particle(),
				 new Test(),
				 new TrashTalk(),
				 new NameTags(),
				 new NoRender(),
				 new MidClick(),
				 new CustomCape(),
				 new InvClicker(),
				 new CustomCamera(),
				 new BlockOverlay(),
				 new HUD(),
				 new ModelTrainer()
		);
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
