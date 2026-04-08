package fuguriprivatecoding.autotoolrecode.module;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.*;
import fuguriprivatecoding.autotoolrecode.module.impl.move.*;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.*;
import fuguriprivatecoding.autotoolrecode.module.impl.client.*;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.*;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.*;
import fuguriprivatecoding.autotoolrecode.module.impl.player.*;
import fuguriprivatecoding.autotoolrecode.module.impl.legit.*;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class Modules {

    @Getter public CopyOnWriteArrayList<Module> modules;

	public void init() {
		modules = new CopyOnWriteArrayList<>();

		register(
			new AutoSoup(),
			new KillAura(),
            new AspectRatio(),
			new InventoryManager(),
			new DeltaRecorder(),
            new BWHelper(),
			new DynamicIsland(),
            new AutoRegister(),
			new FastLadder(),
			new BridgeAssist(),
			new AntiBot(),
			new FovModifier(),
            new Notifications(),
			new ItemPhysics(),
			new Trajectory(),
            new FastBreak(),
            new EntityColor(),
            new Test2(),
			new Regen(),
			new Reach(),
            new Teams(),
			new Hitbox(),
            new Blur(),
            new Dot(),
            new FastPlace(),
            new NameProtect(),
            new BedESP(),
            new CustomSkin(),
            new Booster(),
            new ChestStealer(),
            new MoreKB(),
            new Velocity(),
            new MurderMystery(),
            new InvManager(),
			new KeyStrokes(),
            new CustomCrosshair(),
            new ESP(),
			new ItemESP(),
			new NoFall(),
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
			new NoScreenClose(),
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
			new FullBright(),
			new FreeLook(),
			new ClientSpoofer(),
			new TimerRange(),
			new ClickGui(),
			new Fixes(),
			new FlagDetector(),
			new AimAssist(),
			new Blink(),
			new MotionBlur(),
			new Hand(),
			new ViewBobbing(),
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
			new FakeGameMode(),
			new KeepSprint(),
			new AutoLeave(),
			new Effects(),
			new GuiMove(),
			new ClientSettings(),
			new ScoreBoard(),
			new IRC(),
			new TimeChanger(),
			new Scaffold(),
			new HighJump(),
			new LongJump(),
			new RawMouseInput()
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
		return modules.stream().filter(module -> !module.isHide() && (module.isToggled() || (!module.toggled && module.arrayListAnim.isAnimating()))).toList();
	}
}
