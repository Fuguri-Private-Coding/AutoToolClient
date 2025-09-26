package fuguriprivatecoding.autotoolrecode.module;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.*;
import fuguriprivatecoding.autotoolrecode.module.impl.move.*;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.*;
import fuguriprivatecoding.autotoolrecode.module.impl.client.*;
import fuguriprivatecoding.autotoolrecode.module.impl.connection.*;
import fuguriprivatecoding.autotoolrecode.module.impl.exploit.*;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.*;
import fuguriprivatecoding.autotoolrecode.module.impl.player.*;
import fuguriprivatecoding.autotoolrecode.module.impl.legit.*;
import lombok.Getter;

import java.util.List;

@Getter
public class ModuleManager {

	private CopyOnWriteArrayList<Module> modules;
	public static ModuleManager INSTANCE;

	public ModuleManager() {
		INSTANCE = this;
		modules = new CopyOnWriteArrayList<>();

		register(
			new AutoSoup(),
			new KillAura(),
			new AutoWater(),
			new BridgeAssist(),
			new FovModifier(),
			new Trajectory(),
			new Regen(),
			new Reach(),
			new Hitbox(),
			new Blur(),
			new TEZD(),
			new Dot(),
			new FastPlace(),
			new BedESP(),
			new Booster(),
			new ChestStealer(),
			new MoreKB(),
			new Velocity(),
			new MurderMystery(),
			new InvManager(),
			new Trails(),
			new ESP(),
			new BackTrack(),
			new RotationHandler(),
			new Glow(),
			new Fly(),
			new AutoClicker(),
			new AutoTool(),
			new AntiFireball(),
			new ChestESP(),
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
			new Debugger(),
			new Ping(),
			new TargetESP(),
			new Notifications(),
			new FullBright(),
			new FreeLook(),
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
			new ClickSettings(),
			new Test(),
			new NameTags(),
			new NoRender(),
			new MidClick(),
			new CustomCape(),
			new InvClicker(),
			new Fucker(),
			new HurtCamera(),
			new CustomCamera(),
			new BlockOverlay(),
			new DynamicIsland(),
			new FakeGameMode(),
			new ModelTrainer(),
			new KeepSprint(),
			new AutoLeave(),
			new Effects(),
			new GuiMove(),
			new ClientSettings(),
			new ScoreBoard(),
			new IRC(),
			new Ambience(),
			new TargetHUD(),
			new Scaffold(),
			new HighJump(),
//			new Fucker(),
			new LongJump()
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
		return modules.stream().filter(module -> !module.isHide() && (module.isToggled() || (!module.toggled && module.arrayListAnimation.isAnimating()))).toList();
	}
}
