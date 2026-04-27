package fuguriprivatecoding.autotoolrecode.utils.client;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiMultiplayer;

public class Discord implements Imports {

    private static volatile boolean running = false;

    @Getter private static String name, id;
    private static long timestamp;

    public static void init() {
        start();
    }

    public static void start() {
        running = true;
        DiscordThread thread = new DiscordThread();
        thread.setDaemon(true);
        thread.start();

        DiscordEventHandlers.Builder handle = new DiscordEventHandlers.Builder();
        handle.setReadyEventHandler(user -> {
            if (user.userId != null) {
                ConsoleScreen.log("Подключен к " + user.username + ".");
                ConsoleScreen.log("Активность запущенна.");
                timestamp = System.currentTimeMillis();

                name = user.username;
                id = user.userId;
            } else {
                System.exit(0);
            }
        });

        DiscordRPC.discordInitialize("1356982126746140713", handle.build(), true);

        update("Запускается игра.", "А пока поешьте печенье.");
    }

    public static void stop() {
        ConsoleScreen.log("Активность остановлена.");
        DiscordRPC.discordShutdown();
        running = false;
    }

    private static void updateInformation() {
        String state, details;

        if (mc.thePlayer != null) {
            state = mc.isSingleplayer() ?
                "В одиночной игре." :
                "На сервере: " + mc.getCurrentServerData().serverIP;
            details = "В игре.";
        } else {
            state = mc.currentScreen instanceof GuiMultiplayer ? "Выбор сервера." : "Главное меню";
            details = "Бездействует.";
        }

        update(state, details);
    }

    public static void update(String line1, String line2) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2)
            .setDetails(line1)
            .setBigImage("logo", "AutoTool " + Client.CLIENT_VERSION)
            .setStartTimestamps(timestamp);

        DiscordRPC.discordUpdatePresence(rpc.build());
        DiscordRPC.discordRunCallbacks();
    }

    private static class DiscordThread extends Thread {
        @Override
        public void run() {
            while (running) {
                updateInformation();

                try {
                    sleep(1000);
                } catch (InterruptedException _) {}
            }
        }
    }
}