package me.hackclient.command;

import lombok.Setter;
import me.hackclient.command.impl.*;

import java.util.ArrayList;
import java.util.List;

@Setter
public class CommandManager {
	public final List<Command> commands = new ArrayList<>();
	
	private String prefix = "/";

	public CommandManager() {
		commands.add(new CommandToggle());
		commands.add(new CommandBind());
		commands.add(new CommandModel());
		commands.add(new CommandIRCChat());
		commands.add(new CommandPrivateIRC());
		commands.add(new CommandHelp());
	}
	
	public boolean handle(String msg) {
		if (!msg.startsWith(prefix)) {
			return false;
		}
		
		String[] args = msg.substring(prefix.length()).split(" ");
		for (Command command : commands) {
			if (command.getAliases().contains(args[0])) {
				command.execute(args);
				return true;
			}
		}
 		
		return false;
	}
}
