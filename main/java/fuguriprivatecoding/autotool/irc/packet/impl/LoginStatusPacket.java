package fuguriprivatecoding.autotool.irc.packet.impl;

import fuguriprivatecoding.autotool.irc.packet.Packet;
import fuguriprivatecoding.autotool.profile.Profile;

public class LoginStatusPacket extends Packet {
    private final Profile profile;

    public LoginStatusPacket(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}