package me.hackclient.utils.discord;

import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.TickEvent;
import me.hackclient.guis.console.ConsoleGuiScreen;
import me.hackclient.guis.main.GuiClientMainMenu;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiMultiplayer;

public class Discord implements InstanceAccess, ConditionCallableObject {

    {
        callables.add(this);
    }

    long timestamp;

    @Getter
    private String name;

    public boolean run = false;

    ConsoleGuiScreen console;

    public void init() {
        run = true;
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {
            console.log("§a[§9Discord§a]§e Connected to user " + discordUser.username + ".");
            console.log("§a[§9Discord§a]§a Rich Presence is started.");
            timestamp = System.currentTimeMillis();
            if (discordUser.userId != null) {
                name = discordUser.username;
            } else {
                System.exit(0);
            }
        }).build();

        DiscordRPC.discordInitialize("1356982126746140713", handlers, true);
    }

    @Override
    public void onEvent(Event event) {
        if (console == null) console = Client.INSTANCE.getConsole();
        if (event instanceof TickEvent) {
            if (run) {
                if (mc.thePlayer != null) {
                    if (mc.isSingleplayer()) {
                        update("Playing lonely in Singleplayer", "In game.");
                    } else if (mc.getCurrentServerData() != null) {
                        update("Maybe cheating on " + mc.getCurrentServerData().serverIP, "Cheating.");
                    }
                } else {
                    if (mc.currentScreen instanceof GuiMultiplayer) {
                        update("Multiplayer menu", "Idle.");
                    } else if (mc.currentScreen instanceof GuiClientMainMenu) {
                        update("Main menu", "Idle.");
                    }
                }

                DiscordRPC.discordRunCallbacks();
            }
        }
    }

    public void stop() {
        console.log("§a[§9Discord§a]§4 Rich Presence was offline.");
        DiscordRPC.discordShutdown();
        run = false;
    }

    public void update(String line1, String line2) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2);
        rpc.setDetails(line1);
        rpc.setBigImage("logo", "AutoTool " + Client.INSTANCE.getVersion());
        rpc.setStartTimestamps(timestamp);
        DiscordRPC.discordUpdatePresence(rpc.build());
    }

    @Override
    public boolean handleEvents() {
        return run;
    }
}