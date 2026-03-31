package fuguriprivatecoding.autotoolrecode.command;

import fuguriprivatecoding.autotoolrecode.command.impl.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
public class Commands {
	public static final List<Command> commands = new ArrayList<>();

    public static void init() {
        commands.add(new CommandToggle());
        commands.add(new CommandBind());
        commands.add(new CommandIRC());
        commands.add(new CommandHelp());
        commands.add(new CommandBinds());
    }
	
	public static boolean handle(String msg) {
        String prefix = "/";

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
