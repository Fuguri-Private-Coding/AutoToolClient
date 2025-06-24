package fuguriprivatecoding.autotoolrecode.utils.discord;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.guis.console.ConsoleGuiScreen;
import fuguriprivatecoding.autotoolrecode.guis.main.GuiClientMainMenu;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiMultiplayer;

public class Discord implements Imports {

    public void startRPC() {
        Client.INST.getEventManager().register(this);
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

        update("Starting Game...", "SOSAl!");
        DiscordRPC.discordRunCallbacks();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (console == null) console = Client.INST.getConsole();
        if (event instanceof TickEvent) {
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
                    } else if (mc.currentScreen instanceof GuiClientMainMenu) {
                        update("Zakontril main menu", "Idle.");
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
        rpc.setBigImage("logo", "AutoTool " + Client.INST.getVersion());
        rpc.setStartTimestamps(timestamp);
        DiscordRPC.discordUpdatePresence(rpc.build());
    }
}