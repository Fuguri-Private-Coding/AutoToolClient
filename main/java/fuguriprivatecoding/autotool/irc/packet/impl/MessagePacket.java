package fuguriprivatecoding.autotool.irc.packet.impl;

import fuguriprivatecoding.autotool.irc.packet.Packet;
import fuguriprivatecoding.autotool.profile.Profile;

public class MessagePacket extends Packet {
    private final Profile sender;
    private final String msg;

    public MessagePacket(Profile sender, String msg) {
        this.sender = sender;
        this.msg = msg;
    }

    public Profile getSender() {
        return sender;
    }

    public String getMsg() {
        return msg;
    }
}
