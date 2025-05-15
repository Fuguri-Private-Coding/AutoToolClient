package me.hackclient.command.impl;

import me.hackclient.Client;
import me.hackclient.command.Command;
import me.hackclient.utils.discord.IRC;

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

        IRC irc = Client.INSTANCE.getIrc();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Client.INSTANCE.getProfile().toString()).append(" ");
        for (String arg : args) {
            if (arg.equalsIgnoreCase(args[0]) || arg.equalsIgnoreCase(args[1])) continue;
            stringBuilder.append(arg).append(" ");
        }
        irc.sendPrivateMessage(args[1], stringBuilder.toString());
    }
}
