package fuguriprivatecoding.autotoolrecode.config;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
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

@UtilityClass
public class Configs implements Imports {

    @Getter File configsDirectory = new File(Client.INST.getCLIENT_DIR() + "/configs");
    @Getter private List<Config> configs = new ArrayList<>();
    @Getter Config defaultConfig = new Config("default");

    public void init() {
        if (configsDirectory.mkdirs()) System.out.println("Successful created configsDirectory.");

        refreshConfigs();
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

    private void applyModuleSettings(Module module, JsonObject moduleObject, boolean includeStates) {
        if (module == null || moduleObject == null) return;
        module.setObject(moduleObject, includeStates);
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
                applyModuleSettings(module, moduleObject, false);
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
                Module module = Modules.getModule(entry.getKey());
                if (module != null && module.getCategory() == category) {
                    applyModuleSettings(module, (JsonObject) entry.getValue(), true);
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
        json.add(module.getName(), module.getObject());
        return json;
    }

    private JsonObject createCategoryExportObject(Category category) {
        JsonObject json = new JsonObject();
        for (Module module : Modules.getModulesByCategory(category)) {
            json.add(module.getName(), module.getObject());
        }
        return json;
    }

    public void loadConfig(Config config) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config.getConfigFile()));
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if ("ConfigInformation".equals(entry.getKey())) continue;

                Module module = Modules.getModule(entry.getKey());
                if (module != null) {
                    applyModuleSettings(module, (JsonObject) entry.getValue(), true);
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

        for (Module module : Modules.getModules()) {
            JsonObject moduleObject = module.getObject();
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
        if (configFile == null) return null;
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

    public void saveAsync(Config config) {
        new Thread(() -> saveConfig(config)).start();
    }
}
