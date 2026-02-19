package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

// created by dicves_recode on 18.02.2026
@UtilityClass
public class Fonts {
    private final File FONTS_DIR = new File(Client.INST.CLIENT_DIR, "test-fonts");
    private final Map<String, MsdfFont> FONTS = new HashMap<>();

    public void init() {
        FONTS.clear();

        if (FONTS_DIR.mkdir()) {
            ClientUtils.chatLog("при загрузке шрифтов папки \"" + FONTS_DIR.getName() + "\" не существовало.");
            ClientUtils.chatLog("загрузка отменена.");
            return;
        }

        File[] files = FONTS_DIR.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            String name = file.getName();

            System.out.println(name);
            FONTS.put(name, MsdfFont.create(name));
        }
    }

    public MsdfFont get(String name) {
        return FONTS.get(name);
    }
}
