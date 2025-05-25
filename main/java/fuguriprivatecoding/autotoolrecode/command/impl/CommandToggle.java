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
			console.log("Inappropriate number of arguments!");
			super.usage();
			return;
		}
		Module module = Client.INST.getModuleManager().getModule(args[1]);
		if (module == null) {
			console.log("There is no such module!");
			return;
		}
		module.toggle();
	}
}
