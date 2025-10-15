package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import org.lwjgl.input.Keyboard;
import fuguriprivatecoding.autotoolrecode.command.Command;

public class CommandBind extends Command {
	public CommandBind() {
		super("Bind", "/bind clear/<module> <key>", "b", "bind");
	}

	@Override
	public void execute(String[] args) {
		if (args.length < 2) {
			addMessage("Not enough arguments!");
			super.usage();
			return;
		}
		
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("clear")) {
				Client.INST.getModuleManager().getModules().forEach(module -> module.setKey(Keyboard.KEY_NONE));
                addMessage("All binds are cleared!");
			}
		} else if (args.length == 3) {
			Module module = Client.INST.getModuleManager().getModule(args[1]);
		
			if (module == null) {
                addMessage("There is no such module!");
				return;
			}
			
			int key = Keyboard.getKeyIndex(args[2].toUpperCase());
			
			if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RETURN) {
                addMessage("You cannot bind a module to this key!");
				return;
			}
			
			module.setKey(key);
            addMessage("Module " + module.getName() + " successfully added to " + args[2].toUpperCase());
		}
	}

}
