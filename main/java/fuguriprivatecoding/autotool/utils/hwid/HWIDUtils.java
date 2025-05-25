package fuguriprivatecoding.autotool.utils.hwid;

import java.security.MessageDigest;

public class HWIDUtils {

    public static String generateHWID() {
        try{
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") +  System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
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
}
