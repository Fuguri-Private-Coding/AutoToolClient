package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.command.Command;

public class CommandToggle extends Command {

	public CommandToggle() {
		super("Toggle", "/t /toggle <module>", "t", "toggle");
	}

	@Override
	public void execute(String[] args) {
		if (args.length != 2) {
			addMessage("Inappropriate number of arguments!");
			super.usage();
			return;
		}
		Module module = Client.INST.getModuleManager().getModule(args[1]);
		if (module == null) {
			addMessage("There is no such module!");
			return;
		}
		module.toggle();
	}
}
