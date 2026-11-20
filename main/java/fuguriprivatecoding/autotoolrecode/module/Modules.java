package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.*;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.Blink;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.Ping;
import fuguriprivatecoding.autotoolrecode.module.impl.legit.*;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.*;
import fuguriprivatecoding.autotoolrecode.module.impl.move.*;
import fuguriprivatecoding.autotoolrecode.module.impl.player.*;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Modules {

	@Getter
	private static final Modules instance = new Modules();
	private Modules() {}

    @Getter public CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<>();
	private final Map<Class<? extends Module>, Module> classModuleMap = new HashMap<>();

	public void init() {
		// Combat
		new AntiBot();
		new AutoSoup();
		new ClickSettings();
		new Hitbox();
		new KeepSprint();
		new MoreKB();
		new Reach();
		new RotationHandler();
		new Teams();
		new TimerRange();
		new Velocity();
		new KillAura();

		// Connect
		new BackTrack();
		new Blink();
		new Ping();

		// Legit
		new AimAssist();
		new AutoClicker();
		new BridgeAssist();
		new CPSBooster();
		new InvClicker();

		// Misc
		new AutoLeave();
		new ClientSpoofer();
		new Debugger();
		new FakeGameMode();
		new Fixes();
		new FlagDetector();
		new MidClick();
		new MurderMystery();
		new NoScreenClose();
		new RawMouseInput();

		// Move
		new AirStuck();
		new Booster();
		new FastLadder();
		new Fly();
		new GuiMove();
		new HighJump();
		new LongJump();
		new NoFall();
		new NoSlow();
		new NoWeb();
		new Speed();
		new Sprint();
		new Timer();
		new VClip();

		// Player
		new AntiFireball();
		new AutoRegister();
		new AutoTool();
		new ChestStealer();
		new FastBreak();
		new FastPlace();
		new Fucker();
		new InvManager();
		new Phase();
		new Regen();
		new Scaffold();

		// Visual
		new ArrayList();
		new AspectRatio();
		new BedESP();
		new BlockOverlay();
		new Blur();
		new CenteredInventory();
		new ChestESP();
		new ClickGui();
		new CPSCounter();
		new CustomCamera();
		new CustomCape();
		new CustomCrosshair();
		new CustomSkin();
		new Dot();
		new DynamicIsland();
		new Effects();
		new ESP();
		new FovModifier();
		new FullBright();
		new Glow();
		new Hand();
		new HurtCamera();
		new ItemPhysics();
		new MoreSwing();
		new MotionBlur();
		new NameTags();
		new NoRender();
		new Notifications();
		new ScoreBoard();
		new TargetESP();
		new TestRender();
		new TimeChanger();
		new Trajectory();
		new ViewBobbing();

		// Client
		new ClientSettings();
	}

	public void register(Module module) {
		modules.add(module);
		classModuleMap.put(module.getClass(), module);
	}
	
	public List<Module> getModulesByCategory(Category category) {
		return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<? extends Module> moduleClass) {
        return (T) classModuleMap.get(moduleClass);
    }

	@SuppressWarnings("unchecked")
    public <T extends Module> T getModule(String name) {
        return (T) modules.stream()
            .filter(module -> module.getName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }

    public List<Module> getEnabledModules() {
        List<Module> moduleList = new java.util.ArrayList<>();

        for (Module module : modules) {
            if (module.isHide() || (!module.isToggled() && !module.getArrayListAnim().isAnimating()))
                continue;

            moduleList.add(module);
        }

        return moduleList;
    }
}
