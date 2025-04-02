package me.hackclient.command.impl;

import me.hackclient.Client;
import me.hackclient.module.Module;
import me.hackclient.command.Command;

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
		
		Module module = Client.INSTANCE.getModuleManager().getModule(args[1]);
		if (module == null) {
			console.log("There is no such module!");
			return;
		}
		
		module.toggle();
	}
}
