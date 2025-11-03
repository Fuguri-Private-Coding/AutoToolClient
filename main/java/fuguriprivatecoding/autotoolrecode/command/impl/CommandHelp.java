package fuguriprivatecoding.autotoolrecode.command.impl;

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
            if (Commands.commands.isEmpty()) {
                addMessage("Command List is empty.");
                return;
            }
            addMessage("Command List:");
            for (Command command : Commands.commands) {
                if (command.getName().equalsIgnoreCase("help")) return;
                addMessage(command.getName() + " -> " + command.getUsage());
            }
        }
    }
}
