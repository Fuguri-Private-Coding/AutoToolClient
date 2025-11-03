package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.gui.main.MainScreen;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiMultiplayer;

public class Discord implements Imports, EventListener {

    public Discord() {
        init();
        Events.register(this);
    }

    static long timestamp, lastTime;

    @Getter
    private static String name, id;

    public static boolean run = false;

    public static void init() {
        run = true;
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {
            ConsoleScreen.log("§a[§9Discord§a]§e Connected to user " + discordUser.username + ".");
            ConsoleScreen.log("§a[§9Discord§a]§a Rich Presence is started.");
            timestamp = System.currentTimeMillis();
            if (discordUser.userId != null) {
                name = discordUser.username;
                id = discordUser.userId;
            } else {
                System.exit(0);
            }
        }).build();

        DiscordRPC.discordInitialize("1356982126746140713", handlers, true);

        update("Starting Game...", "YOOO!");
        DiscordRPC.discordRunCallbacks();
    }

    @Override
    public boolean listen() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 1000) {
            if (run) {
                if (mc.thePlayer != null) {
                    if (mc.isSingleplayer()) {
                        update("Playing Singleplayer Odini4ka ebani", "In game.");
                    } else if (mc.getCurrentServerData() != null) {
                        update("AutoTool fucks " + mc.getCurrentServerData().serverIP, "In game.");
                    }
                } else {
                    if (mc.currentScreen instanceof GuiMultiplayer) {
                        update("Vibiraet server", "Idle.");
                    } else if (mc.currentScreen instanceof MainScreen) {
                        update("Zakontril main menu", "Idle.");
                    }
                }

                DiscordRPC.discordRunCallbacks();
                lastTime = System.currentTimeMillis();
            }
        }
    }

    public static void stop() {
        ConsoleScreen.log("§a[§9Discord§a]§4 Rich Presence was offline.");
        DiscordRPC.discordShutdown();
        run = false;
    }

    public static void update(String line1, String line2) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2);
        rpc.setDetails(line1);
        rpc.setBigImage("logo", "AutoTool " + Client.INST.getVersion());
        rpc.setStartTimestamps(timestamp);
        DiscordRPC.discordUpdatePresence(rpc.build());
    }
}