package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.multiplayer.ServerData;

@Getter
@Setter
@AllArgsConstructor
public class ServerJoinEvent extends Event {
    ServerData serverData;
}
