package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.DiscordRPCModule;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiMultiplayer;

public class Discord implements Imports, EventListener {

    public static Discord INST;

    public static void init() {
        INST = new Discord();
    }

    private Discord() {
        Events.register(this);
    }

    static long timestamp, lastTime;

    @Getter private static String name, id;

    String state;
    String details;

    public static void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {
            ConsoleScreen.logWithoutPrefix("§f[§9Discord§f]§a Подключен к " + discordUser.username + ".");
            ConsoleScreen.logWithoutPrefix("§f[§9Discord§f]§a Активность запущенна.");
            timestamp = System.currentTimeMillis();
            if (discordUser.userId != null) {
                name = discordUser.username;
                id = discordUser.userId;
            } else {
                System.exit(0);
            }
        }).build();

        DiscordRPC.discordInitialize("1356982126746140713", handlers, true);

        update("Запускается игра.", "А пока поешьте печенье.");
        DiscordRPC.discordRunCallbacks();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent && System.currentTimeMillis() - lastTime >= 1000) {
            updateInformation();

            DiscordRPC.discordRunCallbacks();
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean listen() {
        return true;
    }

    public void updateInformation() {
        DiscordRPCModule discordRPCModule = Modules.getModule(DiscordRPCModule.class);

        if (discordRPCModule != null && discordRPCModule.isToggled()) {
            if (mc.thePlayer != null) {
                state = mc.isSingleplayer() ?
                    "В одиночной игре." :
                    "На сервере: " + mc.getCurrentServerData().serverIP;
                details = "В игре.";
            } else {
                state = mc.currentScreen instanceof GuiMultiplayer ? "Выбор сервера." : "Главное меню";
                details = "Бездействует.";
            }
        }

        update(state, details);
    }

    public static void stop() {
        ConsoleScreen.logWithoutPrefix("§f[§9Discord§f]§4 Активность остановлена.");
        DiscordRPC.discordShutdown();
    }

    public static void update(String line1, String line2) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2)
            .setDetails(line1)
            .setBigImage("logo", "AutoTool " + Client.INST.getCLIENT_VERSION())
            .setStartTimestamps(timestamp);

        DiscordRPC.discordUpdatePresence(rpc.build());
    }
}