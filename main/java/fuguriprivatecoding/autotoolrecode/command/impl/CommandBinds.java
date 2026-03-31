package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.command.Command;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CommandBinds extends Command {

    public CommandBinds() {
        super("Binds", "/binds", "binds");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            super.usage();
            return;
        }

        if (args[0].equalsIgnoreCase("binds")) {
            List<Module> modules = Modules.getModules().stream()
                .filter(module -> module.getKey() != Keyboard.KEY_NONE).toList();

            for (Module module : modules) {
                addMessage(module.getName() + " Zabindjen on " + Keyboard.getKeyName(module.getKey()));
            }
        }
    }
}
