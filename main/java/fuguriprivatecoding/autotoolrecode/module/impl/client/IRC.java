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
import java.util.concurrent.*;

@ModuleInfo(name = "IRC", category = Category.CLIENT, description = "ХАЛЯЛЬ ДОКСИНГ В МАЙНКРАФТЕ НАХУЙ")
public class IRC extends Module {

    public static HashMap<String, Profile> usersOnline = new HashMap<>();
    private static final List<Message> history = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;

    @Override
    public void onEnable() {
        ClientIRC.connectServer();
        enableThread();
    }

    @Override
    public void onDisable() {
        ClientIRC.disconnectServer();
        history.clear();
        usersOnline.clear();
        disableThread();
    }

    private void enableThread() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        updateLists();
    }

    public void disableThread() {
        if (task != null) {
            task.cancel(true);
        }

        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void updateLists() {
        task = scheduler.scheduleAtFixedRate(() -> {
            List<Message> newList = ClientIRC.getServerChannel()
                .getIterableHistory()
                .stream()
                .toList();

            history.clear();
            history.addAll(newList);

            if (!history.isEmpty()) {
                for (Message message : history) {
                    String msg = message.getContentRaw();
                    String[] args = msg.split(" ");

                    if (args.length >= 3) {
                        String ign = args[0];
                        String clientName = args[1].replace("[", "").replace("]", "");
                        String role = args[2].replace("[", "").replace("]", "");

                        if (usersOnline.containsKey(ign)
                            && usersOnline.containsValue(new Profile(clientName, Role.fromRoleName(role))))
                            continue;

                        usersOnline.put(ign, new Profile(clientName, Role.fromRoleName(role)));
                    }
                }
            }

        }, 0, 15, TimeUnit.SECONDS);
    }

    public static boolean isClientUser(String name) {
        return usersOnline.get(name) != null;
    }

    public static boolean isClientUser(EntityPlayer ent) {
        return isClientUser(ent.getName());
    }
}