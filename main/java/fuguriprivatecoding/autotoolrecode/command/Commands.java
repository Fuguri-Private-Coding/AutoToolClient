package fuguriprivatecoding.autotoolrecode.command;

import fuguriprivatecoding.autotoolrecode.command.impl.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public class Commands {
	public final List<Command> commands = new ArrayList<>();
	
	private String prefix = "/";

	public Commands() {
		commands.add(new CommandToggle());
		commands.add(new CommandBind());
		commands.add(new CommandIRC());
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
