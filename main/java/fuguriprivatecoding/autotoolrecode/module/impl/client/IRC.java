package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.List;

@ModuleInfo(name = "IRC", category = Category.CLIENT, description = "ХАЛЯЛЬ ДОКСИНГ В МАЙНКРАФТЕ НАХУЙ")
public class IRC extends Module {

    public static HashMap<String, Profile> usersOnline = new HashMap<>();

    List<Message> history;
    private long lastTime;

    @Override
    public void onDisable() {
        Client.INST.disconnect();
        history = null;
    }

    @Override
    public void onEnable() {
        Client.INST.connect();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent && System.currentTimeMillis() - lastTime >= 7000) {
            new Thread(() -> {
                history = Client.INST.getIrc().getServerChannel().getIterableHistory().stream().toList();
                if (!history.isEmpty()) {
                    for (Message message : history) {
                        String msg = message.getContentRaw();
                        String[] args = msg.split(" ");

                        String ign = args[0];
                        String clientName = args[1].replace("[", "").replace("]", "");
                        String role = args[2].replace("[", "").replace("]", "");

                        if (usersOnline.containsKey(ign)) continue;

                        usersOnline.put(ign, new Profile(clientName, role));
                    }
                }
            }).start();
            lastTime = System.currentTimeMillis();
        }
    }
}