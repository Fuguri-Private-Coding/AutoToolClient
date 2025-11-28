package fuguriprivatecoding.autotoolrecode.bind;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.util.Map;

@UtilityClass
public class KeyBinds {

    @Getter final File BIND_DIRECTORY = new File(Client.INST.CLIENT_DIR + "/binds");
    @Getter File bindFile = new File(BIND_DIRECTORY, "binds.json");

    public void init() {
        if (BIND_DIRECTORY.mkdirs()) System.out.println("Successful created Binds Directory.");
    }

    public void loadBinds() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(bindFile));
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(reader);
            reader.close();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                Module module = Modules.getModule(entry.getKey());
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
        for (Module module : Modules.getModules()) {
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

}
