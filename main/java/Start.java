import java.util.Arrays;

import me.hackclient.utils.hwid.HWIDUtils;
import net.minecraft.client.main.Main;

public class Start {
    public static void main(String[] args) {
        System.out.println(HWIDUtils.generateHWID());
        HWIDUtils.check();
        Main.main(concat(new String[]{"--version", "AutoTool", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
