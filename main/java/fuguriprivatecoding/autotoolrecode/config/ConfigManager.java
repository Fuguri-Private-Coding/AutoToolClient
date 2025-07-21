package fuguriprivatecoding.autotoolrecode.config;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.alt.Account;
import fuguriprivatecoding.autotoolrecode.hottext.HotText;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
public class ConfigManager implements Imports {

    File configsDirectory;
    File bindsDirectory;
    File accountDirectory;
    File hotTextDirectory;
    private List<Config> configs;
    Config defaultConfig;
    File bindFile;
    File accountFile;
    File hotTextFile;

    public void init() {
        configs = new ArrayList<>();
        configsDirectory = new File(Client.INST.getName() + "/configs");
        bindsDirectory = new File(Client.INST.getName() + "/binds");
        accountDirectory = new File(Client.INST.getName() + "/account");
        hotTextDirectory = new File(Client.INST.getName() + "/hotkeys");
        bindsDirectory.mkdirs();
        configsDirectory.mkdirs();
        accountDirectory.mkdirs();
        hotTextDirectory.mkdirs();
        defaultConfig = new Config("default");
        bindFile = new File(bindsDirectory, "binds.json");
        accountFile = new File(accountDirectory, "accounts.json");
        hotTextFile = new File(hotTextDirectory, "hotkeys.json");
        refreshConfigs();
    }

    ClientSettings clientSettings = Client.INST.getModuleManager().getModule(ClientSettings.class);

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

