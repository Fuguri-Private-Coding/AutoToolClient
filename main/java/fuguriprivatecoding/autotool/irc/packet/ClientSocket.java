package fuguriprivatecoding.autotool.irc.packet;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.irc.packet.impl.LoginPacket;
import fuguriprivatecoding.autotool.irc.packet.impl.LoginStatusPacket;
import fuguriprivatecoding.autotool.irc.packet.impl.MessagePacket;
import fuguriprivatecoding.autotool.utils.hwid.HWIDUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
    private static final int PORT = 5050;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientSocket() {
        try {
            // 178.236.243.172
            Socket socket = new Socket("178.236.243.172", PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new LoginPacket(HWIDUtils.generateHWID()));
            out.flush();

            Object packet = null;
            try {
                packet = in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.exit(-1);
            }
            if (packet instanceof LoginStatusPacket loginStatusPacket) {
                if (loginStatusPacket.getProfile() == null) {
                    System.exit(0);
                } else {
                    Client.INST.setProfile(loginStatusPacket.getProfile());
                }
            }

            new Thread(this::listenPacket).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenPacket() {
        try {
            while (true) {
                Object packet = null;
                try {
                    packet = in.readObject();
                } catch (IOException e) {
                    reconnect();
                    e.printStackTrace();
                }
                switch (packet) {
                    case MessagePacket messagePacket -> Client.INST.getConsole().history.add(
                            messagePacket.getSender() + ": " + messagePacket.getMsg()
                    );
                    default -> System.out.println("Получен неизвестный пакет от сервера");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendPacketToServer(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        try {
            Socket socket = new Socket("178.236.243.172", PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new LoginPacket(HWIDUtils.generateHWID()));
            out.flush();

            Object packet = null;
            try {
                packet = in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.exit(-1);
            }
            if (packet instanceof LoginStatusPacket loginStatusPacket) {
                if (loginStatusPacket.getProfile() == null) {
                    System.exit(0);
                } else {
                    Client.INST.setProfile(loginStatusPacket.getProfile());
                }
            }

            new Thread(this::listenPacket).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
