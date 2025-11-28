package fuguriprivatecoding.autotoolrecode.config;

import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.Setter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Config {
    private static final String CONFIG_FORMAT = ".json";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Getter @Setter
    private String name;
    @Getter private final File configFile;
    private Date lastUpdateDate;

    public Config(String name) {
       this.name = name;
       configFile = new File(Configs.CONFIG_DIRECTORY, name + CONFIG_FORMAT);
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

    public void onUpdate() {
        lastUpdateDate = new Date();
    }
}
