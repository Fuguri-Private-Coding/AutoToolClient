package fuguriprivatecoding.autotoolrecode.irc.packet.impl;

import fuguriprivatecoding.autotoolrecode.irc.packet.Packet;

public class MyNickNamePacket extends Packet {
    private final String nickname;

    public MyNickNamePacket(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
