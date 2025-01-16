package me.hackclient.cfg;

import com.google.gson.*;
import me.hackclient.Client;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.interfaces.InstanceAccess;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ConfigManager implements InstanceAccess {

    File directory = new File("AutoTool/configs");
    File defaultConfig = new File(directory, "default.json");

    public void save(File file) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!defaultConfig.exists()) {
            defaultConfig.createNewFile();
        }

        try {
            JsonObject json = new JsonObject();
            defaultConfig = file;

            for (Module module : Client.INSTANCE.getModuleManager().modules) {
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
                }
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

    public void load(File file) throws IOException {
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
        defaultConfig = file;
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
            }
        }
    }
}
