package fuguriprivatecoding.autotoolrecode.irc.packet;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.irc.packet.impl.LoginPacket;
import fuguriprivatecoding.autotoolrecode.irc.packet.impl.LoginStatusPacket;
import fuguriprivatecoding.autotoolrecode.irc.packet.impl.MessagePacket;
import fuguriprivatecoding.autotoolrecode.irc.packet.impl.UsersListPacket;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.utils.hwid.HWIDUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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

            Object packet = in.readObject();
            if (packet instanceof LoginStatusPacket loginStatusPacket) {
                if (loginStatusPacket.getProfile() == null) {
                    System.exit(0);
                } else {
                    Client.INST.setProfile(loginStatusPacket.getProfile());
                }
            }

            new Thread(this::listenPacket).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Profile> users = new HashMap<>();

    private void listenPacket() {
        try {
            while (true) {
                Object packet = in.readObject();
                switch (packet) {
                    case MessagePacket messagePacket -> Client.INST.getConsole().history.add(
                            messagePacket.getSender() + ": " + messagePacket.getMsg()
                    );
                    case UsersListPacket usersListPacket -> users = usersListPacket.getUsers();
                    default -> System.out.println("Получен неизвестный пакет от сервера");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("Trying to reconnect...");
//            try {
//                // 178.236.243.172
//                Socket socket = new Socket("localhost", PORT);
//                out = new ObjectOutputStream(socket.getOutputStream());
//                in = new ObjectInputStream(socket.getInputStream());
//
//                out.writeObject(new LoginPacket(HWIDUtils.generateHWID()));
//
//                Object packet = in.readObject();
//                if (packet instanceof LoginStatusPacket loginStatusPacket) {
//                    if (loginStatusPacket.getProfile() == null) {
//                        System.exit(0);
//                    } else {
//                        Client.INST.setProfile(loginStatusPacket.getProfile());
//                    }
//                }
//
//                new Thread(this::listenPacket).start();
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
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

    private void connect() {

    }
}
