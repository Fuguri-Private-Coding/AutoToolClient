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
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
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
		new AutoSoup();
		new KillAura();
		new AspectRatio();
		new DynamicIsland();
		new AutoRegister();
		new FastLadder();
		new BridgeAssist();
		new AntiBot();
		new FovModifier();
		new Notifications();
		new ItemPhysics();
		new CPSBooster();
		new Trajectory();
		new FastBreak();
		new Test2();
		new Regen();
		new Reach();
		new Teams();
		new Hitbox();
		new Blur();
		new Dot();
		new FastPlace();
		new BedESP();
		new CustomSkin();
		new Booster();
		new ChestStealer();
		new MoreKB();
		new Velocity();
		new MurderMystery();
		new InvManager();
		new CPSCounter();
		new CenteredInventory();
		new CustomCrosshair();
		new ESP();
		new NoFall();
		new BackTrack();
		new RotationHandler();
		new Glow();
		new Fly();
		new AutoClicker();
		new AutoTool();
		new AntiFireball();
		new ChestESP();
		new Phase();
		new NoScreenClose();
		new AirStuck();
		new VClip();
		new NoSlow();
		new NoWeb();
		new Speed();
		new Sprint();
		new Timer();
		new ArrayList();
		new MoreSwing();
		new Debugger();
		new Ping();
		new TargetESP();
		new FullBright();
		new ClientSpoofer();
		new TimerRange();
		new ClickGui();
		new Fixes();
		new FlagDetector();
		new AimAssist();
		new Blink();
		new MotionBlur();
		new Hand();
		new ViewBobbing();
		new ClickSettings();
		new NameTags();
		new NoRender();
		new Test();
		new MidClick();
		new CustomCape();
		new InvClicker();
		new Fucker();
		new HurtCamera();
		new CustomCamera();
		new BlockOverlay();
		new FakeGameMode();
		new KeepSprint();
		new AutoLeave();
		new Effects();
		new GuiMove();
		new ClientSettings();
		new ScoreBoard();
		new TimeChanger();
		new TestRender();
		new Scaffold();
		new HighJump();
		new LongJump();
		new RawMouseInput();

		ClientUtils.chatLog("Успешно инициализировал модули.");
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
