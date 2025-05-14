package me.hackclient.utils.hwid;

import me.hackclient.Client;
import me.hackclient.utils.profile.Profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;

public class HWIDUtils {
    private static final String ID = "e6XXX8TA";
    private static final String Database_URL = "https://pastebin.com/raw/" + ID;

    public static void check() {
        String hwid = generateHWID();
        if (isWhiteList(hwid)) {
            System.out.println("Logged as " + Client.INSTANCE.getProfile() + " (" + hwid + ")");
        } else {
            System.out.println(hwid);
            System.exit(0);
        }
    }

    public static String generateHWID() {
        try{
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") +  System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte[] byteData = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public static boolean isWhiteList(String hwid) {
        try {
            URL url = new URL(Database_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            for (String s : reader.lines().toList()) {
                String[] args = s.split(":");
                if (hwid.equalsIgnoreCase(args[0])) {
                    Client.INSTANCE.setProfile(new Profile(args[1], args[2]));
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
