package fuguriprivatecoding.autotoolrecode.irc.packet.impl;

import fuguriprivatecoding.autotoolrecode.irc.packet.Packet;

public class LoginPacket extends Packet {
    private final String hwid;

    public LoginPacket(String hwid) {
        this.hwid = hwid;
    }

    public String getHwid() {
        return hwid;
    }
}
