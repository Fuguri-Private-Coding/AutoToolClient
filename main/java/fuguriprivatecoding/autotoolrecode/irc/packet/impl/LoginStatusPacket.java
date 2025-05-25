package fuguriprivatecoding.autotoolrecode.irc.packet.impl;

import fuguriprivatecoding.autotoolrecode.irc.packet.Packet;
import fuguriprivatecoding.autotoolrecode.profile.Profile;

public class LoginStatusPacket extends Packet {
    private final Profile profile;

    public LoginStatusPacket(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}