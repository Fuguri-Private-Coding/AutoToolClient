import java.util.Arrays;

import net.minecraft.client.main.Main;

public class Start {
    public static void main(String[] args) {
        Main.main(concat(new String[]{"--version", "АХУЕТЬ БРАТИШКА ТЫ ЕбАНУЛСЯ ИЛИ ЧО НАХУЙ С ВЕСЕНИЙ АБАСТРЕНЬЕ ЕПТА", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
