package fuguriprivatecoding.autotoolrecode.config;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.Getter;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Config {
    private static final String CONFIG_FORMAT = ".json";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Getter private final String name;
    @Getter private final File file;
    private Date lastUpdateDate;

    private final JsonParser PARSER = new JsonParser();
    private final Gson GSON = new GsonBuilder().create();

    public Config(String name) {
       this.name = name;
       file = new File(Configs.CONFIG_DIRECTORY, name + CONFIG_FORMAT);
       lastUpdateDate = new Date();
       FileUtils.createIfNotExists(file);
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

    public void load() {
        try (Reader reader = new FileReader(getFile())) {
            JsonObject json = PARSER.parse(reader).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if ("ConfigInformation".equals(entry.getKey())) continue;

                Module module = Modules.getInstance().getModule(entry.getKey());
                JsonObject moduleObject = entry.getValue().getAsJsonObject();

                if (module != null && moduleObject != null) {
                    module.setObject(moduleObject, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void save() {
        FileUtils.createIfNotExists(getFile());
        onUpdate();

        JsonObject mainObject = new JsonObject();

        JsonObject infoObject = new JsonObject();
        infoObject.addProperty("Name", name);
        infoObject.addProperty("LastUpdate", getLastUpdateDate());
        mainObject.add("ConfigInformation", infoObject);

        for (Module module : Modules.getInstance().getModules()) {
            JsonObject moduleObject = module.getObject();
            mainObject.add(module.getName(), moduleObject);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(mainObject, writer);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean delete() {
        return getFile().delete();
    }
}
