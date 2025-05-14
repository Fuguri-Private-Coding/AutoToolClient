package me.hackclient.command.impl;

import me.hackclient.Client;
import me.hackclient.command.Command;
import me.hackclient.utils.discord.IRC;

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
            IRC irc = Client.INSTANCE.getIrc();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Client.INSTANCE.getProfile().toString()).append(" ");
            for (String arg : args) {
                if (arg.equalsIgnoreCase(args[0])) continue;
                stringBuilder.append(arg).append(" ");
            }
            irc.sendIRCMessage(stringBuilder.toString());
        }
    }
}
