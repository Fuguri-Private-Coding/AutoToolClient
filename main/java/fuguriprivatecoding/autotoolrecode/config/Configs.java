package fuguriprivatecoding.autotoolrecode.config;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
public class Configs implements Imports {

    @Getter final File CONFIG_DIRECTORY = new File(Client.getInstance().CLIENT_DIR + "/configs");
    @Getter private final List<Config> configs = new CopyOnWriteArrayList<>();
    @Getter Config defaultConfig = new Config("default");
    @Getter @Setter Config lastLoadedConfig = new Config("default");

    private final Gson GSON = new Gson();
    private final GsonBuilder GSON_BUILDER = new GsonBuilder();
    private final JsonParser JSON_PARSER = new JsonParser();

    public void init() {
        if (CONFIG_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию для конфигов.");

        refreshConfigs();
    }

    private void logError(String message) {
        ClientUtils.chatLog(message);
    }

    private void logSuccess(String message) {
        ClientUtils.chatLog("Successful " + message);
    }

    private JsonObject parseJson(String text) {
        if (text == null) return null;

        try {
            return GSON.fromJson(text, JsonObject.class);
        } catch (JsonSyntaxException | ClassCastException e) {
            logError("Uncorrected JSON in Clipboard");
            return null;
        }
    }

    public Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    private String getClipboardText() throws UnsupportedFlavorException, IOException {
        Clipboard clipboard = getClipboard();
        return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
            ? (String) clipboard.getData(DataFlavor.stringFlavor)
            : null;
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
                module.setObject(moduleObject, true);
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
                Module module = Modules.getInstance().getModule(entry.getKey());
                if (module != null && module.getCategory() == category) {
                    module.setObject((JsonObject) entry.getValue(), true);
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
        String textToCopy = GSON_BUILDER.create().toJson(json);

        copyToClipboard(textToCopy);
        logSuccess("Exported settings from " + module.getName());
    }

    public void exportSettings(Category category) {
        JsonObject json = createCategoryExportObject(category);
        String textToCopy = GSON_BUILDER.create().toJson(json);

        copyToClipboard(textToCopy);
        logSuccess("Exported settings from " + category.name);
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Configs.getClipboard().setContents(selection, null);
    }

    private JsonObject createModuleExportObject(Module module) {
        JsonObject json = new JsonObject();
        json.add(module.getName(), module.getObject());
        return json;
    }

    private JsonObject createCategoryExportObject(Category category) {
        JsonObject json = new JsonObject();
        for (Module module : Modules.getInstance().getModulesByCategory(category)) {
            json.add(module.getName(), module.getObject());
        }
        return json;
    }

    public void refreshConfigs() {
        configs.clear();
        for (File file : Objects.requireNonNull(CONFIG_DIRECTORY.listFiles())) {
            if (file == null)
                continue;

            Config config = getConfigFromFile(file);
            if (config != null) {
                configs.add(config);
            }
        }
    }

    private Config getConfigFromFile(File configFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            JsonObject json = JSON_PARSER.parse(reader).getAsJsonObject();
            JsonObject config = json.getAsJsonObject("ConfigInformation");

            return new Config(
                config.get("Name").getAsString(),
                Config.DATE_FORMAT.parse(config.get("LastUpdate").getAsString())
            );
        } catch (IOException | ParseException e) {
            return null;
        }
    }
}
