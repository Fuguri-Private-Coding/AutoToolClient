package fuguriprivatecoding.autotoolrecode.module;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.Getter;
import org.atteo.classindex.ClassIndex;

import java.util.List;

@Getter
public class ModuleManager {

	private final CopyOnWriteArrayList<Module> modules;
	public static ModuleManager INSTANCE;

	public ModuleManager() {
		INSTANCE = this;

		modules = new CopyOnWriteArrayList<>();

		ClassIndex.getAnnotated(ModuleInfo.class).forEach(module -> {
			try {
				modules.add((Module) module.getDeclaredConstructor().newInstance());
			} catch (Exception _) {}
		});
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
		return modules.stream().filter(Module::isToggled).filter(m -> !m.isHide()).collect(Collectors.toList());
	}
}
