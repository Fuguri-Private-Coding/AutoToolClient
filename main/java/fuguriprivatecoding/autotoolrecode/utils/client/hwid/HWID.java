package fuguriprivatecoding.autotoolrecode.utils.client.hwid;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;

import java.security.MessageDigest;

public class HWID {

    public static boolean isConnectionLost;
    static boolean isValidUser;

    public static StopWatch connectionTimer = new StopWatch();

    public static EasingAnimation noConnectionAnim = new EasingAnimation(0);

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
                    isValidUser = true;
                    isConnectionLost = false;
                    noConnectionAnim.setEnd(false);
                    return true;
                }
            }

            isValidUser = false;

            String userName = System.getProperty("user.name");
            String denialMessage = "[" + hwid + "] " + userName + " This user does not have access to the client.";

            ClientIRC.sendMessage(ClientIRC.getLoginChannel(), denialMessage);
            return isValidUser;
        } catch (Exception e) {
            if (!isConnectionLost) connectionTimer.reset();
            isConnectionLost = true;
            noConnectionAnim.setEnd(true);
        }

        return !connectionTimer.reachedMS(30000) || !isConnectionLost;
    }
}