    public void loadBinds() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(bindFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Module module = Client.INST.getModuleManager().getModule(entry.getKey());
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
        for (Module module : Client.INST.getModuleManager().getModules()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("key", module.getKey());
            mainObject.add(module.getName(), moduleObject);
        }
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(bindFile));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            writer.println(prettyGson.toJson(mainObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void loadAccounts() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(accountFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            Client.INST.getAltManagerGui().accounts.clear();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Account account = getAccount(entry);

                Client.INST.getAltManagerGui().accounts.add(account);
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static Account getAccount(Map.Entry<String, JsonElement> entry) {
        JsonObject moduleObject = (JsonObject) entry.getValue();
        Account account;
        if (moduleObject.get("uuid") != null && moduleObject.get("token") != null) {
            account = new Account(moduleObject.get("name").getAsString(), moduleObject.get("token").getAsString(), moduleObject.get("uuid").getAsString());
        } else {
            account = new Account(moduleObject.get("name").getAsString());
        }
        return account;
    }

    public void loadHotKeys() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(hotTextFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            Client.INST.getHotTextGui().hotKeys.clear();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                HotText hotText = getHotKey(entry);

                Client.INST.getHotTextGui().hotKeys.add(hotText);
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static HotText getHotKey(Map.Entry<String, JsonElement> entry) {
        JsonObject moduleObject = (JsonObject) entry.getValue();
        return new HotText(moduleObject.get("key").getAsInt(),moduleObject.get("text").getAsString(),0);
    }

    public void saveAccounts() {
        FileUtils.createIfNotExists(accountFile);
        JsonObject mainObject = getAccountObject();
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(accountFile));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            writer.println(prettyGson.toJson(mainObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static JsonObject getAccountObject() {
        JsonObject mainObject = new JsonObject();
        for (Account account : Client.INST.getAltManagerGui().accounts) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("name", account.getName());
            if (account.getUuid() != null && account.getRefreshToken() != null) {
                moduleObject.addProperty("uuid", account.getUuid());
                moduleObject.addProperty("token", account.getRefreshToken());
            }
            mainObject.add(account.getName(), moduleObject);
        }
        return mainObject;
    }

    public void saveHotKeys() {
        FileUtils.createIfNotExists(hotTextFile);
        JsonObject mainObject = getHotKeyObject();
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(hotTextFile));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            writer.println(prettyGson.toJson(mainObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static JsonObject getHotKeyObject() {
        JsonObject mainObject = new JsonObject();
        for (HotText hotText : Client.INST.getHotTextGui().hotKeys) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("text", hotText.getText());
            moduleObject.addProperty("key", hotText.getKey());
            mainObject.add(hotText.getText(), moduleObject);
        }
        return mainObject;
    }

    public void loadConfig(Config config) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config.getConfigFile()));
            System.out.println(config.getConfigFile().getAbsolutePath());
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("ConfigInformation")) { // В загрузке конфига нужно скипать значения с его названием и датой для избежания багов так как при загрузке нам нужны только модули
                    continue;
                }
                Module module = Client.INST.getModuleManager().getModule(entry.getKey());
//                if (clientSettings.notLoadCategoryFromConfig.get("Combat")) {
//                    List<Module> notToLoadModules = Client.INST.getModuleManager().getModulesByCategory(Category.COMBAT);
//                }

                if (module == null) {
                    continue;
                }
                JsonObject moduleObject = (JsonObject) entry.getValue();
                module.setToggled(moduleObject.get("toggled").getAsBoolean());
                if (moduleObject.get("hide") != null) module.setHide(moduleObject.get("hide").getAsBoolean());
                for (Setting setting : module.getSettings()) {
                    JsonElement settingElement = moduleObject.get(setting.getName());
                    if (settingElement == null) {
                        continue;
                    }
                    switch (setting) {
                        case IntegerSetting set -> set.setValue(settingElement.getAsInt());
                        case FloatSetting set -> set.setValue(settingElement.getAsFloat());
                        case CheckBox set -> set.setToggled(settingElement.getAsBoolean());
                        case KeyBind set -> set.setKey(settingElement.getAsInt());
                        case Mode set -> set.setMode(settingElement.getAsString());
                        case MultiMode set -> {
                            JsonObject jsonObject = settingElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry1 : jsonObject.entrySet()) {
                                set.set(entry1.getKey(), entry1.getValue().getAsBoolean());
                            }
                        }
                        case ColorSetting set -> {
                            JsonObject jsonObject = settingElement.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> entry1 : jsonObject.entrySet()) {
                                switch (entry1.getKey()) {
                                    case "Red" -> set.setRed(entry1.getValue().getAsFloat());
                                    case "Green" -> set.setGreen(entry1.getValue().getAsFloat());
                                    case "Blue" -> set.setBlue(entry1.getValue().getAsFloat());
                                    case "Alpha" -> set.setAlpha(entry1.getValue().getAsFloat());
                                }
                            }
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + setting);
                    }
                }
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace(System.out);
        }
        saveConfig(defaultConfig);
    }

    public void saveConfig(Config config) {
        FileUtils.createIfNotExists(config.getConfigFile());
        config.onUpdate();
        JsonObject mainObject = new JsonObject();
        JsonObject infoObject = new JsonObject();
        infoObject.addProperty("Name", config.getName());
        infoObject.addProperty("LastUpdate", config.getLastUpdateDate());
        mainObject.add("ConfigInformation", infoObject);
        for (Module module : Client.INST.getModuleManager().getModules()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("toggled", module.isToggled());
            moduleObject.addProperty("hide", module.isHide());
            for (Setting setting : module.getSettings()) {
                switch (setting) {
                    case IntegerSetting set -> moduleObject.addProperty(setting.getName(), set.getValue());
                    case FloatSetting set -> moduleObject.addProperty(setting.getName(), set.getValue());
                    case CheckBox set -> moduleObject.addProperty(setting.getName(), set.isToggled());
                    case KeyBind set -> moduleObject.addProperty(setting.getName(), set.getKey());
                    case Mode set -> moduleObject.addProperty(setting.getName(), set.getMode());
                    case MultiMode set -> {
                        JsonObject multiBooleanObject = new JsonObject();
                        for (Doubles<String, Boolean> value : set.getValues()) {
                            multiBooleanObject.addProperty(value.getFirst(), value.getSecond());
                        }
                        moduleObject.add(set.getName(), multiBooleanObject);
                    }
                    case ColorSetting set -> {
                        JsonObject colorSettingObject = new JsonObject();
                        colorSettingObject.addProperty("Red", set.getRed());
                        colorSettingObject.addProperty("Green", set.getGreen());
                        colorSettingObject.addProperty("Blue", set.getBlue());
                        colorSettingObject.addProperty("Alpha", set.getAlpha());
                        moduleObject.add(set.getName(), colorSettingObject);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + setting);
                }
            }
            mainObject.add(module.getName(), moduleObject);
        }
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(config.getConfigFile()));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            writer.println(prettyGson.toJson(mainObject));
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

    public void loadAsync(Config config) {
        new Thread(() -> loadConfig(config)).start();
    }

    public void saveAsync(Config config) {
        new Thread(() -> saveConfig(config)).start();
    }
}
