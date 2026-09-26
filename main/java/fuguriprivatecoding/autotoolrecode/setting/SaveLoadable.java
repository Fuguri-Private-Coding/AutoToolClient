package fuguriprivatecoding.autotoolrecode.setting;

import com.google.gson.JsonObject;

public interface SaveLoadable {
    JsonObject getObject();
    void setObject(JsonObject object);
}
