package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import net.dv8tion.jda.api.entities.Message;
import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "IRC", category = Category.CLIENT, description = "ХАЛЯЛЬ ДОКСИНГ В МАЙНКРАФТЕ НАХУЙ")
public class IRC extends Module {

    public static HashMap<String, Profile> usersOnline = new HashMap<>();
    private static List<Message> history = new CopyOnWriteArrayList<>();

    private static boolean running = false;

    @Override
    public void onEnable() {
        ClientIRC.connectServer();
        running = true;
        IRCThread thread = new IRCThread();
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void onDisable() {
        ClientIRC.disconnectServer();
        running = false;
        history = new CopyOnWriteArrayList<>();
    }

    public static boolean isClientUser(EntityPlayer ent) {
        return usersOnline.get(ent.getName()) != null;
    }

    private static class IRCThread extends Thread {
        @Override
        public void run() {
            while (running) {
                history = ClientIRC.getServerChannel().getIterableHistory().stream().toList();
                if (!history.isEmpty()) {
                    for (Message message : history) {
                        String msg = message.getContentRaw();
                        String[] args = msg.split(" ");

                        String ign = args[0];
                        String clientName = args[1].replace("[", "").replace("]", "");
                        String role = args[2].replace("[", "").replace("]", "");

                        if (usersOnline.containsKey(ign) && usersOnline.containsValue(new Profile(clientName, Role.fromRoleName(role)))) continue;

                        usersOnline.put(ign, new Profile(clientName, Role.fromRoleName(role)));
                    }

                    try {
                        sleep(15000);
                    } catch (InterruptedException _) {}
                }
            }
        }
    }
}