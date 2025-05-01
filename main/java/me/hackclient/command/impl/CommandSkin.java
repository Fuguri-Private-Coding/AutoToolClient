package me.hackclient.command.impl;

import me.hackclient.command.Command;
import me.hackclient.utils.interfaces.InstanceAccess;

public class CommandSkin extends Command implements InstanceAccess {

    public CommandSkin() {
        super("CustomSkin", "/cs /customSkin <set/clear>", "cs", "customSkin");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            console.log("Not enough arguments!");
            super.usage();
            return;
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
            String name = args[2];

            console.log("Successfully installed custom skin");
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {

            console.log("Successfully cleared custom skin");
        }
    }
}
