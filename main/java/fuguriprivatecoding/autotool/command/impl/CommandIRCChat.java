package fuguriprivatecoding.autotool.command.impl;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.command.Command;
import fuguriprivatecoding.autotool.utils.discord.IRC;

public class CommandIRCChat extends Command {

    public CommandIRCChat() {
        super("IRCChat", "/irc <message>" , "irc");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            super.usage();
            return;
        }

        if (args[0].equalsIgnoreCase("irc")) {
            IRC irc = Client.INST.getIrc();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Client.INST.getProfile().toString()).append(" ");
            for (String arg : args) {
                if (arg.equalsIgnoreCase(args[0])) continue;
                stringBuilder.append(arg).append(" ");
            }
            irc.sendIRCMessage(stringBuilder.toString());
        }
    }
}
