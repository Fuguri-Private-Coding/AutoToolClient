package me.hackclient.utils.hwid;

import me.hackclient.Client;
import me.hackclient.utils.profile.Profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HWIDUtils {
    private static final String PASTEBIN_ID = "32VeuV38";
    private static final String Database_URL = "https://pastebin.com/raw/" + PASTEBIN_ID;

    public static void check() {
        String hwid = generateHWID();
        if (IsWhiteList(hwid)) {
            System.out.println("Logged as " + Client.INSTANCE.getProfile() + " (" + hwid + ")");
        } else {
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

    public static boolean IsWhiteList(String hwid) {
        try {
            URL url = new URL(Database_URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.trim().split(":");
                String hwidLine = args[0];
                String nameLine = args[1];
                String roleLine = args[2];
                if (hwidLine.equals(hwid)) {
                    reader.close();
                    Client.INSTANCE.setProfile(new Profile(nameLine, roleLine));
                    return true;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
