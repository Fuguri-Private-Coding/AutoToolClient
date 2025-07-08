package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.command.Command;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;

public class CommandIRC extends Command {

    public CommandIRC() {
        super("IRC", "/irc <message> || /bc <message> (Only Owner)" , "irc", "bc");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            super.usage();
            return;
        }

        if (args[0].equalsIgnoreCase("irc")) {
            ClientIRC irc = Client.INST.getIrc();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Client.INST.getProfile().getColored()).append(" ");
            for (String arg : args) {
                if (arg.equalsIgnoreCase(args[0])) continue;
                stringBuilder.append(arg).append(" ");
            }
            irc.sendIRCMessage(stringBuilder.toString());
        }

        if (args[0].equalsIgnoreCase("bc") && Client.INST.getProfile().getRole().equalsIgnoreCase("Owner")) {
            ClientIRC irc = Client.INST.getIrc();
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
