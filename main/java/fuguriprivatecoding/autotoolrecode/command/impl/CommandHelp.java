package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.command.Command;
import fuguriprivatecoding.autotoolrecode.command.Commands;

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
            Commands commandManager = Client.INST.getCommands();
            if (commandManager.commands.isEmpty()) {
                addMessage("Command List is empty.");
                return;
            }
            addMessage("Command List:");
            for (Command command : commandManager.commands) {
                if (command.getName().equalsIgnoreCase("help")) return;
                addMessage(command.getName() + " -> " + command.getUsage());
            }
        }
    }
}
