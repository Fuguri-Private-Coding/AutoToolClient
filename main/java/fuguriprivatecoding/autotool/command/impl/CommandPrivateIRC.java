package fuguriprivatecoding.autotool.command.impl;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.command.Command;
import fuguriprivatecoding.autotool.utils.discord.IRC;

public class CommandPrivateIRC extends Command {

    public CommandPrivateIRC() {
        super("PrivateIRCChat", "/privateirc <user> <message>" , "pirc", "privateirc");

    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            super.usage();
            return;
        }

        IRC irc = Client.INST.getIrc();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Client.INST.getProfile().toString()).append(" ");
        for (String arg : args) {
            if (arg.equalsIgnoreCase(args[0]) || arg.equalsIgnoreCase(args[1])) continue;
            stringBuilder.append(arg).append(" ");
        }
        irc.sendPrivateMessage(args[1], stringBuilder.toString());
        Client.INST.getConsole().history.add("§f[§2IRC§f] " + args[1] + " " + stringBuilder);
    }
}
