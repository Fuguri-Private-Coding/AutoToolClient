package fuguriprivatecoding.autotoolrecode.module.impl.client;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import net.dv8tion.jda.api.entities.Message;
import net.minecraft.entity.player.EntityPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ModuleInfo(name = "IRC", category = Category.CLIENT, description = "ХАЛЯЛЬ ДОКСИНГ В МАЙНКРАФТЕ НАХУЙ")
public class IRC extends Module {

    public static HashMap<String, Profile> usersOnline = new HashMap<>();

    List<Message> history;
    private long lastTime;

    @Override
    public void onDisable() {
        ClientIRC.disconnectServer();
        history = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        ClientIRC.connectServer();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent && System.currentTimeMillis() - lastTime >= 10000) {
            new Thread(() -> {
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
                }
            }).start();
            lastTime = System.currentTimeMillis();
        }
    }

    public static boolean isClientUser(EntityPlayer ent) {
        return usersOnline.get(ent.getName()) != null;
    }
}