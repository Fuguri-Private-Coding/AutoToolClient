package fuguriprivatecoding.autotool.command.impl;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.command.Command;
import fuguriprivatecoding.autotool.command.CommandManager;

public class CommandHelp extends Command {

    public CommandHelp() {
        super("Help", "/help" , "help");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            super.usage();
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            CommandManager commandManager = Client.INST.getCommandManager();
            if (commandManager.commands.isEmpty()) {
                console.log("Command List is empty.");
                return;
            }
            console.log("Command List:");
            for (Command command : commandManager.commands) {
                if (command.getName().equalsIgnoreCase("help")) return;
                console.log(command.getName() + " -> " + command.getUsage());
            }
        }
    }
}
