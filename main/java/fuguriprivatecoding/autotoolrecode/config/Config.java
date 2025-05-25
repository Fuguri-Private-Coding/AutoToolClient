package fuguriprivatecoding.autotoolrecode.config;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {
    private static final String CONFIG_FORMAT = ".json";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Getter private String name;
    @Getter private final File configFile;
    private Date lastUpdateDate;

    public Config(String name) {
       this.name = name;
       configFile = new File(Client.INST.getConfigManager().getConfigsDirectory(), name + CONFIG_FORMAT);
       lastUpdateDate = new Date();
       FileUtils.createIfNotExists(configFile);
    }

    public Config(String name, Date date) {
        this(name);
        lastUpdateDate = date;
    }

    public String getLastUpdateDate() {
        return DATE_FORMAT.format(lastUpdateDate);
    }

    public void rename(String name) {
        if (this.name.equalsIgnoreCase(name)) {
            ClientUtils.chatLog("Нельзя переименовать конфигурацию в такое-же название как и до этого!");
            return;
        }
        this.name = name;
        if (!configFile.renameTo(new File(name))) {
            ClientUtils.chatLog("Не удалось переименовать конфигурацию");
        }
    }

    public void onUpdate() {
        lastUpdateDate = new Date();
    }
}
