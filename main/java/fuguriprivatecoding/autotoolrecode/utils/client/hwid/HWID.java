package fuguriprivatecoding.autotoolrecode.utils.client.hwid;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;

import java.security.MessageDigest;

public class HWID {

    public static long lastTimeConnection;
    public static boolean noConnection;
    static boolean notUser;

    public static void check() {
        if (!authenticateUser(generateHWID())) System.exit(-1);
    }

    public static String generateHWID() {
        try {
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuilder hexString = new StringBuilder();

            byte[] byteData = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }

    public static boolean authenticateUser(String hwid) {
        try {
            MessageChannel keyChannel = ClientIRC.getKeyChannel();
            MessagePaginationAction history = keyChannel.getIterableHistory();

            for (Message message : history.stream().toList()) {
                String rawContent = message.getContentRaw();
                String[] args = rawContent.split(":");

                if (args.length >= 3 && hwid.equalsIgnoreCase(args[0])) {
                    String username = args[1];
                    Role userRole = Role.fromRoleName(args[2]);

                    Client.INST.setProfile(new Profile(username, userRole));
                    noConnection = false;
                    return true;
                }
            }

            String userName = System.getProperty("user.name");
            String denialMessage = "[" + hwid + "] " + userName + " This user does not have access to the client.";

            notUser = true;

            ClientIRC.sendMessage(ClientIRC.getLoginChannel(), denialMessage);
        } catch (Exception e) {
            if (!noConnection) lastTimeConnection = System.currentTimeMillis();
            noConnection = true;
        }
        return System.currentTimeMillis() - lastTimeConnection < 30000 || !noConnection || !notUser;
    }
}
