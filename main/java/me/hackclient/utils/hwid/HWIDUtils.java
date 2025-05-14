package me.hackclient.utils.hwid;

import me.hackclient.Client;
import me.hackclient.utils.profile.Profile;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;

public class HWIDUtils {
    private static final String ID = "c29262c9360e3680bc02b89831c6b359/raw/4f9d550d4362e0b508f2fa5b229671221dd5bacd/nkvaernsdlw";
    private static final String Database_URL = "https://gist.githubusercontent.com/Deathlksr/" + ID;

    public static void check() {
        String hwid = generateHWID();
        if (isWhiteList(hwid)) {
            System.out.println("Logged as " + Client.INSTANCE.getProfile() + " (" + hwid + ")");
            Client.INSTANCE.getIrc().sendMessage(Client.INSTANCE.getIrc().getHwidChannel(),
                    "[" + HWIDUtils.generateHWID() + "] " + System.getProperty("user.name") + " Successful connect. " + Client.INSTANCE.getProfile()
            );
        } else {
            Client.INSTANCE.getIrc().sendMessage(Client.INSTANCE.getIrc().getHwidChannel(),
                    "[" + HWIDUtils.generateHWID() + "] " + System.getProperty("user.name") + " This user does not have access to the client."
            );
            System.out.println(hwid);
            System.exit(-1);
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
