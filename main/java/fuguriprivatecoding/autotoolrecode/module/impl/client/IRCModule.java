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

@ModuleInfo(name = "IRC", category = Category.CLIENT)
public class IRCModule extends Module {

    public static HashMap<String, Profile> usersOnline = new HashMap<>();

    private long lastTime;

    @Override
    public void onDisable() {
        super.onDisable();
        if (!usersOnline.isEmpty()) usersOnline.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent && System.currentTimeMillis() - lastTime >= 500) {
            new Thread(() -> {
                for (Message message : Client.INST.getIrc().getServerChannel().getIterableHistory().stream().toList()) {
                    String msg = message.getContentRaw();
                    String[] args = msg.split(" ");

                    String ign = args[0];
                    String clientName = args[1].replace("[", "").replace("]", "");
                    String role = args[2].replace("[", "").replace("]", "");

                    if (usersOnline.containsKey(ign) && usersOnline.containsValue(new Profile(clientName, role))) continue;

                    usersOnline.put(ign, new Profile(clientName, role));
                }
            }).start();
            lastTime = System.currentTimeMillis();
        }
    }
}