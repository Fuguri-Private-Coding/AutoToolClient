package me.hackclient.cfg;

import com.google.gson.*;
import me.hackclient.Client;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.interfaces.InstanceAccess;

import java.io.*;
import java.util.Map;

public class ConfigManager implements InstanceAccess {

    public void saveBinds(File file) throws IOException {
        File directory = Client.INSTANCE.getClientDirectory();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!Client.INSTANCE.getBindsDirectory().exists()) {
            Client.INSTANCE.getBindsDirectory().createNewFile();
        }

        try {
            JsonObject json = new JsonObject();
            Client.INSTANCE.setBindsDirectory(file);

            for (Module module : Client.INSTANCE.getModuleManager().modules) {
                JsonObject jsonModule = new JsonObject();
                jsonModule.addProperty("key", module.getKey());
                json.add(module.getName(), jsonModule);
            }

            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            printWriter.println(prettyGson.toJson(json));
            printWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadBinds(File file) throws IOException {
        File directory = Client.INSTANCE.getClientDirectory();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!file.exists()) {
            saveBinds(file);
            return;
        }

        BufferedReader load = new BufferedReader(new FileReader(file));
        JsonParser jsonParser = new JsonParser();
        JsonObject json = (JsonObject) jsonParser.parse(load);
        Client.INSTANCE.setBindsDirectory(file);
        load.close();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Module loadingModule = Client.INSTANCE.getModuleManager().getModule(entry.getKey());
            if (loadingModule == null) {
                continue;
            }

            JsonObject moduleObject = (JsonObject) entry.getValue();

            JsonElement settingObject = moduleObject.get("key");
            loadingModule.setKey(settingObject.getAsInt());
        }
    }

    public void save(File file) throws IOException {
        File directory = Client.INSTANCE.getClientDirectory();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!Client.INSTANCE.getDefaultConfig().exists()) {
            Client.INSTANCE.getDefaultConfig().createNewFile();
        }

        try {
            JsonObject json = new JsonObject();
            Client.INSTANCE.setDefaultConfig(file);

            for (Module module : Client.INSTANCE.getModuleManager().modules) {
                JsonObject jsonModule = getJsonObject(module);
                json.add(module.getName(), jsonModule);
            }

            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            printWriter.println(prettyGson.toJson(json));
            printWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject getJsonObject(Module module) {
        JsonObject jsonModule = new JsonObject();
        jsonModule.addProperty("toggled", module.isToggled());
        for (Setting setting : module.getSettings()) {
            if (setting instanceof IntegerSetting integerSetting) {
                jsonModule.addProperty(setting.getName(), integerSetting.getValue());
            }
            if (setting instanceof FloatSetting floatSetting) {
                jsonModule.addProperty(setting.getName(), floatSetting.getValue());
            }
            if (setting instanceof BooleanSetting booleanSetting) {
                jsonModule.addProperty(setting.getName(), booleanSetting.isToggled());
            }
            if (setting instanceof ModeSetting modeSetting) {
                jsonModule.addProperty(setting.getName(), modeSetting.getMode());
            }
            if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
                JsonObject test = new JsonObject();
                for (Doubles<String, Boolean> value : multiBooleanSetting.getValues()) {
                    test.addProperty(value.getFirst(), value.getSecond());
                }
                jsonModule.add(multiBooleanSetting.getName(), test);
            }
        }
        return jsonModule;
    }

    public void load(File file) throws IOException {
        File directory = Client.INSTANCE.getClientDirectory();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!file.exists()) {
            save(file);
            return;
        }

        BufferedReader load = new BufferedReader(new FileReader(file));
        JsonParser jsonParser = new JsonParser();
        JsonObject json = (JsonObject) jsonParser.parse(load);
        Client.INSTANCE.setDefaultConfig(file);
        load.close();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Module loadingModule = Client.INSTANCE.getModuleManager().getModule(entry.getKey());
            if (loadingModule == null) {
                continue;
            }

            JsonObject moduleObject = (JsonObject) entry.getValue();

            loadingModule.setToggled(moduleObject.get("toggled").getAsBoolean());
            for (Setting setting : loadingModule.getSettings()) {
                JsonElement settingObject = moduleObject.get(setting.getName());
                if (settingObject == null) {
                    continue;
                }

                if (setting instanceof IntegerSetting integerSetting) {
                    integerSetting.setValue(settingObject.getAsInt());
                }
                if (setting instanceof FloatSetting floatSetting) {
                    floatSetting.setValue(settingObject.getAsFloat());
                }
                if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.setToggled(settingObject.getAsBoolean());
                }
                if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.setMode(settingObject.getAsString());
                }
                if (setting instanceof MultiBooleanSetting multiBooleanSetting) {
                    JsonObject jsonObject = settingObject.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry1 : jsonObject.entrySet()) {
                        multiBooleanSetting.set(entry1.getKey(), entry1.getValue().getAsBoolean());
                    }
                }
            }
        }
    }
}
