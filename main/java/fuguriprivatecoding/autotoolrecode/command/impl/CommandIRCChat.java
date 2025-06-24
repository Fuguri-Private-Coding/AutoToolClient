package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.command.Command;
import fuguriprivatecoding.autotoolrecode.utils.discord.IRC;

public class CommandIRCChat extends Command {

    public CommandIRCChat() {
        super("IRCChat", "/irc <message> || /bc <message> (Only Owner)" , "irc", "bc");
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
            stringBuilder.append(Client.INST.getProfile().getColored()).append(" ");
            for (String arg : args) {
                if (arg.equalsIgnoreCase(args[0])) continue;
                stringBuilder.append(arg).append(" ");
            }
            irc.sendIRCMessage(stringBuilder.toString());
        }

        if (args[0].equalsIgnoreCase("bc") && Client.INST.getProfile().getRole().equalsIgnoreCase("Owner")) {
            IRC irc = Client.INST.getIrc();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("§f[§7Server§f]§f").append(" ");
            for (String arg : args) {
                if (arg.equalsIgnoreCase(args[0])) continue;
                stringBuilder.append(arg).append(" ");
            }
            irc.sendIRCMessage(stringBuilder.toString());
        }
    }
}
