package fuguriprivatecoding.autotoolrecode.irc.packet.impl;

import fuguriprivatecoding.autotoolrecode.irc.packet.Packet;
import fuguriprivatecoding.autotoolrecode.profile.Profile;

import java.util.Map;

public class UsersListPacket extends Packet {
    private final Map<String, Profile> users;

    public UsersListPacket(Map<String, Profile> users) {
        this.users = users;
    }

    public Map<String, Profile> getUsers() {
        return users;
    }
}
