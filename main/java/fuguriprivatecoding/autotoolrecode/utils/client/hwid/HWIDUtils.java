package fuguriprivatecoding.autotoolrecode.utils.client.hwid;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.profile.Profile;
import fuguriprivatecoding.autotoolrecode.profile.Role;
import net.dv8tion.jda.api.entities.Message;

import java.security.MessageDigest;

public class HWIDUtils {

    public static void check() {
        String hwid = generateHWID();
        if (!isWhiteList(hwid)) System.exit(-1);
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

    public static boolean isWhiteList(String hwid) {
        for (Message message : Client.INST.getIrc().getKeyChannel().getIterableHistory().stream().toList()) {
            String[] args = message.getContentRaw().split(":");
            if (hwid.equalsIgnoreCase(args[0])) {
                Client.INST.setProfile(new Profile(args[1], Role.fromRoleName(args[2])));
                return true;
            }
        }
        Client.INST.getIrc().sendMessage(Client.INST.getIrc().getLoginChannel(),
                "[" + HWIDUtils.generateHWID() + "] " + System.getProperty("user.name") + " This user does not have access to the client."
        );
        return false;
    }
}
