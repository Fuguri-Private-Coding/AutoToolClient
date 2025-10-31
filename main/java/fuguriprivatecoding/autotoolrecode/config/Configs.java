package fuguriprivatecoding.autotoolrecode.config;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
public class Configs implements Imports {

    File configsDirectory, bindsDirectory;
    File bindFile, loadModulesFile, hotTextFile, accountFile;
    private List<Config> configs;
    Config defaultConfig;

    public void init() {
        configs = new ArrayList<>();
        configsDirectory = new File(Client.INST.getName() + "/configs");
        bindsDirectory = new File(Client.INST.getName() + "/binds");
        defaultConfig = new Config("default");
        bindFile = new File(bindsDirectory, "binds.json");

        try {
            FileUtils.createDirectoriesIfNotExists(configsDirectory, bindsDirectory);
        } catch (IOException e) {
            System.out.println("Failed create directories.");
        }

        refreshConfigs();
    }

    ClientSettings clientSettings = Client.INST.getModules().getModule(ClientSettings.class);

    public void loadBinds() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(bindFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Module module = Client.INST.getModules().getModule(entry.getKey());
                if (module == null) {
                    continue;
                }
                JsonObject moduleObject = (JsonObject) entry.getValue();
                module.setKey(moduleObject.get("key").getAsInt());

            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void saveBinds() {
        FileUtils.createIfNotExists(bindFile);
        JsonObject mainObject = new JsonObject();
        for (Module module : Client.INST.getModules().getModules()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("key", module.getKey());
            mainObject.add(module.getName(), moduleObject);
        }
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(bindFile));
            Gson prettyGson = new GsonBuilder().create();
            writer.println(prettyGson.toJson(mainObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void logError(String message) {
        ClientUtils.chatLog(message);
    }

    private void logSuccess(String message) {
        ClientUtils.chatLog("Successful " + message);
    }

    private Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    private String getClipboardText() throws UnsupportedFlavorException, IOException {
        Clipboard clipboard = getClipboard();
        return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
            ? (String) clipboard.getData(DataFlavor.stringFlavor)
            : null;
    }

    private JsonObject parseJson(String text) {
        if (text == null) return null;

        try {
            return new Gson().fromJson(text, JsonObject.class);
        } catch (JsonSyntaxException | ClassCastException e) {
            logError("Uncorrected JSON in Clipboard");
            return null;
        }
    }

    private void applyModuleSettings(Module module, JsonObject moduleObject) {
        if (module == null || moduleObject == null) return;

        if (moduleObject.has("toggled")) {
            module.setToggled(moduleObject.get("toggled").getAsBoolean());
        }

        if (moduleObject.has("hide")) {
            module.setHide(moduleObject.get("hide").getAsBoolean());
        }

        for (Setting setting : module.getSettings()) {
            JsonObject settingObject = moduleObject.getAsJsonObject(setting.getName());
            if (settingObject != null) {
                setting.setObject(settingObject);
            }
        }
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        getClipboard().setContents(selection, null);
    }

    public void importSettings(Module module) {
        try {
            String clipboardText = getClipboardText();
            if (clipboardText == null) {
                logError("Clipboard is empty or unavailable");
                return;
            }

            JsonObject json = parseJson(clipboardText);
            if (json == null || module == null) return;

            JsonObject moduleObject = json.getAsJsonObject(module.getName());
            if (moduleObject != null) {
                applyModuleSettings(module, moduleObject);
                logSuccess("Imported settings to " + module.getName());
            }

        } catch (UnsupportedFlavorException | IOException e) {
            logError("Failed to read settings from Clipboard");
        }
    }

    public void importSettings(Category category) {
        try {
            String clipboardText = getClipboardText();
            if (clipboardText == null) {
                logError("Clipboard is empty or unavailable");
                return;
            }

            JsonObject json = parseJson(clipboardText);
            if (json == null) return;

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Module module = Client.INST.getModules().getModule(entry.getKey());
                if (module != null && module.getCategory() == category) {
                    applyModuleSettings(module, (JsonObject) entry.getValue());
                }
            }

            logSuccess("Imported settings to " + category.name);

        } catch (UnsupportedFlavorException | IOException e) {
            logError("Failed to read settings from Clipboard");
        }
    }

    public void exportSettings(Module module) {
        if (module == null) return;

        JsonObject json = createModuleExportObject(module);
        String textToCopy = new GsonBuilder().create().toJson(json);

        copyToClipboard(textToCopy);
        logSuccess("Exported settings from " + module.getName());
    }

    public void exportSettings(Category category) {
        JsonObject json = createCategoryExportObject(category);
        String textToCopy = new GsonBuilder().create().toJson(json);

        copyToClipboard(textToCopy);
        logSuccess("Exported settings from " + category.name);
    }

    private JsonObject createModuleExportObject(Module module) {
        JsonObject json = new JsonObject();
        json.add(module.getName(), createModuleObject(module, false));
        return json;
    }

    private JsonObject createCategoryExportObject(Category category) {
        JsonObject json = new JsonObject();
        for (Module module : Client.INST.getModules().getModulesByCategory(category)) {
            json.add(module.getName(), createModuleObject(module, true));
        }
        return json;
    }

    private JsonObject createModuleObject(Module module, boolean includeState) {
        JsonObject moduleObject = new JsonObject();

        if (includeState) {
            moduleObject.addProperty("toggled", module.isToggled());
        }

        for (Setting setting : module.getSettings()) {
            moduleObject.add(setting.getName(), setting.getObject());
        }

        return moduleObject;
    }

    public void loadConfig(Config config) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config.getConfigFile()));
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if ("ConfigInformation".equals(entry.getKey())) continue;

                Module module = Client.INST.getModules().getModule(entry.getKey());
                if (module != null) {
                    applyModuleSettings(module, (JsonObject) entry.getValue());
                }
            }

            saveConfig(defaultConfig);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void saveConfig(Config config) {
        FileUtils.createIfNotExists(config.getConfigFile());
        config.onUpdate();

        JsonObject mainObject = new JsonObject();

        JsonObject infoObject = new JsonObject();
        infoObject.addProperty("Name", config.getName());
        infoObject.addProperty("LastUpdate", config.getLastUpdateDate());
        mainObject.add("ConfigInformation", infoObject);

        for (Module module : Client.INST.getModules().getModules()) {
            JsonObject moduleObject = createModuleObject(module, true);
            moduleObject.addProperty("hide", module.isHide());
            mainObject.add(module.getName(), moduleObject);
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(config.getConfigFile()));
            String json = new GsonBuilder().create().toJson(mainObject);
            writer.println(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void deleteConfig(Config config) {
        configs.remove(config);
        config.getConfigFile().delete();
    }

    public void refreshConfigs() {
        File[] files = configsDirectory.listFiles();
        if (files != null) {
            configs.clear();
            for (File file : files) {
                Config config = loadConfigFromFile(file);
                if (config != null) {
                    configs.add(config);
                }
            }
        }
    }

    private Config loadConfigFromFile(File configFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            JsonObject elementObject = json.get("ConfigInformation").getAsJsonObject();
            String name = elementObject.get("Name").getAsString();
            Date lastUpdate = Config.DATE_FORMAT.parse(elementObject.get("LastUpdate").getAsString());
            return new Config(name, lastUpdate);
        } catch (IOException | ParseException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    public void loadAsync(Config config) {
        new Thread(() -> loadConfig(config)).start();
    }

    public void saveAsync(Config config) {
        new Thread(() -> saveConfig(config)).start();
    }
}
